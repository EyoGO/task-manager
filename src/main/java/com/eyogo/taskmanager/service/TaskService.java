package com.eyogo.taskmanager.service;

import com.eyogo.taskmanager.database.entity.Task;
import com.eyogo.taskmanager.database.event.CudEvent;
import com.eyogo.taskmanager.database.event.EventType;
import com.eyogo.taskmanager.database.repository.DatabaseRepository;
import com.eyogo.taskmanager.database.repository.maindb.TaskRepositoryH2;
import com.eyogo.taskmanager.database.repository.reserve.TaskRepositoryPostgres;
import com.eyogo.taskmanager.dto.TaskCreateEditDto;
import com.eyogo.taskmanager.dto.TaskReadDto;
import com.eyogo.taskmanager.dto.TaskStatusEditDto;
import com.eyogo.taskmanager.mapper.TaskCreateEditMapper;
import com.eyogo.taskmanager.mapper.TaskReadMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
//
@Service
@Transactional
@Slf4j
public class TaskService {

    private DatabaseRepository jpaRepository;

    private final TaskRepositoryH2 taskRepositoryH2;
    private final TaskRepositoryPostgres taskRepositoryPostgres;

    private final DatabaseAvailabilityService databaseAvailabilityService;

    private final TaskCreateEditMapper taskCreateEditMapper;
    private final TaskReadMapper taskReadMapper;

    private final List<CudEvent> h2CudEvents = new ArrayList<>();
    private final List<CudEvent> postgresCudEvents = new ArrayList<>();

    private List<CudEvent> currentDbEvents = h2CudEvents;

    public TaskService(@Qualifier("taskRepositoryH2") DatabaseRepository jpaRepository,
                       TaskRepositoryH2 taskRepositoryH2, TaskRepositoryPostgres taskRepositoryPostgres,
                       DatabaseAvailabilityService databaseAvailabilityService,
                       TaskCreateEditMapper taskCreateEditMapper, TaskReadMapper taskReadMapper) {
        this.jpaRepository = jpaRepository;
        this.taskRepositoryH2 = taskRepositoryH2;
        this.taskRepositoryPostgres = taskRepositoryPostgres;
        this.databaseAvailabilityService = databaseAvailabilityService;
        this.taskCreateEditMapper = taskCreateEditMapper;
        this.taskReadMapper = taskReadMapper;
    }

    @Scheduled(fixedDelay = 50000)
    public void checkH2() {
        if (h2CudEvents.isEmpty()) {
            log.info("No entities to synchronize from H2 to PostgreSQL.");
            return;
        }
        boolean isH2Available = databaseAvailabilityService.isH2Available();

        currentDbEvents = isH2Available ? h2CudEvents : postgresCudEvents;
        DatabaseRepository repositoryToUse = isH2Available ? taskRepositoryH2 : taskRepositoryPostgres;

        if (jpaRepository != null && jpaRepository != repositoryToUse) {
            if (isH2Available) {
                log.warn("H2 database was restored. All data from reserve DB will be in H2 soon.");
                synchronizeToPostgres();
            } else {
                log.warn("H2 database is not available. Data will be stored to reserve PostgreSQL database.");
                synchronizeToH2();
            }
        }
        jpaRepository = repositoryToUse;
    }

    @Scheduled(fixedDelay = 20000)
    public void synchronizeToPostgres() {
        if (postgresCudEvents.isEmpty()) {
            log.info("No entities to synchronize from PostgreSQL to H2.");
            return;
        }

        log.info("Synchronizing {} requests from PostgreSQL to H2", postgresCudEvents.size());
        for (CudEvent cudEvent : postgresCudEvents) {
            if (cudEvent.getEventType() == EventType.DELETE) {
                taskRepositoryH2.deleteById(cudEvent.getId());
                continue;
            }
            taskRepositoryH2.save(cudEvent.getObject());
        }
        taskRepositoryH2.flush();
        postgresCudEvents.clear();
    }

    @Scheduled(fixedDelay = 20000)
    public void synchronizeToH2() {
        if (h2CudEvents.isEmpty()) {
            log.info("No entities to synchronize from H2 to PostgreSQL.");
            return;
        }

        log.info("Synchronizing {} requests from H2 to PostgreSQL", h2CudEvents.size());
        for (CudEvent cudEvent : h2CudEvents) {
            if (cudEvent.getEventType() == EventType.DELETE) {
                taskRepositoryPostgres.deleteById(cudEvent.getId());
                continue;
            }
            taskRepositoryPostgres.save(cudEvent.getObject());
        }
        taskRepositoryPostgres.flush();
        h2CudEvents.clear();
    }

    public TaskReadDto create(TaskCreateEditDto taskToCreateDto) {
        Task taskToCreate = taskCreateEditMapper.map(taskToCreateDto);

        if (jpaRepository.existsByDescriptionAndStatus(taskToCreate.getDescription(), taskToCreate.getStatus())) { // Duplicate case
            log.error("Unable to create new task {}. Equivalent task already exists.", taskToCreateDto);
            throw new IllegalArgumentException("Entity with the same description and status already exists.");
        }

        taskToCreate.setId(UUID.randomUUID());
        Optional<Task> taskOptional = Optional.of(taskToCreate)
                .map(jpaRepository::save);

        taskOptional.ifPresent(task -> log.info("Creating new task {}.", task));

        return taskOptional
                .map(task -> {
                    currentDbEvents.add(new CudEvent(EventType.CREATE, task.getId(), task));
                    return task;
                })
                .map(taskReadMapper::map)
                .orElseThrow();
    }

    public boolean delete(UUID id) {
        log.info("Deleting task with ID {}.", id);
        return jpaRepository.findById(id)
                .map(entity -> {
                    jpaRepository.delete(entity);
                    jpaRepository.flush();
                    return true;
                })
                .map(bool -> {
                    currentDbEvents.add(new CudEvent(EventType.DELETE, id, null));
                    return bool;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return jpaRepository.findAll();
    }

    public boolean updateStatus(UUID id, TaskStatusEditDto statusEditDto) {
        Optional<Task> updatedOptional = jpaRepository.findById(id)
                .map(task -> {
                    task.setStatus(statusEditDto.getStatus());
                    return task;
                })
                .map(jpaRepository::saveAndFlush)
                .map(task -> {
                    currentDbEvents.add(new CudEvent(EventType.UPDATE, task.getId(), task));
                    return task;
                });
        return updatedOptional.isPresent();
    }

    public boolean update(UUID id, TaskCreateEditDto taskToEdit) {
        log.info("Updating task with ID {} to {}.", id, taskToEdit);
        Optional<Object> updatedOptional = jpaRepository.findById(id)
                .map(task -> {
                    task.setStatus(taskToEdit.getStatus());
                    task.setDescription(taskToEdit.getDescription());
                    return task;
                })
                .map(jpaRepository::saveAndFlush)
                .map(task -> {
                    currentDbEvents.add(new CudEvent(EventType.UPDATE, task.getId(), task));
                    return task;
                });
        return updatedOptional.isPresent();
    }
}

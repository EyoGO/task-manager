package com.eyogo.taskmanager.service;

import com.eyogo.taskmanager.database.entity.Status;
import com.eyogo.taskmanager.database.entity.Task;
import com.eyogo.taskmanager.database.repository.maindb.TaskRepositoryH2;
import com.eyogo.taskmanager.dto.TaskCreateEditDto;
import com.eyogo.taskmanager.dto.TaskReadDto;
import com.eyogo.taskmanager.mapper.TaskCreateEditMapper;
import com.eyogo.taskmanager.mapper.TaskReadMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepositoryH2 taskRepositoryH2;

    @Mock
    private TaskCreateEditMapper taskCreateEditMapper;

    @Mock
    private TaskReadMapper taskReadMapper;

    private TaskCreateEditDto taskCreateEditDto;
    private Task task;

    @Before
    public void setUp() {
        taskCreateEditDto = new TaskCreateEditDto("New Task", Status.TESTING);
        task = new Task(UUID.randomUUID(), taskCreateEditDto.getStatus(), taskCreateEditDto.getDescription());

        Mockito.when(taskCreateEditMapper.map(taskCreateEditDto)).thenReturn(task);
        Mockito.when(taskReadMapper.map(task)).thenReturn(new TaskReadDto(task.getId(), task.getDescription(), task.getStatus()));
    }

    @Test
    public void create() {
        Mockito.when(taskRepositoryH2.existsByDescriptionAndStatus(task.getDescription(), task.getStatus())).thenReturn(false);
        Mockito.when(taskRepositoryH2.save(task)).thenReturn(task);

        TaskReadDto result = taskService.create(taskCreateEditDto);

        Mockito.verify(taskRepositoryH2).save(task);
        Mockito.verify(taskReadMapper).map(task);

        assertNotNull(result);
        assertEquals(taskCreateEditDto.getDescription(), result.getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTask_duplicate() {
        Mockito.when(taskRepositoryH2.existsByDescriptionAndStatus(task.getDescription(), task.getStatus())).thenReturn(true);

        taskService.create(taskCreateEditDto);
    }

    @Test
    public void delete() {
        Mockito.when(taskRepositoryH2.findById(task.getId())).thenReturn(Optional.of(task));

        boolean result = taskService.delete(task.getId());

        Mockito.verify(taskRepositoryH2).delete(task);

        assertTrue(result);
    }

    @Test
    public void findAll() {
        List<Task> list = List.of(task);
        Mockito.when(taskRepositoryH2.findAll()).thenReturn(list);

        List<Task> result = taskService.findAll();

        Mockito.verify(taskRepositoryH2).findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(list, result);
    }

    @Test
    public void update() {
        Mockito.when(taskRepositoryH2.saveAndFlush(task)).thenReturn(task);
        Mockito.when(taskRepositoryH2.findById(task.getId())).thenReturn(Optional.of(task));

        boolean result = taskService.update(task.getId(), taskCreateEditDto);

        Mockito.verify(taskRepositoryH2).saveAndFlush(task);

        assertTrue(result);
    }
}
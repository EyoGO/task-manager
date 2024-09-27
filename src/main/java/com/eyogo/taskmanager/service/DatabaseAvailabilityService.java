package com.eyogo.taskmanager.service;

import com.eyogo.taskmanager.database.repository.DatabaseRepository;
import com.eyogo.taskmanager.database.repository.maindb.TaskRepositoryH2;
import com.eyogo.taskmanager.database.repository.reserve.TaskRepositoryPostgres;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseAvailabilityService {

    private final TaskRepositoryH2 taskRepositoryH2;
    private final TaskRepositoryPostgres taskRepositoryPostgres;

    public boolean isH2Available() {
        return isDatabaseAvailable(taskRepositoryH2);
    }

    public boolean isPostgresAvailable() {
        return isDatabaseAvailable(taskRepositoryPostgres);
    }

    private boolean isDatabaseAvailable(DatabaseRepository repository) {
        try {
            repository.checkHealth();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

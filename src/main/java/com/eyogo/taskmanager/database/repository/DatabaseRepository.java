package com.eyogo.taskmanager.database.repository;

import com.eyogo.taskmanager.database.entity.Status;
import com.eyogo.taskmanager.database.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface DatabaseRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT 1")
    Integer checkHealth();

    boolean existsByDescriptionAndStatus(String description, Status status);

}

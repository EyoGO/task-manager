package com.eyogo.taskmanager.dto;

import com.eyogo.taskmanager.database.entity.Status;
import lombok.Value;

import java.util.UUID;

@Value
public class TaskReadDto {
    UUID id;
    String description;
    Status status;
}

package com.eyogo.taskmanager.dto;

import com.eyogo.taskmanager.database.entity.Status;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class TaskStatusEditDto {

    @NotNull
    Status status;

    @JsonCreator
    public TaskStatusEditDto( Status status) {
        this.status = status;
    }
}
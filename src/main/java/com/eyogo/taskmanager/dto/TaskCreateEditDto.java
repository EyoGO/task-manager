package com.eyogo.taskmanager.dto;

import com.eyogo.taskmanager.database.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class TaskCreateEditDto {

    @Size(min=5, max=256)
    String description;

    Status status;
}

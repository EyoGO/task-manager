package com.eyogo.taskmanager.database.event;

import com.eyogo.taskmanager.database.entity.Task;
import lombok.Value;

import java.util.UUID;

@Value
public class CudEvent {
    EventType eventType;
    UUID id;
    Task object;
}

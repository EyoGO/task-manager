package com.eyogo.taskmanager.mapper;

import com.eyogo.taskmanager.database.entity.Task;
import com.eyogo.taskmanager.dto.TaskReadDto;
import org.springframework.stereotype.Component;

@Component
public class TaskReadMapper implements Mapper<Task, TaskReadDto> {
    @Override
    public TaskReadDto map(Task from) {
        return new TaskReadDto(from.getId(), from.getDescription(), from.getStatus());
    }
}

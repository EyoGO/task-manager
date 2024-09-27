package com.eyogo.taskmanager.mapper;

import com.eyogo.taskmanager.database.entity.Task;
import com.eyogo.taskmanager.dto.TaskCreateEditDto;
import org.springframework.stereotype.Component;

@Component
public class TaskCreateEditMapper implements Mapper<TaskCreateEditDto, Task> {
    @Override
    public Task map(TaskCreateEditDto from) {
        Task task = new Task();
        task.setStatus(from.getStatus());
        task.setDescription(from.getDescription());
        return task;
    }
}

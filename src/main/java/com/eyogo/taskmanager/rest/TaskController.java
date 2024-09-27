package com.eyogo.taskmanager.rest;

import com.eyogo.taskmanager.database.entity.Task;
import com.eyogo.taskmanager.dto.TaskCreateEditDto;
import com.eyogo.taskmanager.dto.TaskStatusEditDto;
import com.eyogo.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private TaskService taskService;

    @PostMapping
    public UUID create(@Valid @RequestBody TaskCreateEditDto taskToCreate) {
        return taskService.create(taskToCreate).getId();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable("id") UUID id, @Valid @RequestBody TaskStatusEditDto statusEditDto) {
        return taskService.updateStatus(id, statusEditDto)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @Valid @RequestBody TaskCreateEditDto taskToEdit) {
        return taskService.update(id, taskToEdit)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        return taskService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Task> get() {
        return taskService.findAll();
    }
}

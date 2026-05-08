package org.lambdasys.service;

import jakarta.persistence.EntityNotFoundException;
import org.lambdasys.dto.TaskRequest;
import org.lambdasys.dto.TaskResponse;
import org.lambdasys.model.Task;
import org.lambdasys.model.TaskStatus;
import org.lambdasys.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository repository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
    }

    public List<TaskResponse> findAll() {
        return repository.findAll().stream().map(TaskResponse::from).toList();
    }

    public TaskResponse findById(Long id) {
        return repository.findById(id)
                .map(TaskResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        TaskResponse response = TaskResponse.from(repository.save(task));
        broadcast("task-created", response);
        return response;
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) task.setStatus(request.status());
        TaskResponse response = TaskResponse.from(repository.save(task));
        broadcast("task-updated", response);
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
        repository.delete(task);
        broadcast("task-deleted", TaskResponse.from(task));
    }

    private void broadcast(String eventName, TaskResponse payload) {
        List<SseEmitter> dead = new java.util.ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}

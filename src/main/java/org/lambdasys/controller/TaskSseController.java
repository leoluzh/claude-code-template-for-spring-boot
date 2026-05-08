package org.lambdasys.controller;

import org.lambdasys.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskSseController {

    private final TaskService service;

    public TaskSseController(TaskService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        service.addEmitter(emitter);
        return emitter;
    }
}

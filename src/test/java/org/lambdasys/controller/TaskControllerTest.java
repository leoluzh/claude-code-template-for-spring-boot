package org.lambdasys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.lambdasys.dto.TaskRequest;
import org.lambdasys.dto.TaskResponse;
import org.lambdasys.model.TaskStatus;
import org.lambdasys.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService service;

    private TaskResponse sampleResponse() {
        return new TaskResponse(1L, "Test Task", "Description", TaskStatus.TODO, LocalDateTime.now());
    }

    @Test
    void findAll_returnsList() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleResponse()));
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void findById_existingId_returnsTask() throws Exception {
        when(service.findById(1L)).thenReturn(sampleResponse());
        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void findById_nonExistingId_returns404() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Task not found: 99"));
        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse());
        TaskRequest request = new TaskRequest("Test Task", "Description", TaskStatus.TODO);
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void create_blankTitle_returns400() throws Exception {
        TaskRequest request = new TaskRequest("", null, null);
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void update_existingId_returnsUpdated() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(sampleResponse());
        TaskRequest request = new TaskRequest("Updated", null, TaskStatus.IN_PROGRESS);
        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void update_nonExistingId_returns404() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new EntityNotFoundException("Task not found: 99"));
        TaskRequest request = new TaskRequest("Title", null, null);
        mockMvc.perform(put("/api/v1/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Task not found: 99")).when(service).delete(99L);
        mockMvc.perform(delete("/api/v1/tasks/99"))
                .andExpect(status().isNotFound());
    }
}

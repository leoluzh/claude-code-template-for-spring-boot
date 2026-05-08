package org.lambdasys.controller;

import org.junit.jupiter.api.Test;
import org.lambdasys.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskSseController.class)
class TaskSseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService service;

    @Test
    void subscribe_returnsTextEventStreamContentType() throws Exception {
        doAnswer(inv -> {
            inv.<SseEmitter>getArgument(0).complete();
            return null;
        }).when(service).addEmitter(any(SseEmitter.class));

        MvcResult result = mockMvc.perform(get("/api/v1/tasks/events"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    void subscribe_registersEmitterWithService() throws Exception {
        doAnswer(inv -> {
            inv.<SseEmitter>getArgument(0).complete();
            return null;
        }).when(service).addEmitter(any(SseEmitter.class));

        mockMvc.perform(get("/api/v1/tasks/events"))
                .andExpect(request().asyncStarted())
                .andReturn();

        verify(service).addEmitter(any(SseEmitter.class));
    }
}

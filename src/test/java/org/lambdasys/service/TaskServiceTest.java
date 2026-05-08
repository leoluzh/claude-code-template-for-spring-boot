package org.lambdasys.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lambdasys.dto.TaskRequest;
import org.lambdasys.dto.TaskResponse;
import org.lambdasys.model.Task;
import org.lambdasys.model.TaskStatus;
import org.lambdasys.repository.TaskRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Description");
        task.setStatus(TaskStatus.TODO);
    }

    @Test
    void findAll_returnsMappedResponses() {
        when(repository.findAll()).thenReturn(List.of(task));
        List<TaskResponse> result = service.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test Task");
    }

    @Test
    void findById_existingId_returnsResponse() {
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponse result = service.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Task");
    }

    @Test
    void findById_nonExistingId_throwsEntityNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_validRequest_savesAndReturnsResponse() {
        when(repository.save(any(Task.class))).thenReturn(task);
        TaskRequest request = new TaskRequest("Test Task", "Description", TaskStatus.TODO);
        TaskResponse result = service.create(request);
        assertThat(result.title()).isEqualTo("Test Task");
        assertThat(result.status()).isEqualTo(TaskStatus.TODO);
        verify(repository).save(any(Task.class));
    }

    @Test
    void create_nullStatus_defaultsToTodo() {
        when(repository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        TaskRequest request = new TaskRequest("Task", null, null);
        TaskResponse result = service.create(request);
        assertThat(result.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void update_existingId_updatesAndReturnsResponse() {
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.save(any(Task.class))).thenReturn(task);
        TaskRequest request = new TaskRequest("Updated", "New desc", TaskStatus.IN_PROGRESS);
        TaskResponse result = service.update(1L, request);
        assertThat(result).isNotNull();
        verify(repository).save(task);
    }

    @Test
    void update_nonExistingId_throwsEntityNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        TaskRequest request = new TaskRequest("Title", null, null);
        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_existingId_deletesTask() {
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        service.delete(1L);
        verify(repository).delete(task);
    }

    @Test
    void delete_nonExistingId_throwsEntityNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

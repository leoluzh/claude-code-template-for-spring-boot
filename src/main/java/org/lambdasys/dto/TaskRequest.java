package org.lambdasys.dto;

import jakarta.validation.constraints.NotBlank;
import org.lambdasys.model.TaskStatus;

public record TaskRequest(
        @NotBlank String title,
        String description,
        TaskStatus status
) {}

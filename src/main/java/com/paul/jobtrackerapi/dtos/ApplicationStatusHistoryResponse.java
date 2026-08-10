package com.paul.jobtrackerapi.dtos;

import com.paul.jobtrackerapi.entities.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationStatusHistoryResponse(
        Long id,
        ApplicationStatus status,
        LocalDateTime changedAt
) {
}

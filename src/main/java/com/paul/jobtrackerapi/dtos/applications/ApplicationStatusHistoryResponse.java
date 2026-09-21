package com.paul.jobtrackerapi.dtos.applications;

import com.paul.jobtrackerapi.entities.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationStatusHistoryResponse(
        Long id,
        ApplicationStatus status,
        LocalDateTime changedAt
) {
}

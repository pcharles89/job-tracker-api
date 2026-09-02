package com.paul.jobtrackerapi.dtos;

import com.paul.jobtrackerapi.entities.InterviewOutcome;
import com.paul.jobtrackerapi.entities.InterviewType;

import java.time.LocalDateTime;

public record InterviewResponse(
        Long id,
        InterviewType type,
        LocalDateTime scheduledAt,
        String notes,
        InterviewOutcome outcome
) {}

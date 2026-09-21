package com.paul.jobtrackerapi.dtos.interviews;

import com.paul.jobtrackerapi.entities.InterviewType;

import java.time.LocalDateTime;

public record CreateInterviewRequest(
        InterviewType type,
        LocalDateTime scheduledAt,
        String notes
) {}

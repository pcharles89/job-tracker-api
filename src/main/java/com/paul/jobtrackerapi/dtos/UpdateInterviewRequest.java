package com.paul.jobtrackerapi.dtos;

import com.paul.jobtrackerapi.entities.InterviewType;

import java.time.LocalDateTime;

public record UpdateInterviewRequest(
        InterviewType type,
        LocalDateTime scheduledAt,
        String notes
) {}

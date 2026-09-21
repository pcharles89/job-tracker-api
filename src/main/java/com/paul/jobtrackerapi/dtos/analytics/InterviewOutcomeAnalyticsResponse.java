package com.paul.jobtrackerapi.dtos.analytics;

import com.paul.jobtrackerapi.entities.InterviewOutcome;

public record InterviewOutcomeAnalyticsResponse(
        InterviewOutcome outcome,
        Long count
) {
}

package com.paul.jobtrackerapi.dtos.analytics;

public record ApplicationSummaryResponse(
        long totalApplications,
        long interviews,
        long offers,
        long rejections
) {
}

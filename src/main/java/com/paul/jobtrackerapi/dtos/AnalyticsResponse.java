package com.paul.jobtrackerapi.dtos;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class AnalyticsResponse {

    private final long totalApplications;

    private final long applied;
    private final long phoneScreen;
    private final long technicalInterview;
    private final long finalInterview;
    private final long offer;
    private final long rejected;
    private final long withdrawn;
}
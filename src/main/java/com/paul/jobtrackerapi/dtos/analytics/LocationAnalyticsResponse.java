package com.paul.jobtrackerapi.dtos.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LocationAnalyticsResponse {

    private final String location;
    private final long count;
}

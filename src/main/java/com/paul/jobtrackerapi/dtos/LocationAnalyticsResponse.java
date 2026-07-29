package com.paul.jobtrackerapi.dtos;

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

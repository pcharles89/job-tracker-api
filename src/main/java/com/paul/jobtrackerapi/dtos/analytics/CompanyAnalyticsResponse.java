package com.paul.jobtrackerapi.dtos.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CompanyAnalyticsResponse {

    private final String companyName;
    private final long count;
}

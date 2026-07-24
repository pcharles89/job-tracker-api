package com.paul.jobtrackerapi.dtos;

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

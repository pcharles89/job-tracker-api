package com.paul.jobtrackerapi.controllers;

import com.paul.jobtrackerapi.dtos.analytics.*;
import com.paul.jobtrackerapi.entities.InterviewOutcome;
import com.paul.jobtrackerapi.security.JwtAuthenticationFilter;
import com.paul.jobtrackerapi.security.JwtService;
import com.paul.jobtrackerapi.services.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobApplicationAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getAnalytics_shouldReturnApplicationAnalytics() throws Exception {

        AnalyticsResponse response = AnalyticsResponse.builder()
                .totalApplications(12)
                .applied(4)
                .phoneScreen(2)
                .technicalInterview(2)
                .finalInterview(1)
                .offer(1)
                .rejected(2)
                .withdrawn(0)
                .build();

        Mockito.when(service.getAnalytics())
                .thenReturn(response);

        mockMvc.perform(get("/applications/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(12))
                .andExpect(jsonPath("$.applied").value(4))
                .andExpect(jsonPath("$.phoneScreen").value(2))
                .andExpect(jsonPath("$.technicalInterview").value(2))
                .andExpect(jsonPath("$.finalInterview").value(1))
                .andExpect(jsonPath("$.offer").value(1))
                .andExpect(jsonPath("$.rejected").value(2))
                .andExpect(jsonPath("$.withdrawn").value(0));

        verify(service).getAnalytics();
    }

    @Test
    void getCompanyAnalytics_shouldReturnApplicationCountsByCompany() throws Exception {

        List<CompanyAnalyticsResponse> response = List.of(
                CompanyAnalyticsResponse.builder()
                        .companyName("Amazon")
                        .count(3)
                        .build(),

                CompanyAnalyticsResponse.builder()
                        .companyName("Google")
                        .count(2)
                        .build(),

                CompanyAnalyticsResponse.builder()
                        .companyName("Microsoft")
                        .count(1)
                        .build()
        );

        Mockito.when(service.getCompanyAnalytics())
                .thenReturn(response);

        mockMvc.perform(get("/applications/analytics/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Amazon"))
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].companyName").value("Google"))
                .andExpect(jsonPath("$[1].count").value(2))
                .andExpect(jsonPath("$[2].companyName").value("Microsoft"))
                .andExpect(jsonPath("$[2].count").value(1));

        verify(service).getCompanyAnalytics();
    }

    @Test
    void getLocationAnalytics_shouldReturnApplicationCountsByLocation() throws Exception {

        List<LocationAnalyticsResponse> response = List.of(
                LocationAnalyticsResponse.builder()
                        .location("New York, NY")
                        .count(3)
                        .build(),

                LocationAnalyticsResponse.builder()
                        .location("Remote")
                        .count(2)
                        .build()
        );

        Mockito.when(service.getLocationAnalytics())
                .thenReturn(response);

        mockMvc.perform(get("/applications/analytics/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location").value("New York, NY"))
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].location").value("Remote"))
                .andExpect(jsonPath("$[1].count").value(2));

        verify(service).getLocationAnalytics();
    }

    @Test
    void getInterviewOutcomeAnalytics_shouldReturnAnalytics() throws Exception {

        InterviewOutcomeAnalyticsResponse pendingResponse =
                new InterviewOutcomeAnalyticsResponse(
                        InterviewOutcome.PENDING,
                        3L
                );

        InterviewOutcomeAnalyticsResponse passedResponse =
                new InterviewOutcomeAnalyticsResponse(
                        InterviewOutcome.PASSED,
                        5L
                );

        Mockito.when(service.getInterviewOutcomeAnalytics())
                .thenReturn(List.of(
                        pendingResponse,
                        passedResponse
                ));

        mockMvc.perform(
                        get("/applications/interviews/analytics/outcomes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].outcome").value("PENDING"))
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].outcome").value("PASSED"))
                .andExpect(jsonPath("$[1].count").value(5));

        verify(service)
                .getInterviewOutcomeAnalytics();
    }

    @Test
    void getInterviewOutcomeAnalytics_shouldReturnEmptyList() throws Exception {

        Mockito.when(service.getInterviewOutcomeAnalytics())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/applications/interviews/analytics/outcomes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service)
                .getInterviewOutcomeAnalytics();
    }

    @Test
    void getApplicationSummary_shouldReturnSummary() throws Exception {

        ApplicationSummaryResponse response =
                new ApplicationSummaryResponse(
                        10,
                        4,
                        1,
                        3
                );

        Mockito.when(service.getApplicationSummary())
                .thenReturn(response);

        mockMvc.perform(
                        get("/applications/analytics/summary")
                                .with(user("paul"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(10))
                .andExpect(jsonPath("$.interviews").value(4))
                .andExpect(jsonPath("$.offers").value(1))
                .andExpect(jsonPath("$.rejections").value(3));

        verify(service).getApplicationSummary();
    }
}

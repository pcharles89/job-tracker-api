package com.paul.jobtrackerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paul.jobtrackerapi.dtos.analytics.*;
import com.paul.jobtrackerapi.dtos.applications.CreateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.applications.JobApplicationResponse;
import com.paul.jobtrackerapi.dtos.applications.PatchJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.applications.UpdateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.interviews.CreateInterviewRequest;
import com.paul.jobtrackerapi.dtos.interviews.InterviewResponse;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewOutcomeRequest;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewRequest;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.entities.InterviewOutcome;
import com.paul.jobtrackerapi.entities.InterviewType;
import com.paul.jobtrackerapi.exceptions.InterviewNotFoundException;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.security.JwtAuthenticationFilter;
import com.paul.jobtrackerapi.security.JwtService;
import com.paul.jobtrackerapi.services.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@WebMvcTest(JobApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());


@Test
void createApplication_shouldReturnCreatedApplication() throws Exception {
    CreateJobApplicationRequest request = new CreateJobApplicationRequest();
    request.setCompanyName("Amazon");
    request.setJobTitle("Backend Developer");

    JobApplicationResponse response = new JobApplicationResponse();
    response.setId(1L);
    response.setCompanyName("Amazon");
    response.setJobTitle("Backend Developer");

    Mockito.when(service.createApplication(Mockito.any(CreateJobApplicationRequest.class)))
            .thenReturn(response);

    mockMvc.perform(post("/applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.companyName").value("Amazon"))
            .andExpect(jsonPath("$.jobTitle").value("Backend Developer"));

    verify(service)
            .createApplication(Mockito.any(CreateJobApplicationRequest.class));
}

    @Test
    void getApplicationById_shouldReturnApplication() throws Exception {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Amazon");
        response.setJobTitle("Backend Developer");

        Mockito.when(service.getApplicationById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.companyName").value("Amazon"))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer"));

        verify(service).getApplicationById(1L);
    }

    @Test
    void getApplicationById_shouldReturnNotFound() throws Exception {
        Mockito.when(service.getApplicationById(1L))
                .thenThrow(new JobApplicationNotFoundException(1L));

        mockMvc.perform(get("/applications/1"))
                .andExpect(status().isNotFound());

        verify(service).getApplicationById(1L);
    }

    @Test
    void updateApplication_shouldReturnUpdatedApplication() throws Exception {
        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Java Developer");
        request.setStatus(ApplicationStatus.APPLIED);

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Google");
        response.setJobTitle("Java Developer");

        Mockito.when(service.updateApplication(
                Mockito.eq(1L),
                Mockito.any(UpdateJobApplicationRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.companyName").value("Google"))
                .andExpect(jsonPath("$.jobTitle").value("Java Developer"));

        verify(service).updateApplication(
                Mockito.eq(1L),
                Mockito.any(UpdateJobApplicationRequest.class)
        );
    }

    @Test
    void patchApplication_shouldReturnPatchedApplication() throws Exception {
        PatchJobApplicationRequest request =
                new PatchJobApplicationRequest(
                        "Netflix",     // companyName
                        null,          // jobTitle
                        null,          // jobUrl
                        null,          // appliedDate
                        null,          // location
                        null,          // salaryRange
                        null,          // notes
                        null           // status
                );

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Netflix");
        response.setJobTitle("Backend Developer");

        Mockito.when(service.patchApplication(
                Mockito.eq(1L),
                Mockito.any(PatchJobApplicationRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.companyName").value("Netflix"))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer"));

        verify(service).patchApplication(
                Mockito.eq(1L),
                Mockito.any(PatchJobApplicationRequest.class)
        );
    }

    @Test
    void deleteApplication_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/applications/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteApplication(1L);
    }

    @Test
    void createApplication_shouldReturnBadRequest_whenCompanyNameIsBlank() throws Exception {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        request.setCompanyName("");
        request.setJobTitle("Backend Developer");

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.companyName")
                        .value("Company name is required"));

        Mockito.verifyNoInteractions(service);
    }

    @Test
    void createApplication_shouldReturnBadRequest_whenJobTitleIsBlank() throws Exception {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        request.setCompanyName("Amazon");
        request.setJobTitle("");

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.jobTitle")
                        .value("Job title is required"));

        Mockito.verifyNoInteractions(service);
    }

    @Test
    void updateApplication_shouldReturnBadRequest_whenCompanyNameIsBlank() throws Exception {
        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("");
        request.setJobTitle("Java Developer");

        mockMvc.perform(put("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.companyName")
                        .value("Company name cannot be blank"));

        Mockito.verifyNoInteractions(service);
    }

    @Test
    void getAllApplications_shouldReturnPage() throws Exception {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Amazon");
        response.setJobTitle("Backend Developer");

        Page<JobApplicationResponse> page =
                new PageImpl<>(List.of(response));

        Mockito.when(service.getAllApplications(Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].companyName").value("Amazon"))
                .andExpect(jsonPath("$.content[0].jobTitle").value("Backend Developer"));

        verify(service).getAllApplications(Mockito.any(Pageable.class));
    }

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
    void searchApplications_shouldReturnPage() throws Exception {
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Amazon");
        response.setJobTitle("Backend Developer");

        Page<JobApplicationResponse> page =
                new PageImpl<>(List.of(response));

        Mockito.when(service.searchApplications(
                Mockito.eq("Amazon"),
                Mockito.eq("Remote"),
                Mockito.eq(ApplicationStatus.APPLIED),
                Mockito.any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/applications/search")
                        .param("companyName", "Amazon")
                        .param("location", "Remote")
                        .param("status", "APPLIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].companyName").value("Amazon"))
                .andExpect(jsonPath("$.content[0].jobTitle").value("Backend Developer"));

        verify(service).searchApplications(
                Mockito.eq("Amazon"),
                Mockito.eq("Remote"),
                Mockito.eq(ApplicationStatus.APPLIED),
                Mockito.any(Pageable.class)
        );
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
    void createInterview_shouldReturnCreatedInterview() throws Exception {
        Long applicationId = 1L;

        LocalDateTime scheduledAt =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        CreateInterviewRequest request =
                new CreateInterviewRequest(
                        InterviewType.TECHNICAL,
                        scheduledAt,
                        "Java and SQL interview"
                );

        InterviewResponse response =
                new InterviewResponse(
                        10L,
                        InterviewType.TECHNICAL,
                        scheduledAt,
                        "Java and SQL interview",
                        InterviewOutcome.PENDING
                );

        Mockito.when(service.createInterview(
                Mockito.eq(applicationId),
                Mockito.any(CreateInterviewRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/applications/{id}/interviews", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.type").value("TECHNICAL"))
                .andExpect(jsonPath("$.notes").value("Java and SQL interview"))
                .andExpect(jsonPath("$.outcome").value("PENDING"));

        verify(service).createInterview(
                Mockito.eq(applicationId),
                Mockito.any(CreateInterviewRequest.class)
        );
    }

    @Test
    void createInterview_shouldReturnNotFound() throws Exception {
        Long applicationId = 999L;

        LocalDateTime scheduledAt =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        CreateInterviewRequest request =
                new CreateInterviewRequest(
                        InterviewType.TECHNICAL,
                        scheduledAt,
                        "Java and SQL interview"
                );

        Mockito.when(service.createInterview(
                Mockito.eq(applicationId),
                Mockito.any(CreateInterviewRequest.class)
        )).thenThrow(new JobApplicationNotFoundException(applicationId));

        mockMvc.perform(
                        post("/applications/{id}/interviews", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getInterviews_shouldReturnInterviews() throws Exception {
        Long applicationId = 1L;

        LocalDateTime firstTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime secondTime =
                LocalDateTime.of(2026, 8, 25, 14, 30);

        InterviewResponse firstResponse =
                new InterviewResponse(
                        10L,
                        InterviewType.PHONE,
                        firstTime,
                        "Recruiter screen",
                        InterviewOutcome.PASSED
                );

        InterviewResponse secondResponse =
                new InterviewResponse(
                        11L,
                        InterviewType.TECHNICAL,
                        secondTime,
                        "Java and SQL interview",
                        InterviewOutcome.PENDING
                );

        Mockito.when(service.getInterviews(applicationId))
                .thenReturn(List.of(firstResponse, secondResponse));

        mockMvc.perform(
                        get("/applications/{id}/interviews", applicationId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].type").value("PHONE"))
                .andExpect(jsonPath("$[0].notes").value("Recruiter screen"))
                .andExpect(jsonPath("$[0].outcome").value("PASSED"))

                .andExpect(jsonPath("$[1].id").value(11L))
                .andExpect(jsonPath("$[1].type").value("TECHNICAL"))
                .andExpect(jsonPath("$[1].notes").value("Java and SQL interview"))
                .andExpect(jsonPath("$[1].outcome").value("PENDING"));

        verify(service).getInterviews(applicationId);
    }

    @Test
    void getInterviews_shouldReturnNotFound() throws Exception {
        Long applicationId = 999L;

        Mockito.when(service.getInterviews(applicationId))
                .thenThrow(new JobApplicationNotFoundException(applicationId));

        mockMvc.perform(
                        get("/applications/{id}/interviews", applicationId)
                )
                .andExpect(status().isNotFound());

        verify(service).getInterviews(applicationId);
    }

    @Test
    void updateInterviewOutcome_shouldReturnUpdatedInterview() throws Exception {
        Long applicationId = 1L;
        Long interviewId = 10L;

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        InterviewResponse response =
                new InterviewResponse(
                        interviewId,
                        InterviewType.TECHNICAL,
                        LocalDateTime.of(2026, 8, 20, 14, 0),
                        "Java and SQL interview",
                        InterviewOutcome.PASSED
                );

        Mockito.when(service.updateInterviewOutcome(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.any(UpdateInterviewOutcomeRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/applications/{applicationId}/interviews/{interviewId}/outcome",
                                applicationId,
                                interviewId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.type").value("TECHNICAL"))
                .andExpect(jsonPath("$.notes").value("Java and SQL interview"))
                .andExpect(jsonPath("$.outcome").value("PASSED"));

        verify(service).updateInterviewOutcome(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.any(UpdateInterviewOutcomeRequest.class)
        );
    }

    @Test
    void updateInterviewOutcome_shouldReturnNotFound() throws Exception {
        Long applicationId = 999L;
        Long interviewId = 10L;

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        Mockito.when(service.updateInterviewOutcome(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.any(UpdateInterviewOutcomeRequest.class)
        )).thenThrow(new JobApplicationNotFoundException(applicationId));

        mockMvc.perform(
                        patch(
                                "/applications/{applicationId}/interviews/{interviewId}/outcome",
                                applicationId,
                                interviewId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(service).updateInterviewOutcome(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        );
    }

    @Test
    void updateInterviewOutcome_shouldReturnNotFound_whenInterviewNotFound() throws Exception {
        Long applicationId = 1L;
        Long interviewId = 999L;

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        Mockito.when(service.updateInterviewOutcome(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.any(UpdateInterviewOutcomeRequest.class)
        )).thenThrow(new InterviewNotFoundException(interviewId));

        mockMvc.perform(
                        patch(
                                "/applications/{applicationId}/interviews/{interviewId}/outcome",
                                applicationId,
                                interviewId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(service).updateInterviewOutcome(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        );
    }

    @Test
    void updateInterview_shouldReturnUpdatedInterview() throws Exception {
        Long applicationId = 1L;
        Long interviewId = 10L;

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round with engineering manager"
                );

        InterviewResponse response =
                new InterviewResponse(
                        interviewId,
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round with engineering manager",
                        InterviewOutcome.PENDING
                );

        Mockito.when(service.updateInterview(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                interviewId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.type").value("FINAL"))
                .andExpect(jsonPath("$.scheduledAt")
                        .value("2026-08-30T15:00:00"))
                .andExpect(jsonPath("$.notes")
                        .value("Final round with engineering manager"))
                .andExpect(jsonPath("$.outcome").value("PENDING"));

        verify(service).updateInterview(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        );
    }

    @Test
    void updateInterview_shouldReturnNotFound_whenApplicationNotFound() throws Exception {
        Long applicationId = 999L;
        Long interviewId = 10L;

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        Mockito.when(service.updateInterview(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        )).thenThrow(new JobApplicationNotFoundException(applicationId));

        mockMvc.perform(
                        put(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                interviewId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(service).updateInterview(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        );
    }

    @Test
    void updateInterview_shouldReturnNotFound_whenInterviewNotFound() throws Exception {
        Long applicationId = 1L;
        Long interviewId = 999L;

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        Mockito.when(service.updateInterview(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        )).thenThrow(new InterviewNotFoundException(interviewId));

        mockMvc.perform(
                        put(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                interviewId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(service).updateInterview(
                Mockito.eq(applicationId),
                Mockito.eq(interviewId),
                Mockito.eq(request)
        );
    }

    @Test
    void deleteInterview_shouldReturnNoContent() throws Exception {
        Long applicationId = 1L;
        Long interviewId = 10L;

        mockMvc.perform(
                        delete(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                interviewId
                        )
                )
                .andExpect(status().isNoContent());

        verify(service)
                .deleteInterview(applicationId, interviewId);
    }

    @Test
    void deleteInterview_shouldReturnNotFound_whenApplicationNotFound()
            throws Exception {

        Long applicationId = 999L;
        Long interviewId = 10L;

        Mockito.doThrow(
                new JobApplicationNotFoundException(applicationId)
        ).when(service).deleteInterview(
                applicationId,
                interviewId
        );

        mockMvc.perform(
                        delete(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                interviewId
                        )
                )
                .andExpect(status().isNotFound());

        verify(service)
                .deleteInterview(applicationId, interviewId);
    }

    @Test
    void deleteInterview_shouldReturnNotFound_whenInterviewNotFound()
            throws Exception {

        Long applicationId = 1L;
        Long interviewId = 999L;

        Mockito.doThrow(
                new InterviewNotFoundException(interviewId)
        ).when(service).deleteInterview(
                applicationId,
                interviewId
        );

        mockMvc.perform(
                        delete(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                interviewId
                        )
                )
                .andExpect(status().isNotFound());

        verify(service)
                .deleteInterview(applicationId, interviewId);
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
    void getUpcomingInterviews_shouldReturnUpcomingInterviews() throws Exception {
        LocalDateTime firstTime =
                LocalDateTime.of(2026, 9, 5, 10, 0);

        LocalDateTime secondTime =
                LocalDateTime.of(2026, 9, 10, 14, 0);

        InterviewResponse firstResponse =
                new InterviewResponse(
                        10L,
                        InterviewType.TECHNICAL,
                        firstTime,
                        "Technical interview",
                        InterviewOutcome.PENDING
                );

        InterviewResponse secondResponse =
                new InterviewResponse(
                        11L,
                        InterviewType.FINAL,
                        secondTime,
                        "Final interview",
                        InterviewOutcome.PENDING
                );

        Mockito.when(service.getUpcomingInterviews())
                .thenReturn(List.of(
                        firstResponse,
                        secondResponse
                ));

        mockMvc.perform(
                        get("/applications/interviews/upcoming")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].type").value("TECHNICAL"))
                .andExpect(jsonPath("$[0].notes").value("Technical interview"))
                .andExpect(jsonPath("$[0].outcome").value("PENDING"))

                .andExpect(jsonPath("$[1].id").value(11L))
                .andExpect(jsonPath("$[1].type").value("FINAL"))
                .andExpect(jsonPath("$[1].notes").value("Final interview"))
                .andExpect(jsonPath("$[1].outcome").value("PENDING"));

        verify(service)
                .getUpcomingInterviews();
    }

    @Test
    void getUpcomingInterviews_shouldReturnEmptyList() throws Exception {

        Mockito.when(service.getUpcomingInterviews())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/applications/interviews/upcoming")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service)
                .getUpcomingInterviews();
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

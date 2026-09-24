package com.paul.jobtrackerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paul.jobtrackerapi.dtos.interviews.CreateInterviewRequest;
import com.paul.jobtrackerapi.dtos.interviews.InterviewResponse;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewOutcomeRequest;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class InterviewControllerTest {

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

}

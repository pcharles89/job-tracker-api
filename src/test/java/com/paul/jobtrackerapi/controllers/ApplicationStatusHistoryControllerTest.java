package com.paul.jobtrackerapi.controllers;

import com.paul.jobtrackerapi.dtos.applications.ApplicationStatusHistoryResponse;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(JobApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationStatusHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getStatusHistory_shouldReturnHistory() throws Exception {

        LocalDateTime firstTime =
                LocalDateTime.of(2026, 8, 1, 9, 0);

        LocalDateTime secondTime =
                LocalDateTime.of(2026, 8, 5, 14, 30);

        ApplicationStatusHistoryResponse firstResponse =
                new ApplicationStatusHistoryResponse(
                        10L,
                        ApplicationStatus.APPLIED,
                        firstTime
                );

        ApplicationStatusHistoryResponse secondResponse =
                new ApplicationStatusHistoryResponse(
                        11L,
                        ApplicationStatus.PHONE_SCREEN,
                        secondTime
                );

        Mockito.when(service.getStatusHistory(1L))
                .thenReturn(List.of(
                        firstResponse,
                        secondResponse
                ));

        mockMvc.perform(
                        get("/applications/{id}/history", 1L)
                                .with(user("paul"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].status").value("APPLIED"))

                .andExpect(jsonPath("$[1].id").value(11L))
                .andExpect(jsonPath("$[1].status").value("PHONE_SCREEN"));

        verify(service)
                .getStatusHistory(1L);
    }
}

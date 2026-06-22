package com.paul.jobtrackerapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paul.jobtrackerapi.dtos.CreateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.JobApplicationResponse;
import com.paul.jobtrackerapi.dtos.PatchJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.UpdateJobApplicationRequest;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
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

import java.util.List;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JobApplicationService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;


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

    Mockito.verify(service)
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

        Mockito.verify(service).getApplicationById(1L);
    }

    @Test
    void getApplicationById_shouldReturnNotFound() throws Exception {
        Mockito.when(service.getApplicationById(1L))
                .thenThrow(new JobApplicationNotFoundException(1L));

        mockMvc.perform(get("/applications/1"))
                .andExpect(status().isNotFound());

        Mockito.verify(service).getApplicationById(1L);
    }

    @Test
    void updateApplication_shouldReturnUpdatedApplication() throws Exception {
        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Java Developer");

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

        Mockito.verify(service).updateApplication(
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

        Mockito.verify(service).patchApplication(
                Mockito.eq(1L),
                Mockito.any(PatchJobApplicationRequest.class)
        );
    }

    @Test
    void deleteApplication_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/applications/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(service).deleteApplication(1L);
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

        Mockito.verify(service).getAllApplications(Mockito.any(Pageable.class));
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

        Mockito.verify(service).searchApplications(
                Mockito.eq("Amazon"),
                Mockito.eq("Remote"),
                Mockito.eq(ApplicationStatus.APPLIED),
                Mockito.any(Pageable.class)
        );
    }



}

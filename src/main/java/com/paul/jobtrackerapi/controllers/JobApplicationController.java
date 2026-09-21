package com.paul.jobtrackerapi.controllers;

import com.paul.jobtrackerapi.dtos.analytics.*;
import com.paul.jobtrackerapi.dtos.applications.*;
import com.paul.jobtrackerapi.dtos.interviews.CreateInterviewRequest;
import com.paul.jobtrackerapi.dtos.interviews.InterviewResponse;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewOutcomeRequest;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewRequest;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.services.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Applications")
@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new job application")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse createApplication(
            @Valid @RequestBody CreateJobApplicationRequest request
    ) {
        return service.createApplication(request);
    }

    @Operation(summary = "Get all job applications")
    @GetMapping
    public Page<JobApplicationResponse> getAllApplications(
            @ParameterObject Pageable pageable
    ) {
        return service.getAllApplications(pageable);
    }

    @Operation(summary = "Get job application analytics")
    @GetMapping("/analytics")
    public AnalyticsResponse getAnalytics() {
        return service.getAnalytics();
    }

    @Operation(summary = "Get application counts by company")
    @GetMapping("/analytics/companies")
    public List<CompanyAnalyticsResponse> getCompanyAnalytics() {
        return service.getCompanyAnalytics();
    }

    @Operation(summary = "Get application counts by location")
    @GetMapping("/analytics/locations")
    public List<LocationAnalyticsResponse> getLocationAnalytics() {
        return service.getLocationAnalytics();
    }

    @Operation(summary = "Get application status history")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ApplicationStatusHistoryResponse>> getStatusHistory(
            @PathVariable Long id
    ) {
        List<ApplicationStatusHistoryResponse> history =
                service.getStatusHistory(id);

        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get a job application by ID")
    @GetMapping("/{id}")
    public JobApplicationResponse getApplicationById(
            @PathVariable Long id
    ) {
        return service.getApplicationById(id);
    }

    @Operation(summary = "Delete a job application")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(
            @PathVariable Long id
    ) {
        service.deleteApplication(id);
    }

    @Operation(summary = "Update a job application")
    @PutMapping("/{id}")
    public JobApplicationResponse updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationRequest request
    ) {
        return service.updateApplication(id, request);
    }

    @Operation(
            summary = "Search job applications",
            description = "Search applications by company name, location, and status with pagination support."
    )
    @GetMapping("/search")
    public Page<JobApplicationResponse> searchApplications(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) ApplicationStatus status,
            @ParameterObject Pageable pageable
    ) {
        return service.searchApplications(
                companyName,
                location,
                status,
                pageable
        );
    }

    @Operation(summary = "Partially update a job application")
    @PatchMapping("/{id}")
    public JobApplicationResponse patchApplication(
            @PathVariable Long id,
            @Valid @RequestBody PatchJobApplicationRequest request
    ) {
        return service.patchApplication(id, request);
    }

    @Operation(summary = "Create an interview for a job application")
    @PostMapping("/{id}/interviews")
    public ResponseEntity<InterviewResponse> createInterview(
            @PathVariable Long id,
            @RequestBody CreateInterviewRequest request
    ) {
        InterviewResponse response =
                service.createInterview(id, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get interviews for a job application")
    @GetMapping("/{id}/interviews")
    public ResponseEntity<List<InterviewResponse>> getInterviews(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                service.getInterviews(id)
        );
    }

    @Operation(summary = "Update an interview outcome")
    @PatchMapping("/{applicationId}/interviews/{interviewId}/outcome")
    public ResponseEntity<InterviewResponse> updateInterviewOutcome(
            @PathVariable Long applicationId,
            @PathVariable Long interviewId,
            @RequestBody UpdateInterviewOutcomeRequest request
    ) {
        InterviewResponse response =
                service.updateInterviewOutcome(
                        applicationId,
                        interviewId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update interview details")
    @PutMapping("/{applicationId}/interviews/{interviewId}")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long applicationId,
            @PathVariable Long interviewId,
            @RequestBody UpdateInterviewRequest request
    ) {
        InterviewResponse response =
                service.updateInterview(
                        applicationId,
                        interviewId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an interview")
    @DeleteMapping("/{applicationId}/interviews/{interviewId}")
    public ResponseEntity<Void> deleteInterview(
            @PathVariable Long applicationId,
            @PathVariable Long interviewId
    ) {
        service.deleteInterview(applicationId, interviewId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get interview counts by outcome")
    @GetMapping("/interviews/analytics/outcomes")
    public ResponseEntity<List<InterviewOutcomeAnalyticsResponse>>
    getInterviewOutcomeAnalytics() {

        List<InterviewOutcomeAnalyticsResponse> response =
                service.getInterviewOutcomeAnalytics();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get upcoming interviews")
    @GetMapping("/interviews/upcoming")
    public ResponseEntity<List<InterviewResponse>> getUpcomingInterviews() {
        List<InterviewResponse> response =
                service.getUpcomingInterviews();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/summary")
    public ApplicationSummaryResponse getApplicationSummary() {
        return service.getApplicationSummary();
    }
}
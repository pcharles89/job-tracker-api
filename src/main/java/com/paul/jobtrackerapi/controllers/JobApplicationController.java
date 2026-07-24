package com.paul.jobtrackerapi.controllers;

import com.paul.jobtrackerapi.dtos.*;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.services.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
}
package com.paul.jobtrackerapi.dtos;

import com.paul.jobtrackerapi.entities.ApplicationStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatchJobApplicationRequest(
        @Size(max = 100, message = "Company name cannot exceed 100 characters")
        String companyName,

        @Size(max = 100, message = "Job title cannot exceed 100 characters")
        String jobTitle,

        String jobUrl,

        LocalDate appliedDate,

        String location,

        String salaryRange,

        String notes,

        ApplicationStatus status
) {
}

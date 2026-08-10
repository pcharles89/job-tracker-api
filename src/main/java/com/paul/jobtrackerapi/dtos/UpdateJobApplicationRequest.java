package com.paul.jobtrackerapi.dtos;

import com.paul.jobtrackerapi.entities.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateJobApplicationRequest {

    @NotBlank(message = "Company name cannot be blank")
    private String companyName;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @Size(max = 2048, message = "Job URL cannot exceed 2048 characters")
    private String jobUrl;

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private LocalDate appliedDate;
    private String location;
    private String salaryRange;
    private String notes;
}

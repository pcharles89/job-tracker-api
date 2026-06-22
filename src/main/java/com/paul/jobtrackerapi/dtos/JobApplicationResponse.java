package com.paul.jobtrackerapi.dtos;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JobApplicationResponse {
    private Long id;
    private String companyName;
    private String jobTitle;
    private String jobUrl;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private String location;
    private String salaryRange;
    private String notes;
}

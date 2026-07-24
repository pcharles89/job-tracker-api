package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.*;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.exceptions.InvalidCredentialsException;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.exceptions.UserNotFoundException;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.projections.StatusCountProjection;
import com.paul.jobtrackerapi.repositories.JobApplicationRepository;
import com.paul.jobtrackerapi.repositories.UserRepository;
import com.paul.jobtrackerapi.specifications.JobApplicationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobApplicationMapper mapper;
    private final UserRepository userRepository;

    public JobApplicationService(JobApplicationRepository repository,
                                 JobApplicationMapper mapper, UserRepository userRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("No authenticated user found.");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found."));
    }

    @Transactional
    public JobApplicationResponse createApplication(CreateJobApplicationRequest request) {
        User currentUser = getCurrentUser();

        JobApplication jobApplication = mapper.toEntity(request);
        jobApplication.setUser(currentUser);

        JobApplication savedApplication = repository.save(jobApplication);
        return mapper.toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getAllApplications(Pageable pageable) {
        User currentUser = getCurrentUser();

        return repository.findByUser(currentUser, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(Long id) {
        User currentUser = getCurrentUser();

        JobApplication jobApplication = repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));

        return mapper.toResponse(jobApplication);
    }

    @Transactional
    public JobApplicationResponse updateApplication(Long id, UpdateJobApplicationRequest request) {
        User currentUser = getCurrentUser();

        JobApplication existingApplication = repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));

        existingApplication.setCompanyName(request.getCompanyName());
        existingApplication.setJobTitle(request.getJobTitle());
        existingApplication.setJobUrl(request.getJobUrl());
        existingApplication.setStatus(request.getStatus());
        existingApplication.setAppliedDate(request.getAppliedDate());
        existingApplication.setLocation(request.getLocation());
        existingApplication.setSalaryRange(request.getSalaryRange());
        existingApplication.setNotes(request.getNotes());

        JobApplication savedApplication = repository.save(existingApplication);

        return mapper.toResponse(savedApplication);
    }

    @Transactional
    public void deleteApplication(Long id) {
        User currentUser = getCurrentUser();

        JobApplication application = repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));

        repository.delete(application);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> searchApplications(
            String companyName,
            String location,
            ApplicationStatus status,
            Pageable pageable
    ) {

        User currentUser = getCurrentUser();

        Specification<JobApplication> spec = JobApplicationSpecification.hasUser(currentUser);

        if (companyName != null) {
            spec = spec.and(
                    JobApplicationSpecification.hasCompanyName(companyName)
            );
        }

        if (location != null) {
            spec = spec.and(
                    JobApplicationSpecification.hasLocation(location)
            );
        }

        if (status != null) {
            spec = spec.and(
                    JobApplicationSpecification.hasStatus(status)
            );
        }

        return repository.findAll(spec, pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public JobApplicationResponse patchApplication(Long id, PatchJobApplicationRequest request) {
        User currentUser = getCurrentUser();

        JobApplication application = repository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));

        if (request.companyName() != null) {
            application.setCompanyName(request.companyName());
        }

        if (request.jobTitle() != null) {
            application.setJobTitle(request.jobTitle());
        }

        if (request.jobUrl() != null) {
            application.setJobUrl(request.jobUrl());
        }

        if (request.appliedDate() != null) {
            application.setAppliedDate(request.appliedDate());
        }

        if (request.location() != null) {
            application.setLocation(request.location());
        }

        if (request.salaryRange() != null) {
            application.setSalaryRange(request.salaryRange());
        }

        if (request.notes() != null) {
            application.setNotes(request.notes());
        }

        if (request.status() != null) {
            application.setStatus(request.status());
        }

        return mapper.toResponse(application);
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {

        User currentUser = getCurrentUser();

        List<StatusCountProjection> results =
                repository.countApplicationsByStatus(currentUser.getId());

        long applied = 0;
        long phoneScreen = 0;
        long technicalInterview = 0;
        long finalInterview = 0;
        long offer = 0;
        long rejected = 0;
        long withdrawn = 0;

        for (StatusCountProjection result : results) {

            switch (result.getStatus()) {

                case APPLIED:
                    applied = result.getCount();
                    break;

                case PHONE_SCREEN:
                    phoneScreen = result.getCount();
                    break;

                case TECHNICAL_INTERVIEW:
                    technicalInterview = result.getCount();
                    break;

                case FINAL_INTERVIEW:
                    finalInterview = result.getCount();
                    break;

                case OFFER:
                    offer = result.getCount();
                    break;

                case REJECTED:
                    rejected = result.getCount();
                    break;

                case WITHDRAWN:
                    withdrawn = result.getCount();
                    break;
            }
        }

        long totalApplications =
                applied
                        + phoneScreen
                        + technicalInterview
                        + finalInterview
                        + offer
                        + rejected
                        + withdrawn;

        return AnalyticsResponse.builder()
                .totalApplications(totalApplications)
                .applied(applied)
                .phoneScreen(phoneScreen)
                .technicalInterview(technicalInterview)
                .finalInterview(finalInterview)
                .offer(offer)
                .rejected(rejected)
                .withdrawn(withdrawn)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CompanyAnalyticsResponse> getCompanyAnalytics() {

        User currentUser = getCurrentUser();

        return repository.countApplicationsByCompany(currentUser.getId())
                .stream()
                .map(result -> CompanyAnalyticsResponse.builder()
                        .companyName(result.getCompanyName())
                        .count(result.getCount())
                        .build())
                .toList();
    }
}

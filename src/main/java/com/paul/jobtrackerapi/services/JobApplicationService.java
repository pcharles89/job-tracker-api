package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.*;
import com.paul.jobtrackerapi.entities.*;
import com.paul.jobtrackerapi.exceptions.InterviewNotFoundException;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.exceptions.UserNotFoundException;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.projections.StatusCountProjection;
import com.paul.jobtrackerapi.repositories.ApplicationStatusHistoryRepository;
import com.paul.jobtrackerapi.repositories.InterviewRepository;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobApplicationMapper mapper;
    private final UserRepository userRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final InterviewRepository interviewRepository;

    public JobApplicationService(JobApplicationRepository repository,
                                 JobApplicationMapper mapper, UserRepository userRepository,
                                 ApplicationStatusHistoryRepository statusHistoryRepository,
                                 InterviewRepository interviewRepository) {

        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.interviewRepository = interviewRepository;
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

        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .jobApplication(savedApplication)
                .status(savedApplication.getStatus())
                .changedAt(LocalDateTime.now())
                .build();

        statusHistoryRepository.save(history);

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

        ApplicationStatus oldStatus = existingApplication.getStatus();

        existingApplication.setCompanyName(request.getCompanyName());
        existingApplication.setJobTitle(request.getJobTitle());
        existingApplication.setJobUrl(request.getJobUrl());
        existingApplication.setStatus(request.getStatus());
        existingApplication.setAppliedDate(request.getAppliedDate());
        existingApplication.setLocation(request.getLocation());
        existingApplication.setSalaryRange(request.getSalaryRange());
        existingApplication.setNotes(request.getNotes());

        JobApplication savedApplication = repository.save(existingApplication);

        if(oldStatus != savedApplication.getStatus()) {
            ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                    .jobApplication(savedApplication)
                    .status(savedApplication.getStatus())
                    .changedAt(LocalDateTime.now())
                    .build();

            statusHistoryRepository.save(history);
        }

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

        ApplicationStatus oldStatus = application.getStatus();

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

        if(oldStatus != application.getStatus()) {
            ApplicationStatusHistory history =
                    ApplicationStatusHistory.builder()
                            .jobApplication(application)
                            .status(application.getStatus())
                            .changedAt(LocalDateTime.now())
                            .build();

            statusHistoryRepository.save(history);
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

    @Transactional(readOnly = true)
    public List<LocationAnalyticsResponse> getLocationAnalytics() {

        User currentUser = getCurrentUser();

        return repository.countApplicationsByLocation(currentUser.getId())
                .stream()
                .map(result -> LocationAnalyticsResponse.builder()
                        .location(result.getLocation())
                        .count(result.getCount())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistoryResponse> getStatusHistory(
            Long applicationId
    ) {
                JobApplication application =
                        getApplicationForCurrentUser(applicationId);

                return statusHistoryRepository
                    .findByJobApplicationIdOrderByChangedAtAsc(applicationId)
                    .stream()
                    .map(history -> new ApplicationStatusHistoryResponse(
                        history.getId(),
                        history.getStatus(),
                        history.getChangedAt()
                    ))
                    .toList();
    }

    @Transactional
    public InterviewResponse createInterview(
            Long applicationId,
            CreateInterviewRequest request
    ) {
        JobApplication application =
                getApplicationForCurrentUser(applicationId);

        Interview interview = Interview.builder()
                .jobApplication(application)
                .type(request.type())
                .scheduledAt(request.scheduledAt())
                .notes(request.notes())
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        return new InterviewResponse(
                savedInterview.getId(),
                savedInterview.getType(),
                savedInterview.getScheduledAt(),
                savedInterview.getNotes(),
                savedInterview.getOutcome()
        );
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviews(Long applicationId) {

        JobApplication application =
                getApplicationForCurrentUser(applicationId);

        return interviewRepository
                .findByJobApplicationIdOrderByScheduledAtAsc(applicationId)
                .stream()
                .map(interview -> new InterviewResponse(
                        interview.getId(),
                        interview.getType(),
                        interview.getScheduledAt(),
                        interview.getNotes(),
                        interview.getOutcome()
                ))
                .toList();
    }

    @Transactional
    public InterviewResponse updateInterviewOutcome(
            Long applicationId,
            Long interviewId,
            UpdateInterviewOutcomeRequest request
    ) {
        JobApplication application =
                getApplicationForCurrentUser(applicationId);

        Interview interview =
                interviewRepository.findById(interviewId)
                        .orElseThrow(() ->
                                new InterviewNotFoundException(interviewId)
                        );

        if (!interview.getJobApplication().getId().equals(application.getId())) {
            throw new InterviewNotFoundException(interviewId);
        }

        interview.setOutcome(request.outcome());

        Interview savedInterview =
                interviewRepository.save(interview);

        return new InterviewResponse(
                savedInterview.getId(),
                savedInterview.getType(),
                savedInterview.getScheduledAt(),
                savedInterview.getNotes(),
                savedInterview.getOutcome()
        );
    }

    @Transactional
    public InterviewResponse updateInterview(
            Long applicationId,
            Long interviewId,
            UpdateInterviewRequest request
    ) {
        JobApplication application =
                getApplicationForCurrentUser(applicationId);

        Interview interview =
                interviewRepository.findById(interviewId)
                        .orElseThrow(() ->
                                new InterviewNotFoundException(interviewId)
                        );

        if (!interview.getJobApplication().getId()
                .equals(application.getId())) {
            throw new InterviewNotFoundException(interviewId);
        }

        interview.setType(request.type());
        interview.setScheduledAt(request.scheduledAt());
        interview.setNotes(request.notes());

        Interview savedInterview =
                interviewRepository.save(interview);

        return new InterviewResponse(
                savedInterview.getId(),
                savedInterview.getType(),
                savedInterview.getScheduledAt(),
                savedInterview.getNotes(),
                savedInterview.getOutcome()
        );
    }

    private JobApplication getApplicationForCurrentUser(Long applicationId) {
        User currentUser = getCurrentUser();

        return repository.findByIdAndUser(applicationId, currentUser)
                .orElseThrow(() ->
                        new JobApplicationNotFoundException(applicationId)
                );
    }

    @Transactional
    public void deleteInterview(
            Long applicationId,
            Long interviewId
    ) {
        JobApplication application =
                getApplicationForCurrentUser(applicationId);

        Interview interview =
                interviewRepository.findById(interviewId)
                        .orElseThrow(() ->
                                new InterviewNotFoundException(interviewId)
                        );

        if (!interview.getJobApplication().getId()
                .equals(application.getId())) {
            throw new InterviewNotFoundException(interviewId);
        }

        interviewRepository.delete(interview);
    }

    @Transactional(readOnly = true)
    public List<InterviewOutcomeAnalyticsResponse> getInterviewOutcomeAnalytics() {
        User currentUser = getCurrentUser();

        return interviewRepository
                .countByOutcomeForUser(currentUser.getId())
                .stream()
                .map(projection ->
                        new InterviewOutcomeAnalyticsResponse(
                                projection.getOutcome(),
                                projection.getCount()
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getUpcomingInterviews() {
        User currentUser = getCurrentUser();

        LocalDateTime now = LocalDateTime.now();

        return interviewRepository
                .findByJobApplicationUserIdAndScheduledAtAfterOrderByScheduledAtAsc(
                        currentUser.getId(),
                        now
                )
                .stream()
                .map(interview ->
                        new InterviewResponse(
                                interview.getId(),
                                interview.getType(),
                                interview.getScheduledAt(),
                                interview.getNotes(),
                                interview.getOutcome()
                        )
                )
                .toList();
    }


}

package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.*;
import com.paul.jobtrackerapi.entities.*;
import com.paul.jobtrackerapi.exceptions.InterviewNotFoundException;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.projections.InterviewOutcomeCountProjection;
import com.paul.jobtrackerapi.repositories.ApplicationStatusHistoryRepository;
import com.paul.jobtrackerapi.repositories.InterviewRepository;
import com.paul.jobtrackerapi.repositories.JobApplicationRepository;
import com.paul.jobtrackerapi.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JobApplicationServiceTest {

    private JobApplicationRepository repository;
    private JobApplicationMapper mapper;
    private JobApplicationService service;
    private UserRepository userRepository;
    private ApplicationStatusHistoryRepository statusHistoryRepository;
    private InterviewRepository interviewRepository;
    private User user;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(JobApplicationRepository.class);
        mapper = Mockito.mock(JobApplicationMapper.class);
        userRepository = Mockito.mock(UserRepository.class);
        statusHistoryRepository = Mockito.mock(ApplicationStatusHistoryRepository.class);
        interviewRepository = Mockito.mock(InterviewRepository.class);

        service = new JobApplicationService(repository, mapper, userRepository, statusHistoryRepository,
                interviewRepository);

        user = User.builder()
                .id(1L)
                .username("paul")
                .password("hashedPassword")
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "paul",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void createApplication_shouldReturnCreatedApplication() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        request.setCompanyName("Amazon");
        request.setJobTitle("Backend Developer");

        JobApplication entity = new JobApplication();
        entity.setCompanyName("Amazon");
        entity.setJobTitle("Backend Developer");

        JobApplication savedEntity = new JobApplication();
        savedEntity.setId(1L);
        savedEntity.setCompanyName("Amazon");
        savedEntity.setJobTitle("Backend Developer");

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Amazon");
        response.setJobTitle("Backend Developer");

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        JobApplicationResponse result = service.createApplication(request);

        assertEquals(1L, result.getId());
        assertEquals("Amazon", result.getCompanyName());
        assertEquals("Backend Developer", result.getJobTitle());

        Mockito.verify(mapper).toEntity(request);
        Mockito.verify(repository).save(entity);
        Mockito.verify(mapper).toResponse(savedEntity);
    }

    @Test
    void getApplicationById_shouldReturnApplication_whenApplicationExists() {
        Long id = 1L;

        JobApplication entity = new JobApplication();
        entity.setId(id);
        entity.setCompanyName("Amazon");
        entity.setJobTitle("Backend Developer");

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(id);
        response.setCompanyName("Amazon");
        response.setJobTitle("Backend Developer");

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.of(entity));

        when(mapper.toResponse(entity)).thenReturn(response);

        JobApplicationResponse result = service.getApplicationById(id);

        assertEquals(id, result.getId());
        assertEquals("Amazon", result.getCompanyName());
        assertEquals("Backend Developer", result.getJobTitle());

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verify(mapper).toResponse(entity);
    }

    @Test
    void getApplicationById_shouldThrowException_whenApplicationDoesNotExist() {
        Long id = 99L;

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.getApplicationById(id)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void updateApplication_shouldReturnUpdatedApplication_whenApplicationExists() {
        Long id = 1L;

        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Java Developer");
        request.setStatus(ApplicationStatus.APPLIED);

        JobApplication existingEntity = new JobApplication();
        existingEntity.setId(id);
        existingEntity.setCompanyName("Amazon");
        existingEntity.setJobTitle("Backend Developer");
        existingEntity.setStatus(ApplicationStatus.APPLIED);

        JobApplication savedEntity = new JobApplication();
        savedEntity.setId(id);
        savedEntity.setCompanyName("Google");
        savedEntity.setJobTitle("Java Developer");
        savedEntity.setStatus(ApplicationStatus.APPLIED);

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(id);
        response.setCompanyName("Google");
        response.setJobTitle("Java Developer");

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.of(existingEntity));

        when(repository.save(existingEntity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        JobApplicationResponse result = service.updateApplication(id, request);

        assertEquals(id, result.getId());
        assertEquals("Google", result.getCompanyName());
        assertEquals("Java Developer", result.getJobTitle());

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verify(repository).save(existingEntity);
        Mockito.verify(mapper).toResponse(savedEntity);
    }

    @Test
    void updateApplication_shouldThrowException_whenApplicationDoesNotExist() {
        Long id = 99L;

        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Java Developer");

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.updateApplication(id, request)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void deleteApplication_shouldDeleteApplication_whenApplicationExists() {
        Long id = 1L;

        JobApplication entity = new JobApplication();
        entity.setId(id);
        entity.setCompanyName("Amazon");
        entity.setJobTitle("Backend Developer");

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.of(entity));

        service.deleteApplication(id);

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verify(repository).delete(entity);
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void patchApplication_shouldUpdateOnlyNonNullFields_whenApplicationExists() {
        Long id = 1L;

        PatchJobApplicationRequest request = new PatchJobApplicationRequest(
                "Netflix",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        JobApplication existingEntity = new JobApplication();
        existingEntity.setId(id);
        existingEntity.setCompanyName("Amazon");
        existingEntity.setJobTitle("Backend Developer");

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(id);
        response.setCompanyName("Netflix");
        response.setJobTitle("Backend Developer");

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.of(existingEntity));

        when(mapper.toResponse(existingEntity))
                .thenReturn(response);

        JobApplicationResponse result =
                service.patchApplication(id, request);

        assertEquals(id, result.getId());
        assertEquals("Netflix", result.getCompanyName());
        assertEquals("Backend Developer", result.getJobTitle());

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verify(mapper).toResponse(existingEntity);
    }

    @Test
    void patchApplication_shouldThrowException_whenApplicationDoesNotExist() {
        Long id = 99L;

        PatchJobApplicationRequest request = new PatchJobApplicationRequest(
                "Netflix",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.patchApplication(id, request)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void deleteApplication_shouldThrowException_whenApplicationDoesNotExist() {
        Long id = 99L;

        when(repository.findByIdAndUser(
                Mockito.eq(id),
                any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.deleteApplication(id)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                any()
        );
        Mockito.verify(repository, Mockito.never())
                .delete(any(JobApplication.class));
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void getAllApplications_shouldReturnPagedApplications() {
        Pageable pageable = Pageable.unpaged();

        JobApplication entity = new JobApplication();
        entity.setId(1L);
        entity.setCompanyName("Amazon");
        entity.setJobTitle("Backend Developer");

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Amazon");
        response.setJobTitle("Backend Developer");

        Page<JobApplication> entityPage = new PageImpl<>(List.of(entity));

        when(repository.findByUser(
                any(),
                Mockito.eq(pageable)
        )).thenReturn(entityPage);

        when(mapper.toResponse(entity)).thenReturn(response);

        Page<JobApplicationResponse> result = service.getAllApplications(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Amazon", result.getContent().get(0).getCompanyName());
        assertEquals("Backend Developer", result.getContent().get(0).getJobTitle());

        Mockito.verify(repository).findByUser(
                any(),
                Mockito.eq(pageable)
        );
        Mockito.verify(mapper).toResponse(entity);
    }

    @Test
    void searchApplications_shouldReturnMatchingApplications() {
        Pageable pageable = Pageable.unpaged();

        JobApplication entity = new JobApplication();
        entity.setId(1L);
        entity.setCompanyName("Amazon");
        entity.setJobTitle("Backend Developer");

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(1L);
        response.setCompanyName("Amazon");
        response.setJobTitle("Backend Developer");

        Page<JobApplication> entityPage = new PageImpl<>(List.of(entity));

        when(repository.findAll(
                Mockito.<Specification<JobApplication>>any(),
                Mockito.eq(pageable)
        )).thenReturn(entityPage);

        when(mapper.toResponse(entity)).thenReturn(response);

        Page<JobApplicationResponse> result = service.searchApplications(
                "amazon",
                null,
                null,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Amazon", result.getContent().get(0).getCompanyName());

        Mockito.verify(repository).findAll(
                Mockito.<Specification<JobApplication>>any(),
                Mockito.eq(pageable)
        );
        Mockito.verify(mapper).toResponse(entity);
    }

    @Test
    void createApplicationShouldCreateInitialStatusHistory() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Backend Developer");

        JobApplication mappedApplication = new JobApplication();
        mappedApplication.setCompanyName("Google");
        mappedApplication.setJobTitle("Backend Developer");
        mappedApplication.setStatus(ApplicationStatus.APPLIED);

        JobApplication savedApplication = new JobApplication();
        savedApplication.setId(10L);
        savedApplication.setCompanyName("Google");
        savedApplication.setJobTitle("Backend Developer");
        savedApplication.setStatus(ApplicationStatus.APPLIED);

        when(mapper.toEntity(request))
                .thenReturn(mappedApplication);

        when(repository.save(mappedApplication))
                .thenReturn(savedApplication);

        LocalDateTime before = LocalDateTime.now();

        service.createApplication(request);

        LocalDateTime after = LocalDateTime.now();

        Mockito.verify(statusHistoryRepository)
                .save(Mockito.argThat(history -> {
                    LocalDateTime changedAt = history.getChangedAt();

                    return history.getJobApplication() == savedApplication
                            && history.getStatus() == ApplicationStatus.APPLIED
                            && changedAt != null
                            && !changedAt.isBefore(before)
                            && !changedAt.isAfter(after);
                }));
    }

    @Test
    void updateApplicationShouldCreateHistoryWhenStatusChanges() {
        Long applicationId = 10L;

        JobApplication existingApplication = new JobApplication();
        existingApplication.setId(applicationId);
        existingApplication.setCompanyName("Google");
        existingApplication.setJobTitle("Backend Developer");
        existingApplication.setStatus(ApplicationStatus.APPLIED);
        existingApplication.setUser(user);

        UpdateJobApplicationRequest request =
                new UpdateJobApplicationRequest();

        request.setCompanyName("Google");
        request.setJobTitle("Backend Developer");
        request.setStatus(ApplicationStatus.PHONE_SCREEN);

        when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(existingApplication));

        when(repository.save(existingApplication))
                .thenReturn(existingApplication);

        LocalDateTime before = LocalDateTime.now();

        service.updateApplication(applicationId, request);

        LocalDateTime after = LocalDateTime.now();

        Mockito.verify(statusHistoryRepository)
                .save(Mockito.argThat(history -> {
                    LocalDateTime changedAt = history.getChangedAt();

                    return history.getJobApplication() == existingApplication
                            && history.getStatus()
                            == ApplicationStatus.PHONE_SCREEN
                            && changedAt != null
                            && !changedAt.isBefore(before)
                            && !changedAt.isAfter(after);
                }));
    }

    @Test
    void updateApplicationShouldNotCreateHistoryWhenStatusDoesNotChange() {
        Long applicationId = 10L;

        JobApplication existingApplication = new JobApplication();
        existingApplication.setId(applicationId);
        existingApplication.setCompanyName("Google");
        existingApplication.setJobTitle("Backend Developer");
        existingApplication.setStatus(ApplicationStatus.APPLIED);
        existingApplication.setUser(user);

        UpdateJobApplicationRequest request =
                new UpdateJobApplicationRequest();

        request.setCompanyName("Google");
        request.setJobTitle("Senior Backend Developer");
        request.setStatus(ApplicationStatus.APPLIED);

        when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(existingApplication));

        when(repository.save(existingApplication))
                .thenReturn(existingApplication);

        service.updateApplication(applicationId, request);

        Mockito.verify(statusHistoryRepository, Mockito.never())
                .save(any(ApplicationStatusHistory.class));
    }

    @Test
    void patchApplicationShouldCreateHistoryWhenStatusChanges() {
        Long applicationId = 10L;

        JobApplication existingApplication = new JobApplication();
        existingApplication.setId(applicationId);
        existingApplication.setCompanyName("Google");
        existingApplication.setJobTitle("Backend Developer");
        existingApplication.setStatus(ApplicationStatus.APPLIED);
        existingApplication.setUser(user);

        PatchJobApplicationRequest request =
                new PatchJobApplicationRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        ApplicationStatus.PHONE_SCREEN
                );

        when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(existingApplication));

        LocalDateTime before = LocalDateTime.now();

        service.patchApplication(applicationId, request);

        LocalDateTime after = LocalDateTime.now();

        Mockito.verify(statusHistoryRepository)
                .save(Mockito.argThat(history -> {
                    LocalDateTime changedAt = history.getChangedAt();

                    return history.getJobApplication() == existingApplication
                            && history.getStatus() == ApplicationStatus.PHONE_SCREEN
                            && changedAt != null
                            && !changedAt.isBefore(before)
                            && !changedAt.isAfter(after);
                }));
    }

    @Test
    void getStatusHistoryShouldReturnHistoryResponses() {
        Long applicationId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        LocalDateTime firstTime =
                LocalDateTime.of(2026, 8, 1, 9, 0);

        LocalDateTime secondTime =
                LocalDateTime.of(2026, 8, 5, 14, 30);

        ApplicationStatusHistory firstHistory =
                ApplicationStatusHistory.builder()
                        .id(1L)
                        .jobApplication(application)
                        .status(ApplicationStatus.APPLIED)
                        .changedAt(firstTime)
                        .build();

        ApplicationStatusHistory secondHistory =
                ApplicationStatusHistory.builder()
                        .id(2L)
                        .jobApplication(application)
                        .status(ApplicationStatus.PHONE_SCREEN)
                        .changedAt(secondTime)
                        .build();

        when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        when(
                statusHistoryRepository
                        .findByJobApplicationIdOrderByChangedAtAsc(applicationId)
        ).thenReturn(List.of(firstHistory, secondHistory));

        List<ApplicationStatusHistoryResponse> result =
                service.getStatusHistory(applicationId);

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).id());
        assertEquals(ApplicationStatus.APPLIED, result.get(0).status());
        assertEquals(firstTime, result.get(0).changedAt());

        assertEquals(2L, result.get(1).id());
        assertEquals(
                ApplicationStatus.PHONE_SCREEN,
                result.get(1).status()
        );
        assertEquals(secondTime, result.get(1).changedAt());
    }

    @Test
    void getStatusHistoryShouldThrowWhenApplicationNotFoundForUser() {
        Long applicationId = 10L;

        when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.getStatusHistory(applicationId)
        );

        Mockito.verify(
                statusHistoryRepository,
                Mockito.never()
        ).findByJobApplicationIdOrderByChangedAtAsc(Mockito.anyLong());
    }

    @Test
    void createInterviewShouldCreateAndReturnInterview() {
        Long applicationId = 1L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        LocalDateTime scheduledAt =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        CreateInterviewRequest request =
                new CreateInterviewRequest(
                        InterviewType.TECHNICAL,
                        scheduledAt,
                        "Java and SQL interview"
                );

        when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        when(interviewRepository.save(any(Interview.class)))
                .thenAnswer(invocation -> {
                    Interview interview = invocation.getArgument(0);
                    interview.setId(10L);
                    return interview;
                });

        InterviewResponse result =
                service.createInterview(applicationId, request);

        assertEquals(10L, result.id());
        assertEquals(InterviewType.TECHNICAL, result.type());
        assertEquals(scheduledAt, result.scheduledAt());
        assertEquals("Java and SQL interview", result.notes());
        assertEquals(InterviewOutcome.PENDING, result.outcome());

        Mockito.verify(interviewRepository)
                .save(Mockito.any(Interview.class));
    }

    @Test
    void createInterviewShouldThrowWhenApplicationNotFoundForUser() {
        Long applicationId = 1L;

        LocalDateTime scheduledAt =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        CreateInterviewRequest request =
                new CreateInterviewRequest(
                        InterviewType.TECHNICAL,
                        scheduledAt,
                        "Java and SQL interview"
                );

        when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.createInterview(applicationId, request)
        );

        Mockito.verify(interviewRepository, Mockito.never())
                .save(Mockito.any(Interview.class));
    }

    @Test
    void getInterviewsShouldReturnInterviewResponses() {
        Long applicationId = 1L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        LocalDateTime firstTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime secondTime =
                LocalDateTime.of(2026, 8, 25, 14, 30);

        Interview firstInterview = Interview.builder()
                .id(10L)
                .jobApplication(application)
                .type(InterviewType.PHONE)
                .scheduledAt(firstTime)
                .notes("Recruiter screen")
                .outcome(InterviewOutcome.PASSED)
                .build();

        Interview secondInterview = Interview.builder()
                .id(11L)
                .jobApplication(application)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(secondTime)
                .notes("Java and SQL interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(
                interviewRepository
                        .findByJobApplicationIdOrderByScheduledAtAsc(applicationId)
        ).thenReturn(List.of(firstInterview, secondInterview));

        List<InterviewResponse> result =
                service.getInterviews(applicationId);

        assertEquals(2, result.size());

        assertEquals(10L, result.get(0).id());
        assertEquals(InterviewType.PHONE, result.get(0).type());
        assertEquals(firstTime, result.get(0).scheduledAt());
        assertEquals("Recruiter screen", result.get(0).notes());
        assertEquals(InterviewOutcome.PASSED, result.get(0).outcome());

        assertEquals(11L, result.get(1).id());
        assertEquals(InterviewType.TECHNICAL, result.get(1).type());
        assertEquals(secondTime, result.get(1).scheduledAt());
        assertEquals("Java and SQL interview", result.get(1).notes());
        assertEquals(InterviewOutcome.PENDING, result.get(1).outcome());
    }

    @Test
    void getInterviewsShouldThrowWhenApplicationNotFoundForUser() {
        Long applicationId = 1L;

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.getInterviews(applicationId)
        );

        Mockito.verify(
                interviewRepository,
                Mockito.never()
        ).findByJobApplicationIdOrderByScheduledAtAsc(Mockito.anyLong());
    }

    @Test
    void updateInterviewOutcomeShouldUpdateAndReturnInterview() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        Interview interview = Interview.builder()
                .id(interviewId)
                .jobApplication(application)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Java and SQL interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        Mockito.when(interviewRepository.save(interview))
                .thenReturn(interview);

        InterviewResponse result =
                service.updateInterviewOutcome(
                        applicationId,
                        interviewId,
                        request
                );

        assertEquals(interviewId, result.id());
        assertEquals(InterviewType.TECHNICAL, result.type());
        assertEquals(InterviewOutcome.PASSED, result.outcome());

        Mockito.verify(interviewRepository).save(
                Mockito.argThat(saved ->
                        saved.getOutcome() == InterviewOutcome.PASSED
                )
        );
    }

    @Test
    void updateInterviewOutcomeShouldThrowWhenApplicationNotFoundForUser() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.updateInterviewOutcome(
                        applicationId,
                        interviewId,
                        request
                )
        );

        Mockito.verify(interviewRepository, Mockito.never())
                .findById(Mockito.anyLong());

        Mockito.verify(interviewRepository, Mockito.never())
                .save(Mockito.any(Interview.class));
    }

    @Test
    void updateInterviewOutcomeShouldThrowWhenInterviewNotFound() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                InterviewNotFoundException.class,
                () -> service.updateInterviewOutcome(
                        applicationId,
                        interviewId,
                        request
                )
        );

        Mockito.verify(interviewRepository, Mockito.never())
                .save(Mockito.any(Interview.class));
    }

    @Test
    void updateInterviewOutcomeShouldThrowWhenInterviewBelongsToDifferentApplication() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        JobApplication differentApplication = new JobApplication();
        differentApplication.setId(2L);
        differentApplication.setUser(user);

        Interview interview = Interview.builder()
                .id(interviewId)
                .jobApplication(differentApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Java and SQL interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        assertThrows(
                InterviewNotFoundException.class,
                () -> service.updateInterviewOutcome(
                        applicationId,
                        interviewId,
                        request
                )
        );

        Mockito.verify(interviewRepository, Mockito.never())
                .save(Mockito.any(Interview.class));
    }

    @Test
    void updateInterviewShouldUpdateAndReturnInterview() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        Interview interview = Interview.builder()
                .id(interviewId)
                .jobApplication(application)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Old interview details")
                .outcome(InterviewOutcome.PENDING)
                .build();

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round with engineering manager"
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        Mockito.when(interviewRepository.save(interview))
                .thenReturn(interview);

        InterviewResponse result =
                service.updateInterview(
                        applicationId,
                        interviewId,
                        request
                );

        assertEquals(interviewId, result.id());
        assertEquals(InterviewType.FINAL, result.type());
        assertEquals(
                LocalDateTime.of(2026, 8, 30, 15, 0),
                result.scheduledAt()
        );
        assertEquals(
                "Final round with engineering manager",
                result.notes()
        );
        assertEquals(
                InterviewOutcome.PENDING,
                result.outcome()
        );

        Mockito.verify(interviewRepository).save(
                Mockito.argThat(saved ->
                        saved.getType() == InterviewType.FINAL
                                && saved.getScheduledAt().equals(
                                LocalDateTime.of(2026, 8, 30, 15, 0)
                        )
                                && saved.getNotes().equals(
                                "Final round with engineering manager"
                        )
                                && saved.getOutcome() == InterviewOutcome.PENDING
                )
        );
    }

    @Test
    void updateInterviewShouldThrowWhenApplicationNotFoundForUser() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.updateInterview(
                        applicationId,
                        interviewId,
                        request
                )
        );

        Mockito.verify(interviewRepository, Mockito.never())
                .findById(Mockito.anyLong());

        Mockito.verify(interviewRepository, Mockito.never())
                .save(Mockito.any(Interview.class));
    }

    @Test
    void updateInterviewShouldThrowWhenInterviewNotFound() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                InterviewNotFoundException.class,
                () -> service.updateInterview(
                        applicationId,
                        interviewId,
                        request
                )
        );

        Mockito.verify(interviewRepository)
                .findById(interviewId);

        Mockito.verify(interviewRepository, Mockito.never())
                .save(Mockito.any(Interview.class));
    }

    @Test
    void updateInterviewShouldThrowWhenInterviewBelongsToDifferentApplication() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        JobApplication differentApplication = new JobApplication();
        differentApplication.setId(2L);
        differentApplication.setUser(user);

        Interview interview = Interview.builder()
                .id(interviewId)
                .jobApplication(differentApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Old interview details")
                .outcome(InterviewOutcome.PENDING)
                .build();

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        assertThrows(
                InterviewNotFoundException.class,
                () -> service.updateInterview(
                        applicationId,
                        interviewId,
                        request
                )
        );

        Mockito.verify(interviewRepository, Mockito.never())
                .save(Mockito.any(Interview.class));
    }

    @Test
    void deleteInterviewShouldDeleteInterview() {
        Long applicationId = 1L;
        Long interviewId = 10L;

        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUser(user);

        Interview interview = Interview.builder()
                .id(interviewId)
                .jobApplication(application)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Java interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Mockito.when(repository.findByIdAndUser(applicationId, user))
                .thenReturn(Optional.of(application));

        Mockito.when(interviewRepository.findById(interviewId))
                .thenReturn(Optional.of(interview));

        service.deleteInterview(
                applicationId,
                interviewId
        );

        Mockito.verify(interviewRepository)
                .delete(interview);
    }

    @Test
    void getInterviewOutcomeAnalyticsShouldReturnAnalytics() {
        InterviewOutcomeCountProjection pendingProjection =
                Mockito.mock(InterviewOutcomeCountProjection.class);

        InterviewOutcomeCountProjection passedProjection =
                Mockito.mock(InterviewOutcomeCountProjection.class);

        Mockito.when(pendingProjection.getOutcome())
                .thenReturn(InterviewOutcome.PENDING);

        Mockito.when(pendingProjection.getCount())
                .thenReturn(3L);

        Mockito.when(passedProjection.getOutcome())
                .thenReturn(InterviewOutcome.PASSED);

        Mockito.when(passedProjection.getCount())
                .thenReturn(5L);

        Mockito.when(
                interviewRepository.countByOutcomeForUser(user.getId())
        ).thenReturn(
                List.of(pendingProjection, passedProjection)
        );

        List<InterviewOutcomeAnalyticsResponse> result =
                service.getInterviewOutcomeAnalytics();

        assertEquals(2, result.size());

        assertEquals(InterviewOutcome.PENDING, result.get(0).outcome());
        assertEquals(3L, result.get(0).count());

        assertEquals(InterviewOutcome.PASSED, result.get(1).outcome());
        assertEquals(5L, result.get(1).count());
    }

    @Test
    void getInterviewOutcomeAnalyticsShouldReturnEmptyListWhenNoInterviewsExist() {

        Mockito.when(
                interviewRepository.countByOutcomeForUser(user.getId())
        ).thenReturn(List.of());

        List<InterviewOutcomeAnalyticsResponse> result =
                service.getInterviewOutcomeAnalytics();

        assertTrue(result.isEmpty());
    }
}

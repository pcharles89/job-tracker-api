package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.applications.*;
import com.paul.jobtrackerapi.entities.*;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.repositories.ApplicationStatusHistoryRepository;
import com.paul.jobtrackerapi.repositories.InterviewRepository;
import com.paul.jobtrackerapi.repositories.JobApplicationRepository;
import com.paul.jobtrackerapi.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
}

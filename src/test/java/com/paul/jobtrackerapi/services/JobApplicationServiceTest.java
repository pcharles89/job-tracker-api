package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.CreateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.JobApplicationResponse;
import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.repositories.JobApplicationRepository;
import com.paul.jobtrackerapi.dtos.UpdateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.PatchJobApplicationRequest;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobApplicationServiceTest {

    private JobApplicationRepository repository;
    private JobApplicationMapper mapper;
    private JobApplicationService service;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(JobApplicationRepository.class);
        mapper = Mockito.mock(JobApplicationMapper.class);
        userRepository = Mockito.mock(UserRepository.class);

        service = new JobApplicationService(repository, mapper, userRepository);

        User user = User.builder()
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

        Mockito.when(userRepository.findByUsername("paul"))
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

        Mockito.when(mapper.toEntity(request)).thenReturn(entity);
        Mockito.when(repository.save(entity)).thenReturn(savedEntity);
        Mockito.when(mapper.toResponse(savedEntity)).thenReturn(response);

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

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.of(entity));

        Mockito.when(mapper.toResponse(entity)).thenReturn(response);

        JobApplicationResponse result = service.getApplicationById(id);

        assertEquals(id, result.getId());
        assertEquals("Amazon", result.getCompanyName());
        assertEquals("Backend Developer", result.getJobTitle());

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        );
        Mockito.verify(mapper).toResponse(entity);
    }

    @Test
    void getApplicationById_shouldThrowException_whenApplicationDoesNotExist() {
        Long id = 99L;

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.getApplicationById(id)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        );
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void updateApplication_shouldReturnUpdatedApplication_whenApplicationExists() {
        Long id = 1L;

        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Java Developer");

        JobApplication existingEntity = new JobApplication();
        existingEntity.setId(id);
        existingEntity.setCompanyName("Amazon");
        existingEntity.setJobTitle("Backend Developer");

        JobApplication savedEntity = new JobApplication();
        savedEntity.setId(id);
        savedEntity.setCompanyName("Google");
        savedEntity.setJobTitle("Java Developer");

        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(id);
        response.setCompanyName("Google");
        response.setJobTitle("Java Developer");

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.of(existingEntity));

        Mockito.when(repository.save(existingEntity)).thenReturn(savedEntity);
        Mockito.when(mapper.toResponse(savedEntity)).thenReturn(response);

        JobApplicationResponse result = service.updateApplication(id, request);

        assertEquals(id, result.getId());
        assertEquals("Google", result.getCompanyName());
        assertEquals("Java Developer", result.getJobTitle());

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
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

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.updateApplication(id, request)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
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

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.of(entity));

        service.deleteApplication(id);

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
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

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.of(existingEntity));

        Mockito.when(mapper.toResponse(existingEntity))
                .thenReturn(response);

        JobApplicationResponse result =
                service.patchApplication(id, request);

        assertEquals(id, result.getId());
        assertEquals("Netflix", result.getCompanyName());
        assertEquals("Backend Developer", result.getJobTitle());

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
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

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.patchApplication(id, request)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        );
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void deleteApplication_shouldThrowException_whenApplicationDoesNotExist() {
        Long id = 99L;

        Mockito.when(repository.findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        )).thenReturn(Optional.empty());

        assertThrows(
                JobApplicationNotFoundException.class,
                () -> service.deleteApplication(id)
        );

        Mockito.verify(repository).findByIdAndUser(
                Mockito.eq(id),
                Mockito.any()
        );
        Mockito.verify(repository, Mockito.never())
                .delete(Mockito.any(JobApplication.class));
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

        Mockito.when(repository.findByUser(
                Mockito.any(),
                Mockito.eq(pageable)
        )).thenReturn(entityPage);

        Mockito.when(mapper.toResponse(entity)).thenReturn(response);

        Page<JobApplicationResponse> result = service.getAllApplications(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Amazon", result.getContent().get(0).getCompanyName());
        assertEquals("Backend Developer", result.getContent().get(0).getJobTitle());

        Mockito.verify(repository).findByUser(
                Mockito.any(),
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

        Mockito.when(repository.findAll(
                Mockito.<Specification<JobApplication>>any(),
                Mockito.eq(pageable)
        )).thenReturn(entityPage);

        Mockito.when(mapper.toResponse(entity)).thenReturn(response);

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
}

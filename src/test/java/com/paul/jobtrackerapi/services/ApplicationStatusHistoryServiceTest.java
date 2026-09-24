package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.applications.ApplicationStatusHistoryResponse;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.entities.ApplicationStatusHistory;
import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.exceptions.JobApplicationNotFoundException;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.repositories.ApplicationStatusHistoryRepository;
import com.paul.jobtrackerapi.repositories.InterviewRepository;
import com.paul.jobtrackerapi.repositories.JobApplicationRepository;
import com.paul.jobtrackerapi.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationStatusHistoryServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private JobApplicationMapper mapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationStatusHistoryRepository statusHistoryRepository;

    @Mock
    private InterviewRepository interviewRepository;

    private JobApplicationService service;

    private User user;

    @BeforeEach
    void setUp() {

        service = new JobApplicationService(
                repository,
                mapper,
                userRepository,
                statusHistoryRepository,
                interviewRepository
        );

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

}

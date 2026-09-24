package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.analytics.ApplicationSummaryResponse;
import com.paul.jobtrackerapi.dtos.analytics.InterviewOutcomeAnalyticsResponse;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.entities.InterviewOutcome;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.mappers.JobApplicationMapper;
import com.paul.jobtrackerapi.projections.InterviewOutcomeCountProjection;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationAnalyticsServiceTest {

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
    void getApplicationSummary_shouldReturnSummary() {

        User user = new User();
        user.setId(1L);
        user.setUsername("paul");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "paul",
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.of(user));

        when(repository.countByUserId(1L))
                .thenReturn(10L);

        when(
                interviewRepository.countByJobApplicationUserId(1L)
        ).thenReturn(4L);

        when(
                repository.countByUserIdAndStatus(
                        1L,
                        ApplicationStatus.OFFER
                )
        ).thenReturn(1L);

        when(
                repository.countByUserIdAndStatus(
                        1L,
                        ApplicationStatus.REJECTED
                )
        ).thenReturn(3L);

        ApplicationSummaryResponse result =
                service.getApplicationSummary();

        assertEquals(10L, result.totalApplications());
        assertEquals(4L, result.interviews());
        assertEquals(1L, result.offers());
        assertEquals(3L, result.rejections());
    }

    @Test
    void getInterviewOutcomeAnalyticsShouldReturnEmptyListWhenNoInterviewsExist() {

        when(
                interviewRepository.countByOutcomeForUser(user.getId())
        ).thenReturn(List.of());

        List<InterviewOutcomeAnalyticsResponse> result =
                service.getInterviewOutcomeAnalytics();

        assertTrue(result.isEmpty());
    }

    @Test
    void getInterviewOutcomeAnalyticsShouldReturnAnalytics() {
        InterviewOutcomeCountProjection pendingProjection =
                Mockito.mock(InterviewOutcomeCountProjection.class);

        InterviewOutcomeCountProjection passedProjection =
                Mockito.mock(InterviewOutcomeCountProjection.class);

        when(pendingProjection.getOutcome())
                .thenReturn(InterviewOutcome.PENDING);

        when(pendingProjection.getCount())
                .thenReturn(3L);

        when(passedProjection.getOutcome())
                .thenReturn(InterviewOutcome.PASSED);

        when(passedProjection.getCount())
                .thenReturn(5L);

        when(
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

}

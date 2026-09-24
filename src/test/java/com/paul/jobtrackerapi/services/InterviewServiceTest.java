package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.interviews.CreateInterviewRequest;
import com.paul.jobtrackerapi.dtos.interviews.InterviewResponse;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewOutcomeRequest;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewRequest;
import com.paul.jobtrackerapi.entities.*;
import com.paul.jobtrackerapi.exceptions.InterviewNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

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
    void getUpcomingInterviewsShouldReturnUpcomingInterviews() {
        LocalDateTime firstTime =
                LocalDateTime.of(2026, 9, 5, 10, 0);

        LocalDateTime secondTime =
                LocalDateTime.of(2026, 9, 10, 14, 0);

        JobApplication application = new JobApplication();
        application.setId(1L);
        application.setUser(user);

        Interview firstInterview = Interview.builder()
                .id(10L)
                .jobApplication(application)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(firstTime)
                .notes("Technical interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview secondInterview = Interview.builder()
                .id(11L)
                .jobApplication(application)
                .type(InterviewType.FINAL)
                .scheduledAt(secondTime)
                .notes("Final interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Mockito.when(
                interviewRepository
                        .findByJobApplicationUserIdAndScheduledAtAfterOrderByScheduledAtAsc(
                                Mockito.eq(user.getId()),
                                Mockito.any(LocalDateTime.class)
                        )
        ).thenReturn(
                List.of(firstInterview, secondInterview)
        );

        List<InterviewResponse> result =
                service.getUpcomingInterviews();

        assertEquals(2, result.size());

        assertEquals(10L, result.get(0).id());
        assertEquals(InterviewType.TECHNICAL, result.get(0).type());
        assertEquals(firstTime, result.get(0).scheduledAt());
        assertEquals("Technical interview", result.get(0).notes());
        assertEquals(InterviewOutcome.PENDING, result.get(0).outcome());

        assertEquals(11L, result.get(1).id());
        assertEquals(InterviewType.FINAL, result.get(1).type());
        assertEquals(secondTime, result.get(1).scheduledAt());
        assertEquals("Final interview", result.get(1).notes());
        assertEquals(InterviewOutcome.PENDING, result.get(1).outcome());
    }

    @Test
    void getUpcomingInterviewsShouldReturnEmptyListWhenNoneExist() {

        Mockito.when(
                interviewRepository
                        .findByJobApplicationUserIdAndScheduledAtAfterOrderByScheduledAtAsc(
                                Mockito.eq(user.getId()),
                                Mockito.any(LocalDateTime.class)
                        )
        ).thenReturn(List.of());

        List<InterviewResponse> result =
                service.getUpcomingInterviews();

        assertTrue(result.isEmpty());
    }
}

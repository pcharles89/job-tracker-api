package com.paul.jobtrackerapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paul.jobtrackerapi.dtos.interviews.CreateInterviewRequest;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewOutcomeRequest;
import com.paul.jobtrackerapi.dtos.interviews.UpdateInterviewRequest;
import com.paul.jobtrackerapi.entities.*;
import com.paul.jobtrackerapi.repositories.ApplicationStatusHistoryRepository;
import com.paul.jobtrackerapi.repositories.InterviewRepository;
import com.paul.jobtrackerapi.repositories.JobApplicationRepository;
import com.paul.jobtrackerapi.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@WithMockUser(username = "paul")
class InterviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobApplicationRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationStatusHistoryRepository statusHistoryRepository;

    private User testUser;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        interviewRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        repository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("paul");
        testUser.setPassword("password");

        testUser = userRepository.save(testUser);
    }


    @Test
    void createInterview_shouldReturnNotFound() throws Exception {
        LocalDateTime scheduledAt =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        CreateInterviewRequest request =
                new CreateInterviewRequest(
                        InterviewType.TECHNICAL,
                        scheduledAt,
                        "Java and SQL interview"
                );

        mockMvc.perform(
                        post("/applications/{id}/interviews", 999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        assertEquals(0, interviewRepository.count());
    }

    @Test
    void createInterview_shouldCreateInterview() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        LocalDateTime scheduledAt =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        CreateInterviewRequest request =
                new CreateInterviewRequest(
                        InterviewType.TECHNICAL,
                        scheduledAt,
                        "Java and SQL interview"
                );

        mockMvc.perform(
                        post("/applications/{id}/interviews",
                                savedApplication.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type")
                        .value("TECHNICAL"))
                .andExpect(jsonPath("$.notes")
                        .value("Java and SQL interview"))
                .andExpect(jsonPath("$.outcome")
                        .value("PENDING"));
    }

    @Test
    void createInterview_shouldReturnNotFound_whenApplicationBelongsToAnotherUser() throws Exception {

        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication application = new JobApplication();
        application.setCompanyName("Alice Company");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(otherUser);

        JobApplication savedApplication =
                repository.save(application);

        CreateInterviewRequest request =
                new CreateInterviewRequest(
                        InterviewType.TECHNICAL,
                        LocalDateTime.of(2026, 8, 20, 14, 0),
                        "Java and SQL interview"
                );

        mockMvc.perform(
                        post("/applications/{id}/interviews",
                                savedApplication.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        assertEquals(0, interviewRepository.count());
    }

    @Test
    void getInterviews_shouldReturnInterviews() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview firstInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.PHONE)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 10, 0))
                .notes("Recruiter screen")
                .outcome(InterviewOutcome.PASSED)
                .build();

        Interview secondInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 25, 14, 30))
                .notes("Java and SQL interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        interviewRepository.save(firstInterview);
        interviewRepository.save(secondInterview);

        mockMvc.perform(
                        get("/applications/{id}/interviews",
                                savedApplication.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id")
                        .value(firstInterview.getId()))
                .andExpect(jsonPath("$[0].type")
                        .value("PHONE"))
                .andExpect(jsonPath("$[0].notes")
                        .value("Recruiter screen"))
                .andExpect(jsonPath("$[0].outcome")
                        .value("PASSED"))

                .andExpect(jsonPath("$[1].id")
                        .value(secondInterview.getId()))
                .andExpect(jsonPath("$[1].type")
                        .value("TECHNICAL"))
                .andExpect(jsonPath("$[1].notes")
                        .value("Java and SQL interview"))
                .andExpect(jsonPath("$[1].outcome")
                        .value("PENDING"));
    }

    @Test
    void getInterviews_shouldReturnNotFound_whenApplicationBelongsToAnotherUser() throws Exception {

        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication application = new JobApplication();
        application.setCompanyName("Alice Company");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(otherUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview interview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 25, 14, 30))
                .notes("Alice interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        interviewRepository.save(interview);

        mockMvc.perform(
                        get("/applications/{id}/interviews",
                                savedApplication.getId())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getInterviews_shouldReturnNotFound() throws Exception {

        mockMvc.perform(
                        get("/applications/{id}/interviews", 999L)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateInterviewOutcome_shouldUpdateInterview() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview interview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Java and SQL interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        mockMvc.perform(
                        patch(
                                "/applications/{applicationId}/interviews/{interviewId}/outcome",
                                savedApplication.getId(),
                                savedInterview.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedInterview.getId()))
                .andExpect(jsonPath("$.type").value("TECHNICAL"))
                .andExpect(jsonPath("$.notes").value("Java and SQL interview"))
                .andExpect(jsonPath("$.outcome").value("PASSED"));

        Interview updatedInterview =
                interviewRepository.findById(savedInterview.getId())
                        .orElseThrow();

        assertEquals(
                InterviewOutcome.PASSED,
                updatedInterview.getOutcome()
        );
    }

    @Test
    void updateInterviewOutcome_shouldReturnNotFound_whenInterviewNotFound() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        mockMvc.perform(
                        patch(
                                "/applications/{applicationId}/interviews/{interviewId}/outcome",
                                savedApplication.getId(),
                                999L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        assertEquals(0, interviewRepository.count());
    }

    @Test
    void updateInterviewOutcome_shouldReturnNotFound_whenInterviewBelongsToDifferentApplication()
            throws Exception {

        JobApplication firstApplication = new JobApplication();
        firstApplication.setCompanyName("Google");
        firstApplication.setJobTitle("Java Developer");
        firstApplication.setLocation("New York");
        firstApplication.setUser(testUser);

        JobApplication savedFirstApplication =
                repository.save(firstApplication);

        JobApplication secondApplication = new JobApplication();
        secondApplication.setCompanyName("Amazon");
        secondApplication.setJobTitle("Backend Developer");
        secondApplication.setLocation("Seattle");
        secondApplication.setUser(testUser);

        JobApplication savedSecondApplication =
                repository.save(secondApplication);

        Interview interview = Interview.builder()
                .jobApplication(savedSecondApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 25, 14, 0))
                .notes("Java interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        mockMvc.perform(
                        patch(
                                "/applications/{applicationId}/interviews/{interviewId}/outcome",
                                savedFirstApplication.getId(),
                                savedInterview.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        Interview unchangedInterview =
                interviewRepository.findById(savedInterview.getId())
                        .orElseThrow();

        assertEquals(
                InterviewOutcome.PENDING,
                unchangedInterview.getOutcome()
        );
    }

    @Test
    void updateInterviewOutcome_shouldReturnNotFound_whenApplicationNotFound()
            throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview interview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 25, 14, 0))
                .notes("Java interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        UpdateInterviewOutcomeRequest request =
                new UpdateInterviewOutcomeRequest(
                        InterviewOutcome.PASSED
                );

        mockMvc.perform(
                        patch(
                                "/applications/{applicationId}/interviews/{interviewId}/outcome",
                                999L,
                                savedInterview.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        Interview unchangedInterview =
                interviewRepository.findById(savedInterview.getId())
                        .orElseThrow();

        assertEquals(
                InterviewOutcome.PENDING,
                unchangedInterview.getOutcome()
        );
    }

    @Test
    void updateInterview_shouldUpdateInterview() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview interview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Old interview details")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round with engineering manager"
                );

        mockMvc.perform(
                        put(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                savedApplication.getId(),
                                savedInterview.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedInterview.getId()))
                .andExpect(jsonPath("$.type")
                        .value("FINAL"))
                .andExpect(jsonPath("$.scheduledAt")
                        .value("2026-08-30T15:00:00"))
                .andExpect(jsonPath("$.notes")
                        .value("Final round with engineering manager"))
                .andExpect(jsonPath("$.outcome")
                        .value("PENDING"));

        Interview updatedInterview =
                interviewRepository.findById(savedInterview.getId())
                        .orElseThrow();

        assertEquals(
                InterviewType.FINAL,
                updatedInterview.getType()
        );

        assertEquals(
                LocalDateTime.of(2026, 8, 30, 15, 0),
                updatedInterview.getScheduledAt()
        );

        assertEquals(
                "Final round with engineering manager",
                updatedInterview.getNotes()
        );

        assertEquals(
                InterviewOutcome.PENDING,
                updatedInterview.getOutcome()
        );
    }

    @Test
    void updateInterview_shouldReturnNotFound_whenInterviewNotFound()
            throws Exception {

        Long interviewId = 999L;

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        mockMvc.perform(
                        put(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                savedApplication.getId(),
                                interviewId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        assertEquals(0, interviewRepository.count());
    }

    @Test
    void updateInterview_shouldReturnNotFound_whenInterviewBelongsToDifferentApplication()
            throws Exception {

        JobApplication firstApplication = new JobApplication();
        firstApplication.setCompanyName("Google");
        firstApplication.setJobTitle("Java Developer");
        firstApplication.setLocation("New York");
        firstApplication.setUser(testUser);

        JobApplication savedFirstApplication =
                repository.save(firstApplication);

        JobApplication secondApplication = new JobApplication();
        secondApplication.setCompanyName("Amazon");
        secondApplication.setJobTitle("Backend Developer");
        secondApplication.setLocation("Seattle");
        secondApplication.setUser(testUser);

        JobApplication savedSecondApplication =
                repository.save(secondApplication);

        Interview interview = Interview.builder()
                .jobApplication(savedSecondApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Old interview details")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        mockMvc.perform(
                        put(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                savedFirstApplication.getId(),
                                savedInterview.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        Interview unchangedInterview =
                interviewRepository.findById(savedInterview.getId())
                        .orElseThrow();

        assertEquals(
                InterviewType.TECHNICAL,
                unchangedInterview.getType()
        );

        assertEquals(
                "Old interview details",
                unchangedInterview.getNotes()
        );

        assertEquals(
                InterviewOutcome.PENDING,
                unchangedInterview.getOutcome()
        );
    }

    @Test
    void updateInterview_shouldReturnNotFound_whenApplicationNotFound()
            throws Exception {

        Long applicationId = 999L;

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview interview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Old interview details")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        UpdateInterviewRequest request =
                new UpdateInterviewRequest(
                        InterviewType.FINAL,
                        LocalDateTime.of(2026, 8, 30, 15, 0),
                        "Final round"
                );

        mockMvc.perform(
                        put(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                savedInterview.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        Interview unchangedInterview =
                interviewRepository.findById(savedInterview.getId())
                        .orElseThrow();

        assertEquals(
                InterviewType.TECHNICAL,
                unchangedInterview.getType()
        );

        assertEquals(
                "Old interview details",
                unchangedInterview.getNotes()
        );

        assertEquals(
                InterviewOutcome.PENDING,
                unchangedInterview.getOutcome()
        );
    }

    @Test
    void deleteInterview_shouldDeleteInterview() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview interview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Java interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        mockMvc.perform(
                        delete(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                savedApplication.getId(),
                                savedInterview.getId()
                        )
                )
                .andExpect(status().isNoContent());

        assertFalse(
                interviewRepository.existsById(savedInterview.getId())
        );
    }

    @Test
    void deleteInterview_shouldReturnNotFound_whenInterviewNotFound()
            throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Long interviewId = 999L;

        mockMvc.perform(
                        delete(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                savedApplication.getId(),
                                interviewId
                        )
                )
                .andExpect(status().isNotFound());

        assertEquals(0, interviewRepository.count());
    }

    @Test
    void deleteInterview_shouldReturnNotFound_whenInterviewBelongsToDifferentApplication()
            throws Exception {

        JobApplication firstApplication = new JobApplication();
        firstApplication.setCompanyName("Google");
        firstApplication.setJobTitle("Java Developer");
        firstApplication.setLocation("New York");
        firstApplication.setUser(testUser);

        JobApplication savedFirstApplication =
                repository.save(firstApplication);

        JobApplication secondApplication = new JobApplication();
        secondApplication.setCompanyName("Amazon");
        secondApplication.setJobTitle("Backend Developer");
        secondApplication.setLocation("Seattle");
        secondApplication.setUser(testUser);

        JobApplication savedSecondApplication =
                repository.save(secondApplication);

        Interview interview = Interview.builder()
                .jobApplication(savedSecondApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Java interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        mockMvc.perform(
                        delete(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                savedFirstApplication.getId(),
                                savedInterview.getId()
                        )
                )
                .andExpect(status().isNotFound());

        assertTrue(
                interviewRepository.existsById(savedInterview.getId())
        );
    }

    @Test
    void deleteInterview_shouldReturnNotFound_whenApplicationNotFound()
            throws Exception {

        Long applicationId = 999L;

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview interview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 8, 20, 14, 0))
                .notes("Java interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview savedInterview =
                interviewRepository.save(interview);

        mockMvc.perform(
                        delete(
                                "/applications/{applicationId}/interviews/{interviewId}",
                                applicationId,
                                savedInterview.getId()
                        )
                )
                .andExpect(status().isNotFound());

        assertTrue(
                interviewRepository.existsById(savedInterview.getId())
        );
    }

    @Test
    void getUpcomingInterviews_shouldReturnOnlyFutureInterviews()
            throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        LocalDateTime now = LocalDateTime.now();

        Interview pastInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(now.minusDays(1))
                .notes("Past interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview firstUpcomingInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(now.plusDays(1))
                .notes("First upcoming interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview secondUpcomingInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.FINAL)
                .scheduledAt(now.plusDays(3))
                .notes("Second upcoming interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        interviewRepository.saveAll(
                List.of(
                        pastInterview,
                        firstUpcomingInterview,
                        secondUpcomingInterview
                )
        );

        mockMvc.perform(
                        get("/applications/interviews/upcoming")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notes")
                        .value("First upcoming interview"))
                .andExpect(jsonPath("$[1].notes")
                        .value("Second upcoming interview"));
    }

    @Test
    void getUpcomingInterviews_shouldOnlyReturnCurrentUsersInterviews()
            throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview userInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .notes("Paul upcoming interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        interviewRepository.save(userInterview);

        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");

        otherUser = userRepository.save(otherUser);

        JobApplication otherApplication = new JobApplication();
        otherApplication.setCompanyName("Amazon");
        otherApplication.setJobTitle("Backend Developer");
        otherApplication.setLocation("Seattle");
        otherApplication.setUser(otherUser);

        JobApplication savedOtherApplication =
                repository.save(otherApplication);

        Interview otherInterview = Interview.builder()
                .jobApplication(savedOtherApplication)
                .type(InterviewType.FINAL)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .notes("Alice upcoming interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        interviewRepository.save(otherInterview);

        mockMvc.perform(
                        get("/applications/interviews/upcoming")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notes")
                        .value("Paul upcoming interview"));
    }

    @Test
    void getUpcomingInterviews_shouldReturnEmptyListWhenNoneExist()
            throws Exception {

        mockMvc.perform(
                        get("/applications/interviews/upcoming")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


}








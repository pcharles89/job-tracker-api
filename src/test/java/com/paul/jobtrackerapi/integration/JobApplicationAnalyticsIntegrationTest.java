package com.paul.jobtrackerapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@WithMockUser(username = "paul")
class JobApplicationAnalyticsIntegrationTest {

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
    void getApplicationSummary_shouldReturnCorrectCounts()
            throws Exception {

        JobApplication offerApplication = new JobApplication();
        offerApplication.setCompanyName("Google");
        offerApplication.setJobTitle("Java Developer");
        offerApplication.setLocation("New York");
        offerApplication.setStatus(ApplicationStatus.OFFER);
        offerApplication.setUser(testUser);

        JobApplication savedOfferApplication =
                repository.save(offerApplication);

        JobApplication rejectedApplication = new JobApplication();
        rejectedApplication.setCompanyName("Amazon");
        rejectedApplication.setJobTitle("Backend Developer");
        rejectedApplication.setLocation("Seattle");
        rejectedApplication.setStatus(ApplicationStatus.REJECTED);
        rejectedApplication.setUser(testUser);

        repository.save(rejectedApplication);

        JobApplication appliedApplication = new JobApplication();
        appliedApplication.setCompanyName("Microsoft");
        appliedApplication.setJobTitle("Software Engineer");
        appliedApplication.setLocation("Remote");
        appliedApplication.setStatus(ApplicationStatus.APPLIED);
        appliedApplication.setUser(testUser);

        repository.save(appliedApplication);

        Interview firstInterview = Interview.builder()
                .jobApplication(savedOfferApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .notes("Technical interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview secondInterview = Interview.builder()
                .jobApplication(savedOfferApplication)
                .type(InterviewType.FINAL)
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .notes("Final interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        interviewRepository.save(firstInterview);
        interviewRepository.save(secondInterview);

        mockMvc.perform(
                        get("/applications/analytics/summary")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(3))
                .andExpect(jsonPath("$.interviews").value(2))
                .andExpect(jsonPath("$.offers").value(1))
                .andExpect(jsonPath("$.rejections").value(1));
    }

    @Test
    void getInterviewOutcomeAnalytics_shouldReturnEmptyListWhenNoInterviewsExist()
            throws Exception {

        mockMvc.perform(
                        get("/applications/interviews/analytics/outcomes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getInterviewOutcomeAnalytics_shouldOnlyCountCurrentUsersInterviews()
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
                .scheduledAt(LocalDateTime.of(2026, 9, 5, 10, 0))
                .notes("My interview")
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
                .scheduledAt(LocalDateTime.of(2026, 9, 6, 14, 0))
                .notes("Alice interview")
                .outcome(InterviewOutcome.PASSED)
                .build();

        interviewRepository.save(otherInterview);

        mockMvc.perform(
                        get("/applications/interviews/analytics/outcomes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].outcome").value("PENDING"))
                .andExpect(jsonPath("$[0].count").value(1));
    }

    @Test
    void getInterviewOutcomeAnalytics_shouldReturnCorrectCounts() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        Interview firstInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 9, 5, 10, 0))
                .notes("Technical interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview secondInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.FINAL)
                .scheduledAt(LocalDateTime.of(2026, 9, 10, 14, 0))
                .notes("Final interview")
                .outcome(InterviewOutcome.PENDING)
                .build();

        Interview thirdInterview = Interview.builder()
                .jobApplication(savedApplication)
                .type(InterviewType.TECHNICAL)
                .scheduledAt(LocalDateTime.of(2026, 9, 1, 9, 0))
                .notes("Passed technical")
                .outcome(InterviewOutcome.PASSED)
                .build();

        interviewRepository.saveAll(
                List.of(
                        firstInterview,
                        secondInterview,
                        thirdInterview
                )
        );

        mockMvc.perform(
                        get("/applications/interviews/analytics/outcomes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].outcome").value("PENDING"))
                .andExpect(jsonPath("$[0].count").value(2))

                .andExpect(jsonPath("$[1].outcome").value("PASSED"))
                .andExpect(jsonPath("$[1].count").value(1));
    }

    @Test
    void getAnalytics_shouldReturnCountsForCurrentUserOnly() throws Exception {

        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication applied1 = new JobApplication();
        applied1.setCompanyName("Amazon");
        applied1.setJobTitle("Backend Developer");
        applied1.setStatus(ApplicationStatus.APPLIED);
        applied1.setUser(testUser);

        JobApplication applied2 = new JobApplication();
        applied2.setCompanyName("Google");
        applied2.setJobTitle("Java Developer");
        applied2.setStatus(ApplicationStatus.APPLIED);
        applied2.setUser(testUser);

        JobApplication phoneScreen = new JobApplication();
        phoneScreen.setCompanyName("Microsoft");
        phoneScreen.setJobTitle("Software Engineer");
        phoneScreen.setStatus(ApplicationStatus.PHONE_SCREEN);
        phoneScreen.setUser(testUser);

        JobApplication rejected = new JobApplication();
        rejected.setCompanyName("Netflix");
        rejected.setJobTitle("Backend Engineer");
        rejected.setStatus(ApplicationStatus.REJECTED);
        rejected.setUser(testUser);

        JobApplication aliceOffer = new JobApplication();
        aliceOffer.setCompanyName("Apple");
        aliceOffer.setJobTitle("Frontend Developer");
        aliceOffer.setStatus(ApplicationStatus.OFFER);
        aliceOffer.setUser(otherUser);

        repository.save(applied1);
        repository.save(applied2);
        repository.save(phoneScreen);
        repository.save(rejected);
        repository.save(aliceOffer);

        mockMvc.perform(get("/applications/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(4))
                .andExpect(jsonPath("$.applied").value(2))
                .andExpect(jsonPath("$.phoneScreen").value(1))
                .andExpect(jsonPath("$.technicalInterview").value(0))
                .andExpect(jsonPath("$.finalInterview").value(0))
                .andExpect(jsonPath("$.offer").value(0))
                .andExpect(jsonPath("$.rejected").value(1))
                .andExpect(jsonPath("$.withdrawn").value(0));
    }

    @Test
    void getCompanyAnalytics_shouldReturnCountsForCurrentUserOnly() throws Exception {

        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication amazon1 = new JobApplication();
        amazon1.setCompanyName("Amazon");
        amazon1.setJobTitle("Backend Developer");
        amazon1.setStatus(ApplicationStatus.APPLIED);
        amazon1.setUser(testUser);

        JobApplication amazon2 = new JobApplication();
        amazon2.setCompanyName("Amazon");
        amazon2.setJobTitle("Java Developer");
        amazon2.setStatus(ApplicationStatus.PHONE_SCREEN);
        amazon2.setUser(testUser);

        JobApplication google = new JobApplication();
        google.setCompanyName("Google");
        google.setJobTitle("Software Engineer");
        google.setStatus(ApplicationStatus.APPLIED);
        google.setUser(testUser);

        JobApplication aliceAmazon = new JobApplication();
        aliceAmazon.setCompanyName("Amazon");
        aliceAmazon.setJobTitle("Frontend Developer");
        aliceAmazon.setStatus(ApplicationStatus.OFFER);
        aliceAmazon.setUser(otherUser);

        repository.save(amazon1);
        repository.save(amazon2);
        repository.save(google);
        repository.save(aliceAmazon);

        mockMvc.perform(get("/applications/analytics/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Amazon"))
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[1].companyName").value("Google"))
                .andExpect(jsonPath("$[1].count").value(1));
    }

    @Test
    void getLocationAnalytics_shouldReturnCountsForCurrentUserOnly() throws Exception {

        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication ny1 = new JobApplication();
        ny1.setCompanyName("Amazon");
        ny1.setJobTitle("Backend Developer");
        ny1.setLocation("New York, NY");
        ny1.setStatus(ApplicationStatus.APPLIED);
        ny1.setUser(testUser);

        JobApplication ny2 = new JobApplication();
        ny2.setCompanyName("Google");
        ny2.setJobTitle("Java Developer");
        ny2.setLocation("New York, NY");
        ny2.setStatus(ApplicationStatus.PHONE_SCREEN);
        ny2.setUser(testUser);

        JobApplication remote = new JobApplication();
        remote.setCompanyName("Microsoft");
        remote.setJobTitle("Software Engineer");
        remote.setLocation("Remote");
        remote.setStatus(ApplicationStatus.APPLIED);
        remote.setUser(testUser);

        JobApplication aliceNewYork = new JobApplication();
        aliceNewYork.setCompanyName("Apple");
        aliceNewYork.setJobTitle("Frontend Developer");
        aliceNewYork.setLocation("New York, NY");
        aliceNewYork.setStatus(ApplicationStatus.OFFER);
        aliceNewYork.setUser(otherUser);

        repository.save(ny1);
        repository.save(ny2);
        repository.save(remote);
        repository.save(aliceNewYork);

        mockMvc.perform(get("/applications/analytics/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location").value("New York, NY"))
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[1].location").value("Remote"))
                .andExpect(jsonPath("$[1].count").value(1));
    }
}

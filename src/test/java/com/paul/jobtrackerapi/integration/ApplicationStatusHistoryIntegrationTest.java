package com.paul.jobtrackerapi.integration;

import com.paul.jobtrackerapi.entities.ApplicationStatus;
import com.paul.jobtrackerapi.entities.ApplicationStatusHistory;
import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.repositories.ApplicationStatusHistoryRepository;
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
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@WithMockUser(username = "paul")
class ApplicationStatusHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobApplicationRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationStatusHistoryRepository statusHistoryRepository;

    private User testUser;

    @BeforeEach
    void setUp() {

        statusHistoryRepository.deleteAll();
        repository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("paul");
        testUser.setPassword("password");

        testUser = userRepository.save(testUser);
    }

    @Test
    void getStatusHistory_shouldReturnHistory() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Google");
        application.setJobTitle("Java Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        LocalDateTime firstTime =
                LocalDateTime.of(2026, 8, 1, 9, 0);

        LocalDateTime secondTime =
                LocalDateTime.of(2026, 8, 5, 14, 30);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        ApplicationStatusHistory firstHistory =
                ApplicationStatusHistory.builder()
                        .jobApplication(savedApplication)
                        .status(ApplicationStatus.APPLIED)
                        .changedAt(firstTime)
                        .build();

        ApplicationStatusHistory secondHistory =
                ApplicationStatusHistory.builder()
                        .jobApplication(savedApplication)
                        .status(ApplicationStatus.PHONE_SCREEN)
                        .changedAt(secondTime)
                        .build();

        statusHistoryRepository.save(firstHistory);
        statusHistoryRepository.save(secondHistory);

        mockMvc.perform(
                        get("/applications/{id}/history",
                                savedApplication.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id")
                        .value(firstHistory.getId()))
                .andExpect(jsonPath("$[0].status")
                        .value("APPLIED"))
                .andExpect(jsonPath("$[0].changedAt")
                        .value(firstTime.format(formatter)))

                .andExpect(jsonPath("$[1].id")
                        .value(secondHistory.getId()))
                .andExpect(jsonPath("$[1].status")
                        .value("PHONE_SCREEN"))
                .andExpect(jsonPath("$[1].changedAt")
                        .value(secondTime.format(formatter)));
    }
}

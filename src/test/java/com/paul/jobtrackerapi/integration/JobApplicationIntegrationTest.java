package com.paul.jobtrackerapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paul.jobtrackerapi.dtos.*;
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
import java.time.format.DateTimeFormatter;
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
public class JobApplicationIntegrationTest {

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
    void createApplication_shouldSaveAndReturnApplication() throws Exception {

        CreateJobApplicationRequest request =
                new CreateJobApplicationRequest();

        request.setCompanyName("Amazon");
        request.setJobTitle("Backend Developer");
        request.setLocation("New York");

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.companyName").value("Amazon"))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer"))
                .andExpect(jsonPath("$.location").value("New York"));

    }

    @Test
    void createApplication_shouldReturnBadRequest_whenCompanyNameIsBlank() throws Exception {

        CreateJobApplicationRequest request =
                new CreateJobApplicationRequest();

        request.setCompanyName("");
        request.setJobTitle("Backend Developer");
        request.setLocation("New York");

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.companyName").value("Company name is required"));
    }

    @Test
    void getApplicationById_shouldReturnApplication() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("Amazon");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication = repository.save(application);

        mockMvc.perform(get("/applications/{id}", savedApplication.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedApplication.getId()))
                .andExpect(jsonPath("$.companyName").value("Amazon"))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer"))
                .andExpect(jsonPath("$.location").value("New York"));
    }

    @Test
    void getApplicationById_shouldReturnNotFound() throws Exception {

        mockMvc.perform(get("/applications/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id 999 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void updateApplication_shouldReturnUpdatedApplication() throws Exception {

        JobApplication application = new JobApplication();

        application.setCompanyName("Amazon");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        UpdateJobApplicationRequest request =
                new UpdateJobApplicationRequest();

        request.setCompanyName("Google");
        request.setJobTitle("Senior Backend Developer");
        request.setLocation("California");
        request.setStatus(ApplicationStatus.APPLIED);

        mockMvc.perform(
                        put("/applications/{id}",
                                savedApplication.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedApplication.getId()))
                .andExpect(jsonPath("$.companyName")
                        .value("Google"))
                .andExpect(jsonPath("$.jobTitle")
                        .value("Senior Backend Developer"))
                .andExpect(jsonPath("$.location")
                        .value("California"));
    }

    @Test
    void updateApplication_shouldReturnNotFound() throws Exception {

        UpdateJobApplicationRequest request =
                new UpdateJobApplicationRequest();

        request.setCompanyName("Google");
        request.setJobTitle("Senior Backend Developer");
        request.setLocation("California");
        request.setStatus(ApplicationStatus.APPLIED);

        mockMvc.perform(
                        put("/applications/{id}", 999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id 999 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void patchApplication_shouldReturnPatchedApplication() throws Exception {

        JobApplication application = new JobApplication();

        application.setCompanyName("Amazon");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        PatchJobApplicationRequest request =
                new PatchJobApplicationRequest(
                        "Google",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        mockMvc.perform(
                        patch("/applications/{id}", savedApplication.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedApplication.getId()))
                .andExpect(jsonPath("$.companyName")
                        .value("Google"))
                .andExpect(jsonPath("$.jobTitle")
                        .value("Backend Developer"))
                .andExpect(jsonPath("$.location")
                        .value("New York"));
    }

    @Test
    void patchApplication_shouldReturnNotFound() throws Exception {

        PatchJobApplicationRequest request =
                new PatchJobApplicationRequest(
                        "Google",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        mockMvc.perform(
                        patch("/applications/{id}", 999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id 999 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void deleteApplication_shouldReturnNoContent() throws Exception {

        JobApplication application = new JobApplication();

        application.setCompanyName("Amazon");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        JobApplication savedApplication =
                repository.save(application);

        mockMvc.perform(delete("/applications/{id}", savedApplication.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteApplication_shouldReturnNotFound() throws Exception {

        mockMvc.perform(delete("/applications/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id 999 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void searchApplications_shouldReturnFilteredResults() throws Exception {

        JobApplication unique = new JobApplication();
        unique.setCompanyName("UniqueTestCompanyXYZ");
        unique.setJobTitle("Backend Developer");
        unique.setLocation("New York");
        unique.setUser(testUser);

        JobApplication other = new JobApplication();
        other.setCompanyName("OtherTestCompanyXYZ");
        other.setJobTitle("Software Engineer");
        other.setLocation("California");
        other.setUser(testUser);

        repository.save(unique);
        repository.save(other);

        mockMvc.perform(
                        get("/applications/search")
                                .param("companyName", "UniqueTestCompanyXYZ")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].companyName")
                        .value("UniqueTestCompanyXYZ"));
    }

    @Test
    void searchApplications_shouldReturnEmptyPageWhenNoMatchesExist() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("UniqueExistingCompanyXYZ");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        repository.save(application);

        mockMvc.perform(
                        get("/applications/search")
                                .param("companyName", "NoMatchingCompanyXYZ")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllApplications_shouldReturnPage() throws Exception {

        JobApplication application = new JobApplication();
        application.setCompanyName("UniqueListCompanyXYZ");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(testUser);

        repository.save(application);

        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].companyName").exists());
    }

    @Test
    void getApplicationById_shouldReturnNotFound_whenApplicationBelongsToAnotherUser() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication application = new JobApplication();
        application.setCompanyName("Alice Company");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(otherUser);

        JobApplication savedApplication = repository.save(application);

        mockMvc.perform(get("/applications/{id}", savedApplication.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id " + savedApplication.getId() + " not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void updateApplication_shouldReturnNotFound_whenApplicationBelongsToAnotherUser() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication application = new JobApplication();
        application.setCompanyName("Alice Company");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(otherUser);

        JobApplication savedApplication = repository.save(application);

        UpdateJobApplicationRequest request = new UpdateJobApplicationRequest();
        request.setCompanyName("Hacked Company");
        request.setJobTitle("Hacked Title");
        request.setLocation("California");
        request.setStatus(ApplicationStatus.APPLIED);

        mockMvc.perform(
                        put("/applications/{id}", savedApplication.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id " + savedApplication.getId() + " not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void deleteApplication_shouldReturnNotFound_whenApplicationBelongsToAnotherUser() throws Exception {

        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication application = new JobApplication();
        application.setCompanyName("Alice Company");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(otherUser);

        JobApplication savedApplication = repository.save(application);

        mockMvc.perform(delete("/applications/{id}", savedApplication.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id " + savedApplication.getId() + " not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void patchApplication_shouldReturnNotFound_whenApplicationBelongsToAnotherUser() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication application = new JobApplication();
        application.setCompanyName("Alice Company");
        application.setJobTitle("Backend Developer");
        application.setLocation("New York");
        application.setUser(otherUser);

        JobApplication savedApplication = repository.save(application);

        PatchJobApplicationRequest request =
                new PatchJobApplicationRequest(
                        "Hacked Company",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        mockMvc.perform(
                        patch("/applications/{id}", savedApplication.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job application with id " + savedApplication.getId() + " not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void getAllApplications_shouldOnlyReturnCurrentUsersApplications() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication paulApplication = new JobApplication();
        paulApplication.setCompanyName("Paul Company");
        paulApplication.setJobTitle("Backend Developer");
        paulApplication.setLocation("New York");
        paulApplication.setUser(testUser);

        JobApplication aliceApplication = new JobApplication();
        aliceApplication.setCompanyName("Alice Company");
        aliceApplication.setJobTitle("Frontend Developer");
        aliceApplication.setLocation("California");
        aliceApplication.setUser(otherUser);

        repository.save(paulApplication);
        repository.save(aliceApplication);

        mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].companyName").value("Paul Company"));
    }

    @Test
    void searchApplications_shouldOnlyReturnCurrentUsersApplications() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("alice");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        JobApplication paulApplication = new JobApplication();
        paulApplication.setCompanyName("SharedCompanyName");
        paulApplication.setJobTitle("Backend Developer");
        paulApplication.setLocation("New York");
        paulApplication.setUser(testUser);

        JobApplication aliceApplication = new JobApplication();
        aliceApplication.setCompanyName("SharedCompanyName");
        aliceApplication.setJobTitle("Frontend Developer");
        aliceApplication.setLocation("California");
        aliceApplication.setUser(otherUser);

        repository.save(paulApplication);
        repository.save(aliceApplication);

        mockMvc.perform(
                        get("/applications/search")
                                .param("companyName", "SharedCompanyName")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].companyName").value("SharedCompanyName"))
                .andExpect(jsonPath("$.content[0].jobTitle").value("Backend Developer"));
    }

    @Test
    void getAllApplications_shouldReturnApplicationsSortedByCompanyNameAscending() throws Exception {
        JobApplication paulApplication = new JobApplication();
        paulApplication.setCompanyName("Amazon");
        paulApplication.setJobTitle("Backend Developer");
        paulApplication.setLocation("New York");
        paulApplication.setUser(testUser);

        JobApplication paulApplication2 = new JobApplication();
        paulApplication2.setCompanyName("Google");
        paulApplication2.setJobTitle("Backend Developer");
        paulApplication2.setLocation("New York");
        paulApplication2.setUser(testUser);

        JobApplication paulApplication3 = new JobApplication();
        paulApplication3.setCompanyName("Microsoft");
        paulApplication3.setJobTitle("Backend Developer");
        paulApplication3.setLocation("New York");
        paulApplication3.setUser(testUser);

        repository.save(paulApplication2);
        repository.save(paulApplication3);
        repository.save(paulApplication);

        mockMvc.perform(
                        get("/applications")
                                .param("sort", "companyName,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].companyName").value("Amazon"))
                .andExpect(jsonPath("$.content[1].companyName").value("Google"))
                .andExpect(jsonPath("$.content[2].companyName").value("Microsoft"));
    }

    @Test
    void getAllApplications_shouldReturnApplicationsSortedByCompanyNameDescending() throws Exception {
        JobApplication paulApplication = new JobApplication();
        paulApplication.setCompanyName("Amazon");
        paulApplication.setJobTitle("Backend Developer");
        paulApplication.setLocation("New York");
        paulApplication.setUser(testUser);

        JobApplication paulApplication2 = new JobApplication();
        paulApplication2.setCompanyName("Google");
        paulApplication2.setJobTitle("Backend Developer");
        paulApplication2.setLocation("New York");
        paulApplication2.setUser(testUser);

        JobApplication paulApplication3 = new JobApplication();
        paulApplication3.setCompanyName("Microsoft");
        paulApplication3.setJobTitle("Backend Developer");
        paulApplication3.setLocation("New York");
        paulApplication3.setUser(testUser);

        repository.save(paulApplication2);
        repository.save(paulApplication3);
        repository.save(paulApplication);

        mockMvc.perform(
                        get("/applications")
                                .param("sort", "companyName,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].companyName").value("Microsoft"))
                .andExpect(jsonPath("$.content[1].companyName").value("Google"))
                .andExpect(jsonPath("$.content[2].companyName").value("Amazon"));
    }

    @Test
    void getAllApplications_shouldReturnFirstPageOfApplications() throws Exception {
        JobApplication paulApplication = new JobApplication();
        paulApplication.setCompanyName("Amazon");
        paulApplication.setJobTitle("Backend Developer");
        paulApplication.setLocation("New York");
        paulApplication.setUser(testUser);

        JobApplication paulApplication2 = new JobApplication();
        paulApplication2.setCompanyName("Google");
        paulApplication2.setJobTitle("Backend Developer");
        paulApplication2.setLocation("New York");
        paulApplication2.setUser(testUser);

        JobApplication paulApplication3 = new JobApplication();
        paulApplication3.setCompanyName("Microsoft");
        paulApplication3.setJobTitle("Backend Developer");
        paulApplication3.setLocation("New York");
        paulApplication3.setUser(testUser);

        repository.save(paulApplication2);
        repository.save(paulApplication3);
        repository.save(paulApplication);

        mockMvc.perform(
                        get("/applications")
                                .param("page", "0")
                                .param("size", "2")
                                .param("sort", "companyName,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].companyName").value("Amazon"))
                .andExpect(jsonPath("$.content[1].companyName").value("Google"))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getAllApplications_shouldReturnSecondPageOfApplications() throws Exception {
        JobApplication paulApplication = new JobApplication();
        paulApplication.setCompanyName("Amazon");
        paulApplication.setJobTitle("Backend Developer");
        paulApplication.setLocation("New York");
        paulApplication.setUser(testUser);

        JobApplication paulApplication2 = new JobApplication();
        paulApplication2.setCompanyName("Google");
        paulApplication2.setJobTitle("Backend Developer");
        paulApplication2.setLocation("New York");
        paulApplication2.setUser(testUser);

        JobApplication paulApplication3 = new JobApplication();
        paulApplication3.setCompanyName("Microsoft");
        paulApplication3.setJobTitle("Backend Developer");
        paulApplication3.setLocation("New York");
        paulApplication3.setUser(testUser);

        repository.save(paulApplication2);
        repository.save(paulApplication3);
        repository.save(paulApplication);

        mockMvc.perform(
                        get("/applications")
                                .param("page", "1")
                                .param("size", "2")
                                .param("sort", "companyName,asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].companyName").value("Microsoft"))
                .andExpect(jsonPath("$.content.length()").value(1));
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
    void getInterviewOutcomeAnalytics_shouldReturnEmptyListWhenNoInterviewsExist()
            throws Exception {

        mockMvc.perform(
                        get("/applications/interviews/analytics/outcomes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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

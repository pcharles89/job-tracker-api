package com.paul.jobtrackerapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paul.jobtrackerapi.dtos.CreateJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.PatchJobApplicationRequest;
import com.paul.jobtrackerapi.dtos.UpdateJobApplicationRequest;
import com.paul.jobtrackerapi.entities.JobApplication;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.entities.ApplicationStatus;
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

    private User testUser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
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
}

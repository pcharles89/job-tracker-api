package com.paul.jobtrackerapi.security;

import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User paul = new User();

        paul.setUsername("paul");
        paul.setPassword(passwordEncoder.encode("password"));

        userRepository.save(paul);
    }

    @Test
    @WithAnonymousUser
    void protectedEndpoint_shouldReturnUnauthorized_whenNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/applications")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldAllowAuthenticatedUser()
            throws Exception {

        mockMvc.perform(
                        get("/applications")
                                .with(user("paul"))
                )
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_shouldAllowRequestWithValidJwt()
            throws Exception {

        String token = jwtService.generateToken("paul");

        mockMvc.perform(
                        get("/applications")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_shouldRejectInvalidJwt()
            throws Exception {

        mockMvc.perform(
                        get("/applications")
                                .header(
                                        "Authorization",
                                        "Bearer definitely-not-a-real-token"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldRejectValidJwtForNonexistentUser()
            throws Exception {

        String token = jwtService.generateToken("ghost");

        mockMvc.perform(
                        get("/applications")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginEndpoint_shouldBeAccessibleWithoutAuthentication()
            throws Exception {

        String json = """
            {
              "username": "nobody",
              "password": "wrong-password"
            }
            """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerEndpoint_shouldBeAccessibleWithoutAuthentication()
            throws Exception {

        String json = """
            {
              "username": "newuser",
              "password": "password123"
            }
            """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }
}
package com.paul.jobtrackerapi.security;

import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        userRepository = Mockito.mock(UserRepository.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                userRepository
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChain_whenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldContinueFilterChain_whenAuthorizationHeaderIsNotBearer()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldAuthenticateUser_whenTokenIsValid()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer fake-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        User user = new User();
        user.setUsername("paul");

        Mockito.when(jwtService.extractUsername("fake-token"))
                .thenReturn("paul");

        Mockito.when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.of(user));

        Mockito.when(
                jwtService.isTokenValid(
                        "fake-token",
                        "paul"
                )
        ).thenReturn(true);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);
        assertEquals("paul", authentication.getName());

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenUserDoesNotExist()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer fake-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        Mockito.when(jwtService.extractUsername("fake-token"))
                .thenReturn("paul");

        Mockito.when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.empty());

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenTokenIsInvalid()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer fake-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        User user = new User();
        user.setUsername("paul");

        Mockito.when(jwtService.extractUsername("fake-token"))
                .thenReturn("paul");

        Mockito.when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.of(user));

        Mockito.when(
                jwtService.isTokenValid(
                        "fake-token",
                        "paul"
                )
        ).thenReturn(false);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChain_whenJwtParsingThrowsException()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer fake-token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                Mockito.mock(FilterChain.class);

        Mockito.when(jwtService.extractUsername("fake-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }
}

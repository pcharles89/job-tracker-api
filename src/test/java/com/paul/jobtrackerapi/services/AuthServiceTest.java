package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.auth.AuthResponse;
import com.paul.jobtrackerapi.dtos.auth.LoginRequest;
import com.paul.jobtrackerapi.dtos.auth.RegisterRequest;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.exceptions.InvalidCredentialsException;
import com.paul.jobtrackerapi.exceptions.UsernameAlreadyExistsException;
import com.paul.jobtrackerapi.repositories.UserRepository;
import com.paul.jobtrackerapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest(
                "paul",
                "password123"
        );

        when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        when(jwtService.generateToken("paul"))
                .thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.token());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("paul", savedUser.getUsername());
        assertEquals("hashedPassword", savedUser.getPassword());

        verify(passwordEncoder).encode("password123");
        verify(jwtService).generateToken("paul");
    }

    @Test
    void register_shouldThrowWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "paul",
                "password123"
        );

        User existingUser = User.builder()
                .id(1L)
                .username("paul")
                .password("hashedPassword")
                .build();

        when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest(
                "paul",
                "password123"
        );

        User user = User.builder()
                .id(1L)
                .username("paul")
                .password("hashedPassword")
                .build();

        when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "hashedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken("paul"))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());

        verify(userRepository).findByUsername("paul");
        verify(passwordEncoder).matches(
                "password123",
                "hashedPassword"
        );
        verify(jwtService).generateToken("paul");
    }

    @Test
    void login_shouldThrowWhenUsernameDoesNotExist() {
        LoginRequest request = new LoginRequest(
                "paul",
                "password123"
        );

        when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder, never()).matches(
                anyString(),
                anyString()
        );

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_shouldThrowWhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest(
                "paul",
                "wrongPassword"
        );

        User user = User.builder()
                .id(1L)
                .username("paul")
                .password("hashedPassword")
                .build();

        when(userRepository.findByUsername("paul"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "hashedPassword"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never()).generateToken(anyString());
    }
}

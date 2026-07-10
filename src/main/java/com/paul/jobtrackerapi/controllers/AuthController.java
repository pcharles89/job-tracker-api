package com.paul.jobtrackerapi.controllers;

import com.paul.jobtrackerapi.dtos.auth.AuthResponse;
import com.paul.jobtrackerapi.dtos.auth.LoginRequest;
import com.paul.jobtrackerapi.dtos.auth.RegisterRequest;
import com.paul.jobtrackerapi.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Endpoints for user registration and authentication."
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns a JWT authentication token."
    )
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(
            summary = "Authenticate a user",
            description = "Authenticates a user and returns a JWT authentication token."
    )
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

package com.paul.jobtrackerapi.controllers;

import com.paul.jobtrackerapi.dtos.auth.AuthResponse;
import com.paul.jobtrackerapi.dtos.auth.LoginRequest;
import com.paul.jobtrackerapi.dtos.auth.RegisterRequest;
import com.paul.jobtrackerapi.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

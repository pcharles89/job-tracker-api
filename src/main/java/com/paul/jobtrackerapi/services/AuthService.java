package com.paul.jobtrackerapi.services;

import com.paul.jobtrackerapi.dtos.auth.AuthResponse;
import com.paul.jobtrackerapi.dtos.auth.LoginRequest;
import com.paul.jobtrackerapi.dtos.auth.RegisterRequest;
import com.paul.jobtrackerapi.entities.User;
import com.paul.jobtrackerapi.exceptions.InvalidCredentialsException;
import com.paul.jobtrackerapi.exceptions.UsernameAlreadyExistsException;
import com.paul.jobtrackerapi.repositories.UserRepository;
import com.paul.jobtrackerapi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if(userRepository.findByUsername(request.username()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }
}

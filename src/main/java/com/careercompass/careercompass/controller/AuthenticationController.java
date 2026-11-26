package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.dto.LoginRequest;
import com.careercompass.careercompass.dto.RegisterRequest;
import com.careercompass.careercompass.dto.AuthenticationResponse;
import com.careercompass.careercompass.service.UserService;
import com.careercompass.careercompass.entity.User;
import com.careercompass.careercompass.repository.UserRepository;
import com.careercompass.careercompass.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthenticationResponse response = userService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthenticationResponse response = userService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetailsImpl userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = userService.generateToken(user);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .build());
    }
}

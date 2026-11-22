package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.dto.LoginRequest;
import com.careercompass.careercompass.dto.RegisterRequest;
import com.careercompass.careercompass.dto.AuthenticationResponse;
import com.careercompass.careercompass.service.UserService;
import com.careercompass.careercompass.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.careercompass.careercompass.repository.UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthenticationResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        AuthenticationResponse response = userService.register(registerRequest);
        return ResponseEntity.status(201).body(response);
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
        if (principal instanceof com.careercompass.careercompass.security.UserDetailsImpl) {
            email = ((com.careercompass.careercompass.security.UserDetailsImpl) principal).getUsername();
        } else {
            email = authentication.getName();
        }
        User user = userRepository.findByEmail(email)
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
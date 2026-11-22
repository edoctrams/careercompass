package com.careercompass.careercompass.service;

import com.careercompass.careercompass.dto.LoginRequest;
import com.careercompass.careercompass.dto.RegisterRequest;
import com.careercompass.careercompass.dto.AuthenticationResponse;
import com.careercompass.careercompass.dto.StudentProfileDTO;
import com.careercompass.careercompass.entity.User;
import com.careercompass.careercompass.entity.StudentProfile;
import com.careercompass.careercompass.repository.UserRepository;
import com.careercompass.careercompass.repository.StudentProfileRepository;
import com.careercompass.careercompass.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setUser(user);
        studentProfile.setBranch(registerRequest.getBranch());
        studentProfile.setGraduationYear(registerRequest.getGraduationYear());
        studentProfile.setCurrentYear(registerRequest.getCurrentYear());
        studentProfile.setDailyAvailableHours(registerRequest.getDailyAvailableHours());
        studentProfile.setCareerGoal("");
        studentProfile.setTargetRole(null);

        studentProfileRepository.save(studentProfile);

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthenticationResponse refreshToken(User user) {
        String token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public String generateToken(User user) {
        return "careercompass-token-" + user.getId();
    }

    @Transactional
    public UserDetailsImpl getCurrentUserDetails() {
        org.springframework.security.core.Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDetailsImpl.build(user);
    }

    @Transactional
    public StudentProfileDTO getStudentProfile() {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return null;
        }

        User user = userDetails.getUser();
        StudentProfile profile = studentProfileRepository.findByUserId(user.getId())
                .orElse(new StudentProfile());

        StudentProfileDTO dto = new StudentProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBranch(profile.getBranch());
        dto.setGraduationYear(profile.getGraduationYear());
        dto.setCurrentYear(profile.getCurrentYear());
        dto.setDailyAvailableHours(profile.getDailyAvailableHours());
        dto.setCareerGoal(profile.getCareerGoal());

        if (profile.getTargetRole() != null) {
            dto.setTargetRoleName(profile.getTargetRole().getName());
        }

        dto.setProfileComplete(isProfileComplete(profile));
        dto.setCreatedAt(user.getCreatedAt().toLocalDate());

        return dto;
    }

    private boolean isProfileComplete(StudentProfile profile) {
        return profile.getBranch() != null
                && profile.getGraduationYear() != null
                && profile.getCurrentYear() != null
                && profile.getDailyAvailableHours() != null
                && profile.getTargetRole() != null
                && profile.getTargetRole().getId() != null;
    }
}
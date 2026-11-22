package com.careercompass.careercompass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import com.careercompass.careercompass.entity.User;
import com.careercompass.careercompass.repository.UserRepository;
import com.careercompass.careercompass.security.UserDetailsImpl;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            return UserDetailsImpl.build(user);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/registration",
                    "/careers",
                    "/careers/**",
                    "/api/auth/**",
                    "/api/careers/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()
                .requestMatchers(
                    "/dashboard",
                    "/profile",
                    "/settings",
                    "/onboarding",
                    "/skill-assessment/**",
                    "/roadmap/**",
                    "/roadmaps/**",
                    "/daily-tasks/**",
                    "/job-description/**",
                    "/resources/**",
                    "/projects/**",
                    "/question-sets/**",
                    "/companies/**",
                    "/interview-experiences/**",
                    "/progress/**",
                    "/activity/**",
                    "/api/**"
                ).authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

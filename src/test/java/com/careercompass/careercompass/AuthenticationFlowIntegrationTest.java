package com.careercompass.careercompass;

import com.careercompass.careercompass.entity.StudentProfile;
import com.careercompass.careercompass.entity.User;
import com.careercompass.careercompass.repository.StudentProfileRepository;
import com.careercompass.careercompass.repository.UserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authflowtest;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=update"
})
class AuthenticationFlowIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Filter springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void formRegistrationShouldCreateUserProfileAutoLoginAndAllowSubsequentLogin() throws Exception {
        String email = "Aayush.Test@Campus.EDU";
        String rawPassword = "Password123!";

        // 1. Register via HTML form POST /register
        MvcResult regResult = mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Aayush Test")
                        .param("email", email)
                        .param("password", rawPassword)
                        .param("confirmPassword", rawPassword)
                        .param("branch", "Computer Science")
                        .param("graduationYear", "2026")
                        .param("dailyAvailableHours", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/onboarding"))
                .andReturn();

        // Verify user & BCrypt password & StudentProfile persisted
        User savedUser = userRepository.findByEmailIgnoreCase("aayush.test@campus.edu").orElse(null);
        assertNotNull(savedUser);
        assertEquals("aayush.test@campus.edu", savedUser.getEmail());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPasswordHash()));

        StudentProfile profile = studentProfileRepository.findByUserId(savedUser.getId()).orElse(null);
        assertNotNull(profile);
        assertEquals("Computer Science", profile.getBranch());
        assertEquals(2026, profile.getGraduationYear());
        assertEquals(4, profile.getDailyAvailableHours());

        // Verify session is authenticated after registration (can access /onboarding)
        mockMvc.perform(get("/onboarding")
                        .session((org.springframework.mock.web.MockHttpSession) regResult.getRequest().getSession(false)))
                .andExpect(status().isOk());

        // 2. Duplicate registration should fail gracefully with 200 & error message (not 500)
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Duplicate User")
                        .param("email", "AAYUSH.TEST@campus.edu")
                        .param("password", rawPassword)
                        .param("confirmPassword", rawPassword)
                        .param("branch", "Computer Science")
                        .param("graduationYear", "2026")
                        .param("dailyAvailableHours", "3"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("registerError"));

        // 3. Login via Spring Security form POST /login (mixed case + whitespace)
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "  Aayush.Test@campus.edu ")
                        .param("password", rawPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        // 4. Invalid password should redirect to /login?error=true
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "aayush.test@campus.edu")
                        .param("password", "WrongPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void apiRegisterAndLoginShouldWorkForJsonClients() throws Exception {
        String jsonBody = """
                {
                  "name": "API Student",
                  "email": "api.student@campus.edu",
                  "password": "SecurePass99!",
                  "branch": "Information Technology",
                  "graduationYear": 2027,
                  "dailyAvailableHours": 3
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("api.student@campus.edu"));

        // Duplicate API register -> 409 Conflict
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict());

        // Valid API login -> 200 OK
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"API.Student@campus.edu\",\"password\":\"SecurePass99!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("api.student@campus.edu"));

        // Invalid API login -> 401 Unauthorized
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"api.student@campus.edu\",\"password\":\"WrongPass!\"}"))
                .andExpect(status().isUnauthorized());
    }
}

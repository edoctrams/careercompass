
package com.careercompass.careercompass.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careercompass.careercompass.entity.CareerInterestAssessment;
import com.careercompass.careercompass.entity.CareerPath;
import com.careercompass.careercompass.entity.StudentProfile;
import com.careercompass.careercompass.entity.User;
import com.careercompass.careercompass.repository.CareerInterestAssessmentRepository;
import com.careercompass.careercompass.repository.CareerPathRepository;
import com.careercompass.careercompass.repository.StudentProfileRepository;
import com.careercompass.careercompass.repository.UserRepository;
import com.careercompass.careercompass.service.CareerInterestAssessmentService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/career-interest-assessment")
public class CareerInterestAssessmentController {

    private final CareerInterestAssessmentService assessmentService;
    private final CareerInterestAssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final CareerPathRepository careerPathRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JsonMapper jsonMapper;

    public CareerInterestAssessmentController(
            CareerInterestAssessmentService assessmentService,
            CareerInterestAssessmentRepository assessmentRepository,
            UserRepository userRepository,
            CareerPathRepository careerPathRepository,
            StudentProfileRepository studentProfileRepository,
            JsonMapper jsonMapper) {

        this.assessmentService = assessmentService;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.careerPathRepository = careerPathRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.jsonMapper = jsonMapper;
    }

    // Request format for assessment submission.
    public record SubmissionRequest(
            Map<String, String> answers,
            String selectedCareerName
    ) {}

    // GET: Retrieve all assessment questions.
    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions() {

        return ResponseEntity.ok(
                assessmentService.getQuestions()
        );
    }

    // POST: Evaluate, save and select a career.
    @PostMapping("/submit")
    @Transactional
    public ResponseEntity<?> submitAssessment(
            @RequestBody SubmissionRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Please log in first."));
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found."));
        }

        if (request == null || request.answers() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Assessment answers are required."));
        }

        try {

            // Calculate scores and career recommendations.
            var result = assessmentService.evaluate(
                    request.answers(),
                    request.selectedCareerName()
            );

            // Find the selected career in the database.
            CareerPath selectedRole = careerPathRepository
                    .findByName(result.selectedCareerName())
                    .orElse(null);

            if (selectedRole == null) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "error",
                                "Selected career stream is not available."
                        ));
            }

            // Find or create the student's profile.
            StudentProfile profile = studentProfileRepository
                    .findByUserId(user.getId())
                    .orElseGet(() -> {

                        StudentProfile newProfile = new StudentProfile();
                        newProfile.setUser(user);

                        return newProfile;
                    });

            // Update the student's selected career.
            profile.setTargetRole(selectedRole);

            studentProfileRepository.save(profile);

            // Create assessment history record.
            CareerInterestAssessment assessment =
                    new CareerInterestAssessment();

            assessment.setUser(user);

            assessment.setAnswersJson(
                    jsonMapper.writeValueAsString(request.answers())
            );

            assessment.setScoresJson(
                    jsonMapper.writeValueAsString(result.scores())
            );

            assessment.setRecommendedCareerName(
                    result.recommendedCareerName()
            );

            assessment.setSelectedCareerName(
                    result.selectedCareerName()
            );

            // Persist assessment.
            CareerInterestAssessment savedAssessment =
                    assessmentRepository.save(assessment);

            // Prepare API response.
            Map<String, Object> response = new LinkedHashMap<>();

            response.put("assessmentId", savedAssessment.getId());
            response.put("scores", result.scores());
            response.put("recommendations", result.recommendations());

            response.put(
                    "recommendedCareerName",
                    result.recommendedCareerName()
            );

            response.put(
                    "selectedCareerName",
                    result.selectedCareerName()
            );

            response.put(
                    "message",
                    "Career assessment completed successfully."
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (JacksonException e) {

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error",
                            "Unable to save assessment responses."
                    ));
        }
    }
}

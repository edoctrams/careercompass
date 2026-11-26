package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.entity.*;
import com.careercompass.careercompass.dto.*;
import com.careercompass.careercompass.repository.*;
import com.careercompass.careercompass.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("/api/skill-assessment")
public class SkillAssessmentController {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CareerPathRepository careerPathRepository;

    @Autowired
    private CareerSkillRepository careerSkillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private com.careercompass.careercompass.repository.UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private SkillProgressHistoryRepository skillProgressHistoryRepository;

    @PostMapping("/assess")
    @Transactional
    public ResponseEntity<Map<String, Object>> assessSkills(@RequestBody Map<String, Integer> proficiencyRatings) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getName();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOpt.get();

        Optional<StudentProfile> studentProfileOpt = studentProfileRepository.findByUserId(user.getId());
        CareerPath targetRole = studentProfileOpt.map(StudentProfile::getTargetRole).orElse(null);

        Map<String, Object> results = new HashMap<>();

        int totalGap = 0;
        int totalSkills = 0;
        List<Map<String, Object>> skillDetails = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : proficiencyRatings.entrySet()) {
            String skillName = entry.getKey();
            Integer rawLevel = entry.getValue();
            if (skillName == null || rawLevel == null) {
                continue;
            }
            int currentLevel = Math.max(0, Math.min(5, rawLevel));

            Optional<Skill> skillOpt = skillRepository.findByNameIgnoreCase(skillName.trim());
            if (skillOpt.isPresent()) {
                Skill skill = skillOpt.get();

                // Upsert only the explicitly assessed skill into user_skills and record history on change
                Optional<UserSkill> existingOpt = userSkillRepository.findByUserIdAndSkillId(user.getId(), skill.getId());
                Integer previousLevel = existingOpt.map(UserSkill::getProficiency).orElse(null);

                if (existingOpt.isPresent()) {
                    UserSkill existing = existingOpt.get();
                    if (!Objects.equals(previousLevel, currentLevel)) {
                        existing.setProficiency(currentLevel);
                        userSkillRepository.save(existing);

                        SkillProgressHistory history = new SkillProgressHistory();
                        history.setUser(user);
                        history.setSkill(skill);
                        history.setPreviousLevel(previousLevel);
                        history.setNewLevel(currentLevel);
                        history.setSource("ASSESSMENT");
                        skillProgressHistoryRepository.save(history);
                    }
                } else {
                    UserSkill userSkill = new UserSkill();
                    userSkill.setUser(user);
                    userSkill.setSkill(skill);
                    userSkill.setProficiency(currentLevel);
                    userSkillRepository.save(userSkill);

                    SkillProgressHistory history = new SkillProgressHistory();
                    history.setUser(user);
                    history.setSkill(skill);
                    history.setPreviousLevel(null);
                    history.setNewLevel(currentLevel);
                    history.setSource("ASSESSMENT");
                    skillProgressHistoryRepository.save(history);
                }

                // Find required level for this student's target career
                int requiredLevel = 0;
                if (targetRole != null) {
                    Optional<CareerSkill> csOpt = careerSkillRepository.findByCareerPathIdAndSkillId(
                            targetRole.getId(), skill.getId());
                    if (csOpt.isPresent()) {
                        requiredLevel = csOpt.get().getRequiredLevel();
                    }
                }

                int skillGap = Math.max(0, requiredLevel - currentLevel);
                totalGap += skillGap;
                totalSkills++;

                Map<String, Object> detail = new HashMap<>();
                detail.put("skillName", skill.getName());
                detail.put("currentLevel", currentLevel);
                detail.put("requiredLevel", requiredLevel);
                detail.put("skillGap", skillGap);
                detail.put("category", skill.getCategory());
                skillDetails.add(detail);
            }
        }

        double averageGap = totalSkills > 0 ? (double) totalGap / totalSkills : 0;

        results.put("skillDetails", skillDetails);
        results.put("averageSkillGap", averageGap);
        results.put("totalSkillsAssessed", totalSkills);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/gap/{skillName}")
    public ResponseEntity<Map<String, Object>> getSkillGap(
            @PathVariable String skillName,
            @RequestParam(required = false) String careerPathName) {

        Optional<Skill> skillOpt = skillRepository.findByNameIgnoreCase(skillName);
        if (skillOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Skill skill = skillOpt.get();

        Map<String, Object> result = new HashMap<>();
        result.put("skillName", skill.getName());
        result.put("category", skill.getCategory());
        result.put("description", skill.getDescription());

        // Find required level based on career path
        int requiredLevel = 0;
        if (careerPathName != null) {
            Optional<CareerPath> careerPathOpt = careerPathRepository.findByName(careerPathName);
            if (careerPathOpt.isPresent()) {
                Optional<CareerSkill> csOpt = careerSkillRepository.findByCareerPathIdAndSkillId(
                        careerPathOpt.get().getId(), skill.getId());
                if (csOpt.isPresent()) {
                    requiredLevel = csOpt.get().getRequiredLevel();
                }
            }
        }
        result.put("requiredLevel", requiredLevel);

        return ResponseEntity.ok(result);
    }
}
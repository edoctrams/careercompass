package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.entity.*;
import com.careercompass.careercompass.dto.*;
import com.careercompass.careercompass.repository.*;
import com.careercompass.careercompass.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/assess")
    public ResponseEntity<Map<String, Object>> assessSkills(@RequestBody Map<String, Integer> proficiencyRatings) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());

        Map<String, Object> results = new HashMap<>();

        int totalGap = 0;
        int totalSkills = 0;
        List<Map<String, Object>> skillDetails = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : proficiencyRatings.entrySet()) {
            String skillName = entry.getKey();
            int currentLevel = entry.getValue();

            Optional<Skill> skillOpt = skillRepository.findByName(skillName);
            if (skillOpt.isPresent()) {
                Skill skill = skillOpt.get();

                // Find required level for this student's target career
                int requiredLevel = 0;
                Optional<StudentProfile> studentProfileOpt = studentProfileRepository.findByUserId(user.getId());
                if (studentProfileOpt.isPresent()) {
                    Optional<CareerPath> targetRoleOpt = Optional.ofNullable(studentProfileOpt.get().getTargetRole());
                    if (targetRoleOpt.isPresent()) {
                        Optional<CareerSkill> csOpt = careerSkillRepository.findByCareerPathIdAndSkillId(
                                targetRoleOpt.get().getId(), skill.getId());
                        if (csOpt.isPresent()) {
                            requiredLevel = csOpt.get().getRequiredLevel();
                        }
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

        Optional<Skill> skillOpt = skillRepository.findByName(skillName);
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
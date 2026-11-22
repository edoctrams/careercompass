package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.dto.StudentProfileDTO;
import com.careercompass.careercompass.entity.*;
import com.careercompass.careercompass.repository.*;
import com.careercompass.careercompass.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class StudentDataController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private CareerPathRepository careerPathRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CareerSkillRepository careerSkillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private RoadmapTaskRepository roadmapTaskRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private QuestionSetRepository questionSetRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private InterviewExperienceRepository interviewExperienceRepository;

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    private DailyActivityRepository dailyActivityRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    @GetMapping("/user/profile")
    public ResponseEntity<StudentProfileDTO> getProfile() {
        StudentProfileDTO profile = userService.getStudentProfile();
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/user/profile")
    @Transactional
    public ResponseEntity<StudentProfileDTO> updateProfile(@RequestBody Map<String, Object> updates) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StudentProfile profile = studentProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    StudentProfile p = new StudentProfile();
                    p.setUser(user);
                    return p;
                });

        if (updates.containsKey("name") && updates.get("name") != null) {
            user.setName((String) updates.get("name"));
            userRepository.save(user);
        }

        if (updates.containsKey("branch")) {
            profile.setBranch((String) updates.get("branch"));
        }
        if (updates.containsKey("graduationYear") && updates.get("graduationYear") != null) {
            profile.setGraduationYear(Integer.valueOf(updates.get("graduationYear").toString()));
        }
        if (updates.containsKey("currentYear") && updates.get("currentYear") != null) {
            profile.setCurrentYear(Integer.valueOf(updates.get("currentYear").toString()));
        }
        if (updates.containsKey("dailyAvailableHours") && updates.get("dailyAvailableHours") != null) {
            profile.setDailyAvailableHours(Integer.valueOf(updates.get("dailyAvailableHours").toString()));
        }
        if (updates.containsKey("careerGoal")) {
            profile.setCareerGoal((String) updates.get("careerGoal"));
        }
        if (updates.containsKey("targetRoleName") && updates.get("targetRoleName") != null) {
            String roleName = (String) updates.get("targetRoleName");
            Optional<CareerPath> cp = careerPathRepository.findByName(roleName);
            cp.ifPresent(profile::setTargetRole);
        }

        studentProfileRepository.save(profile);
        return ResponseEntity.ok(userService.getStudentProfile());
    }

    @GetMapping("/user/skills")
    public ResponseEntity<List<Map<String, Object>>> getUserSkills() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserSkill> skills = userSkillRepository.findByUserId(user.getId());
        List<Map<String, Object>> result = skills.stream().map(us -> {
            Map<String, Object> map = new HashMap<>();
            map.put("skillId", us.getSkill().getId());
            map.put("skillName", us.getSkill().getName());
            map.put("category", us.getSkill().getCategory());
            map.put("proficiency", us.getProficiency());
            map.put("lastUpdated", us.getLastUpdated());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/skills/all")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillRepository.findAll());
    }

    @GetMapping("/companies/all")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<Map<String, Object>> getCompanyDetails(@PathVariable Long id) {
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Company company = companyOpt.get();
        List<QuestionSet> questionSets = questionSetRepository.findByCompanyId(company.getId());
        List<InterviewExperience> experiences = interviewExperienceRepository.findByCompanyId(company.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("company", company);
        response.put("questionSets", questionSets);
        response.put("experiences", experiences);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/question-sets/all")
    public ResponseEntity<List<Map<String, Object>>> getAllQuestionSets() {
        List<QuestionSet> sets = questionSetRepository.findAll();
        List<Map<String, Object>> result = sets.stream().map(qs -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", qs.getId());
            map.put("title", qs.getTitle());
            map.put("branch", qs.getBranch());
            map.put("batch", qs.getBatch());
            map.put("difficulty", qs.getDifficulty());
            map.put("companyName", qs.getCompany() != null ? qs.getCompany().getName() : null);
            map.put("careerPathName", qs.getCareerPath() != null ? qs.getCareerPath().getName() : null);
            List<Question> questions = questionRepository.findByQuestionSetId(qs.getId());
            map.put("questionCount", questions.size());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/question-sets/{id}/questions")
    public ResponseEntity<List<Question>> getQuestionsForSet(@PathVariable Long id) {
        return ResponseEntity.ok(questionRepository.findByQuestionSetId(id));
    }

    @GetMapping("/interview-experiences/all")
    public ResponseEntity<List<Map<String, Object>>> getAllExperiences() {
        List<InterviewExperience> list = interviewExperienceRepository.findAll();
        List<Map<String, Object>> result = list.stream().map(exp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", exp.getId());
            map.put("companyName", exp.getCompany().getName());
            map.put("role", exp.getRole());
            map.put("batch", exp.getBatch());
            map.put("experienceText", exp.getExperienceText());
            map.put("difficulty", exp.getDifficulty());
            map.put("interviewDate", exp.getInterviewDate());
            map.put("candidateName", exp.getUser() != null ? exp.getUser().getName() : "Anonymous");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/interview-experiences")
    @Transactional
    public ResponseEntity<Map<String, Object>> createExperience(@RequestBody Map<String, Object> payload) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String companyName = (String) payload.get("companyName");
        String role = (String) payload.get("role");
        String batch = (String) payload.get("batch");
        String experienceText = (String) payload.get("experienceText");
        String difficulty = (String) payload.get("difficulty");

        Company company = companyRepository.findByNameIgnoreCase(companyName).orElseGet(() -> {
            Company c = new Company();
            c.setName(companyName);
            c.setIndustry("Technology");
            return companyRepository.save(c);
        });

        InterviewExperience exp = new InterviewExperience();
        exp.setUser(user);
        exp.setCompany(company);
        exp.setRole(role);
        exp.setBatch(batch);
        exp.setExperienceText(experienceText);
        exp.setDifficulty(difficulty != null ? difficulty : "Medium");
        exp.setInterviewDate(LocalDate.now());

        interviewExperienceRepository.save(exp);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("id", exp.getId());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/roadmaps/current")
    @Transactional
    public ResponseEntity<Map<String, Object>> getCurrentRoadmap() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Roadmap> userRoadmaps = roadmapRepository.findByUserId(user.getId());
        Roadmap currentRoadmap = userRoadmaps.stream()
                .filter(r -> r.getStatus() == RoadmapStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        if (currentRoadmap == null) {
            Optional<StudentProfile> profileOpt = studentProfileRepository.findByUserId(user.getId());
            if (profileOpt.isPresent() && profileOpt.get().getTargetRole() != null) {
                currentRoadmap = generateInitialRoadmap(user, profileOpt.get().getTargetRole());
            } else {
                return ResponseEntity.ok(Map.of("hasRoadmap", false));
            }
        }

        List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmapId(currentRoadmap.getId());

        long completedCount = tasks.stream().filter(t -> t.getStatus() == RoadmapTaskStatus.COMPLETED).count();
        double progressPercentage = tasks.isEmpty() ? 0 : ((double) completedCount / tasks.size()) * 100.0;

        Map<String, Object> response = new HashMap<>();
        response.put("hasRoadmap", true);
        response.put("id", currentRoadmap.getId());
        response.put("title", currentRoadmap.getTitle());
        response.put("careerPathName", currentRoadmap.getCareerPath().getName());
        response.put("status", currentRoadmap.getStatus().name());
        response.put("targetDeadline", currentRoadmap.getTargetDeadline());
        response.put("createdAt", currentRoadmap.getCreatedAt());
        response.put("progressPercentage", Math.round(progressPercentage));
        response.put("totalTasks", tasks.size());
        response.put("completedTasks", completedCount);
        response.put("tasks", tasks);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/roadmaps/tasks/{taskId}/status")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> payload) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<RoadmapTask> taskOpt = roadmapTaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RoadmapTask task = taskOpt.get();
        if (!task.getRoadmap().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String newStatus = payload.get("status");
        if (newStatus != null) {
            try {
                RoadmapTaskStatus status = RoadmapTaskStatus.valueOf(newStatus);
                task.setStatus(status);
                roadmapTaskRepository.save(task);

                if (status == RoadmapTaskStatus.COMPLETED) {
                    DailyActivity activity = new DailyActivity();
                    activity.setUser(user);
                    activity.setActivityDate(LocalDate.now());
                    activity.setActivityType("TASK_COMPLETED");
                    dailyActivityRepository.save(activity);
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value"));
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "status", task.getStatus().name()));
    }

    @PostMapping("/roadmaps/generate")
    @Transactional
    public ResponseEntity<Map<String, Object>> generateRoadmap(@RequestBody Map<String, Object> payload) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String careerName = (String) payload.get("careerPathName");
        CareerPath career = null;
        if (careerName != null) {
            career = careerPathRepository.findByName(careerName).orElse(null);
        }

        if (career == null) {
            Optional<StudentProfile> profileOpt = studentProfileRepository.findByUserId(user.getId());
            if (profileOpt.isPresent() && profileOpt.get().getTargetRole() != null) {
                career = profileOpt.get().getTargetRole();
            }
        }

        if (career == null) {
            career = careerPathRepository.findAll().stream().findFirst().orElse(null);
        }

        if (career == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No career path available to generate roadmap"));
        }

        Roadmap roadmap = generateInitialRoadmap(user, career);
        return ResponseEntity.ok(Map.of("success", true, "roadmapId", roadmap.getId()));
    }

    @PostMapping("/job-descriptions/analyze")
    @Transactional
    public ResponseEntity<Map<String, Object>> analyzeJobDescription(@RequestBody Map<String, String> request) {
        String companyName = request.get("companyName");
        String role = request.get("role");
        String jdText = request.get("description");

        if (jdText == null || jdText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Job description text cannot be empty."));
        }

        User user = getCurrentUser();
        if (user != null) {
            JobDescription jd = new JobDescription();
            jd.setUser(user);
            jd.setCompanyName(companyName != null ? companyName : "Target Company");
            jd.setRole(role != null ? role : "Engineering Role");
            jd.setDescription(jdText);
            jobDescriptionRepository.save(jd);
        }

        List<Skill> allSkills = skillRepository.findAll();
        String lowerJd = jdText.toLowerCase();

        List<Skill> matchedSkillsInJD = new ArrayList<>();
        for (Skill s : allSkills) {
            String nameLower = s.getName().toLowerCase();
            if (lowerJd.contains(nameLower) ||
                (nameLower.equals("dsa") && (lowerJd.contains("data structures") || lowerJd.contains("algorithms"))) ||
                (nameLower.equals("dbms") && (lowerJd.contains("database") || lowerJd.contains("sql"))) ||
                (nameLower.equals("oop") && (lowerJd.contains("object oriented") || lowerJd.contains("oops")))) {
                matchedSkillsInJD.add(s);
            }
        }

        Map<Long, Integer> userProficiencies = new HashMap<>();
        if (user != null) {
            List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());
            for (UserSkill us : userSkills) {
                userProficiencies.put(us.getSkill().getId(), us.getProficiency());
            }
        }

        List<Map<String, Object>> skillsAnalysis = new ArrayList<>();
        int matchedCount = 0;
        int totalExtracted = matchedSkillsInJD.size();

        for (Skill s : matchedSkillsInJD) {
            int currentLevel = userProficiencies.getOrDefault(s.getId(), 0);
            boolean possessed = currentLevel >= 3;
            if (possessed) matchedCount++;

            Map<String, Object> item = new HashMap<>();
            item.put("skillName", s.getName());
            item.put("category", s.getCategory());
            item.put("possessed", possessed);
            item.put("currentLevel", currentLevel);
            item.put("recommendedLevel", 4);
            skillsAnalysis.add(item);
        }

        int matchPercentage = totalExtracted > 0 ? (int) Math.round(((double) matchedCount / totalExtracted) * 100.0) : 0;

        List<String> recommendations = new ArrayList<>();
        List<String> missingSkills = skillsAnalysis.stream()
                .filter(m -> !(Boolean) m.get("possessed"))
                .map(m -> (String) m.get("skillName"))
                .collect(Collectors.toList());

        if (!missingSkills.isEmpty()) {
            recommendations.add("Prioritize learning foundational requirements: " + String.join(", ", missingSkills.subList(0, Math.min(3, missingSkills.size()))));
            recommendations.add("Build a capstone project demonstrating practical usage of " + missingSkills.get(0));
        } else {
            recommendations.add("You have strong alignment with this role's core technical stack! Focus on system design and mock interview practice.");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("companyName", companyName);
        result.put("role", role);
        result.put("matchPercentage", matchPercentage);
        result.put("totalSkillsIdentified", totalExtracted);
        result.put("matchedSkillsCount", matchedCount);
        result.put("skillsAnalysis", skillsAnalysis);
        result.put("missingSkills", missingSkills);
        result.put("recommendations", recommendations);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/activity/summary")
    public ResponseEntity<Map<String, Object>> getActivitySummary() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<DailyActivity> activities = dailyActivityRepository.findByUserId(user.getId());
        List<Roadmap> userRoadmaps = roadmapRepository.findByUserId(user.getId());
        int totalCompletedTasks = 0;
        for (Roadmap r : userRoadmaps) {
            totalCompletedTasks += roadmapTaskRepository.findByRoadmapIdAndStatus(r.getId(), RoadmapTaskStatus.COMPLETED).size();
        }

        int streak = calculateStreak(activities);

        Map<String, Object> map = new HashMap<>();
        map.put("streakDays", streak);
        map.put("completedTasks", totalCompletedTasks);
        map.put("totalActivityRecords", activities.size());
        map.put("learningHours", Math.max(1, totalCompletedTasks * 2));
        return ResponseEntity.ok(map);
    }

    private int calculateStreak(List<DailyActivity> activities) {
        if (activities.isEmpty()) return 1;
        Set<LocalDate> dates = activities.stream()
                .map(DailyActivity::getActivityDate)
                .collect(Collectors.toSet());

        LocalDate check = LocalDate.now();
        int streak = 0;
        if (dates.contains(check) || dates.contains(check.minusDays(1))) {
            if (!dates.contains(check)) check = check.minusDays(1);
            while (dates.contains(check)) {
                streak++;
                check = check.minusDays(1);
            }
        }
        return Math.max(1, streak);
    }

    private Roadmap generateInitialRoadmap(User user, CareerPath career) {
        Roadmap roadmap = new Roadmap();
        roadmap.setUser(user);
        roadmap.setCareerPath(career);
        roadmap.setTitle(career.getName() + " Preparation Roadmap");
        roadmap.setTargetDeadline(LocalDate.now().plusMonths(career.getAveragePreparationMonths() != null ? career.getAveragePreparationMonths() : 6));
        roadmap.setStatus(RoadmapStatus.ACTIVE);
        roadmap = roadmapRepository.save(roadmap);

        List<CareerSkill> careerSkills = careerSkillRepository.findByCareerPathId(career.getId());

        int week = 1;
        for (CareerSkill cs : careerSkills) {
            Skill skill = cs.getSkill();

            RoadmapTask task1 = new RoadmapTask();
            task1.setRoadmap(roadmap);
            task1.setSkill(skill);
            task1.setTitle(skill.getName() + " Fundamentals & Theory");
            task1.setDescription("Study core concepts, standard patterns, and fundamental implementations of " + skill.getName() + ".");
            task1.setWeekNumber(week);
            task1.setEstimatedHours(6);
            task1.setStatus(RoadmapTaskStatus.NOT_STARTED);
            roadmapTaskRepository.save(task1);

            RoadmapTask task2 = new RoadmapTask();
            task2.setRoadmap(roadmap);
            task2.setSkill(skill);
            task2.setTitle(skill.getName() + " Hands-on Problem Solving & Projects");
            task2.setDescription("Solve target placement questions and build practical exercises applying " + skill.getName() + ".");
            task2.setWeekNumber(week);
            task2.setEstimatedHours(8);
            task2.setStatus(RoadmapTaskStatus.NOT_STARTED);
            roadmapTaskRepository.save(task2);

            week++;
        }

        return roadmap;
    }
}

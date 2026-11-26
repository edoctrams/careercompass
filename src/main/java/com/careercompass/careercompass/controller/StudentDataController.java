package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.dto.StudentProfileDTO;
import com.careercompass.careercompass.entity.*;
import com.careercompass.careercompass.repository.*;
import com.careercompass.careercompass.service.PersonalizedRoadmapService;
import com.careercompass.careercompass.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

    @Autowired
    private QuestionAttemptRepository questionAttemptRepository;

    @Autowired
    private PersonalizedRoadmapService personalizedRoadmapService;

    @Autowired
    private CompanyCareerSkillRepository companyCareerSkillRepository;

    @Autowired
    private QuestionSubtopicRepository questionSubtopicRepository;

    @Autowired
    private QuestionCompanyRepository questionCompanyRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
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
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getUserSkills() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CareerPath targetRole = studentProfileRepository.findByUserId(user.getId())
                .map(StudentProfile::getTargetRole)
                .orElse(null);
        Map<Long, Integer> requiredBySkill = new HashMap<>();
        if (targetRole != null) {
            for (CareerSkill cs : careerSkillRepository.findByCareerPathId(targetRole.getId())) {
                requiredBySkill.put(cs.getSkill().getId(), cs.getRequiredLevel());
            }
        }

        List<UserSkill> skills = userSkillRepository.findByUserId(user.getId());
        List<Map<String, Object>> result = skills.stream().map(us -> {
            Map<String, Object> map = new HashMap<>();
            map.put("skillId", us.getSkill().getId());
            map.put("skillName", us.getSkill().getName());
            map.put("category", us.getSkill().getCategory());
            map.put("proficiency", us.getProficiency());
            map.put("requiredLevel", requiredBySkill.getOrDefault(us.getSkill().getId(), 3));
            map.put("lastUpdated", us.getLastUpdated());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/careers/curriculum")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCareerCurriculumByParam(
            @RequestParam String name,
            @RequestParam(required = false) String companyName) {
        return getCareerCurriculum(name, companyName);
    }

    @GetMapping("/careers/{name}/curriculum")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCareerCurriculum(
            @PathVariable String name,
            @RequestParam(required = false) String companyName) {
        Optional<CareerPath> careerOpt = careerPathRepository.findByName(name);
        if (careerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        CareerPath career = careerOpt.get();

        Company company = null;
        if (companyName != null && !companyName.isBlank()) {
            company = companyRepository.findByNameIgnoreCase(companyName.trim()).orElse(null);
        }
        Map<Long, CompanyCareerSkill> companyOverrides = new HashMap<>();
        if (company != null) {
            for (CompanyCareerSkill ccs : companyCareerSkillRepository.findByCompanyIdAndCareerPathId(company.getId(), career.getId())) {
                companyOverrides.put(ccs.getSkill().getId(), ccs);
            }
        }

        List<CareerSkill> careerSkills = careerSkillRepository.findByCareerPathId(career.getId());
        List<Map<String, Object>> items = careerSkills.stream().map(cs -> {
            Skill s = cs.getSkill();
            Skill prereq = cs.getPrerequisiteSkill();
            CompanyCareerSkill ccs = companyOverrides.get(s.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("name", s.getName());
            item.put("category", s.getCategory());
            item.put("description", s.getDescription());
            item.put("difficulty", s.getDifficulty());
            item.put("nodeType", s.getNodeType());
            item.put("parentSkillId", s.getParentSkill() != null ? s.getParentSkill().getId() : null);
            item.put("parentSkillName", s.getParentSkill() != null ? s.getParentSkill().getName() : null);
            item.put("prerequisiteSkillId", prereq != null ? prereq.getId() : null);
            item.put("prerequisiteSkillName", prereq != null ? prereq.getName() : null);
            item.put("requiredLevel", cs.getRequiredLevel());
            item.put("importance", cs.getImportance());
            item.put("companyTargetLevel", ccs != null ? ccs.getTargetLevel() : null);
            item.put("companyImportance", ccs != null ? ccs.getImportance() : null);
            item.put("evidenceType", ccs != null ? ccs.getEvidenceType() : null);
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("careerId", career.getId());
        response.put("careerName", career.getName());
        response.put("companyName", company != null ? company.getName() : null);
        response.put("totalSkills", items.size());
        response.put("skills", items);
        return ResponseEntity.ok(response);
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

    @GetMapping("/companies/preparation")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCompanyPreparation(
            @RequestParam String name,
            @RequestParam(required = false) String careerPathName) {
        Optional<Company> companyOpt = companyRepository.findByNameIgnoreCase(name.trim());
        if (companyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Company company = companyOpt.get();

        User user = getCurrentUser();
        CareerPath resolvedCareer = null;
        if (careerPathName != null && !careerPathName.isBlank()) {
            resolvedCareer = careerPathRepository.findByName(careerPathName.trim()).orElse(null);
        } else if (user != null) {
            resolvedCareer = studentProfileRepository.findByUserId(user.getId())
                    .map(StudentProfile::getTargetRole)
                    .orElse(null);
        }

        Map<Long, Integer> userProficiencies = new HashMap<>();
        if (user != null) {
            for (UserSkill us : userSkillRepository.findByUserId(user.getId())) {
                userProficiencies.put(us.getSkill().getId(), us.getProficiency());
            }
        }

        List<CompanyCareerSkill> companySkills = resolvedCareer != null
                ? companyCareerSkillRepository.findByCompanyIdAndCareerPathId(company.getId(), resolvedCareer.getId())
                : companyCareerSkillRepository.findByCompanyId(company.getId());

        List<Map<String, Object>> focusSkills = companySkills.stream()
                .sorted(Comparator.comparingInt(CompanyCareerSkill::getImportance).reversed()
                        .thenComparing(ccs -> ccs.getSkill().getName()))
                .map(ccs -> {
                    Skill s = ccs.getSkill();
                    int current = userProficiencies.getOrDefault(s.getId(), 0);
                    int target = ccs.getTargetLevel() != null ? ccs.getTargetLevel() : 4;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("skillId", s.getId());
                    item.put("skillName", s.getName());
                    item.put("category", s.getCategory());
                    item.put("nodeType", s.getNodeType());
                    item.put("careerPathName", ccs.getCareerPath().getName());
                    item.put("targetLevel", target);
                    item.put("currentLevel", current);
                    item.put("skillGap", Math.max(0, target - current));
                    item.put("importance", ccs.getImportance());
                    item.put("evidenceType", ccs.getEvidenceType());
                    item.put("sourceUrl", ccs.getSourceUrl());
                    return item;
                })
                .collect(Collectors.toList());

        List<InterviewExperience> experiences = interviewExperienceRepository.findByCompanyId(company.getId());
        List<Map<String, Object>> experienceDtos = experiences.stream().map(exp -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", exp.getId());
            map.put("companyName", company.getName());
            map.put("role", exp.getRole());
            map.put("batch", exp.getBatch());
            map.put("experienceText", exp.getExperienceText());
            map.put("difficulty", exp.getDifficulty());
            map.put("interviewDate", exp.getInterviewDate());
            map.put("candidateName", exp.getUser() != null ? exp.getUser().getName() : "Anonymous");
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("companyId", company.getId());
        response.put("companyName", company.getName());
        response.put("industry", company.getIndustry());
        response.put("description", company.getDescription());
        response.put("website", company.getWebsite());
        response.put("selectedCareerPathName", resolvedCareer != null ? resolvedCareer.getName() : null);
        response.put("focusSkills", focusSkills);
        response.put("experiences", experienceDtos);
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

    @PostMapping("/questions/{id}/attempt")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordQuestionAttempt(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Question> questionOpt = questionRepository.findById(id);
        if (questionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Question question = questionOpt.get();

        String selectedAnswer = payload.get("selectedAnswer") != null
                ? payload.get("selectedAnswer").toString().trim()
                : null;

        Boolean isCorrect = null;
        if (payload.get("isCorrect") instanceof Boolean boolVal) {
            isCorrect = boolVal;
        } else if (selectedAnswer != null && question.getAnswer() != null && question.getQuestionType() == QuestionType.MCQ) {
            isCorrect = evaluateMcqAnswer(selectedAnswer, question.getAnswer());
        }

        Integer timeSpentSeconds = null;
        if (payload.get("timeSpentSeconds") instanceof Number num) {
            timeSpentSeconds = Math.max(0, num.intValue());
        }

        QuestionAttempt attempt = new QuestionAttempt();
        attempt.setUser(user);
        attempt.setQuestion(question);
        attempt.setSelectedAnswer(selectedAnswer);
        attempt.setIsCorrect(isCorrect);
        attempt.setTimeSpentSeconds(timeSpentSeconds);
        attempt = questionAttemptRepository.save(attempt);

        LocalDate today = LocalDate.now();
        boolean alreadyLoggedToday = dailyActivityRepository.findByUserIdAndActivityDate(user.getId(), today).stream()
                .anyMatch(a -> "QUESTION_PRACTICED".equals(a.getActivityType()));
        if (!alreadyLoggedToday) {
            DailyActivity activity = new DailyActivity();
            activity.setUser(user);
            activity.setActivityDate(today);
            activity.setActivityType("QUESTION_PRACTICED");
            dailyActivityRepository.save(activity);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("attemptId", attempt.getId());
        response.put("questionId", question.getId());
        response.put("isCorrect", attempt.getIsCorrect());
        response.put("attemptedAt", attempt.getAttemptedAt());
        response.put("correctAnswer", question.getAnswer());
        response.put("explanation", question.getExplanation());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions/attempts/summary")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getQuestionAttemptsSummary() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<QuestionAttempt> attempts = questionAttemptRepository.findByUserIdOrderByAttemptedAtDesc(user.getId());
        long totalAttempts = attempts.size();
        long uniqueQuestionsAttempted = attempts.stream()
                .map(a -> a.getQuestion().getId())
                .distinct()
                .count();
        long evaluatedAttempts = attempts.stream()
                .filter(a -> a.getIsCorrect() != null)
                .count();
        long correctAttempts = attempts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();
        double accuracyPercent = evaluatedAttempts > 0
                ? Math.round((correctAttempts * 1000.0) / evaluatedAttempts) / 10.0
                : 0.0;

        List<Map<String, Object>> recentAttempts = attempts.stream().limit(10).map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("attemptId", a.getId());
            item.put("questionId", a.getQuestion().getId());
            item.put("questionType", a.getQuestion().getQuestionType() != null ? a.getQuestion().getQuestionType().name() : null);
            item.put("difficulty", a.getQuestion().getDifficulty());
            item.put("selectedAnswer", a.getSelectedAnswer());
            item.put("isCorrect", a.getIsCorrect());
            item.put("timeSpentSeconds", a.getTimeSpentSeconds());
            item.put("attemptedAt", a.getAttemptedAt());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAttempts", totalAttempts);
        summary.put("uniqueQuestionsAttempted", uniqueQuestionsAttempted);
        summary.put("evaluatedAttempts", evaluatedAttempts);
        summary.put("correctAttempts", correctAttempts);
        summary.put("accuracyPercent", accuracyPercent);
        summary.put("recentAttempts", recentAttempts);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/questions/filter")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> filterQuestions(
            @RequestParam(required = false) Long careerPathId,
            @RequestParam(required = false) String careerPathName,
            @RequestParam(required = false) Long subtopicId,
            @RequestParam(required = false) String subtopicName,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false, defaultValue = "false") boolean mine) {

        CareerPath resolvedCareer = null;
        if (careerPathId != null) {
            resolvedCareer = careerPathRepository.findById(careerPathId).orElse(null);
        } else if (careerPathName != null && !careerPathName.isBlank()) {
            resolvedCareer = careerPathRepository.findByName(careerPathName.trim()).orElse(null);
        } else if (mine) {
            User user = getCurrentUser();
            if (user != null) {
                resolvedCareer = studentProfileRepository.findByUserId(user.getId())
                        .map(StudentProfile::getTargetRole)
                        .orElse(null);
            }
        }

        Skill resolvedSubtopic = null;
        if (subtopicId != null) {
            resolvedSubtopic = skillRepository.findById(subtopicId).orElse(null);
        } else if (subtopicName != null && !subtopicName.isBlank()) {
            resolvedSubtopic = skillRepository.findByNameIgnoreCase(subtopicName.trim()).orElse(null);
        }

        Company resolvedCompany = null;
        if (companyId != null) {
            resolvedCompany = companyRepository.findById(companyId).orElse(null);
        } else if (companyName != null && !companyName.isBlank()) {
            resolvedCompany = companyRepository.findByNameIgnoreCase(companyName.trim()).orElse(null);
        }

        List<Question> candidates;
        if (resolvedCompany != null && resolvedCareer != null) {
            candidates = questionRepository.findByCompanyIdAndCareerPathId(resolvedCompany.getId(), resolvedCareer.getId());
        } else if (resolvedSubtopic != null) {
            candidates = questionRepository.findBySubtopicSkillId(resolvedSubtopic.getId());
        } else {
            candidates = questionRepository.findAll();
        }

        final CareerPath targetCareer = resolvedCareer;
        final Skill targetSubtopic = resolvedSubtopic;
        final Company targetCompany = resolvedCompany;
        final Set<Long> careerSkillIds = targetCareer != null
                ? careerSkillRepository.findByCareerPathId(targetCareer.getId()).stream()
                        .map(cs -> cs.getSkill().getId())
                        .collect(Collectors.toSet())
                : Collections.emptySet();

        // Batch-load associations to avoid N+1 lazy queries over remote DB connections
        questionSetRepository.findAll();
        skillRepository.findAll();
        careerPathRepository.findAll();
        companyRepository.findAll();

        Map<Long, List<QuestionSubtopic>> subtopicsByQuestion = questionSubtopicRepository.findAll().stream()
                .collect(Collectors.groupingBy(qs -> qs.getQuestion().getId()));
        Map<Long, List<QuestionCompany>> companiesByQuestion = questionCompanyRepository.findAll().stream()
                .collect(Collectors.groupingBy(qc -> qc.getQuestion().getId()));

        List<Map<String, Object>> filtered = candidates.stream()
                .filter(q -> {
                    List<QuestionSubtopic> qSubs = subtopicsByQuestion.getOrDefault(q.getId(), Collections.emptyList());
                    List<QuestionCompany> qComps = companiesByQuestion.getOrDefault(q.getId(), Collections.emptyList());

                    if (targetCareer != null) {
                        CareerPath qsCareer = q.getQuestionSet() != null ? q.getQuestionSet().getCareerPath() : null;
                        boolean matchesSetCareer = qsCareer != null && qsCareer.getId().equals(targetCareer.getId());
                        boolean matchesSkillCareer = q.getSkill() != null && careerSkillIds.contains(q.getSkill().getId());
                        boolean matchesSubtopicCareer = qSubs.stream()
                                .anyMatch(qs -> careerSkillIds.contains(qs.getSkill().getId()));
                        if (!matchesSetCareer && !matchesSkillCareer && !matchesSubtopicCareer) {
                            return false;
                        }
                    }
                    if (targetSubtopic != null) {
                        boolean matchesSubtopic = qSubs.stream()
                                .anyMatch(qs -> qs.getSkill().getId().equals(targetSubtopic.getId()));
                        boolean matchesPrimarySkill = q.getSkill() != null
                                && q.getSkill().getId().equals(targetSubtopic.getId());
                        if (!matchesSubtopic && !matchesPrimarySkill) {
                            return false;
                        }
                    }
                    if (targetCompany != null) {
                        boolean matchesQc = qComps.stream()
                                .anyMatch(qc -> qc.getCompany().getId().equals(targetCompany.getId()));
                        boolean matchesSetCompany = q.getQuestionSet() != null
                                && q.getQuestionSet().getCompany() != null
                                && q.getQuestionSet().getCompany().getId().equals(targetCompany.getId());
                        if (!matchesQc && !matchesSetCompany) {
                            return false;
                        }
                    }
                    if (difficulty != null && !difficulty.isBlank()) {
                        if (q.getDifficulty() == null || !q.getDifficulty().equalsIgnoreCase(difficulty.trim())) {
                            return false;
                        }
                    }
                    if (questionType != null && !questionType.isBlank()) {
                        if (q.getQuestionType() == null || !q.getQuestionType().name().equalsIgnoreCase(questionType.trim())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(q -> toQuestionDto(
                        q,
                        subtopicsByQuestion.getOrDefault(q.getId(), Collections.emptyList()),
                        companiesByQuestion.getOrDefault(q.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    private boolean evaluateMcqAnswer(String selectedAnswer, String storedAnswer) {
        String sel = selectedAnswer.trim();
        String ans = storedAnswer.trim();
        if (sel.equalsIgnoreCase(ans)) {
            return true;
        }
        String selOption = extractLeadingOption(sel);
        String ansOption = extractLeadingOption(ans);
        return selOption != null && selOption.equalsIgnoreCase(ansOption);
    }

    private String extractLeadingOption(String text) {
        if (text.length() == 1 && Character.isLetter(text.charAt(0))) {
            return text;
        }
        if (text.length() >= 2 && Character.isLetter(text.charAt(0)) && (text.charAt(1) == ')' || text.charAt(1) == '.' || text.charAt(1) == ':')) {
            return String.valueOf(text.charAt(0));
        }
        return null;
    }

    private Map<String, Object> toQuestionDto(Question q, List<QuestionSubtopic> qSubtopics, List<QuestionCompany> qCompanies) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", q.getId());
        dto.put("questionText", q.getQuestionText());
        dto.put("answer", q.getAnswer());
        dto.put("explanation", q.getExplanation());
        dto.put("difficulty", q.getDifficulty());
        dto.put("questionType", q.getQuestionType() != null ? q.getQuestionType().name() : null);
        dto.put("skillName", q.getSkill() != null ? q.getSkill().getName() : null);
        dto.put("questionSetId", q.getQuestionSet() != null ? q.getQuestionSet().getId() : null);
        dto.put("questionSetTitle", q.getQuestionSet() != null ? q.getQuestionSet().getTitle() : null);
        dto.put("careerPathName", q.getQuestionSet() != null && q.getQuestionSet().getCareerPath() != null
                ? q.getQuestionSet().getCareerPath().getName()
                : null);

        List<Map<String, Object>> subtopics = qSubtopics.stream().map(qs -> {
            Map<String, Object> sMap = new LinkedHashMap<>();
            sMap.put("id", qs.getSkill().getId());
            sMap.put("name", qs.getSkill().getName());
            return sMap;
        }).collect(Collectors.toList());
        dto.put("subtopics", subtopics);

        List<Map<String, Object>> companies = qCompanies.stream().map(qc -> {
            Map<String, Object> cMap = new LinkedHashMap<>();
            cMap.put("id", qc.getCompany().getId());
            cMap.put("name", qc.getCompany().getName());
            cMap.put("evidenceType", qc.getEvidenceType());
            return cMap;
        }).collect(Collectors.toList());
        dto.put("companies", companies);
        return dto;
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
                currentRoadmap = personalizedRoadmapService.generateOrUpdateRoadmap(
                        user, profileOpt.get().getTargetRole(), null, null);
            } else {
                return ResponseEntity.ok(Map.of("hasRoadmap", false));
            }
        }

        List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmapId(currentRoadmap.getId());
        tasks.sort(Comparator
                .comparing(RoadmapTask::getWeekNumber, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(RoadmapTask::getId));

        Map<Long, Integer> userProficiencies = userSkillRepository.findByUserId(user.getId()).stream()
                .collect(Collectors.toMap(us -> us.getSkill().getId(), UserSkill::getProficiency, (a, b) -> b));
        Map<Long, CareerSkill> careerSkillMap = careerSkillRepository.findByCareerPathId(currentRoadmap.getCareerPath().getId()).stream()
                .collect(Collectors.toMap(cs -> cs.getSkill().getId(), cs -> cs, (a, b) -> a));
        Map<Long, CompanyCareerSkill> companySkillMap = new HashMap<>();
        if (currentRoadmap.getCompany() != null) {
            for (CompanyCareerSkill ccs : companyCareerSkillRepository.findByCompanyIdAndCareerPathId(
                    currentRoadmap.getCompany().getId(), currentRoadmap.getCareerPath().getId())) {
                companySkillMap.put(ccs.getSkill().getId(), ccs);
            }
        }
        Map<Long, List<LearningResource>> resourcesBySkillId = learningResourceRepository.findAll().stream()
                .collect(Collectors.groupingBy(lr -> lr.getSkill().getId()));

        List<Map<String, Object>> taskDtos = tasks.stream().map(t -> {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", t.getId());
            dto.put("title", t.getTitle());
            dto.put("description", t.getDescription());
            dto.put("weekNumber", t.getWeekNumber());
            dto.put("estimatedHours", t.getEstimatedHours());
            dto.put("resourceUrl", t.getResourceUrl());
            dto.put("status", t.getStatus().name());

            Skill skill = t.getSkill();
            if (skill != null) {
                CareerSkill cs = careerSkillMap.get(skill.getId());
                CompanyCareerSkill ccs = companySkillMap.get(skill.getId());
                int currentLevel = userProficiencies.getOrDefault(skill.getId(), 0);
                int baseRequired = cs != null && cs.getRequiredLevel() != null ? cs.getRequiredLevel() : 3;
                int requiredLevel = Math.max(baseRequired, ccs != null && ccs.getTargetLevel() != null ? ccs.getTargetLevel() : 0);
                int baseImportance = cs != null && cs.getImportance() != null ? cs.getImportance() : 3;
                int importance = Math.max(baseImportance, ccs != null && ccs.getImportance() != null ? ccs.getImportance() : 0);
                int gap = Math.max(0, requiredLevel - currentLevel);
                boolean companyTargeted = ccs != null;
                int priorityScore = (gap * 3) + importance + (companyTargeted ? 1 : 0);
                double weightedPriority = Math.round(((0.50 * (gap / 5.0)) + (0.30 * (importance / 5.0)) + (0.20 * (companyTargeted ? 1.0 : 0.0))) * 100.0);

                dto.put("skillId", skill.getId());
                dto.put("skillName", skill.getName());
                dto.put("category", skill.getCategory());
                dto.put("nodeType", skill.getNodeType());
                dto.put("currentLevel", currentLevel);
                dto.put("requiredLevel", requiredLevel);
                dto.put("skillGap", gap);
                dto.put("importance", importance);
                dto.put("companyTargeted", companyTargeted);
                dto.put("priorityScore", priorityScore);
                dto.put("weightedPriorityPercent", (int) weightedPriority);
                dto.put("priorityLabel", gap >= 3 || priorityScore >= 12 ? "High Priority" : (gap >= 1 ? "Medium Priority" : "Reinforcement"));

                Skill prereq = cs != null ? cs.getPrerequisiteSkill() : null;
                dto.put("prerequisiteSkillName", prereq != null ? prereq.getName() : null);

                List<Map<String, Object>> resList = resourcesBySkillId.getOrDefault(skill.getId(), Collections.emptyList())
                        .stream().limit(3).map(lr -> {
                            Map<String, Object> r = new LinkedHashMap<>();
                            r.put("title", lr.getTitle());
                            r.put("url", lr.getResourceUrl());
                            r.put("type", lr.getResourceType());
                            r.put("provider", lr.getProvider());
                            return r;
                        }).collect(Collectors.toList());
                dto.put("resources", resList);
            }
            return dto;
        }).collect(Collectors.toList());

        long completedCount = tasks.stream().filter(t -> t.getStatus() == RoadmapTaskStatus.COMPLETED).count();
        double progressPercentage = tasks.isEmpty() ? 0 : ((double) completedCount / tasks.size()) * 100.0;

        Map<String, Object> response = new HashMap<>();
        response.put("hasRoadmap", true);
        response.put("id", currentRoadmap.getId());
        response.put("title", currentRoadmap.getTitle());
        response.put("careerPathName", currentRoadmap.getCareerPath().getName());
        response.put("companyName", currentRoadmap.getCompany() != null ? currentRoadmap.getCompany().getName() : null);
        response.put("status", currentRoadmap.getStatus().name());
        response.put("targetDeadline", currentRoadmap.getTargetDeadline());
        response.put("createdAt", currentRoadmap.getCreatedAt());
        response.put("progressPercentage", Math.round(progressPercentage));
        response.put("totalTasks", tasks.size());
        response.put("completedTasks", completedCount);
        response.put("tasks", taskDtos);

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
    public ResponseEntity<Map<String, Object>> generateRoadmap(@RequestBody(required = false) Map<String, Object> payload) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> body = payload != null ? payload : Collections.emptyMap();
        String careerName = body.get("careerPathName") != null ? body.get("careerPathName").toString() : null;
        CareerPath career = null;
        if (careerName != null && !careerName.isBlank()) {
            career = careerPathRepository.findByName(careerName.trim()).orElse(null);
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

        Company company = null;
        if (body.get("companyId") instanceof Number companyIdNum) {
            company = companyRepository.findById(companyIdNum.longValue()).orElse(null);
        } else if (body.get("companyName") != null && !body.get("companyName").toString().isBlank()) {
            company = companyRepository.findByNameIgnoreCase(body.get("companyName").toString().trim()).orElse(null);
        }

        LocalDate targetDeadline = null;
        if (body.get("targetDeadline") != null && !body.get("targetDeadline").toString().isBlank()) {
            try {
                targetDeadline = LocalDate.parse(body.get("targetDeadline").toString().trim());
            } catch (Exception ignored) {
            }
        }

        Roadmap roadmap = personalizedRoadmapService.generateOrUpdateRoadmap(user, career, company, targetDeadline);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("roadmapId", roadmap.getId());
        result.put("careerPathName", career.getName());
        result.put("companyName", company != null ? company.getName() : null);
        result.put("targetDeadline", roadmap.getTargetDeadline());
        return ResponseEntity.ok(result);
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
        map.put("learningHours", Math.max(0, totalCompletedTasks * 2));
        map.put("activeDates", activities.stream()
                .map(a -> a.getActivityDate().toString())
                .distinct()
                .collect(Collectors.toList()));
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
}

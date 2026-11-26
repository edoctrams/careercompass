package com.careercompass.careercompass;

import com.careercompass.careercompass.entity.*;
import com.careercompass.careercompass.repository.*;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:phaseftest;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=update"
})
class PhaseFFunctionalIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyCareerSkillRepository companyCareerSkillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillProgressHistoryRepository skillProgressHistoryRepository;

    @Autowired
    private QuestionSetRepository questionSetRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionSubtopicRepository questionSubtopicRepository;

    @Autowired
    private QuestionCompanyRepository questionCompanyRepository;

    @Autowired
    private QuestionAttemptRepository questionAttemptRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private RoadmapTaskRepository roadmapTaskRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    private MockHttpSession createAndLoginUser(String email, CareerPath targetRole) throws Exception {
        User user = new User();
        user.setName("Phase F Student");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Secret123!"));
        user = userRepository.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        profile.setBranch("CSE");
        profile.setCurrentYear(3);
        profile.setGraduationYear(2027);
        profile.setDailyAvailableHours(3);
        profile.setTargetRole(targetRole);
        studentProfileRepository.save(profile);

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", email.toUpperCase())
                        .param("password", "Secret123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    private Skill ensureSkill(String name, String category, String nodeType) {
        return skillRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Skill s = new Skill();
            s.setName(name);
            s.setCategory(category);
            s.setNodeType(nodeType);
            return skillRepository.save(s);
        });
    }

    private CareerPath ensureCareer(String name) {
        return careerPathRepository.findByName(name).orElseGet(() -> {
            CareerPath cp = new CareerPath();
            cp.setName(name);
            cp.setDescription(name + " path");
            cp.setAveragePreparationMonths(4);
            return careerPathRepository.save(cp);
        });
    }

    @Test
    void stageF1_assessmentPersistsOnlyAssessedSkillsAndRecordsHistory() throws Exception {
        CareerPath sde = ensureCareer("SDE-" + UUID.randomUUID());
        Skill javaSkill = ensureSkill("Java-" + UUID.randomUUID(), "PROGRAMMING", "SUBTOPIC");
        Skill sqlSkill = ensureSkill("SQL-" + UUID.randomUUID(), "CORE_CS", "SUBTOPIC");
        Skill unassessedSkill = ensureSkill("Docker-" + UUID.randomUUID(), "TOOLS", "SUBTOPIC");

        CareerSkill csJava = new CareerSkill();
        csJava.setCareerPath(sde);
        csJava.setSkill(javaSkill);
        csJava.setImportance(5);
        csJava.setRequiredLevel(4);
        careerSkillRepository.save(csJava);

        CareerSkill csSql = new CareerSkill();
        csSql.setCareerPath(sde);
        csSql.setSkill(sqlSkill);
        csSql.setImportance(4);
        csSql.setRequiredLevel(3);
        careerSkillRepository.save(csSql);

        String email = "f1." + UUID.randomUUID() + "@test.edu";
        MockHttpSession session = createAndLoginUser(email, sde);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();

        // First assessment with case-insensitive skill name
        mockMvc.perform(post("/api/skill-assessment/assess")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"" + javaSkill.getName().toLowerCase() + "\":2,\"" + sqlSkill.getName() + "\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSkillsAssessed").value(2))
                .andExpect(jsonPath("$.averageSkillGap").value(1.0))
                .andExpect(jsonPath("$.skillDetails.length()").value(2));

        assertTrue(userSkillRepository.findByUserIdAndSkillId(user.getId(), javaSkill.getId()).isPresent());
        assertEquals(2, userSkillRepository.findByUserIdAndSkillId(user.getId(), javaSkill.getId()).get().getProficiency());
        assertTrue(userSkillRepository.findByUserIdAndSkillId(user.getId(), sqlSkill.getId()).isPresent());
        assertTrue(userSkillRepository.findByUserIdAndSkillId(user.getId(), unassessedSkill.getId()).isEmpty());

        List<SkillProgressHistory> javaHistory = skillProgressHistoryRepository.findByUserIdAndSkillIdOrderByRecordedAtDesc(
                user.getId(), javaSkill.getId());
        assertEquals(1, javaHistory.size());
        assertNull(javaHistory.get(0).getPreviousLevel());
        assertEquals(2, javaHistory.get(0).getNewLevel());

        // Second assessment updates Java from 2 -> 4
        mockMvc.perform(post("/api/skill-assessment/assess")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"" + javaSkill.getName() + "\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSkillsAssessed").value(1));

        assertEquals(4, userSkillRepository.findByUserIdAndSkillId(user.getId(), javaSkill.getId()).get().getProficiency());
        javaHistory = skillProgressHistoryRepository.findByUserIdAndSkillIdOrderByRecordedAtDesc(
                user.getId(), javaSkill.getId());
        assertEquals(2, javaHistory.size());
        assertEquals(2, javaHistory.get(0).getPreviousLevel());
        assertEquals(4, javaHistory.get(0).getNewLevel());
    }

    @Test
    void stageF2_questionAttemptSummaryAndFilteringWork() throws Exception {
        CareerPath dataAnalyst = ensureCareer("Data Analyst-" + UUID.randomUUID());
        Skill sqlSubtopic = ensureSkill("Window Functions-" + UUID.randomUUID(), "CORE_CS", "SUBTOPIC");

        CareerSkill cs = new CareerSkill();
        cs.setCareerPath(dataAnalyst);
        cs.setSkill(sqlSubtopic);
        cs.setImportance(5);
        cs.setRequiredLevel(4);
        careerSkillRepository.save(cs);

        Company company = new Company();
        company.setName("Company-" + UUID.randomUUID());
        company.setIndustry("Tech");
        company = companyRepository.save(company);

        String email = "f2." + UUID.randomUUID() + "@test.edu";
        MockHttpSession session = createAndLoginUser(email, dataAnalyst);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();

        QuestionSet qs = new QuestionSet();
        qs.setTitle("Data Analyst Practice");
        qs.setDifficulty("Medium");
        qs.setCareerPath(dataAnalyst);
        qs.setCreatedBy(user);
        qs = questionSetRepository.save(qs);

        Question q1 = new Question();
        q1.setQuestionSet(qs);
        q1.setSkill(sqlSubtopic);
        q1.setQuestionText("Which SQL clause defines a window frame?");
        q1.setAnswer("B) OVER()");
        q1.setExplanation("OVER() defines the window partition and ordering.");
        q1.setDifficulty("Medium");
        q1.setQuestionType(QuestionType.MCQ);
        q1 = questionRepository.save(q1);

        questionSubtopicRepository.save(new QuestionSubtopic(q1, sqlSubtopic));
        questionCompanyRepository.save(new QuestionCompany(q1, company, "GENERAL", null));

        // Filter by career, subtopic, company, difficulty, type
        mockMvc.perform(get("/api/questions/filter")
                        .session(session)
                        .param("careerPathId", dataAnalyst.getId().toString())
                        .param("subtopicId", sqlSubtopic.getId().toString())
                        .param("companyId", company.getId().toString())
                        .param("difficulty", "Medium")
                        .param("questionType", "MCQ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(q1.getId()))
                .andExpect(jsonPath("$[0].subtopics[0].name").value(sqlSubtopic.getName()))
                .andExpect(jsonPath("$[0].companies[0].name").value(company.getName()));

        // Filter using mine=true
        mockMvc.perform(get("/api/questions/filter")
                        .session(session)
                        .param("mine", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(q1.getId()));

        // Submit correct attempt ("B" matches "B) OVER()") and then incorrect attempt
        mockMvc.perform(post("/api/questions/" + q1.getId() + "/attempt")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedAnswer\":\"B\",\"timeSpentSeconds\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isCorrect").value(true));

        mockMvc.perform(post("/api/questions/" + q1.getId() + "/attempt")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedAnswer\":\"A\",\"timeSpentSeconds\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCorrect").value(false));

        assertEquals(2, questionAttemptRepository.findByUserIdOrderByAttemptedAtDesc(user.getId()).size());

        mockMvc.perform(get("/api/questions/attempts/summary")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttempts").value(2))
                .andExpect(jsonPath("$.uniqueQuestionsAttempted").value(1))
                .andExpect(jsonPath("$.correctAttempts").value(1))
                .andExpect(jsonPath("$.accuracyPercent").value(50.0));
    }

    @Test
    void stageF3_personalizedRoadmapRespectsGapsPrerequisitesCompanyAndPreservesCompleted() throws Exception {
        CareerPath sde = ensureCareer("SDE-F3-" + UUID.randomUUID());
        Skill prereqSkill = ensureSkill("CoreJava-" + UUID.randomUUID(), "PROGRAMMING", "SUBTOPIC");
        Skill dependentSkill = ensureSkill("SpringBoot-" + UUID.randomUUID(), "FRAMEWORK", "SUBTOPIC");

        CareerSkill csPrereq = new CareerSkill();
        csPrereq.setCareerPath(sde);
        csPrereq.setSkill(prereqSkill);
        csPrereq.setImportance(4);
        csPrereq.setRequiredLevel(4);
        careerSkillRepository.save(csPrereq);

        CareerSkill csDependent = new CareerSkill();
        csDependent.setCareerPath(sde);
        csDependent.setSkill(dependentSkill);
        csDependent.setImportance(5);
        csDependent.setRequiredLevel(4);
        csDependent.setPrerequisiteSkill(prereqSkill);
        careerSkillRepository.save(csDependent);

        Company amazon = new Company();
        amazon.setName("Amazon-" + UUID.randomUUID());
        amazon.setIndustry("Cloud & E-Commerce");
        amazon = companyRepository.save(amazon);

        CompanyCareerSkill ccs = new CompanyCareerSkill();
        ccs.setCompany(amazon);
        ccs.setCareerPath(sde);
        ccs.setSkill(dependentSkill);
        ccs.setTargetLevel(5);
        ccs.setImportance(5);
        ccs.setEvidenceType("GENERAL");
        ccs.setReviewedAt(LocalDate.now());
        companyCareerSkillRepository.save(ccs);

        LearningResource lr = new LearningResource();
        lr.setSkill(prereqSkill);
        lr.setTitle("Java Official Docs");
        lr.setResourceUrl("https://docs.oracle.com/en/java/");
        lr.setResourceType("DOCS");
        lr = learningResourceRepository.save(lr);

        String email = "f3." + UUID.randomUUID() + "@test.edu";
        MockHttpSession session = createAndLoginUser(email, sde);
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();

        // Assess prereq at 1, dependent at 1
        mockMvc.perform(post("/api/skill-assessment/assess")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"" + prereqSkill.getName() + "\":1,\"" + dependentSkill.getName() + "\":1}"))
                .andExpect(status().isOk());

        // Generate company-targeted roadmap
        MvcResult genResult = mockMvc.perform(post("/api/roadmaps/generate")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"careerPathName\":\"" + sde.getName() + "\",\"companyName\":\"" + amazon.getName() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.companyName").value(amazon.getName()))
                .andReturn();

        List<Roadmap> roadmaps = roadmapRepository.findByUserId(user.getId());
        assertEquals(1, roadmaps.size());
        Long roadmapId = roadmaps.get(0).getId();

        List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmapId(roadmapId);
        assertEquals(2, tasks.size());
        // Prerequisite must appear before dependent skill
        assertEquals(prereqSkill.getId(), tasks.get(0).getSkill().getId());
        assertEquals(dependentSkill.getId(), tasks.get(1).getSkill().getId());
        // Learning resource linked on prereq
        assertEquals(lr.getResourceUrl(), tasks.get(0).getResourceUrl());
        // Company target level (5) reflected on dependent task title
        assertTrue(tasks.get(1).getTitle().contains("-> 5"));

        // Verify /api/roadmaps/current returns structured gap, priorityScore, prerequisiteSkillName, resources and does NOT leak passwordHash
        MvcResult currentRoadmapRes = mockMvc.perform(get("/api/roadmaps/current")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasRoadmap").value(true))
                .andExpect(jsonPath("$.tasks[0].skillGap").value(3))
                .andExpect(jsonPath("$.tasks[0].priorityScore").isNumber())
                .andExpect(jsonPath("$.tasks[0].resources[0].url").value("https://docs.oracle.com/en/java/"))
                .andExpect(jsonPath("$.tasks[1].prerequisiteSkillName").value(prereqSkill.getName()))
                .andReturn();
        assertFalse(currentRoadmapRes.getResponse().getContentAsString().contains("passwordHash"));

        // Verify /api/companies/preparation returns focus skills with gaps for target company
        mockMvc.perform(get("/api/companies/preparation")
                        .session(session)
                        .param("name", amazon.getName())
                        .param("careerPathName", sde.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value(amazon.getName()))
                .andExpect(jsonPath("$.focusSkills[0].skillName").value(dependentSkill.getName()))
                .andExpect(jsonPath("$.focusSkills[0].targetLevel").value(5))
                .andExpect(jsonPath("$.focusSkills[0].skillGap").value(4));

        // Mark first task COMPLETED
        Long completedTaskId = tasks.get(0).getId();
        mockMvc.perform(post("/api/roadmaps/tasks/" + completedTaskId + "/status")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Re-generate same career + company roadmap: must reuse roadmap and preserve COMPLETED task
        mockMvc.perform(post("/api/roadmaps/generate")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"careerPathName\":\"" + sde.getName() + "\",\"companyName\":\"" + amazon.getName() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roadmapId").value(roadmapId));

        assertEquals(1, roadmapRepository.findByUserId(user.getId()).stream()
                .filter(r -> r.getStatus() == RoadmapStatus.ACTIVE).count());
        assertEquals(RoadmapTaskStatus.COMPLETED, roadmapTaskRepository.findById(completedTaskId).orElseThrow().getStatus());
    }
}

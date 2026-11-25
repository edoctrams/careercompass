package com.careercompass.careercompass.config;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.careercompass.careercompass.entity.CareerPath;
import com.careercompass.careercompass.entity.CareerSkill;
import com.careercompass.careercompass.entity.Company;
import com.careercompass.careercompass.entity.InterviewExperience;
import com.careercompass.careercompass.entity.Question;
import com.careercompass.careercompass.entity.QuestionSet;
import com.careercompass.careercompass.entity.QuestionType;
import com.careercompass.careercompass.entity.Skill;
import com.careercompass.careercompass.entity.User;
import com.careercompass.careercompass.repository.CareerPathRepository;
import com.careercompass.careercompass.repository.CareerSkillRepository;
import com.careercompass.careercompass.repository.CompanyRepository;
import com.careercompass.careercompass.repository.InterviewExperienceRepository;
import com.careercompass.careercompass.repository.QuestionRepository;
import com.careercompass.careercompass.repository.QuestionSetRepository;
import com.careercompass.careercompass.repository.SkillRepository;
import com.careercompass.careercompass.repository.UserRepository;

@Configuration
public class CareerCompassDataInitializer {

    @Bean
    CommandLineRunner seedCareerAndSkillData(CareerPathRepository careers,
                                             SkillRepository skills,
                                             CareerSkillRepository mappings,
                                             CompanyRepository companies,
                                             QuestionSetRepository questionSets,
                                             QuestionRepository questions,
                                             InterviewExperienceRepository experiences,
                                             UserRepository users,
                                             PasswordEncoder passwordEncoder,
                                             PlatformTransactionManager transactionManager) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        return args -> transaction.executeWithoutResult(status -> {
            seedCareersAndSkills(careers, skills, mappings);
            seedCompaniesAndQuestions(companies, questionSets, questions, experiences, users, passwordEncoder, careers, skills);
        });
    }

    void seedCareersAndSkills(CareerPathRepository careers, SkillRepository skills, CareerSkillRepository mappings) {
        Map<String, String> careerDescriptions = Map.of(
                "Software Development Engineer (SDE)", "Build, test, and maintain robust software products, algorithms, and distributed systems.",
                "Backend Developer", "Design scalable server-side architectures, microservices, databases, and secure REST APIs.",
                "Frontend Developer", "Architect responsive, performant user interfaces, design systems, and modern web applications.",
                "Full Stack Developer", "Master end-to-end web engineering from responsive frontends to scalable cloud backends and databases.",
                "AI/ML Engineer", "Develop machine learning systems, deep neural networks, and deploy AI solutions at scale.",
                "Data Scientist", "Use statistical modeling, machine learning, and exploratory analysis to extract actionable insights.",
                "Data Analyst", "Transform complex business datasets into intuitive dashboards, reports, and strategic insights.",
                "Cybersecurity Engineer", "Protect infrastructure, conduct vulnerability assessments, and secure enterprise networks and applications.");

        careerDescriptions.forEach((name, description) -> {
            CareerPath career = careers.findByName(name).orElseGet(() -> {
                CareerPath value = new CareerPath();
                value.setName(name);
                value.setDescription(description);
                value.setDifficulty("Intermediate");
                value.setAveragePreparationMonths(6);
                return careers.save(value);
            });
            if (!description.equals(career.getDescription())) {
                career.setDescription(description);
                careers.save(career);
            }
        });

        Map<String, String> catalogue = Map.ofEntries(
                Map.entry("Java", "Programming"),
                Map.entry("C++", "Programming"),
                Map.entry("Python", "Programming"),
                Map.entry("JavaScript", "Programming"),
                Map.entry("DSA", "Computer Science"),
                Map.entry("OOP", "Computer Science"),
                Map.entry("DBMS", "Computer Science"),
                Map.entry("SQL", "Data"),
                Map.entry("Operating Systems", "Computer Science"),
                Map.entry("Computer Networks", "Computer Science"),
                Map.entry("Git", "Tools"),
                Map.entry("GitHub", "Tools"),
                Map.entry("Linux", "Tools"),
                Map.entry("Docker", "Tools"),
                Map.entry("HTML", "Web Development"),
                Map.entry("CSS", "Web Development"),
                Map.entry("React", "Web Development"),
                Map.entry("Spring Boot", "Backend"),
                Map.entry("REST APIs", "Backend"),
                Map.entry("Machine Learning", "AI/ML"),
                Map.entry("Deep Learning", "AI/ML"),
                Map.entry("Statistics", "Data Science"),
                Map.entry("Data Analysis", "Data"),
                Map.entry("NumPy", "Data Science"),
                Map.entry("Pandas", "Data Science"),
                Map.entry("Scikit-learn", "AI/ML"),
                Map.entry("System Design", "Computer Science"),
                Map.entry("Network Security", "Cybersecurity"),
                Map.entry("Cryptography", "Cybersecurity"),
                Map.entry("Communication", "Soft Skills")
        );

        catalogue.forEach((name, category) -> skills.findByNameIgnoreCase(name).orElseGet(() -> {
            Skill skill = new Skill();
            skill.setName(name);
            skill.setCategory(category);
            skill.setDifficulty("Beginner");
            return skills.save(skill);
        }));

        Map<String, Integer> sde = Map.of(
                "Java", 5, "C++", 4, "DSA", 5, "OOP", 4,
                "DBMS", 3, "Operating Systems", 3, "Computer Networks", 3, "Git", 3, "System Design", 4);

        Map<String, Integer> backend = Map.of(
                "Java", 5, "Spring Boot", 5, "REST APIs", 5, "SQL", 4,
                "DBMS", 4, "Git", 3, "OOP", 4, "Computer Networks", 3, "System Design", 4);

        Map<String, Integer> frontend = Map.of(
                "HTML", 5, "CSS", 5, "JavaScript", 5, "React", 5,
                "Git", 4, "REST APIs", 4);

        Map<String, Integer> fullstack = Map.of(
                "HTML", 5, "CSS", 4, "JavaScript", 5, "React", 5,
                "Java", 4, "Spring Boot", 4, "REST APIs", 5, "SQL", 4, "Git", 4);

        Map<String, Integer> ai = Map.of(
                "Python", 5, "DSA", 3, "Statistics", 5, "Machine Learning", 5,
                "Deep Learning", 4, "NumPy", 4, "Pandas", 4, "Scikit-learn", 5, "SQL", 3);

        Map<String, Integer> dataSci = Map.of(
                "Python", 5, "Statistics", 5, "SQL", 4, "Data Analysis", 5,
                "NumPy", 4, "Pandas", 5, "Scikit-learn", 4, "Machine Learning", 4);

        Map<String, Integer> analyst = Map.of(
                "SQL", 5, "Data Analysis", 5, "Statistics", 4,
                "Python", 3, "Pandas", 4);

        Map<String, Integer> cyber = Map.of(
                "Operating Systems", 5, "Computer Networks", 5, "Linux", 5,
                "Network Security", 5, "Cryptography", 4, "Python", 4);

        map(careers, skills, mappings, "Software Development Engineer (SDE)", sde);
        map(careers, skills, mappings, "Backend Developer", backend);
        map(careers, skills, mappings, "Frontend Developer", frontend);
        map(careers, skills, mappings, "Full Stack Developer", fullstack);
        map(careers, skills, mappings, "AI/ML Engineer", ai);
        map(careers, skills, mappings, "Data Scientist", dataSci);
        map(careers, skills, mappings, "Data Analyst", analyst);
        map(careers, skills, mappings, "Cybersecurity Engineer", cyber);
    }

    private void map(CareerPathRepository careers, SkillRepository skills, CareerSkillRepository mappings,
                     String careerName, Map<String, Integer> requirements) {
        CareerPath career = careers.findByName(careerName).orElse(null);
        if (career == null) return;
        requirements.forEach((skillName, level) -> {
            skills.findByNameIgnoreCase(skillName).ifPresent(skill -> {
                if (!mappings.existsByCareerPathIdAndSkillId(career.getId(), skill.getId())) {
                    CareerSkill mapping = new CareerSkill();
                    mapping.setCareerPath(career);
                    mapping.setSkill(skill);
                    mapping.setImportance(level);
                    mapping.setRequiredLevel(level);
                    mappings.save(mapping);
                }
            });
        });
    }

    private void seedCompaniesAndQuestions(CompanyRepository companies,
                                          QuestionSetRepository questionSets,
                                          QuestionRepository questions,
                                          InterviewExperienceRepository experiences,
                                          UserRepository users,
                                          PasswordEncoder passwordEncoder,
                                          CareerPathRepository careers,
                                          SkillRepository skills) {
        
if (companies.count() > 0) {

    Company amazon = findOrCreateCompany(
            companies,
            "Amazon",
            "E-commerce, AWS cloud computing and technology.",
            "https://www.amazon.jobs/",
            "Cloud Computing & E-Commerce"
    );

    Company microsoft = findOrCreateCompany(
            companies,
            "Microsoft",
            "Software, Azure cloud and enterprise technology.",
            "https://careers.microsoft.com/",
            "Software & Cloud"
    );

    Company tcs = findOrCreateCompany(
            companies,
            "TCS",
            "IT services, consulting and enterprise technology.",
            "https://www.tcs.com/careers",
            "IT Services & Consulting"
    );

    seedTcsQuestionSetIfMissing(
            tcs, questionSets, questions, users,
            passwordEncoder, careers, skills
    );

    return;
}


        Company google = createCompany(companies, "Google",
                "Multinational technology leader specializing in search, cloud, AI, and consumer electronics.",
                "https://careers.google.com", "Internet & Cloud Computing");

        Company microsoft = createCompany(companies, "Microsoft",
                "Global leader in software platforms, Azure cloud services, enterprise solutions, and AI.",
                "https://careers.microsoft.com", "Software & Cloud");

        Company amazon = createCompany(companies, "Amazon",
                "World leader in e-commerce, AWS distributed cloud infrastructure, and logistics technology.",
                "https://amazon.jobs", "Cloud Computing & E-Commerce");

        Company atlassian = createCompany(companies, "Atlassian",
                "Leading enterprise collaboration software company behind Jira, Confluence, Bitbucket, and Trello.",
                "https://www.atlassian.com/company/careers", "Software Development");

        Company cisco = createCompany(companies, "Cisco",
                "Worldwide technology leader that powers the Internet through networking hardware and cybersecurity.",
                "https://jobs.cisco.com", "Networking & Cybersecurity");

        Company adobe = createCompany(companies, "Adobe",
                "Industry standard in creative media software, digital experience platforms, and document cloud.",
                "https://www.adobe.com/careers.html", "Digital Media & Cloud");
        
        Company tcs = createCompany(
        companies,
        "TCS",
        "IT services, consulting and enterprise technology.",
        "https://www.tcs.com/careers",
        "IT Services & Consulting"
);

        User systemUser = users.findByEmail("admin@careercompass.io").orElseGet(() -> {
            User u = new User();
            u.setName("CareerCompass Placement Lead");
            u.setEmail("admin@careercompass.io");
            u.setPasswordHash(passwordEncoder.encode("CompassSecure2026!"));
            return users.save(u);
        });

        CareerPath sdeRole = careers.findByName("Software Development Engineer (SDE)").orElse(null);
        Skill dsaSkill = skills.findByNameIgnoreCase("DSA").orElse(null);
        Skill oopSkill = skills.findByNameIgnoreCase("OOP").orElse(null);
        Skill dbmsSkill = skills.findByNameIgnoreCase("DBMS").orElse(null);

        seedTcsQuestionSetIfMissing(
        tcs, questionSets, questions, users,
        passwordEncoder, careers, skills
); 

        QuestionSet dsaSet = new QuestionSet();
        dsaSet.setTitle("Tier-1 DSA & Algorithms Placement Question Set");
        dsaSet.setCompany(google);
        dsaSet.setCareerPath(sdeRole);
        dsaSet.setBranch("Computer Science / Engineering");
        dsaSet.setBatch("2024-2027");
        dsaSet.setDifficulty("Medium");
        dsaSet.setCreatedBy(systemUser);
        questionSets.save(dsaSet);

        createQuestion(questions, dsaSet, dsaSkill,
                "What is the average time complexity of searching an element in a balanced Binary Search Tree (AVL / Red-Black Tree)?\n" +
                "A) O(1)\nB) O(log N)\nC) O(N)\nD) O(N log N)",
                "B) O(log N). A balanced BST maintains height proportional to log2(N), guaranteeing O(log N) lookup.",
                "Medium", QuestionType.MCQ);

        createQuestion(questions, dsaSet, dsaSkill,
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\n" +
                "Constraints:\n- 2 <= nums.length <= 10^4\n- -10^9 <= nums[i] <= 10^9\n- Only one valid answer exists.\n\n" +
                "Example:\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]",
                "Use a Hash Map storing value -> index. Iterate through the array. For each num, calculate complement = target - num. If complement exists in map, return indices. Time: O(N), Space: O(N).",
                "Easy", QuestionType.CODING);

        createQuestion(questions, dsaSet, oopSkill,
                "Explain the four core pillars of Object-Oriented Programming and describe how Runtime Polymorphism is achieved in Java.",
                "1. Encapsulation: bundling data and methods with access controls.\n2. Abstraction: exposing only necessary interfaces while hiding internal details.\n3. Inheritance: acquiring properties from base classes.\n4. Polymorphism: single interface with multiple implementations. In Java, Runtime Polymorphism is achieved through method overriding via dynamic method dispatch (vtable).",
                "Medium", QuestionType.TECHNICAL);

        createQuestion(questions, dsaSet, null,
                "Tell me about a time when you encountered a major technical roadblock during a project. How did you diagnose and overcome it?",
                "STAR Response Framework:\n- Situation: Describe the project context and deadline.\n- Task: Define your specific engineering responsibility.\n- Action: Detail the systematic debugging, log inspection, documentation review, or team collaboration.\n- Result: Quantifiable outcome (e.g. reduced latency by 30%, met launch deadline) and lessons learned.",
                "Medium", QuestionType.HR);

        QuestionSet amazonSet = new QuestionSet();
        amazonSet.setTitle("Amazon SDE Technical & Leadership Principles Kit");
        amazonSet.setCompany(amazon);
        amazonSet.setCareerPath(sdeRole);
        amazonSet.setBranch("Computer Science / IT");
        amazonSet.setBatch("2024-2027");
        amazonSet.setDifficulty("Hard");
        amazonSet.setCreatedBy(systemUser);
        questionSets.save(amazonSet);

        createQuestion(questions, amazonSet, dbmsSkill,
                "In relational databases, which isolation level prevents Dirty Reads, Non-repeatable Reads, and Phantom Reads completely?\n" +
                "A) Read Uncommitted\nB) Read Committed\nC) Repeatable Read\nD) Serializable",
                "D) Serializable. It provides strict serial execution semantics, completely preventing phantom reads and non-repeatable reads.",
                "Medium", QuestionType.MCQ);

        createQuestion(questions, amazonSet, dsaSkill,
                "Design and implement a Data Structure for Least Recently Used (LRU) Cache with get(key) and put(key, value) operations running in O(1) time.",
                "Combine a Doubly Linked List with a Hash Map. The Hash Map stores key -> Node reference for O(1) lookup. The Doubly Linked List keeps nodes in order of access; most recently accessed node is moved to the head, and LRU node at the tail is evicted when capacity is reached.",
                "Hard", QuestionType.CODING);

        createExperience(experiences, google, systemUser, "Software Development Engineer (Campus)", "2024",
                "The hiring process had 4 rounds:\n" +
                "1. Online Assessment: 2 Medium DSA problems on dynamic programming and graph BFS.\n" +
                "2. Technical Round 1: Interval scheduling and tree traversal with deep focus on time/space tradeoffs.\n" +
                "3. Technical Round 2: Design an in-memory rate limiter with concurrency considerations.\n" +
                "4. Googleyness & Behavioral: Cultural fit, feedback handling, and teamwork.\n\n" +
                "Tip: Clean, modular code and explaining your thinking out loud are heavily evaluated.",
                LocalDate.now().minusMonths(3), "Hard");

        createExperience(experiences, microsoft, systemUser, "Software Engineer Intern / FTE", "2024",
                "Rounds included:\n" +
                "Round 1: Coding test on arrays and string manipulation.\n" +
                "Round 2: Data structures (Linked lists, binary search) and OS fundamentals.\n" +
                "Round 3: System architecture, databases, and explaining past academic projects.\n" +
                "Round 4: Director / HR interview on motivation and leadership.\n\n" +
                "Preparation Advice: Master standard LeetCode mediums and know your resume projects thoroughly.",
                LocalDate.now().minusMonths(2), "Medium");

        createExperience(experiences, amazon, systemUser, "SDE-1", "2024",
                "The interview had 1 OA and 3 technical rounds focusing on DSA and Amazon's 16 Leadership Principles.\n" +
                "Every technical round had 20 minutes dedicated to LP questions using the STAR format.\n" +
                "Coding problems involved Trie search and Top-K elements using Min-Heap.\n\n" +
                "Key takeaway: Prepare 2-3 genuine project stories aligned with Customer Obsession and Bias for Action.",
                LocalDate.now().minusMonths(1), "Hard");
    }

private Company findOrCreateCompany(
        CompanyRepository repo,
        String name,
        String description,
        String website,
        String industry) {

    return repo.findByNameIgnoreCase(name)
            .orElseGet(() -> createCompany(
                    repo, name, description, website, industry
            ));
}

private void seedTcsQuestionSetIfMissing(
        Company tcs,
        QuestionSetRepository questionSets,
        QuestionRepository questions,
        UserRepository users,
        PasswordEncoder passwordEncoder,
        CareerPathRepository careers,
        SkillRepository skills) {

    String title = "TCS General Placement Practice";

    boolean alreadyExists = questionSets
            .findByCompanyId(tcs.getId())
            .stream()
            .anyMatch(set -> title.equals(set.getTitle()));

    if (alreadyExists) {
        return;
    }

    User systemUser = users.findByEmail("admin@careercompass.io")
            .orElseGet(() -> {
                User user = new User();
                user.setName("CareerCompass Placement Lead");
                user.setEmail("admin@careercompass.io");
                user.setPasswordHash(
                        passwordEncoder.encode("CompassSecure2026!")
                );
                return users.save(user);
            });

    CareerPath sdeRole = careers.findByName(
            "Software Development Engineer (SDE)"
    ).orElse(null);

    Skill dsa = skills.findByNameIgnoreCase("DSA").orElse(null);
    Skill oop = skills.findByNameIgnoreCase("OOP").orElse(null);

    QuestionSet set = new QuestionSet();
    set.setTitle(title);
    set.setCompany(tcs);
    set.setCareerPath(sdeRole);
    set.setBranch("Computer Science / IT");
    set.setBatch("General Practice");
    set.setDifficulty("Medium");
    set.setCreatedBy(systemUser);

    questionSets.save(set);

    createQuestion(
            questions, set, dsa,
            "Which Java collection stores unique elements without guaranteeing insertion order?\n"
                    + "A) ArrayList\nB) HashSet\nC) LinkedList\nD) Vector",
            "B) HashSet. It stores unique elements and does not guarantee iteration order.",
            "Easy", QuestionType.MCQ
    );

    createQuestion(
            questions, set, dsa,
            "Given an integer array and a target, return the indices of two numbers that add up to the target.",
            "Use a HashMap to store previously visited values and their indices. Look up target minus the current value. Expected time complexity: O(N).",
            "Medium", QuestionType.CODING
    );

    createQuestion(
            questions, set, oop,
            "Explain the four pillars of Object-Oriented Programming and how method overriding supports runtime polymorphism in Java.",
            "The four pillars are encapsulation, abstraction, inheritance and polymorphism. Runtime polymorphism is achieved through method overriding and dynamic method dispatch.",
            "Medium", QuestionType.TECHNICAL
    );
}

    private Company createCompany(CompanyRepository repo, String name, String desc, String website, String industry) {
        Company c = new Company();
        c.setName(name);
        c.setDescription(desc);
        c.setWebsite(website);
        c.setIndustry(industry);
        return repo.save(c);
    }

    private void createQuestion(QuestionRepository repo, QuestionSet qs, Skill skill, String text, String answer, String difficulty, QuestionType type) {
        Question q = new Question();
        q.setQuestionSet(qs);
        q.setSkill(skill);
        q.setQuestionText(text);
        q.setAnswer(answer);
        q.setDifficulty(difficulty);
        q.setQuestionType(type);
        repo.save(q);
    }

    private void createExperience(InterviewExperienceRepository repo, Company company, User user, String role, String batch, String text, LocalDate date, String difficulty) {
        InterviewExperience exp = new InterviewExperience();
        exp.setCompany(company);
        exp.setUser(user);
        exp.setRole(role);
        exp.setBatch(batch);
        exp.setExperienceText(text);
        exp.setInterviewDate(date);
        exp.setDifficulty(difficulty);
        repo.save(exp);
    }
}

-- Career Compass Phase D: Company-Specific Skill Requirements (company_career_skills)
-- Covers 3 MVP companies (2=Microsoft, 3=Amazon, 7=TCS) x 4 MVP roles (1=AI/ML, 3=Data Analyst, 4=SDE, 6=Full Stack).
-- Enforces curriculum integrity: only skills already mapped in career_skills for that career_path_id are inserted.
-- Evidence types: OFFICIAL, JOB_DESCRIPTION, REPORTED, GENERAL.

DO $$
BEGIN
    CREATE TEMP TABLE tmp_ccs (
        company_id BIGINT NOT NULL,
        career_path_id BIGINT NOT NULL,
        skill_name VARCHAR(120) NOT NULL,
        target_level INTEGER NOT NULL,
        importance INTEGER NOT NULL,
        evidence_type VARCHAR(40) NOT NULL,
        source_url TEXT,
        reviewed_at DATE
    ) ON COMMIT DROP;

    -- =========================================================================
    -- 1. AMAZON (company_id = 3)
    -- =========================================================================
    -- Amazon x SDE (career_path_id = 4)
    INSERT INTO tmp_ccs VALUES
    (3, 4, 'DSA', 5, 5, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Arrays & Hashing', 5, 5, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Two Pointers & Sliding Window', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 4, 'Trees & Binary Search Trees', 5, 5, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Graphs & Shortest Paths', 5, 5, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Dynamic Programming & Recursion', 4, 4, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Time & Space Complexity (Big-O)', 5, 5, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'OOP', 4, 5, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'SOLID Design Principles', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 4, 'System Design', 4, 5, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Horizontal Scaling & Load Balancers', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 4, 'Caching Strategies & Redis', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 4, 'DBMS', 4, 4, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Operating Systems', 4, 4, 'OFFICIAL', 'https://www.amazon.jobs/en/landing_pages/software-development-topics', '2026-11-26'),
    (3, 4, 'Concurrency, Mutexes & Deadlocks', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- Amazon x Data Analyst (career_path_id = 3)
    INSERT INTO tmp_ccs VALUES
    (3, 3, 'SQL', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'SQL Joins & Subqueries', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Window Functions & CTEs', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Excel', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Pivot Tables & Pivot Charts', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Business Analytics', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Funnel, Cohort & Retention Analysis', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Business Metrics & Root-Cause Analysis', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Data Visualization', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Statistics', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Confidence Intervals & A/B Testing', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 3, 'Python', 3, 3, 'GENERAL', NULL, '2026-11-26');

    -- Amazon x Full Stack Developer (career_path_id = 6)
    INSERT INTO tmp_ccs VALUES
    (3, 6, 'JavaScript', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 6, 'React', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 6, 'Spring Boot', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 6, 'REST APIs', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 6, 'SQL', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 6, 'Docker Containers & Compose', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 6, 'Cloud Deployment & Reverse Proxies', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 6, 'Git', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- Amazon x AI/ML Engineer (career_path_id = 1)
    INSERT INTO tmp_ccs VALUES
    (3, 1, 'Python', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'Machine Learning', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'Deep Learning', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'Scikit-learn', 5, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'Statistics', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'Feature Engineering & Encoding', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'Evaluation Metrics (ROC-AUC, F1, RMSE)', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'MLOps', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (3, 1, 'Model Serving via FastAPI / REST', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- =========================================================================
    -- 2. MICROSOFT (company_id = 2)
    -- =========================================================================
    -- Microsoft x SDE (career_path_id = 4)
    INSERT INTO tmp_ccs VALUES
    (2, 4, 'DSA', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Arrays & Hashing', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Linked Lists & Stacks/Queues', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Trees & Binary Search Trees', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Graphs & Shortest Paths', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Dynamic Programming & Recursion', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'OOP', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Creational & Structural Design Patterns', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Operating Systems', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Processes, Threads & Scheduling', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'System Design', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'DBMS', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 4, 'Unit Testing & Mocking', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- Microsoft x Data Analyst (career_path_id = 3)
    INSERT INTO tmp_ccs VALUES
    (2, 3, 'SQL', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'SQL Joins & Subqueries', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'Window Functions & CTEs', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'Excel', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'Power Query & Spreadsheet Modeling', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'Interactive BI Dashboards (Power BI / Tableau)', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'KPI Design & Executive Reporting', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'Statistics', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 3, 'Python', 4, 3, 'GENERAL', NULL, '2026-11-26');

    -- Microsoft x Full Stack Developer (career_path_id = 6)
    INSERT INTO tmp_ccs VALUES
    (2, 6, 'JavaScript', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'React', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'State Management & Performance', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'REST APIs', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'Spring Boot', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'Spring Security & Session/JWT Auth', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'SQL', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'Git', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 6, 'Cloud Deployment & Reverse Proxies', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- Microsoft x AI/ML Engineer (career_path_id = 1)
    INSERT INTO tmp_ccs VALUES
    (2, 1, 'Python', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 1, 'Mathematics for ML', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 1, 'Machine Learning', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 1, 'Deep Learning', 5, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 1, 'Transformers & Attention Mechanisms', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 1, 'LLM Prompting & RAG Fundamentals', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (2, 1, 'MLOps', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (2, 1, 'Reproducible ML Pipelines', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- =========================================================================
    -- 3. TCS (company_id = 7)
    -- =========================================================================
    -- TCS x SDE (career_path_id = 4)
    INSERT INTO tmp_ccs VALUES
    (7, 4, 'Programming Fundamentals', 4, 5, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'Collections & Generics', 4, 5, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'OOP', 4, 5, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'Encapsulation, Inheritance & Polymorphism', 4, 5, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'DSA', 4, 4, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'Arrays & Hashing', 4, 5, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'Two Pointers & Sliding Window', 3, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 4, 'Linked Lists & Stacks/Queues', 3, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 4, 'DBMS', 4, 4, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'ER Modeling & Normalization', 4, 4, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'SQL', 4, 4, 'REPORTED', NULL, '2026-11-26'),
    (7, 4, 'Operating Systems', 3, 3, 'GENERAL', NULL, '2026-11-26'),
    (7, 4, 'Computer Networks', 3, 3, 'GENERAL', NULL, '2026-11-26'),
    (7, 4, 'System Design', 2, 2, 'GENERAL', NULL, '2026-11-26');

    -- TCS x Data Analyst (career_path_id = 3)
    INSERT INTO tmp_ccs VALUES
    (7, 3, 'SQL', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'SQL Joins & Subqueries', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Aggregations, GROUP BY & HAVING', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Excel', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Excel Lookup Functions (XLOOKUP, VLOOKUP)', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Pivot Tables & Pivot Charts', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Python', 3, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Pandas', 3, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Statistics', 3, 3, 'GENERAL', NULL, '2026-11-26'),
    (7, 3, 'Data Visualization', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- TCS x Full Stack Developer (career_path_id = 6)
    INSERT INTO tmp_ccs VALUES
    (7, 6, 'HTML', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'CSS', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'JavaScript', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'React', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'Spring Boot', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'Spring Data JPA & Hibernate', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'REST APIs', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'SQL', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 6, 'Git', 4, 4, 'GENERAL', NULL, '2026-11-26');

    -- TCS x AI/ML Engineer (career_path_id = 1)
    INSERT INTO tmp_ccs VALUES
    (7, 1, 'Python', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 1, 'Python Data Structures & Comprehensions', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 1, 'Pandas', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 1, 'NumPy', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 1, 'Statistics', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 1, 'Machine Learning', 4, 5, 'GENERAL', NULL, '2026-11-26'),
    (7, 1, 'Scikit-learn', 4, 4, 'GENERAL', NULL, '2026-11-26'),
    (7, 1, 'Deep Learning', 3, 3, 'GENERAL', NULL, '2026-11-26');

    -- Insert ONLY skills already mapped to the corresponding career_path_id in career_skills
    INSERT INTO company_career_skills (company_id, career_path_id, skill_id, target_level, importance, evidence_type, source_url, reviewed_at)
    SELECT t.company_id, t.career_path_id, s.id, t.target_level, t.importance, t.evidence_type, t.source_url, t.reviewed_at
    FROM tmp_ccs t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    JOIN career_skills cs ON cs.career_path_id = t.career_path_id AND cs.skill_id = s.id
    ON CONFLICT (company_id, career_path_id, skill_id) DO NOTHING;

    -- Record inserted rows in seed_manifest
    INSERT INTO seed_manifest (migration_version, table_name, record_key, record_id)
    SELECT 'V3', 'company_career_skills', ccs.company_id || ':' || ccs.career_path_id || ':' || ccs.skill_id, ccs.id
    FROM tmp_ccs t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    JOIN company_career_skills ccs ON ccs.company_id = t.company_id AND ccs.career_path_id = t.career_path_id AND ccs.skill_id = s.id
    ON CONFLICT (migration_version, table_name, record_key) DO NOTHING;
END $$;

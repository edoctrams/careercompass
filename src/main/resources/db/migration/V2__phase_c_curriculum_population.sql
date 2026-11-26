-- Career Compass Phase C: Structured Curriculum Population
-- Populates hierarchical topics/subtopics (skills) and career_skills mappings for the 4 MVP roles:
--   1 = AI/ML Engineer
--   3 = Data Analyst
--   4 = Software Development Engineer (SDE)
--   6 = Full Stack Developer
-- Preserves all existing skill IDs (1..41), career_path IDs, and foreign-key references.

DO $$
DECLARE
    -- Helper variables
    v_skill_id BIGINT;
    v_parent_id BIGINT;
    v_prereq_id BIGINT;
    v_cs_id BIGINT;

    -- Helper procedure-like inline logic is implemented via temporary tables for clarity and idempotency
BEGIN
    CREATE TEMP TABLE tmp_subtopics (
        name VARCHAR(120) NOT NULL,
        parent_name VARCHAR(120) NOT NULL,
        category VARCHAR(80) NOT NULL,
        difficulty VARCHAR(40) NOT NULL,
        description TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tmp_subtopics (name, parent_name, category, difficulty, description) VALUES
    -- SDE: DSA (6)
    ('Arrays & Hashing', 'DSA', 'Computer Science', 'Beginner', 'Contiguous arrays, hash maps, prefix sums, and frequency counting.'),
    ('Two Pointers & Sliding Window', 'DSA', 'Computer Science', 'Intermediate', 'Linear-time subarray and sequence optimization techniques.'),
    ('Linked Lists & Stacks/Queues', 'DSA', 'Computer Science', 'Beginner', 'Pointer manipulation, monotonic stacks, and deque patterns.'),
    ('Trees & Binary Search Trees', 'DSA', 'Computer Science', 'Intermediate', 'DFS/BFS tree traversals, BST invariants, and LCA problems.'),
    ('Graphs & Shortest Paths', 'DSA', 'Computer Science', 'Advanced', 'Adjacency lists, topological sort, Dijkstra, and union-find.'),
    ('Dynamic Programming & Recursion', 'DSA', 'Computer Science', 'Advanced', 'Memoization, tabulation, knapsack, and state-transition modeling.'),

    -- SDE: Operating Systems (4)
    ('Processes, Threads & Scheduling', 'Operating Systems', 'Computer Science', 'Intermediate', 'Context switching, CPU scheduling algorithms, and thread lifecycles.'),
    ('Concurrency, Mutexes & Deadlocks', 'Operating Systems', 'Computer Science', 'Advanced', 'Locks, semaphores, race conditions, and deadlock prevention.'),
    ('Memory Management & Paging', 'Operating Systems', 'Computer Science', 'Intermediate', 'Virtual memory, page tables, TLB, and page replacement policies.'),
    ('File Systems & I/O', 'Operating Systems', 'Computer Science', 'Intermediate', 'Inodes, buffering, disk scheduling, and system calls.'),

    -- SDE: DBMS (4)
    ('ER Modeling & Normalization', 'DBMS', 'Computer Science', 'Beginner', 'Entity-relationship design and 1NF through BCNF normalization.'),
    ('ACID Transactions & Concurrency Control', 'DBMS', 'Computer Science', 'Intermediate', 'Atomicity, isolation levels, MVCC, and two-phase locking.'),
    ('B-Tree Indexing & Query Optimization', 'DBMS', 'Computer Science', 'Intermediate', 'Clustered/non-clustered indexes and query execution plans.'),
    ('NoSQL vs Relational Storage', 'DBMS', 'Computer Science', 'Intermediate', 'Tradeoffs between relational engines, document stores, and key-value DBs.'),

    -- SDE: Computer Networks (4)
    ('OSI & TCP/IP Layers', 'Computer Networks', 'Computer Science', 'Beginner', 'Protocol stack responsibilities from physical layer to application layer.'),
    ('HTTP, HTTPS & TLS Handshake', 'Computer Networks', 'Computer Science', 'Intermediate', 'HTTP/1.1 vs HTTP/2, TLS certificates, and encrypted transport.'),
    ('DNS, Routing & Load Balancing', 'Computer Networks', 'Computer Science', 'Intermediate', 'DNS resolution, IP routing, NAT, and L4/L7 load balancing.'),
    ('Sockets & Network Troubleshooting', 'Computer Networks', 'Computer Science', 'Intermediate', 'TCP three-way handshake, UDP, ports, and latency diagnostics.'),

    -- SDE: OOP (4)
    ('Encapsulation, Inheritance & Polymorphism', 'OOP', 'Computer Science', 'Beginner', 'Core object-oriented modeling and runtime dispatch.'),
    ('SOLID Design Principles', 'OOP', 'Computer Science', 'Intermediate', 'Single responsibility, open-closed, Liskov, ISP, and DIP.'),
    ('Creational & Structural Design Patterns', 'OOP', 'Computer Science', 'Intermediate', 'Singleton, Factory, Builder, Adapter, and Decorator patterns.'),
    ('Exception Handling & Interfaces', 'OOP', 'Computer Science', 'Beginner', 'Clean error hierarchies, contracts, and abstract classes.'),

    -- SDE: System Design (4)
    ('Horizontal Scaling & Load Balancers', 'System Design', 'Computer Science', 'Intermediate', 'Stateless services, health checks, and consistent hashing.'),
    ('Caching Strategies & Redis', 'System Design', 'Computer Science', 'Intermediate', 'Cache-aside, write-through, TTLs, and LRU eviction.'),
    ('Database Sharding & Replication', 'System Design', 'Computer Science', 'Advanced', 'Read replicas, partitioning keys, and CAP theorem tradeoffs.'),
    ('Message Queues & Rate Limiting', 'System Design', 'Computer Science', 'Advanced', 'Async event pipelines, token bucket, and sliding window limiters.'),

    -- SDE: Programming Fundamentals (4)
    ('Time & Space Complexity (Big-O)', 'Programming Fundamentals', 'Programming', 'Beginner', 'Asymptotic runtime and memory analysis of algorithms.'),
    ('Bit Manipulation & Math Foundations', 'Programming Fundamentals', 'Programming', 'Intermediate', 'Bitwise masks, modular arithmetic, and prime sieves.'),
    ('Collections & Generics', 'Programming Fundamentals', 'Programming', 'Beginner', 'Lists, sets, maps, priority queues, and type-safe generics.'),
    ('Multithreading & Async Basics', 'Programming Fundamentals', 'Programming', 'Intermediate', 'Thread pools, futures, and non-blocking execution.'),

    -- SDE: Software Engineering (3)
    ('Unit Testing & Mocking', 'Software Engineering', 'Software Engineering', 'Intermediate', 'JUnit, assertions, test doubles, and regression safety.'),
    ('Code Reviews & Clean Architecture', 'Software Engineering', 'Software Engineering', 'Intermediate', 'Modular layering, naming, and maintainability standards.'),
    ('Agile & CI/CD Workflows', 'Software Engineering', 'Software Engineering', 'Beginner', 'Iterative delivery, automated builds, and deployment pipelines.'),

    -- Full Stack: HTML & CSS (4)
    ('Semantic HTML5 & Accessibility', 'HTML', 'Web Development', 'Beginner', 'Semantic landmarks, ARIA roles, and keyboard navigation.'),
    ('Web Forms & Input Validation', 'HTML', 'Web Development', 'Beginner', 'Native form controls, constraints, and accessible labels.'),
    ('Flexbox & CSS Grid Layouts', 'CSS', 'Web Development', 'Beginner', 'Modern two-dimensional and one-dimensional responsive layouts.'),
    ('Responsive Design & Media Queries', 'CSS', 'Web Development', 'Beginner', 'Mobile-first breakpoints, fluid units, and viewport scaling.'),

    -- Full Stack: JavaScript (4)
    ('ES6+ Syntax, Closures & Scope', 'JavaScript', 'Web Development', 'Beginner', 'Lexical scope, destructuring, arrow functions, and modules.'),
    ('DOM Manipulation & Events', 'JavaScript', 'Web Development', 'Beginner', 'Event delegation, selectors, and dynamic UI updates.'),
    ('Promises, Async/Await & Event Loop', 'JavaScript', 'Web Development', 'Intermediate', 'Microtasks, macrotasks, and asynchronous control flow.'),
    ('Fetch API & Browser Storage', 'JavaScript', 'Web Development', 'Intermediate', 'HTTP requests from browser, cookies, and localStorage.'),

    -- Full Stack: React (4)
    ('Components, Props & JSX', 'React', 'Web Development', 'Beginner', 'Declarative UI composition and unidirectional data flow.'),
    ('React Hooks (useState, useEffect)', 'React', 'Web Development', 'Intermediate', 'Component state, side effects, and custom hooks.'),
    ('Client-Side Routing & Forms', 'React', 'Web Development', 'Intermediate', 'Multi-view navigation, controlled inputs, and validation.'),
    ('State Management & Performance', 'React', 'Web Development', 'Advanced', 'Context API, memoization, and avoiding unnecessary renders.'),

    -- Full Stack: Spring Boot (4)
    ('Dependency Injection & IoC', 'Spring Boot', 'Backend', 'Beginner', 'Spring beans, constructor injection, and application context.'),
    ('Spring Data JPA & Hibernate', 'Spring Boot', 'Backend', 'Intermediate', 'Entity relationships, repositories, and transaction management.'),
    ('Spring Security & Session/JWT Auth', 'Spring Boot', 'Backend', 'Intermediate', 'Filter chains, BCrypt hashing, and endpoint authorization.'),
    ('Spring Boot Validation & Exception Handling', 'Spring Boot', 'Backend', 'Intermediate', 'Bean validation, @ControllerAdvice, and structured API errors.'),

    -- Full Stack: REST APIs (4)
    ('RESTful Resource Design & HTTP Verbs', 'REST APIs', 'Backend', 'Beginner', 'URI naming, GET/POST/PUT/DELETE semantics, and idempotency.'),
    ('Status Codes, Pagination & Filtering', 'REST APIs', 'Backend', 'Intermediate', 'Proper 2xx/4xx/5xx responses and scalable list endpoints.'),
    ('JSON Serialization & DTO Mapping', 'REST APIs', 'Backend', 'Intermediate', 'Request/response DTOs and avoiding entity leakage.'),
    ('API Documentation (OpenAPI/Swagger)', 'REST APIs', 'Backend', 'Beginner', 'Contract-first specs and interactive endpoint testing.'),

    -- Full Stack & Data Analyst: SQL (5)
    ('SQL Joins & Subqueries', 'SQL', 'Data', 'Beginner', 'INNER, LEFT, RIGHT, FULL joins, and correlated subqueries.'),
    ('Aggregations, GROUP BY & HAVING', 'SQL', 'Data', 'Beginner', 'Summary metrics, distinct counts, and grouped filtering.'),
    ('Window Functions & CTEs', 'SQL', 'Data', 'Intermediate', 'ROW_NUMBER, RANK, LAG/LEAD, and readable CTE pipelines.'),
    ('Views, Constraints & Schema Design', 'SQL', 'Data', 'Intermediate', 'Primary/foreign keys, check constraints, and views.'),
    ('SQL Query Tuning & Execution Plans', 'SQL', 'Data', 'Advanced', 'EXPLAIN ANALYZE, index usage, and avoiding full table scans.'),

    -- Full Stack: Git (3)
    ('Git Branching, Merging & Rebasing', 'Git', 'Tools', 'Beginner', 'Feature branches, fast-forward merges, and clean history.'),
    ('Pull Requests & Conflict Resolution', 'Git', 'Tools', 'Beginner', 'Resolving merge conflicts and reviewing diffs safely.'),
    ('Git Workflows & Release Tagging', 'Git', 'Tools', 'Intermediate', 'Trunk-based vs GitFlow branching and semantic tags.'),

    -- Full Stack: Deployment (3)
    ('Environment Variables & Secrets Management', 'Deployment', 'Tools', 'Beginner', '12-factor config, .env hygiene, and avoiding hardcoded secrets.'),
    ('Docker Containers & Compose', 'Deployment', 'Tools', 'Intermediate', 'Dockerfiles, multi-stage builds, and local service orchestration.'),
    ('Cloud Deployment & Reverse Proxies', 'Deployment', 'Tools', 'Intermediate', 'Health checks, TLS termination, and managed DB connectivity.'),

    -- Data Analyst & AI/ML: Python (4)
    ('Python Data Structures & Comprehensions', 'Python', 'Programming', 'Beginner', 'Lists, dicts, sets, tuples, and pythonic comprehensions.'),
    ('Functions, Modules & Virtual Environments', 'Python', 'Programming', 'Beginner', 'Modular code, pip dependencies, and venv isolation.'),
    ('File I/O, JSON & CSV Processing', 'Python', 'Programming', 'Beginner', 'Reading, parsing, and writing structured data files.'),
    ('Object-Oriented Python', 'Python', 'Programming', 'Intermediate', 'Classes, dunder methods, and clean reusable pipelines.'),

    -- Data Analyst & AI/ML: NumPy & Pandas (5)
    ('NumPy Arrays, Broadcasting & Vectorization', 'NumPy', 'Data Science', 'Beginner', 'N-dimensional arrays and fast vectorized numerical compute.'),
    ('Linear Algebra Operations in NumPy', 'NumPy', 'Data Science', 'Intermediate', 'Dot products, matrix multiplication, and norms.'),
    ('Pandas DataFrames, Indexing & Filtering', 'Pandas', 'Data Science', 'Beginner', 'loc/iloc selection, boolean masks, and sorting.'),
    ('Data Cleaning, Missing Values & Merging', 'Pandas', 'Data Science', 'Intermediate', 'Imputation, deduplication, and multi-table merges.'),
    ('GroupBy Aggregations & Time Series', 'Pandas', 'Data Science', 'Intermediate', 'Split-apply-combine, pivot tables, and date resampling.'),

    -- Data Analyst & AI/ML: Statistics (5)
    ('Descriptive Statistics & Distributions', 'Statistics', 'Data Science', 'Beginner', 'Mean, median, variance, skewness, and normal distributions.'),
    ('Probability Rules & Bayes Theorem', 'Statistics', 'Data Science', 'Intermediate', 'Conditional probability, independence, and Bayesian updating.'),
    ('Hypothesis Testing & p-Values', 'Statistics', 'Data Science', 'Intermediate', 'Null/alternative hypotheses, t-tests, and Type I/II errors.'),
    ('Confidence Intervals & A/B Testing', 'Statistics', 'Data Science', 'Intermediate', 'Sample size estimation, uplift testing, and statistical significance.'),
    ('Correlation & Regression Fundamentals', 'Statistics', 'Data Science', 'Intermediate', 'Pearson/Spearman correlation and simple linear regression.'),

    -- Data Analyst: Excel (4)
    ('Excel Lookup Functions (XLOOKUP, VLOOKUP)', 'Excel', 'Data', 'Beginner', 'Cross-sheet lookups, INDEX/MATCH, and error handling.'),
    ('Pivot Tables & Pivot Charts', 'Excel', 'Data', 'Beginner', 'Interactive multi-dimensional summarization and slicers.'),
    ('Conditional Formatting & Data Validation', 'Excel', 'Data', 'Beginner', 'Rule-based highlighting and clean data entry templates.'),
    ('Power Query & Spreadsheet Modeling', 'Excel', 'Data', 'Intermediate', 'Automated CSV/DB ingestion and repeatable transforms.'),

    -- Data Analyst: Data Visualization (4)
    ('Chart Selection & Visual Storytelling', 'Data Visualization', 'Data Science', 'Beginner', 'Choosing bar, line, scatter, and box plots accurately.'),
    ('Matplotlib & Seaborn Plotting', 'Data Visualization', 'Data Science', 'Intermediate', 'Programmatic statistical plots and multi-panel figures.'),
    ('Interactive BI Dashboards (Power BI / Tableau)', 'Data Visualization', 'Data Science', 'Intermediate', 'Filters, drill-downs, and data model relationships.'),
    ('KPI Design & Executive Reporting', 'Data Visualization', 'Data Science', 'Intermediate', 'Actionable metrics, clarity, and avoiding misleading scales.'),

    -- Data Analyst: Data Analysis & Business Analytics (5)
    ('Exploratory Data Analysis (EDA)', 'Data Analysis', 'Data', 'Beginner', 'Profiling distributions, outliers, and data quality checks.'),
    ('Data Storytelling & Stakeholder Insights', 'Data Analysis', 'Data', 'Intermediate', 'Translating analytical findings into clear recommendations.'),
    ('Funnel, Cohort & Retention Analysis', 'Business Analytics', 'Data', 'Intermediate', 'Conversion drop-offs, user cohorts, and churn tracking.'),
    ('Business Metrics & Root-Cause Analysis', 'Business Analytics', 'Data', 'Intermediate', 'KPI trees, segmentation, and diagnosing metric shifts.'),
    ('Forecasting & Trend Analysis', 'Business Analytics', 'Data', 'Intermediate', 'Seasonality, moving averages, and baseline projections.'),

    -- AI/ML: Mathematics for ML (4)
    ('Vectors, Matrices & Eigenvalues', 'Mathematics for ML', 'AI/ML', 'Intermediate', 'Linear transformations, rank, and eigendecomposition.'),
    ('Matrix Calculus & Gradients', 'Mathematics for ML', 'AI/ML', 'Intermediate', 'Partial derivatives, Jacobians, and chain rule.'),
    ('Optimization & Gradient Descent', 'Mathematics for ML', 'AI/ML', 'Intermediate', 'Convexity, SGD, momentum, and Adam optimizers.'),
    ('Probability Distributions for ML', 'Mathematics for ML', 'AI/ML', 'Intermediate', 'Gaussian, Bernoulli, Multinomial, and MLE.'),

    -- AI/ML: Machine Learning & Scikit-learn (7)
    ('Supervised Learning: Linear & Logistic Regression', 'Machine Learning', 'AI/ML', 'Beginner', 'Cost functions, decision boundaries, and interpretability.'),
    ('Tree-Based Models (Decision Trees, Random Forest, XGBoost)', 'Machine Learning', 'AI/ML', 'Intermediate', 'Bagging, gradient boosting, and feature importance.'),
    ('Unsupervised Learning: Clustering & PCA', 'Machine Learning', 'AI/ML', 'Intermediate', 'K-Means, DBSCAN, and dimensionality reduction.'),
    ('Bias-Variance Tradeoff & Regularization', 'Machine Learning', 'AI/ML', 'Intermediate', 'Overfitting control via L1/L2 penalties and learning curves.'),
    ('Feature Engineering & Encoding', 'Scikit-learn', 'AI/ML', 'Intermediate', 'Scaling, one-hot encoding, and sklearn Pipelines.'),
    ('Cross-Validation & Hyperparameter Tuning', 'Scikit-learn', 'AI/ML', 'Intermediate', 'K-Fold CV, GridSearchCV, and preventing data leakage.'),
    ('Evaluation Metrics (ROC-AUC, F1, RMSE)', 'Scikit-learn', 'AI/ML', 'Beginner', 'Precision-recall tradeoffs and regression error metrics.'),

    -- AI/ML: Deep Learning & Advanced ML (6)
    ('Neural Networks & Backpropagation', 'Deep Learning', 'AI/ML', 'Intermediate', 'Activations, loss functions, and weight updates.'),
    ('CNNs for Computer Vision', 'Deep Learning', 'AI/ML', 'Advanced', 'Convolutions, pooling, and image classification.'),
    ('RNNs, LSTMs & Sequence Modeling', 'Deep Learning', 'AI/ML', 'Advanced', 'Recurrent states, vanishing gradients, and sequence tasks.'),
    ('Transformers & Attention Mechanisms', 'Advanced Machine Learning', 'AI/ML', 'Advanced', 'Self-attention, positional encoding, and encoder/decoder blocks.'),
    ('Transfer Learning & Fine-Tuning', 'Advanced Machine Learning', 'AI/ML', 'Advanced', 'Pretrained backbones and parameter-efficient adaptation.'),
    ('LLM Prompting & RAG Fundamentals', 'Advanced Machine Learning', 'AI/ML', 'Intermediate', 'Embeddings, vector retrieval, and grounded generation.'),

    -- AI/ML: MLOps (4)
    ('Experiment Tracking & Model Versioning', 'MLOps', 'AI/ML', 'Intermediate', 'Reproducible runs, metrics logging, and model registries.'),
    ('Model Serving via FastAPI / REST', 'MLOps', 'AI/ML', 'Intermediate', 'Inference endpoints, input schemas, and latency budgets.'),
    ('Data Drift & Model Monitoring', 'MLOps', 'AI/ML', 'Advanced', 'Detecting feature shifts and degradation in production.'),
    ('Reproducible ML Pipelines', 'MLOps', 'AI/ML', 'Intermediate', 'Automated training, validation, and artifact packaging.');

    -- Insert subtopics idempotently and record newly created ones in seed_manifest
    INSERT INTO skills (name, category, description, difficulty, parent_skill_id, node_type)
    SELECT ts.name, ts.category, ts.description, ts.difficulty, p.id, 'SUBTOPIC'
    FROM tmp_subtopics ts
    JOIN skills p ON LOWER(p.name) = LOWER(ts.parent_name)
    ON CONFLICT (name) DO NOTHING;

    INSERT INTO seed_manifest (migration_version, table_name, record_key, record_id)
    SELECT 'V2', 'skills', s.name, s.id
    FROM tmp_subtopics ts
    JOIN skills s ON LOWER(s.name) = LOWER(ts.name)
    ON CONFLICT (migration_version, table_name, record_key) DO NOTHING;

    -- Build career_skills mappings for the 4 MVP roles (1=AI/ML, 3=Data Analyst, 4=SDE, 6=Full Stack)
    CREATE TEMP TABLE tmp_career_skills (
        career_path_id BIGINT NOT NULL,
        skill_name VARCHAR(120) NOT NULL,
        importance INTEGER NOT NULL,
        required_level INTEGER NOT NULL,
        prereq_name VARCHAR(120)
    ) ON COMMIT DROP;

    -- -------------------------------------------------------------------------
    -- ROLE 4: Software Development Engineer (SDE) — 9 Topics + 33 Subtopics
    -- -------------------------------------------------------------------------
    INSERT INTO tmp_career_skills VALUES
    (4, 'Programming Fundamentals', 5, 5, NULL),
    (4, 'OOP', 5, 4, 'Programming Fundamentals'),
    (4, 'DSA', 5, 5, 'Programming Fundamentals'),
    (4, 'DBMS', 4, 4, 'Programming Fundamentals'),
    (4, 'Operating Systems', 4, 3, 'Programming Fundamentals'),
    (4, 'Computer Networks', 3, 3, 'Operating Systems'),
    (4, 'System Design', 4, 4, 'DBMS'),
    (4, 'Software Engineering', 3, 3, 'OOP'),
    (4, 'SQL', 4, 4, 'DBMS'),
    -- SDE Subtopics
    (4, 'Time & Space Complexity (Big-O)', 5, 5, 'Programming Fundamentals'),
    (4, 'Bit Manipulation & Math Foundations', 3, 3, 'Time & Space Complexity (Big-O)'),
    (4, 'Collections & Generics', 5, 4, 'Programming Fundamentals'),
    (4, 'Multithreading & Async Basics', 4, 3, 'Collections & Generics'),
    (4, 'Encapsulation, Inheritance & Polymorphism', 5, 5, 'OOP'),
    (4, 'SOLID Design Principles', 4, 4, 'Encapsulation, Inheritance & Polymorphism'),
    (4, 'Creational & Structural Design Patterns', 3, 3, 'SOLID Design Principles'),
    (4, 'Exception Handling & Interfaces', 4, 4, 'Encapsulation, Inheritance & Polymorphism'),
    (4, 'Arrays & Hashing', 5, 5, 'Time & Space Complexity (Big-O)'),
    (4, 'Two Pointers & Sliding Window', 5, 4, 'Arrays & Hashing'),
    (4, 'Linked Lists & Stacks/Queues', 4, 4, 'Arrays & Hashing'),
    (4, 'Trees & Binary Search Trees', 5, 4, 'Linked Lists & Stacks/Queues'),
    (4, 'Graphs & Shortest Paths', 5, 4, 'Trees & Binary Search Trees'),
    (4, 'Dynamic Programming & Recursion', 5, 4, 'Trees & Binary Search Trees'),
    (4, 'ER Modeling & Normalization', 4, 4, 'DBMS'),
    (4, 'ACID Transactions & Concurrency Control', 4, 4, 'ER Modeling & Normalization'),
    (4, 'B-Tree Indexing & Query Optimization', 4, 4, 'ACID Transactions & Concurrency Control'),
    (4, 'NoSQL vs Relational Storage', 3, 3, 'DBMS'),
    (4, 'Processes, Threads & Scheduling', 4, 4, 'Operating Systems'),
    (4, 'Concurrency, Mutexes & Deadlocks', 4, 3, 'Processes, Threads & Scheduling'),
    (4, 'Memory Management & Paging', 4, 3, 'Processes, Threads & Scheduling'),
    (4, 'File Systems & I/O', 3, 3, 'Memory Management & Paging'),
    (4, 'OSI & TCP/IP Layers', 3, 3, 'Computer Networks'),
    (4, 'HTTP, HTTPS & TLS Handshake', 4, 4, 'OSI & TCP/IP Layers'),
    (4, 'DNS, Routing & Load Balancing', 3, 3, 'OSI & TCP/IP Layers'),
    (4, 'Sockets & Network Troubleshooting', 3, 3, 'OSI & TCP/IP Layers'),
    (4, 'Horizontal Scaling & Load Balancers', 4, 4, 'System Design'),
    (4, 'Caching Strategies & Redis', 4, 4, 'Horizontal Scaling & Load Balancers'),
    (4, 'Database Sharding & Replication', 3, 3, 'B-Tree Indexing & Query Optimization'),
    (4, 'Message Queues & Rate Limiting', 3, 3, 'Horizontal Scaling & Load Balancers'),
    (4, 'Unit Testing & Mocking', 4, 4, 'Software Engineering'),
    (4, 'Code Reviews & Clean Architecture', 3, 3, 'SOLID Design Principles'),
    (4, 'Agile & CI/CD Workflows', 2, 3, 'Software Engineering');

    -- -------------------------------------------------------------------------
    -- ROLE 6: Full Stack Developer — 9 Topics + 31 Subtopics
    -- -------------------------------------------------------------------------
    INSERT INTO tmp_career_skills VALUES
    (6, 'HTML', 4, 5, NULL),
    (6, 'CSS', 4, 4, 'HTML'),
    (6, 'JavaScript', 5, 5, 'HTML'),
    (6, 'React', 5, 5, 'JavaScript'),
    (6, 'Spring Boot', 5, 4, 'Java'),
    (6, 'REST APIs', 5, 5, 'Spring Boot'),
    (6, 'SQL', 4, 4, NULL),
    (6, 'Git', 4, 4, NULL),
    (6, 'Deployment', 3, 3, 'Git'),
    -- Full Stack Subtopics
    (6, 'Semantic HTML5 & Accessibility', 4, 4, 'HTML'),
    (6, 'Web Forms & Input Validation', 4, 4, 'Semantic HTML5 & Accessibility'),
    (6, 'Flexbox & CSS Grid Layouts', 4, 4, 'CSS'),
    (6, 'Responsive Design & Media Queries', 4, 4, 'Flexbox & CSS Grid Layouts'),
    (6, 'ES6+ Syntax, Closures & Scope', 5, 5, 'JavaScript'),
    (6, 'DOM Manipulation & Events', 4, 4, 'ES6+ Syntax, Closures & Scope'),
    (6, 'Promises, Async/Await & Event Loop', 5, 4, 'ES6+ Syntax, Closures & Scope'),
    (6, 'Fetch API & Browser Storage', 4, 4, 'Promises, Async/Await & Event Loop'),
    (6, 'Components, Props & JSX', 5, 5, 'React'),
    (6, 'React Hooks (useState, useEffect)', 5, 4, 'Components, Props & JSX'),
    (6, 'Client-Side Routing & Forms', 4, 4, 'React Hooks (useState, useEffect)'),
    (6, 'State Management & Performance', 4, 3, 'React Hooks (useState, useEffect)'),
    (6, 'Dependency Injection & IoC', 4, 4, 'Spring Boot'),
    (6, 'Spring Data JPA & Hibernate', 5, 4, 'Dependency Injection & IoC'),
    (6, 'Spring Security & Session/JWT Auth', 4, 4, 'Spring Boot'),
    (6, 'Spring Boot Validation & Exception Handling', 4, 4, 'Spring Data JPA & Hibernate'),
    (6, 'RESTful Resource Design & HTTP Verbs', 5, 5, 'REST APIs'),
    (6, 'Status Codes, Pagination & Filtering', 4, 4, 'RESTful Resource Design & HTTP Verbs'),
    (6, 'JSON Serialization & DTO Mapping', 4, 4, 'RESTful Resource Design & HTTP Verbs'),
    (6, 'API Documentation (OpenAPI/Swagger)', 3, 3, 'RESTful Resource Design & HTTP Verbs'),
    (6, 'SQL Joins & Subqueries', 5, 4, 'SQL'),
    (6, 'Aggregations, GROUP BY & HAVING', 4, 4, 'SQL Joins & Subqueries'),
    (6, 'Window Functions & CTEs', 3, 3, 'Aggregations, GROUP BY & HAVING'),
    (6, 'Views, Constraints & Schema Design', 4, 4, 'SQL'),
    (6, 'SQL Query Tuning & Execution Plans', 3, 3, 'Views, Constraints & Schema Design'),
    (6, 'Git Branching, Merging & Rebasing', 4, 4, 'Git'),
    (6, 'Pull Requests & Conflict Resolution', 4, 4, 'Git Branching, Merging & Rebasing'),
    (6, 'Git Workflows & Release Tagging', 3, 3, 'Pull Requests & Conflict Resolution'),
    (6, 'Environment Variables & Secrets Management', 4, 4, 'Deployment'),
    (6, 'Docker Containers & Compose', 3, 3, 'Deployment'),
    (6, 'Cloud Deployment & Reverse Proxies', 3, 3, 'Docker Containers & Compose');

    -- -------------------------------------------------------------------------
    -- ROLE 3: Data Analyst — 9 Topics + 32 Subtopics
    -- -------------------------------------------------------------------------
    INSERT INTO tmp_career_skills VALUES
    (3, 'SQL', 5, 5, NULL),
    (3, 'Excel', 5, 5, NULL),
    (3, 'Python', 4, 4, NULL),
    (3, 'Pandas', 5, 4, 'Python'),
    (3, 'NumPy', 3, 3, 'Python'),
    (3, 'Statistics', 4, 4, NULL),
    (3, 'Data Visualization', 5, 4, 'Data Analysis'),
    (3, 'Data Analysis', 5, 5, 'SQL'),
    (3, 'Business Analytics', 4, 4, 'Data Analysis'),
    -- Data Analyst Subtopics
    (3, 'SQL Joins & Subqueries', 5, 5, 'SQL'),
    (3, 'Aggregations, GROUP BY & HAVING', 5, 5, 'SQL Joins & Subqueries'),
    (3, 'Window Functions & CTEs', 5, 4, 'Aggregations, GROUP BY & HAVING'),
    (3, 'Views, Constraints & Schema Design', 3, 3, 'SQL'),
    (3, 'SQL Query Tuning & Execution Plans', 3, 3, 'Window Functions & CTEs'),
    (3, 'Excel Lookup Functions (XLOOKUP, VLOOKUP)', 5, 5, 'Excel'),
    (3, 'Pivot Tables & Pivot Charts', 5, 5, 'Excel'),
    (3, 'Conditional Formatting & Data Validation', 4, 4, 'Excel'),
    (3, 'Power Query & Spreadsheet Modeling', 4, 4, 'Pivot Tables & Pivot Charts'),
    (3, 'Python Data Structures & Comprehensions', 4, 4, 'Python'),
    (3, 'Functions, Modules & Virtual Environments', 3, 3, 'Python Data Structures & Comprehensions'),
    (3, 'File I/O, JSON & CSV Processing', 4, 4, 'Python Data Structures & Comprehensions'),
    (3, 'Object-Oriented Python', 2, 2, 'Functions, Modules & Virtual Environments'),
    (3, 'NumPy Arrays, Broadcasting & Vectorization', 3, 3, 'NumPy'),
    (3, 'Pandas DataFrames, Indexing & Filtering', 5, 5, 'Pandas'),
    (3, 'Data Cleaning, Missing Values & Merging', 5, 5, 'Pandas DataFrames, Indexing & Filtering'),
    (3, 'GroupBy Aggregations & Time Series', 5, 4, 'Data Cleaning, Missing Values & Merging'),
    (3, 'Descriptive Statistics & Distributions', 5, 4, 'Statistics'),
    (3, 'Probability Rules & Bayes Theorem', 3, 3, 'Descriptive Statistics & Distributions'),
    (3, 'Hypothesis Testing & p-Values', 4, 4, 'Descriptive Statistics & Distributions'),
    (3, 'Confidence Intervals & A/B Testing', 4, 4, 'Hypothesis Testing & p-Values'),
    (3, 'Correlation & Regression Fundamentals', 4, 4, 'Descriptive Statistics & Distributions'),
    (3, 'Chart Selection & Visual Storytelling', 5, 5, 'Data Visualization'),
    (3, 'Matplotlib & Seaborn Plotting', 4, 4, 'Pandas'),
    (3, 'Interactive BI Dashboards (Power BI / Tableau)', 5, 4, 'Chart Selection & Visual Storytelling'),
    (3, 'KPI Design & Executive Reporting', 4, 4, 'Chart Selection & Visual Storytelling'),
    (3, 'Exploratory Data Analysis (EDA)', 5, 5, 'Data Analysis'),
    (3, 'Data Storytelling & Stakeholder Insights', 4, 4, 'Exploratory Data Analysis (EDA)'),
    (3, 'Funnel, Cohort & Retention Analysis', 4, 4, 'Business Analytics'),
    (3, 'Business Metrics & Root-Cause Analysis', 4, 4, 'Business Analytics'),
    (3, 'Forecasting & Trend Analysis', 3, 3, 'Business Analytics');

    -- -------------------------------------------------------------------------
    -- ROLE 1: AI/ML Engineer — 9 Topics + 34 Subtopics
    -- -------------------------------------------------------------------------
    INSERT INTO tmp_career_skills VALUES
    (1, 'Python', 5, 5, NULL),
    (1, 'Mathematics for ML', 5, 4, 'Python'),
    (1, 'Statistics', 5, 4, 'Mathematics for ML'),
    (1, 'NumPy', 4, 4, 'Python'),
    (1, 'Pandas', 4, 4, 'Python'),
    (1, 'Machine Learning', 5, 5, 'Statistics'),
    (1, 'Scikit-learn', 5, 5, 'Machine Learning'),
    (1, 'Deep Learning', 4, 4, 'Machine Learning'),
    (1, 'Advanced Machine Learning', 4, 3, 'Deep Learning'),
    (1, 'MLOps', 4, 3, 'Machine Learning'),
    -- AI/ML Subtopics
    (1, 'Python Data Structures & Comprehensions', 5, 5, 'Python'),
    (1, 'Functions, Modules & Virtual Environments', 4, 4, 'Python Data Structures & Comprehensions'),
    (1, 'File I/O, JSON & CSV Processing', 4, 4, 'Python Data Structures & Comprehensions'),
    (1, 'Object-Oriented Python', 4, 4, 'Functions, Modules & Virtual Environments'),
    (1, 'NumPy Arrays, Broadcasting & Vectorization', 5, 4, 'NumPy'),
    (1, 'Linear Algebra Operations in NumPy', 4, 4, 'NumPy Arrays, Broadcasting & Vectorization'),
    (1, 'Pandas DataFrames, Indexing & Filtering', 4, 4, 'Pandas'),
    (1, 'Data Cleaning, Missing Values & Merging', 4, 4, 'Pandas DataFrames, Indexing & Filtering'),
    (1, 'GroupBy Aggregations & Time Series', 3, 3, 'Data Cleaning, Missing Values & Merging'),
    (1, 'Vectors, Matrices & Eigenvalues', 5, 4, 'Mathematics for ML'),
    (1, 'Matrix Calculus & Gradients', 4, 4, 'Vectors, Matrices & Eigenvalues'),
    (1, 'Optimization & Gradient Descent', 5, 4, 'Matrix Calculus & Gradients'),
    (1, 'Probability Distributions for ML', 4, 4, 'Mathematics for ML'),
    (1, 'Descriptive Statistics & Distributions', 4, 4, 'Statistics'),
    (1, 'Probability Rules & Bayes Theorem', 5, 4, 'Descriptive Statistics & Distributions'),
    (1, 'Hypothesis Testing & p-Values', 3, 3, 'Descriptive Statistics & Distributions'),
    (1, 'Correlation & Regression Fundamentals', 4, 4, 'Descriptive Statistics & Distributions'),
    (1, 'Supervised Learning: Linear & Logistic Regression', 5, 5, 'Machine Learning'),
    (1, 'Tree-Based Models (Decision Trees, Random Forest, XGBoost)', 5, 4, 'Supervised Learning: Linear & Logistic Regression'),
    (1, 'Unsupervised Learning: Clustering & PCA', 4, 4, 'Machine Learning'),
    (1, 'Bias-Variance Tradeoff & Regularization', 5, 4, 'Supervised Learning: Linear & Logistic Regression'),
    (1, 'Feature Engineering & Encoding', 5, 5, 'Scikit-learn'),
    (1, 'Cross-Validation & Hyperparameter Tuning', 5, 4, 'Feature Engineering & Encoding'),
    (1, 'Evaluation Metrics (ROC-AUC, F1, RMSE)', 5, 5, 'Scikit-learn'),
    (1, 'Neural Networks & Backpropagation', 5, 4, 'Deep Learning'),
    (1, 'CNNs for Computer Vision', 4, 3, 'Neural Networks & Backpropagation'),
    (1, 'RNNs, LSTMs & Sequence Modeling', 3, 3, 'Neural Networks & Backpropagation'),
    (1, 'Transformers & Attention Mechanisms', 4, 4, 'Advanced Machine Learning'),
    (1, 'Transfer Learning & Fine-Tuning', 4, 4, 'Transformers & Attention Mechanisms'),
    (1, 'LLM Prompting & RAG Fundamentals', 4, 3, 'Transformers & Attention Mechanisms'),
    (1, 'Experiment Tracking & Model Versioning', 4, 4, 'MLOps'),
    (1, 'Model Serving via FastAPI / REST', 4, 4, 'MLOps'),
    (1, 'Data Drift & Model Monitoring', 3, 3, 'Model Serving via FastAPI / REST'),
    (1, 'Reproducible ML Pipelines', 4, 4, 'Experiment Tracking & Model Versioning');

    -- Insert career_skills idempotently (preserving any pre-existing rows)
    INSERT INTO career_skills (career_path_id, skill_id, importance, required_level, prerequisite_skill_id)
    SELECT t.career_path_id, s.id, t.importance, t.required_level, pr.id
    FROM tmp_career_skills t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    LEFT JOIN skills pr ON t.prereq_name IS NOT NULL AND LOWER(pr.name) = LOWER(t.prereq_name)
    ON CONFLICT (career_path_id, skill_id) DO NOTHING;

    -- Backfill prerequisite_skill_id on existing career_skills where it was NULL
    UPDATE career_skills cs
    SET prerequisite_skill_id = pr.id
    FROM tmp_career_skills t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    JOIN skills pr ON t.prereq_name IS NOT NULL AND LOWER(pr.name) = LOWER(t.prereq_name)
    WHERE cs.career_path_id = t.career_path_id
      AND cs.skill_id = s.id
      AND cs.prerequisite_skill_id IS NULL;

    -- Track seeded career_skills in seed_manifest
    INSERT INTO seed_manifest (migration_version, table_name, record_key, record_id)
    SELECT 'V2', 'career_skills', cs.career_path_id || ':' || cs.skill_id, cs.id
    FROM tmp_career_skills t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    JOIN career_skills cs ON cs.career_path_id = t.career_path_id AND cs.skill_id = s.id
    ON CONFLICT (migration_version, table_name, record_key) DO NOTHING;
END $$;

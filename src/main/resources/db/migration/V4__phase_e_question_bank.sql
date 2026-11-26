-- Career Compass Phase E: Centralized Question Bank Population (V4)
-- Populates 120 NEW high-quality questions (30 per MVP career path: 1, 3, 4, 6),
-- maps every question to 1-2 career-curriculum SUBTOPICs in question_subtopics,
-- backfills question_subtopics and question_companies for existing q1..q9,
-- and leaves general practice questions unattributed to companies.

DO $$
DECLARE
    v_creator_id BIGINT;
    v_sde_set_id BIGINT;
    v_fs_set_id BIGINT;
    v_da_set_id BIGINT;
    v_aiml_set_id BIGINT;
BEGIN
    -- Resolve system creator user safely (first admin or lowest user id)
    SELECT id INTO v_creator_id
    FROM users
    ORDER BY CASE WHEN email = 'admin@careercompass.io' THEN 0 ELSE 1 END, id
    LIMIT 1;

    IF v_creator_id IS NULL THEN
        RAISE EXCEPTION 'Cannot seed question_sets: no users exist in users table';
    END IF;

    -- -------------------------------------------------------------------------
    -- 1. Create 4 Role-Based Question Sets idempotently (general practice: company_id = NULL)
    -- -------------------------------------------------------------------------
    INSERT INTO question_sets (title, company_id, career_path_id, branch, batch, difficulty, created_by_id)
    SELECT 'SDE Core Curriculum Practice Bank', NULL, 4, 'Computer Science / IT', 'General Practice', 'Mixed', v_creator_id
    WHERE NOT EXISTS (SELECT 1 FROM question_sets WHERE title = 'SDE Core Curriculum Practice Bank' AND career_path_id = 4);

    INSERT INTO question_sets (title, company_id, career_path_id, branch, batch, difficulty, created_by_id)
    SELECT 'Full Stack Developer Practice Bank', NULL, 6, 'Computer Science / IT', 'General Practice', 'Mixed', v_creator_id
    WHERE NOT EXISTS (SELECT 1 FROM question_sets WHERE title = 'Full Stack Developer Practice Bank' AND career_path_id = 6);

    INSERT INTO question_sets (title, company_id, career_path_id, branch, batch, difficulty, created_by_id)
    SELECT 'Data Analyst SQL, Python & Analytics Practice Bank', NULL, 3, 'All Branches', 'General Practice', 'Mixed', v_creator_id
    WHERE NOT EXISTS (SELECT 1 FROM question_sets WHERE title = 'Data Analyst SQL, Python & Analytics Practice Bank' AND career_path_id = 3);

    INSERT INTO question_sets (title, company_id, career_path_id, branch, batch, difficulty, created_by_id)
    SELECT 'AI/ML Engineer Foundations & Applied Practice Bank', NULL, 1, 'Computer Science / AI', 'General Practice', 'Mixed', v_creator_id
    WHERE NOT EXISTS (SELECT 1 FROM question_sets WHERE title = 'AI/ML Engineer Foundations & Applied Practice Bank' AND career_path_id = 1);

    SELECT id INTO v_sde_set_id FROM question_sets WHERE title = 'SDE Core Curriculum Practice Bank' AND career_path_id = 4 LIMIT 1;
    SELECT id INTO v_fs_set_id FROM question_sets WHERE title = 'Full Stack Developer Practice Bank' AND career_path_id = 6 LIMIT 1;
    SELECT id INTO v_da_set_id FROM question_sets WHERE title = 'Data Analyst SQL, Python & Analytics Practice Bank' AND career_path_id = 3 LIMIT 1;
    SELECT id INTO v_aiml_set_id FROM question_sets WHERE title = 'AI/ML Engineer Foundations & Applied Practice Bank' AND career_path_id = 1 LIMIT 1;

    INSERT INTO seed_manifest (migration_version, table_name, record_key, record_id) VALUES
    ('V4', 'question_sets', 'SDE Core Curriculum Practice Bank', v_sde_set_id),
    ('V4', 'question_sets', 'Full Stack Developer Practice Bank', v_fs_set_id),
    ('V4', 'question_sets', 'Data Analyst SQL, Python & Analytics Practice Bank', v_da_set_id),
    ('V4', 'question_sets', 'AI/ML Engineer Foundations & Applied Practice Bank', v_aiml_set_id)
    ON CONFLICT (migration_version, table_name, record_key) DO NOTHING;

    -- -------------------------------------------------------------------------
    -- 2. Backfill question_subtopics & question_companies for existing q1..q9
    -- -------------------------------------------------------------------------
    CREATE TEMP TABLE tmp_backfill_subtopics (
        question_id BIGINT NOT NULL,
        subtopic_name VARCHAR(120) NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tmp_backfill_subtopics VALUES
    (1, 'Trees & Binary Search Trees'),
    (2, 'Arrays & Hashing'),
    (3, 'Encapsulation, Inheritance & Polymorphism'),
    (5, 'ACID Transactions & Concurrency Control'),
    (6, 'Arrays & Hashing'),
    (6, 'Linked Lists & Stacks/Queues'),
    (7, 'Collections & Generics'),
    (8, 'Arrays & Hashing'),
    (9, 'Encapsulation, Inheritance & Polymorphism');

    INSERT INTO question_subtopics (question_id, skill_id)
    SELECT b.question_id, s.id
    FROM tmp_backfill_subtopics b
    JOIN questions q ON q.id = b.question_id
    JOIN skills s ON LOWER(s.name) = LOWER(b.subtopic_name) AND s.node_type = 'SUBTOPIC'
    ON CONFLICT (question_id, skill_id) DO NOTHING;

    -- Backfill question_companies from existing question_sets.company_id for q1..q9
    INSERT INTO question_companies (question_id, company_id, evidence_type, source_url)
    SELECT q.id, qs.company_id,
           CASE WHEN qs.company_id = 7 THEN 'REPORTED' ELSE 'GENERAL' END,
           NULL
    FROM questions q
    JOIN question_sets qs ON qs.id = q.question_set_id
    WHERE q.id <= 9 AND qs.company_id IS NOT NULL
    ON CONFLICT (question_id, company_id) DO NOTHING;

    -- -------------------------------------------------------------------------
    -- 3. Stage 120 NEW Questions (30 per MVP career path)
    -- -------------------------------------------------------------------------
    CREATE TEMP TABLE tmp_new_questions (
        career_path_id BIGINT NOT NULL,
        question_set_id BIGINT NOT NULL,
        topic_name VARCHAR(120) NOT NULL,
        subtopic1_name VARCHAR(120) NOT NULL,
        subtopic2_name VARCHAR(120),
        question_type VARCHAR(30) NOT NULL,
        difficulty VARCHAR(40) NOT NULL,
        question_text TEXT NOT NULL,
        answer TEXT NOT NULL,
        explanation TEXT NOT NULL
    ) ON COMMIT DROP;

    -- =========================================================================
    -- ROLE 4: SDE (30 Questions: 10 MCQ, 10 CODING, 7 TECHNICAL, 3 HR)
    -- =========================================================================
    INSERT INTO tmp_new_questions VALUES
    (4, v_sde_set_id, 'DSA', 'Two Pointers & Sliding Window', NULL, 'MCQ', 'Easy',
     E'Which technique best finds the maximum sum of any contiguous subarray of fixed size k in O(N) time and O(1) space?\nA) Nested loops\nB) Fixed-size Sliding Window\nC) Merge Sort\nD) Binary Search',
     'B) Fixed-size Sliding Window',
     'By adding the incoming element and subtracting the outgoing element as the window shifts by 1, each element is processed once in O(N) time.'),
    (4, v_sde_set_id, 'DSA', 'Linked Lists & Stacks/Queues', NULL, 'MCQ', 'Easy',
     E'Which data structure is used to evaluate a postfix (Reverse Polish) arithmetic expression in a single pass?\nA) Queue\nB) Stack\nC) Min-Heap\nD) Adjacency Matrix',
     'B) Stack',
     'Operands are pushed onto the stack; when an operator appears, the top two operands are popped, evaluated, and pushed back.'),
    (4, v_sde_set_id, 'DSA', 'Graphs & Shortest Paths', NULL, 'MCQ', 'Medium',
     E'Which algorithm detects cycles in a directed graph and produces a linear ordering of vertices in O(V + E) time?\nA) Kruskal''s Algorithm\nB) Kahn''s Topological Sort (BFS)\nC) Floyd-Warshall\nD) Prim''s Algorithm',
     'B) Kahn''s Topological Sort (BFS)',
     'Kahn''s algorithm repeatedly removes vertices with in-degree 0; if fewer than V vertices are processed, the graph contains a directed cycle.'),
    (4, v_sde_set_id, 'Operating Systems', 'Concurrency, Mutexes & Deadlocks', NULL, 'MCQ', 'Medium',
     E'Which of the following is NOT one of the four Coffman conditions required for a deadlock?\nA) Mutual Exclusion\nB) Hold and Wait\nC) Preemption\nD) Circular Wait',
     'C) Preemption',
     'Deadlock requires No Preemption (not Preemption), along with Mutual Exclusion, Hold and Wait, and Circular Wait.'),
    (4, v_sde_set_id, 'Operating Systems', 'Memory Management & Paging', NULL, 'MCQ', 'Medium',
     E'What hardware cache speeds up virtual-to-physical page number translation?\nA) L1 Instruction Cache\nB) Translation Lookaside Buffer (TLB)\nC) Write Buffer\nD) DMA Controller',
     'B) Translation Lookaside Buffer (TLB)',
     'The TLB caches recent page-table entries so most virtual address translations avoid an extra main-memory lookup.'),
    (4, v_sde_set_id, 'DBMS', 'ER Modeling & Normalization', NULL, 'MCQ', 'Easy',
     E'A relation in 2NF is also in Third Normal Form (3NF) if it has no:\nA) Composite primary keys\nB) Transitive dependencies of non-prime attributes on a candidate key\nC) Foreign keys\nD) Multi-valued attributes',
     'B) Transitive dependencies of non-prime attributes on a candidate key',
     '3NF eliminates transitive functional dependencies (A -> B -> C where A is a key and B is non-prime).'),
    (4, v_sde_set_id, 'Computer Networks', 'HTTP, HTTPS & TLS Handshake', NULL, 'MCQ', 'Medium',
     E'During a TLS handshake, why is asymmetric cryptography used initially before switching to symmetric encryption?\nA) Symmetric keys cannot encrypt HTTP headers\nB) Asymmetric cryptography safely exchanges/derives session keys, while symmetric encryption is much faster for bulk data\nC) TLS does not support symmetric ciphers\nD) Browsers cannot store symmetric keys',
     'B) Asymmetric cryptography safely exchanges/derives session keys, while symmetric encryption is much faster for bulk data',
     'RSA/ECDHE authenticates the server and establishes a shared symmetric session key (e.g., AES-GCM) for high-throughput traffic.'),
    (4, v_sde_set_id, 'OOP', 'SOLID Design Principles', NULL, 'MCQ', 'Medium',
     E'Which SOLID principle states that high-level modules should depend on abstractions rather than concrete implementations?\nA) Single Responsibility Principle\nB) Open-Closed Principle\nC) Interface Segregation Principle\nD) Dependency Inversion Principle',
     'D) Dependency Inversion Principle',
     'Dependency Inversion decouples business logic from concrete storage/transport implementations using interfaces.'),
    (4, v_sde_set_id, 'System Design', 'Caching Strategies & Redis', NULL, 'MCQ', 'Medium',
     E'In the cache-aside (lazy loading) pattern, what happens on a cache miss?\nA) The cache queries the database automatically via a trigger\nB) The application reads from the database, writes the result to the cache, and returns it\nC) The request returns HTTP 404\nD) All cache entries are evicted',
     'B) The application reads from the database, writes the result to the cache, and returns it',
     'In cache-aside, the application manages both cache lookups and database fallback population.'),
    (4, v_sde_set_id, 'Programming Fundamentals', 'Time & Space Complexity (Big-O)', NULL, 'MCQ', 'Easy',
     E'What is the worst-case time complexity of inserting n elements one by one into an initially empty binary min-heap?\nA) O(log n)\nB) O(n)\nC) O(n log n)\nD) O(n^2)',
     'C) O(n log n)',
     'Each heap insertion takes O(log n) in the worst case; repeating n times yields O(n log n) (whereas bottom-up heapify of an array is O(n)).'),

    -- SDE CODING (10)
    (4, v_sde_set_id, 'DSA', 'Two Pointers & Sliding Window', 'Arrays & Hashing', 'CODING', 'Medium',
     'Given a string s, find the length of the longest substring without repeating characters.',
     'Use a sliding window [left, right] and a HashMap<Character, Integer> storing the latest index of each character. When s[right] is seen at idx >= left, move left = idx + 1 and update maxLen = max(maxLen, right - left + 1).',
     'Time complexity is O(N) because both pointers traverse the string at most once; space complexity is O(min(N, charset)).'),
    (4, v_sde_set_id, 'DSA', 'Linked Lists & Stacks/Queues', NULL, 'CODING', 'Easy',
     'Given a string containing ''('', '')'', ''{'', ''}'', ''['' and '']'', determine if the input brackets are valid and properly nested.',
     'Push expected closing brackets onto a Stack when encountering opening brackets. On a closing bracket, return false if the stack is empty or pop() != char. Return stack.isEmpty() at the end.',
     'O(N) time and O(N) worst-case space for the stack.'),
    (4, v_sde_set_id, 'DSA', 'Linked Lists & Stacks/Queues', NULL, 'CODING', 'Medium',
     'Detect whether a singly linked list has a cycle and, if so, return the node where the cycle begins using O(1) extra space.',
     'Use Floyd''s Tortoise and Hare: advance slow by 1 and fast by 2 until they meet. Then reset one pointer to head and advance both by 1 step at a time; their meeting point is the cycle entry.',
     'Mathematically, the distance from head to cycle start equals the remaining distance from the meeting point modulo cycle length. O(N) time, O(1) space.'),
    (4, v_sde_set_id, 'DSA', 'Trees & Binary Search Trees', NULL, 'CODING', 'Medium',
     'Given the root of a binary tree, return its level-order traversal (values grouped level by level from left to right).',
     'Perform BFS using a Queue<TreeNode>. For each level, record size = queue.size(), pop size nodes into a level list, and enqueue non-null left and right children.',
     'Standard BFS visits every node once in O(N) time and O(W) space where W is max tree width.'),
    (4, v_sde_set_id, 'DSA', 'Trees & Binary Search Trees', NULL, 'CODING', 'Medium',
     'Given a Binary Search Tree and two nodes p and q, find their Lowest Common Ancestor (LCA).',
     'Start at root: if both p.val and q.val < root.val, move to root.left; if both > root.val, move to root.right; otherwise root is the split point (LCA).',
     'Iterative traversal takes O(H) time (O(log N) balanced, O(N) skewed) and O(1) auxiliary space.'),
    (4, v_sde_set_id, 'DSA', 'Graphs & Shortest Paths', NULL, 'CODING', 'Medium',
     'Given a 2D grid of ''1''s (land) and ''0''s (water), count the number of islands (connected horizontally or vertically).',
     'Iterate through every cell; when ''1'' is found, increment island count and run DFS/BFS to mark all connected ''1'' cells as visited (''0'').',
     'Each cell is visited a constant number of times: O(R * C) time.'),
    (4, v_sde_set_id, 'DSA', 'Dynamic Programming & Recursion', NULL, 'CODING', 'Medium',
     'Given an array of coin denominations and a target amount, compute the fewest number of coins needed to make up that amount (or -1 if impossible).',
     'Initialize dp[0..amount] with amount + 1, set dp[0] = 0. For each a from 1..amount and each coin <= a, dp[a] = min(dp[a], 1 + dp[a - coin]). Return dp[amount] > amount ? -1 : dp[amount].',
     'Bottom-up 1D DP runs in O(amount * coins.length) time and O(amount) space.'),
    (4, v_sde_set_id, 'DSA', 'Arrays & Hashing', NULL, 'CODING', 'Medium',
     'Given an array of intervals [start, end], merge all overlapping intervals and return the non-overlapping intervals.',
     'Sort intervals by start time. Initialize merged list with intervals[0]. For each next interval, if next.start <= last.end, update last.end = max(last.end, next.end); else append next.',
     'Sorting dominates at O(N log N) time; merging pass is O(N).'),
    (4, v_sde_set_id, 'Programming Fundamentals', 'Bit Manipulation & Math Foundations', NULL, 'CODING', 'Easy',
     'Every element in an integer array appears twice except for one single element. Find that single element in O(N) time and O(1) space.',
     'XOR all elements together: int ans = 0; for (int x : nums) ans ^= x; return ans.',
     'XOR is associative/commutative with a ^ a = 0 and a ^ 0 = a, cancelling all pairs.'),
    (4, v_sde_set_id, 'DBMS', 'B-Tree Indexing & Query Optimization', NULL, 'CODING', 'Medium',
     'Write an SQL query to find the second highest distinct salary from an Employee(id, salary) table, returning NULL if none exists.',
     'SELECT MAX(salary) AS SecondHighestSalary FROM Employee WHERE salary < (SELECT MAX(salary) FROM Employee);',
     'Using MAX() over the filtered subquery guarantees a single NULL row when fewer than two distinct salaries exist.'),

    -- SDE TECHNICAL (7)
    (4, v_sde_set_id, 'Operating Systems', 'Processes, Threads & Scheduling', 'Concurrency, Mutexes & Deadlocks', 'TECHNICAL', 'Medium',
     'Explain the key differences between a process and a thread, and why context switching between threads is cheaper.',
     'A process has its own isolated virtual address space, file descriptors, and PCB, whereas threads within the same process share heap/code/open files and only maintain separate stacks and registers. Thread context switches avoid flushing page tables/TLB.',
     'Shared memory makes inter-thread communication fast but requires synchronization primitives (mutexes, atomics) to prevent race conditions.'),
    (4, v_sde_set_id, 'DBMS', 'B-Tree Indexing & Query Optimization', NULL, 'TECHNICAL', 'Medium',
     'How does a B+ Tree index improve read performance, and what write overhead does adding too many indexes introduce?',
     'A B+ Tree keeps sorted keys in high-fanout internal nodes and links leaf pages sequentially, enabling O(log N) point lookups and fast range scans. However, every INSERT/UPDATE/DELETE must update each affected index structure and write WAL logs.',
     'Index selectively on high-cardinality columns used in WHERE, JOIN, and ORDER BY clauses.'),
    (4, v_sde_set_id, 'System Design', 'Horizontal Scaling & Load Balancers', 'Database Sharding & Replication', 'TECHNICAL', 'Hard',
     'How would you design a URL shortener (like bit.ly) supporting high read throughput and custom short aliases?',
     'Use base62 encoding over a distributed unique ID generator (or hash + collision check), store mappings in a partitioned key-value/relational DB, place Redis in front for hot URL reads (80/20 rule), and route traffic through stateless API servers behind a load balancer.',
     'Reads vastly outnumber writes, so caching and read replicas are the primary scaling levers.'),
    (4, v_sde_set_id, 'System Design', 'Message Queues & Rate Limiting', NULL, 'TECHNICAL', 'Hard',
     'Compare the Token Bucket and Sliding Window Log rate-limiting algorithms for an API gateway.',
     'Token Bucket allows controlled bursts up to bucket capacity while refilling at a steady rate using O(1) memory per key. Sliding Window Log tracks request timestamps for exact enforcement without boundary spikes, at higher memory cost.',
     'Redis atomic Lua scripts or sorted sets are commonly used to implement distributed rate limiters.'),
    (4, v_sde_set_id, 'Computer Networks', 'DNS, Routing & Load Balancing', 'Sockets & Network Troubleshooting', 'TECHNICAL', 'Medium',
     'Walk through what happens when you type a URL into a browser and press Enter, from DNS lookup to TCP/TLS and HTTP response.',
     '1) Browser checks local/OS DNS cache and resolves domain via recursive DNS. 2) Opens TCP connection via 3-way handshake (SYN, SYN-ACK, ACK). 3) Negotiates TLS handshake for HTTPS. 4) Sends HTTP GET; server/load balancer returns response; browser parses HTML/CSS/JS.',
     'Covers networking fundamentals across DNS, transport, security, and application layers.'),
    (4, v_sde_set_id, 'OOP', 'Creational & Structural Design Patterns', 'Exception Handling & Interfaces', 'TECHNICAL', 'Medium',
     'When should you prefer Composition over Inheritance in object-oriented design?',
     'Prefer composition ("has-a") when behavior needs to vary at runtime, when combining multiple independent capabilities, or when inheritance would expose fragile parent internals and create deep, rigid hierarchies.',
     'Patterns like Strategy and Decorator rely on composition to extend behavior cleanly.'),
    (4, v_sde_set_id, 'Software Engineering', 'Unit Testing & Mocking', 'Code Reviews & Clean Architecture', 'TECHNICAL', 'Easy',
     'What is the purpose of mocking dependencies in unit tests, and when should you write an integration test instead?',
     'Mocking isolates the unit under test from slow/external dependencies (DBs, HTTP clients) so unit tests stay fast and deterministic. Integration tests verify real wiring, SQL queries, serialization, and security filters end-to-end.',
     'A balanced test pyramid combines fast unit tests with targeted integration tests.'),

    -- SDE HR (3)
    (4, v_sde_set_id, 'Software Engineering', 'Code Reviews & Clean Architecture', NULL, 'HR', 'Easy',
     'Tell me about a time you received critical feedback during a code review or project evaluation. How did you respond?',
     'Use STAR: describe the context, the specific feedback received, how you listened objectively without defensiveness, refactored the code or tests, and what lasting engineering habit you adopted.',
     'Demonstrates coachability, humility, and commitment to code quality.'),
    (4, v_sde_set_id, 'Software Engineering', 'Agile & CI/CD Workflows', NULL, 'HR', 'Medium',
     'Describe a project where requirements changed mid-way or a deadline was tight. How did you prioritize and deliver?',
     'Use STAR: clarify must-have vs nice-to-have scope, communicate tradeoffs early with teammates, break work into verifiable milestones, and ensure core paths remained tested.',
     'Shows ownership, pragmatic prioritization, and clear communication.'),
    (4, v_sde_set_id, 'Programming Fundamentals', 'Multithreading & Async Basics', NULL, 'HR', 'Medium',
     'Describe the toughest bug you debugged in a personal or academic project and how you isolated the root cause.',
     'Use STAR: explain symptoms, how you formed hypotheses, used logs/debugger/reproduction tests to narrow down the root cause, verified the fix, and added a regression test.',
     'Highlights systematic problem solving rather than trial-and-error.'),

    -- =========================================================================
    -- ROLE 6: FULL STACK DEVELOPER (30 Questions: 10 MCQ, 8 CODING, 9 TECHNICAL, 3 HR)
    -- =========================================================================
    (6, v_fs_set_id, 'HTML', 'Semantic HTML5 & Accessibility', NULL, 'MCQ', 'Easy',
     E'Which HTML5 element best represents self-contained content such as a blog post or forum card that could be syndicated independently?\nA) <div>\nB) <section>\nC) <article>\nD) <span>',
     'C) <article>',
     '<article> communicates independent, self-contained semantic meaning to screen readers and search engines.'),
    (6, v_fs_set_id, 'CSS', 'Flexbox & CSS Grid Layouts', NULL, 'MCQ', 'Easy',
     E'In CSS Flexbox, which property aligns flex items along the cross axis (perpendicular to flex-direction)?\nA) justify-content\nB) align-items\nC) flex-wrap\nD) order',
     'B) align-items',
     'justify-content controls the main axis, whereas align-items controls alignment along the cross axis.'),
    (6, v_fs_set_id, 'JavaScript', 'Promises, Async/Await & Event Loop', NULL, 'MCQ', 'Medium',
     E'In the JavaScript event loop, which queue executes immediately after the current call stack clears and before the next macrotask (e.g., setTimeout)?\nA) Render Queue\nB) Microtask Queue (Promises)\nC) Callback Queue\nD) WebWorker Queue',
     'B) Microtask Queue (Promises)',
     'Resolved Promise `.then`/`.catch`/await continuations run in the microtask queue before the next macrotask.'),
    (6, v_fs_set_id, 'React', 'React Hooks (useState, useEffect)', NULL, 'MCQ', 'Medium',
     E'What happens when you pass an empty dependency array [] to React''s useEffect(fn, [])?\nA) The effect runs on every re-render\nB) The effect runs once after initial mount (and its cleanup runs on unmount)\nC) The effect never runs\nD) React throws a compile error',
     'B) The effect runs once after initial mount (and its cleanup runs on unmount)',
     'An empty dependency array tells React the effect depends on no reactive state/props.'),
    (6, v_fs_set_id, 'Spring Boot', 'Dependency Injection & IoC', NULL, 'MCQ', 'Easy',
     E'Why is constructor injection recommended over field injection (@Autowired on fields) in Spring Boot services?\nA) It disables transactions\nB) It allows immutable final fields, explicit required dependencies, and easy unit testing without Spring\nC) It skips bean creation\nD) It is required for SQL queries',
     'B) It allows immutable final fields, explicit required dependencies, and easy unit testing without Spring',
     'Constructor injection makes dependencies explicit and prevents NullPointerExceptions in plain unit tests.'),
    (6, v_fs_set_id, 'Spring Boot', 'Spring Security & Session/JWT Auth', NULL, 'MCQ', 'Medium',
     E'Which password hashing algorithm should be used to store user passwords in Spring Security?\nA) MD5\nB) SHA-1\nC) BCrypt (adaptive salted hash)\nD) Base64 encoding',
     'C) BCrypt (adaptive salted hash)',
     'BCrypt automatically salts passwords and uses a configurable work factor to resist brute-force attacks.'),
    (6, v_fs_set_id, 'REST APIs', 'RESTful Resource Design & HTTP Verbs', NULL, 'MCQ', 'Easy',
     E'Which HTTP method is idempotent and intended to replace an entire existing resource at a known URI?\nA) POST\nB) PUT\nC) CONNECT\nD) TRACE',
     'B) PUT',
     'PUT replaces the target resource state idempotently, whereas POST creates new subordinate resources or non-idempotent actions.'),
    (6, v_fs_set_id, 'REST APIs', 'Status Codes, Pagination & Filtering', NULL, 'MCQ', 'Easy',
     E'Which HTTP status code is most appropriate when a POST request attempts to register an email that already exists?\nA) 200 OK\nB) 201 Created\nC) 409 Conflict\nD) 500 Internal Server Error',
     'C) 409 Conflict',
     '409 Conflict indicates the request conflicts with the current state of the target resource (e.g., duplicate unique key).'),
    (6, v_fs_set_id, 'Git', 'Git Branching, Merging & Rebasing', NULL, 'MCQ', 'Easy',
     E'What does `git switch -c feature/login` (or `git checkout -b feature/login`) do?\nA) Deletes the remote branch\nB) Creates a new local branch and switches to it\nC) Merges main into HEAD\nD) Reverts the last commit',
     'B) Creates a new local branch and switches to it',
     'Creating an isolated feature branch keeps `main` clean while developing changes.'),
    (6, v_fs_set_id, 'Deployment', 'Environment Variables & Secrets Management', NULL, 'MCQ', 'Easy',
     E'Where should database passwords and API keys be stored in a Spring Boot project?\nA) Hardcoded in application.properties and committed to Git\nB) In environment variables (loaded securely outside version control)\nC) Inside JavaScript frontend bundles\nD) In HTML comments',
     'B) In environment variables (loaded securely outside version control)',
     'Externalizing secrets via environment variables prevents credential leaks in Git history.'),

    -- Full Stack CODING (8)
    (6, v_fs_set_id, 'JavaScript', 'ES6+ Syntax, Closures & Scope', 'Promises, Async/Await & Event Loop', 'CODING', 'Medium',
     'Implement a JavaScript `debounce(fn, delayMs)` function that delays invoking `fn` until `delayMs` milliseconds have elapsed since the last call.',
     'function debounce(fn, delayMs) { let timerId; return function(...args) { clearTimeout(timerId); timerId = setTimeout(() => fn.apply(this, args), delayMs); }; }',
     'Uses a closure over `timerId` to cancel pending timers on rapid calls (e.g., search input filtering).'),
    (6, v_fs_set_id, 'JavaScript', 'Promises, Async/Await & Event Loop', 'Fetch API & Browser Storage', 'CODING', 'Medium',
     'Write an async JavaScript function `fetchUserProfile(url)` that fetches JSON, throws an Error if `response.ok` is false, and handles network errors cleanly.',
     'async function fetchUserProfile(url) { const res = await fetch(url, { credentials: "same-origin" }); if (!res.ok) throw new Error("HTTP " + res.status); return await res.json(); }',
     '`fetch()` only rejects on network failures, not HTTP 4xx/5xx, so checking `res.ok` is essential.'),
    (6, v_fs_set_id, 'React', 'React Hooks (useState, useEffect)', 'Components, Props & JSX', 'CODING', 'Medium',
     'Write a React functional component `SearchableList({ items })` with a controlled input that filters `items` case-insensitively.',
     'function SearchableList({ items }) { const [q, setQ] = useState(""); const filtered = items.filter(i => i.toLowerCase().includes(q.toLowerCase())); return (<><input value={q} onChange={e => setQ(e.target.value)} /><ul>{filtered.map((item, idx) => <li key={idx}>{item}</li>)}</ul></>); }',
     'Demonstrates controlled inputs via `useState` and derived filtered rendering.'),
    (6, v_fs_set_id, 'Spring Boot', 'Spring Boot Validation & Exception Handling', 'JSON Serialization & DTO Mapping', 'CODING', 'Medium',
     'Write a Spring Boot `@RestControllerAdvice` handler method that catches `MethodArgumentNotValidException` and returns HTTP 400 with a map of field errors.',
     '@ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) { Map<String, String> errors = new HashMap<>(); ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage())); return ResponseEntity.badRequest().body(errors); }',
     'Converts Bean Validation errors into clean, structured 400 JSON responses.'),
    (6, v_fs_set_id, 'SQL', 'SQL Joins & Subqueries', 'Aggregations, GROUP BY & HAVING', 'CODING', 'Easy',
     'Given `users(id, name)` and `roadmaps(id, user_id, status)`, write an SQL query returning each user''s name and count of ACTIVE roadmaps (including users with 0).',
     'SELECT u.name, COUNT(r.id) AS active_roadmaps FROM users u LEFT JOIN roadmaps r ON r.user_id = u.id AND r.status = ''ACTIVE'' GROUP BY u.id, u.name;',
     'Filtering `r.status = ''ACTIVE''` in the `LEFT JOIN ... ON` clause preserves users with 0 active roadmaps.'),
    (6, v_fs_set_id, 'SQL', 'Window Functions & CTEs', NULL, 'CODING', 'Medium',
     'Given `roadmap_tasks(id, roadmap_id, week_number, estimated_hours)`, write an SQL query using a window function to compute cumulative estimated hours per roadmap ordered by week_number and id.',
     'SELECT id, roadmap_id, week_number, estimated_hours, SUM(estimated_hours) OVER (PARTITION BY roadmap_id ORDER BY week_number, id) AS cumulative_hours FROM roadmap_tasks;',
     'Window `SUM(...) OVER (PARTITION BY ... ORDER BY ...)` computes running totals without collapsing rows.'),
    (6, v_fs_set_id, 'CSS', 'Responsive Design & Media Queries', 'Flexbox & CSS Grid Layouts', 'CODING', 'Easy',
     'Write CSS for a `.card-grid` class that displays 3 equal columns on desktop and automatically collapses to 1 column below 768px.',
     '.card-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; } @media (max-width: 768px) { .card-grid { grid-template-columns: 1fr; } }',
     'CSS Grid combined with a mobile breakpoint provides clean responsive stacking.'),
    (6, v_fs_set_id, 'HTML', 'Web Forms & Input Validation', NULL, 'CODING', 'Easy',
     'Explain why `<button type="button">` must be specified on non-submit buttons inside an HTML `<form>`.',
     'Without an explicit `type` attribute, `<button>` inside a `<form>` defaults to `type="submit"`, causing accidental form submissions when clicked.',
     'Setting `type="button"` on modal triggers, toggles, and tab buttons prevents unintended POSTs.'),

    -- Full Stack TECHNICAL (9)
    (6, v_fs_set_id, 'Spring Boot', 'Spring Data JPA & Hibernate', NULL, 'TECHNICAL', 'Medium',
     'What is the N+1 query problem in Hibernate/JPA, and how do you resolve it?',
     'Fetching N parent entities with LAZY child collections causes 1 query for parents plus N separate queries when iterating children. Fix it using `JOIN FETCH` in JPQL, `@EntityGraph`, or batch fetching.',
     'Eliminates redundant database round trips under load.'),
    (6, v_fs_set_id, 'REST APIs', 'JSON Serialization & DTO Mapping', 'Spring Data JPA & Hibernate', 'TECHNICAL', 'Medium',
     'Why should REST controllers return DTOs instead of JPA entities with LAZY relationships?',
     'Returning JPA entities directly risks LazyInitializationException or Jackson proxy serialization failures, leaks internal database fields, and couples API contracts to DB schema.',
     'DTOs keep API payloads stable, minimal, and safe from over-exposure.'),
    (6, v_fs_set_id, 'Spring Boot', 'Spring Security & Session/JWT Auth', NULL, 'TECHNICAL', 'Medium',
     'Compare stateful session-cookie authentication vs stateless JWT authentication in a web application.',
     'Session cookies store session state server-side/Redis, support instant server-side revocation, and use HttpOnly/SameSite cookies. Stateless JWTs avoid session lookups across microservices but are harder to revoke before expiration.',
     'For server-rendered or single-backend SaaS apps, HttpOnly sessions are simple and secure.'),
    (6, v_fs_set_id, 'JavaScript', 'DOM Manipulation & Events', NULL, 'TECHNICAL', 'Easy',
     'What is event delegation in the DOM and why is it useful for dynamic lists?',
     'Event delegation attaches one listener to a parent container and inspects `event.target.closest(...)` instead of binding listeners to every child element, saving memory and handling dynamically added items automatically.',
     'Essential for dynamically rendered tables, task lists, and question banks.'),
    (6, v_fs_set_id, 'React', 'Client-Side Routing & Forms', 'State Management & Performance', 'TECHNICAL', 'Medium',
     'How does React''s `key` prop affect list reconciliation, and why should array indexes be avoided as keys for mutable lists?',
     'React uses `key` to match elements across renders. If items are reordered, inserted, or deleted, array index keys cause React to reuse stale DOM/component state on the wrong item.',
     'Always use a stable unique ID (e.g., `task.id`) as `key`.'),
    (6, v_fs_set_id, 'SQL', 'SQL Query Tuning & Execution Plans', 'Views, Constraints & Schema Design', 'TECHNICAL', 'Medium',
     'When you run `EXPLAIN ANALYZE` on a slow PostgreSQL query, what signals indicate a missing index or bad join plan?',
     'Look for `Seq Scan` on large tables with selective `Filter` conditions, large discrepancies between estimated and actual row counts, and expensive `Sort` or `Nested Loop` nodes over unindexed foreign keys.',
     'Adding a B-Tree index on filter/join columns and running `ANALYZE` typically resolves sequential scan bottlenecks.'),
    (6, v_fs_set_id, 'Git', 'Pull Requests & Conflict Resolution', 'Git Workflows & Release Tagging', 'TECHNICAL', 'Easy',
     'What is the difference between `git merge` and `git rebase`, and when should you avoid rebasing?',
     '`git merge` preserves true branch history with a merge commit; `git rebase` replays commits onto a new base for linear history. Never rebase commits already pushed to a shared branch like `main` because it rewrites commit hashes.',
     'Safe rule: rebase local feature branches, merge/squash into shared branches.'),
    (6, v_fs_set_id, 'Deployment', 'Docker Containers & Compose', 'Cloud Deployment & Reverse Proxies', 'TECHNICAL', 'Medium',
     'What are the benefits of multi-stage Docker builds for a Java Spring Boot application?',
     'Stage 1 uses a full JDK + Maven image to compile the JAR; Stage 2 copies only the JAR into a slim JRE runtime image, drastically reducing image size, build cache churn, and attack surface.',
     'Smaller images deploy faster and exclude build tools from production containers.'),
    (6, v_fs_set_id, 'REST APIs', 'API Documentation (OpenAPI/Swagger)', 'Status Codes, Pagination & Filtering', 'TECHNICAL', 'Easy',
     'Why is cursor-based or page/size pagination critical for list endpoints?',
     'Returning unbounded rows causes high memory usage, slow serialization, and timeouts as tables grow. Pagination bounds response size and latency.',
     'Cursor pagination also avoids skipped/duplicated rows on rapidly changing datasets.'),

    -- Full Stack HR (3)
    (6, v_fs_set_id, 'Git', 'Pull Requests & Conflict Resolution', NULL, 'HR', 'Easy',
     'Tell me about a time you integrated frontend and backend changes with a teammate and encountered a mismatch or conflict.',
     'Use STAR: describe the API contract/merge mismatch, how you aligned on exact field names/status codes, tested end-to-end, and documented the contract to prevent recurrence.',
     'Demonstrates cross-stack collaboration and clear API communication.'),
    (6, v_fs_set_id, 'Deployment', 'Cloud Deployment & Reverse Proxies', NULL, 'HR', 'Medium',
     'Describe a time an application worked locally on your machine but failed in another environment. How did you resolve it?',
     'Use STAR: identify the environment discrepancy (ports, env vars, DB SSL/pooler settings, case-sensitivity), reproduce systematically, and standardize configuration.',
     'Shows production readiness and disciplined troubleshooting.'),
    (6, v_fs_set_id, 'HTML', 'Semantic HTML5 & Accessibility', NULL, 'HR', 'Easy',
     'How do you balance visual polish with usability and accessibility when building user-facing features?',
     'Discuss prioritizing clear workflows, keyboard/screen-reader semantics, responsive mobile layouts, and meaningful error states before decorative polish.',
     'Reflects user-centric engineering judgment.'),

    -- =========================================================================
    -- ROLE 3: DATA ANALYST (30 Questions: 10 MCQ, 8 CODING, 9 TECHNICAL, 3 HR)
    -- =========================================================================
    (3, v_da_set_id, 'SQL', 'SQL Joins & Subqueries', NULL, 'MCQ', 'Easy',
     E'Which SQL join returns all rows from the left table and matching rows from the right table, filling unmatched right columns with NULL?\nA) INNER JOIN\nB) LEFT JOIN\nC) CROSS JOIN\nD) INTERSECT',
     'B) LEFT JOIN',
     'LEFT JOIN preserves every row from the left relation regardless of whether a match exists on the right.'),
    (3, v_da_set_id, 'SQL', 'Aggregations, GROUP BY & HAVING', NULL, 'MCQ', 'Easy',
     E'What is the key difference between `WHERE` and `HAVING` in an SQL query?\nA) They are identical\nB) WHERE filters rows before grouping; HAVING filters groups after GROUP BY aggregation\nC) HAVING runs before FROM\nD) WHERE can filter on SUM()',
     'B) WHERE filters rows before grouping; HAVING filters groups after GROUP BY aggregation',
     'Aggregate predicates like `HAVING COUNT(*) > 5` cannot be placed in `WHERE`.'),
    (3, v_da_set_id, 'SQL', 'Window Functions & CTEs', NULL, 'MCQ', 'Medium',
     E'How do `RANK()` and `DENSE_RANK()` differ when two rows tie for 1st place?\nA) RANK() assigns 1, 1, 3; DENSE_RANK() assigns 1, 1, 2\nB) Both assign 1, 2, 3\nC) DENSE_RANK() skips numbers\nD) RANK() removes ties',
     'A) RANK() assigns 1, 1, 3; DENSE_RANK() assigns 1, 1, 2',
     'DENSE_RANK() leaves no gaps in ranking values after ties.'),
    (3, v_da_set_id, 'Excel', 'Excel Lookup Functions (XLOOKUP, VLOOKUP)', NULL, 'MCQ', 'Easy',
     E'Which limitation of VLOOKUP is solved by XLOOKUP and INDEX/MATCH?\nA) VLOOKUP cannot sum numbers\nB) VLOOKUP can only search the leftmost column and return values to the right\nC) VLOOKUP only works on CSV files\nD) VLOOKUP cannot handle text',
     'B) VLOOKUP can only search the leftmost column and return values to the right',
     'XLOOKUP can search any column and return from any column (left or right) with built-in not-found handling.'),
    (3, v_da_set_id, 'Excel', 'Pivot Tables & Pivot Charts', NULL, 'MCQ', 'Easy',
     E'In an Excel Pivot Table, where should you place a categorical column like `Region` to compare metrics side by side as columns?\nA) Values\nB) Columns\nC) Filters only\nD) Formula Bar',
     'B) Columns',
     'Placing a dimension in Columns creates one column per category across aggregated Values.'),
    (3, v_da_set_id, 'Statistics', 'Descriptive Statistics & Distributions', NULL, 'MCQ', 'Easy',
     E'When a dataset of salaries is heavily right-skewed due to a few extreme outliers, which measure of central tendency best represents a typical value?\nA) Arithmetic Mean\nB) Median\nC) Range\nD) Variance',
     'B) Median',
     'The median is robust to extreme outliers, whereas the mean is pulled toward the skewed tail.'),
    (3, v_da_set_id, 'Statistics', 'Hypothesis Testing & p-Values', NULL, 'MCQ', 'Medium',
     E'If a two-sample hypothesis test yields p-value = 0.02 at significance level alpha = 0.05, what is the correct conclusion?\nA) The null hypothesis is proven 100% false\nB) Reject the null hypothesis at the 5% significance level\nC) Accept the null hypothesis\nD) Increase sample size because p < 0.05 is invalid',
     'B) Reject the null hypothesis at the 5% significance level',
     'Since p (0.02) < alpha (0.05), the observed difference is statistically significant at the 5% level.'),
    (3, v_da_set_id, 'Pandas', 'Data Cleaning, Missing Values & Merging', NULL, 'MCQ', 'Easy',
     E'In Pandas, which method removes rows containing any NaN values in a DataFrame `df`?\nA) df.fillna(0)\nB) df.dropna()\nC) df.isna()\nD) df.drop_duplicates()',
     'B) df.dropna()',
     '`df.dropna()` drops rows (axis=0) or columns (axis=1) with missing values.'),
    (3, v_da_set_id, 'Data Visualization', 'Chart Selection & Visual Storytelling', NULL, 'MCQ', 'Easy',
     E'Which chart type is best suited for visualizing the distribution, median, quartiles, and outliers of a continuous variable across categories?\nA) Pie Chart\nB) Box Plot\nC) Stacked Area Chart\nD) Donut Chart',
     'B) Box Plot',
     'Box plots compactly display IQR, median, whiskers, and outlier points across groups.'),
    (3, v_da_set_id, 'Business Analytics', 'Funnel, Cohort & Retention Analysis', NULL, 'MCQ', 'Medium',
     E'What does a user retention cohort table group users by?\nA) Alphabetical username\nB) Their signup/first-action time period (e.g., week or month) tracked over subsequent periods\nC) Browser window size\nD) Server IP address',
     'B) Their signup/first-action time period (e.g., week or month) tracked over subsequent periods',
     'Cohort analysis separates acquisition timing effects from behavioral retention trends.'),

    -- Data Analyst CODING (8: SQL + Python/Pandas)
    (3, v_da_set_id, 'SQL', 'Aggregations, GROUP BY & HAVING', 'SQL Joins & Subqueries', 'CODING', 'Easy',
     'Given `orders(order_id, customer_id, order_date, amount)`, write an SQL query to find customers whose total spend exceeds 10,000, sorted by total spend descending.',
     'SELECT customer_id, SUM(amount) AS total_spend FROM orders GROUP BY customer_id HAVING SUM(amount) > 10000 ORDER BY total_spend DESC;',
     'Groups by customer_id and filters post-aggregation using HAVING.'),
    (3, v_da_set_id, 'SQL', 'Window Functions & CTEs', NULL, 'CODING', 'Medium',
     'Given `sales(region, salesperson, revenue)`, write an SQL query to return the top 2 salespeople by revenue in each region.',
     'WITH ranked AS (SELECT region, salesperson, revenue, DENSE_RANK() OVER (PARTITION BY region ORDER BY revenue DESC) AS rnk FROM sales) SELECT region, salesperson, revenue FROM ranked WHERE rnk <= 2;',
     'Uses a CTE with DENSE_RANK() partitioned by region.'),
    (3, v_da_set_id, 'SQL', 'Window Functions & CTEs', 'Aggregations, GROUP BY & HAVING', 'CODING', 'Hard',
     'Given `monthly_revenue(month_start, revenue)`, write an SQL query to calculate month-over-month (MoM) percentage revenue growth.',
     'WITH prev AS (SELECT month_start, revenue, LAG(revenue) OVER (ORDER BY month_start) AS prev_rev FROM monthly_revenue) SELECT month_start, revenue, ROUND(((revenue - prev_rev) * 100.0) / NULLIF(prev_rev, 0), 2) AS mom_pct FROM prev;',
     'Uses `LAG()` to access the prior month and `NULLIF(..., 0)` to guard against division by zero.'),
    (3, v_da_set_id, 'SQL', 'SQL Joins & Subqueries', NULL, 'CODING', 'Easy',
     'Given `customers(id, name)` and `orders(id, customer_id)`, write an SQL query to find all customers who have NEVER placed an order.',
     'SELECT c.id, c.name FROM customers c LEFT JOIN orders o ON o.customer_id = c.id WHERE o.id IS NULL;',
     'The anti-join pattern (`LEFT JOIN ... WHERE right.id IS NULL`) cleanly finds unmatched rows without NOT IN NULL traps.'),
    (3, v_da_set_id, 'Pandas', 'Pandas DataFrames, Indexing & Filtering', 'GroupBy Aggregations & Time Series', 'CODING', 'Medium',
     'In Pandas, given DataFrame `df` with columns `category`, `region`, and `sales`, write code to compute total and average sales per category, sorted by total sales descending.',
     'result = df.groupby("category", as_index=False)["sales"].agg(total_sales="sum", avg_sales="mean").sort_values("total_sales", ascending=False)',
     'Named aggregation produces clean flat columns ready for reporting.'),
    (3, v_da_set_id, 'Pandas', 'Data Cleaning, Missing Values & Merging', NULL, 'CODING', 'Easy',
     'In Pandas, write code to remove duplicate `order_id` rows keeping the latest row (after sorting by `updated_at`), and fill missing `discount` values with 0.',
     'df = df.sort_values("updated_at").drop_duplicates(subset=["order_id"], keep="last").fillna({"discount": 0})',
     'Sorting before `drop_duplicates(..., keep="last")` ensures deterministic retention of the newest record.'),
    (3, v_da_set_id, 'Python', 'Python Data Structures & Comprehensions', 'File I/O, JSON & CSV Processing', 'CODING', 'Easy',
     'Write a Python function `top_n_words(words, n)` that counts word frequencies in a list and returns the `n` most common `(word, count)` pairs.',
     'from collections import Counter\ndef top_n_words(words, n):\n    return Counter(words).most_common(n)',
     '`collections.Counter` counts frequencies in O(N) time and provides `.most_common(n)`.'),
    (3, v_da_set_id, 'NumPy', 'NumPy Arrays, Broadcasting & Vectorization', NULL, 'CODING', 'Easy',
     'Using NumPy, compute the z-score standardized array `(x - mean) / std` for a 1D array `arr` without using Python loops.',
     'import numpy as np\nz = (arr - np.mean(arr)) / np.std(arr)',
     'NumPy vectorization and broadcasting apply element-wise operations in compiled C loops.'),

    -- Data Analyst TECHNICAL (9)
    (3, v_da_set_id, 'Statistics', 'Confidence Intervals & A/B Testing', 'Hypothesis Testing & p-Values', 'TECHNICAL', 'Medium',
     'You run an A/B test on a checkout button and see a 1.5% conversion lift with p = 0.04, but the experiment ran for only 2 days. What caveats should you check before shipping?',
     'Check novelty effects, day-of-week seasonality, whether the pre-computed minimum sample size was reached (avoiding peeking bias), Sample Ratio Mismatch (SRM), and guardrail metrics (refunds/latency).',
     'Statistical significance alone is insufficient if experiment duration or randomization integrity is flawed.'),
    (3, v_da_set_id, 'Business Analytics', 'Business Metrics & Root-Cause Analysis', 'Data Storytelling & Stakeholder Insights', 'TECHNICAL', 'Medium',
     'Daily active users (DAU) dropped by 12% yesterday. Walk through a structured root-cause investigation.',
     '1) Verify tracking/pipeline health (instrumentation bug?). 2) Segment by platform (iOS/Android/Web), geography, acquisition channel, and new vs returning users. 3) Check recent releases, outages, or external holidays/events.',
     'Top-down segmentation quickly isolates technical regressions from behavioral shifts.'),
    (3, v_da_set_id, 'Data Analysis', 'Exploratory Data Analysis (EDA)', NULL, 'TECHNICAL', 'Easy',
     'What steps do you follow during Exploratory Data Analysis (EDA) on a brand-new dataset?',
     'Inspect row/column counts, data types, missingness %, duplicate IDs, summary statistics, distribution shapes/outliers, impossible values (e.g., negative prices), and correlations between key variables.',
     'Thorough EDA prevents garbage-in-garbage-out conclusions.'),
    (3, v_da_set_id, 'SQL', 'SQL Query Tuning & Execution Plans', 'Views, Constraints & Schema Design', 'TECHNICAL', 'Medium',
     'Why can `WHERE YEAR(order_date) = 2026` prevent an index on `order_date` from being used, and how do you rewrite it?',
     'Wrapping an indexed column in a function makes the predicate non-SARGable, forcing a full scan. Rewrite as a range predicate: `WHERE order_date >= ''2026-01-01'' AND order_date < ''2027-01-01''`.',
     'Range predicates allow B-Tree index range scans.'),
    (3, v_da_set_id, 'Excel', 'Power Query & Spreadsheet Modeling', 'Conditional Formatting & Data Validation', 'TECHNICAL', 'Medium',
     'When should an analyst use Power Query in Excel instead of manual copy-paste and formulas?',
     'Use Power Query whenever data comes from recurring CSV/database exports requiring repeatable cleaning steps (splitting columns, unpivoting, type casting, merging) so refreshes take one click.',
     'Eliminates manual human error in weekly/monthly reporting.'),
    (3, v_da_set_id, 'Data Visualization', 'Interactive BI Dashboards (Power BI / Tableau)', 'KPI Design & Executive Reporting', 'TECHNICAL', 'Medium',
     'What makes an executive KPI dashboard effective vs cluttered?',
     'Start with 3–5 North Star & supporting KPIs with clear comparisons (vs target / prior period), consistent time granularity, drill-down filters, uncluttered labels, and zero misleading truncated axes.',
     'Dashboards should answer decisions quickly rather than display every available metric.'),
    (3, v_da_set_id, 'Statistics', 'Correlation & Regression Fundamentals', NULL, 'TECHNICAL', 'Easy',
     'Explain why correlation does not imply causation and give a concrete analytical example (confounding variable).',
     'Two variables can move together due to a hidden confounder (e.g., ice cream sales and drowning incidents both rise in summer due to temperature). Establishing causation requires controlled experiments (A/B tests) or causal inference methods.',
     'Core statistical reasoning question for analyst interviews.'),
    (3, v_da_set_id, 'Business Analytics', 'Forecasting & Trend Analysis', NULL, 'TECHNICAL', 'Medium',
     'How do you separate long-term trend from seasonality when forecasting monthly retail sales?',
     'Use time-series decomposition (additive or multiplicative into Trend + Seasonality + Residual) or compare year-over-year (YoY) growth alongside moving averages.',
     'Prevents mistaking seasonal holiday spikes for permanent growth.'),
    (3, v_da_set_id, 'Python', 'Functions, Modules & Virtual Environments', 'Object-Oriented Python', 'TECHNICAL', 'Easy',
     'Why should analysts use Python virtual environments (`venv` or `conda`) for data projects?',
     'Virtual environments isolate package versions (pandas, numpy, matplotlib) per project so upgrading a library for one analysis does not break older notebooks/scripts.',
     'Reproducibility depends on pinned `requirements.txt` inside isolated environments.'),

    -- Data Analyst HR (3)
    (3, v_da_set_id, 'Data Analysis', 'Data Storytelling & Stakeholder Insights', NULL, 'HR', 'Easy',
     'Describe a time you had to explain a complex analytical finding to a non-technical stakeholder. How did you make it actionable?',
     'Use STAR: skip raw statistical jargon, lead with the business decision/impact, support with 1–2 clear charts, and quantify expected upside or risk.',
     'Tests stakeholder communication skills.'),
    (3, v_da_set_id, 'Data Analysis', 'Exploratory Data Analysis (EDA)', NULL, 'HR', 'Medium',
     'Tell me about a time you discovered messy, incomplete, or contradictory data right before a deadline.',
     'Use STAR: explain how you detected the anomaly during sanity checks, documented limitations transparently, cleaned or excluded corrupted segments, and communicated caveats.',
     'Shows analytical integrity and attention to data quality.'),
    (3, v_da_set_id, 'Business Analytics', 'Business Metrics & Root-Cause Analysis', NULL, 'HR', 'Medium',
     'How do you prioritize requests when multiple teams ask for ad-hoc reports at the same time?',
     'Discuss assessing business urgency/impact, clarifying the underlying decision behind each request, negotiating timelines, and automating recurring asks into self-serve dashboards.',
     'Highlights prioritization and impact orientation.'),

    -- =========================================================================
    -- ROLE 1: AI/ML ENGINEER (30 Questions: 10 MCQ, 8 CODING, 9 TECHNICAL, 3 HR)
    -- =========================================================================
    (1, v_aiml_set_id, 'Machine Learning', 'Bias-Variance Tradeoff & Regularization', NULL, 'MCQ', 'Easy',
     E'A model achieves 99% training accuracy but only 62% validation accuracy. Which issue is occurring and what helps?\nA) High bias (underfitting); increase model complexity\nB) High variance (overfitting); apply regularization, gather more data, or simplify model\nC) Data leakage in validation\nD) Learning rate is too small',
     'B) High variance (overfitting); apply regularization, gather more data, or simplify model',
     'A large gap between training and validation performance indicates overfitting (high variance).'),
    (1, v_aiml_set_id, 'Machine Learning', 'Supervised Learning: Linear & Logistic Regression', NULL, 'MCQ', 'Easy',
     E'Which loss function is minimized when training binary Logistic Regression via Maximum Likelihood Estimation?\nA) Mean Squared Error\nB) Binary Cross-Entropy (Log Loss)\nC) Hinge Loss\nD) Cosine Similarity',
     'B) Binary Cross-Entropy (Log Loss)',
     'Binary cross-entropy is convex for logistic regression and corresponds to negative log-likelihood of Bernoulli outcomes.'),
    (1, v_aiml_set_id, 'Scikit-learn', 'Evaluation Metrics (ROC-AUC, F1, RMSE)', NULL, 'MCQ', 'Medium',
     E'For a fraud detection dataset where only 0.5% of transactions are fraudulent, why is raw Accuracy misleading?\nA) Accuracy cannot be computed on binary labels\nB) A trivial model predicting "not fraud" for every row achieves 99.5% accuracy while catching 0 fraud\nC) Accuracy requires normalized features\nD) ROC-AUC is always 1.0',
     'B) A trivial model predicting "not fraud" for every row achieves 99.5% accuracy while catching 0 fraud',
     'Use Precision, Recall, F1, and PR-AUC on heavily imbalanced classification tasks.'),
    (1, v_aiml_set_id, 'Scikit-learn', 'Cross-Validation & Hyperparameter Tuning', 'Feature Engineering & Encoding', 'MCQ', 'Medium',
     E'Why must `StandardScaler().fit()` be called ONLY on training folds inside cross-validation (e.g., via sklearn `Pipeline`) rather than on the entire dataset first?\nA) To speed up training\nB) Fitting on the full dataset leaks validation mean/variance into training (data leakage)\nC) StandardScaler fails on test data\nD) Cross-validation forbids scaling',
     'B) Fitting on the full dataset leaks validation mean/variance into training (data leakage)',
     'Using `Pipeline` ensures scalers/encoders fit exclusively on each training fold.'),
    (1, v_aiml_set_id, 'Machine Learning', 'Tree-Based Models (Decision Trees, Random Forest, XGBoost)', NULL, 'MCQ', 'Medium',
     E'How do Random Forests and Gradient Boosted Trees (e.g., XGBoost) primarily differ?\nA) Random Forest builds independent trees in parallel (bagging) to reduce variance; Gradient Boosting builds trees sequentially to correct prior residual errors (reducing bias)\nB) Gradient Boosting cannot do classification\nC) Random Forest requires feature scaling\nD) Only Decision Trees use splits',
     'A) Random Forest builds independent trees in parallel (bagging) to reduce variance; Gradient Boosting builds trees sequentially to correct prior residual errors (reducing bias)',
     'Bagging averages high-variance trees; boosting sequentially fits residuals.'),
    (1, v_aiml_set_id, 'Mathematics for ML', 'Optimization & Gradient Descent', NULL, 'MCQ', 'Medium',
     E'What does the Adam optimizer combine compared to vanilla Stochastic Gradient Descent (SGD)?\nA) L1 and L2 penalties\nB) Momentum (first moment) and adaptive per-parameter learning rates (second moment, like RMSProp)\nC) Second-order Hessian inversion\nD) K-Means clustering',
     'B) Momentum (first moment) and adaptive per-parameter learning rates (second moment, like RMSProp)',
     'Adam tracks exponentially decaying averages of both gradients and squared gradients.'),
    (1, v_aiml_set_id, 'Deep Learning', 'Neural Networks & Backpropagation', NULL, 'MCQ', 'Easy',
     E'Why are non-linear activation functions (like ReLU or GELU) required between hidden layers of a neural network?\nA) To reduce GPU memory\nB) Without non-linearities, stacked linear layers collapse into a single linear transformation\nC) Linear layers cannot be differentiated\nD) ReLU prevents all overfitting',
     'B) Without non-linearities, stacked linear layers collapse into a single linear transformation',
     'Non-linear activations allow multi-layer networks to approximate complex non-linear functions.'),
    (1, v_aiml_set_id, 'Advanced Machine Learning', 'Transformers & Attention Mechanisms', NULL, 'MCQ', 'Medium',
     E'In Transformer architectures, why is positional encoding added to token embeddings?\nA) To compress vocabulary size\nB) Self-attention is permutation-invariant and has no inherent notion of token order\nC) To replace layer normalization\nD) To enforce greedy decoding',
     'B) Self-attention is permutation-invariant and has no inherent notion of token order',
     'Positional encodings inject sequence order into parallel attention computations.'),
    (1, v_aiml_set_id, 'Statistics', 'Probability Rules & Bayes Theorem', NULL, 'MCQ', 'Medium',
     E'A medical test has 99% sensitivity and 99% specificity, and a disease affects 0.1% of the population. Approximately what is P(Disease | Positive Test)?\nA) 99%\nB) 50%\nC) ~9%\nD) 0.1%',
     'C) ~9%',
     'By Bayes'' theorem: (0.99 * 0.001) / (0.99 * 0.001 + 0.01 * 0.999) ≈ 0.090 (base-rate fallacy).'),
    (1, v_aiml_set_id, 'MLOps', 'Data Drift & Model Monitoring', NULL, 'MCQ', 'Medium',
     E'What is the difference between Data Drift (Covariate Shift) and Concept Drift in production ML?\nA) Data drift is a change in input feature distribution P(X); concept drift is a change in the relationship P(Y|X)\nB) Both mean server CPU spike\nC) Concept drift only affects SQL databases\nD) Data drift improves accuracy automatically',
     'A) Data drift is a change in input feature distribution P(X); concept drift is a change in the relationship P(Y|X)',
     'Monitoring both feature distributions and delayed ground-truth labels catches both drift types.'),

    -- AI/ML CODING (8)
    (1, v_aiml_set_id, 'NumPy', 'Linear Algebra Operations in NumPy', 'NumPy Arrays, Broadcasting & Vectorization', 'CODING', 'Medium',
     'Write a vectorized NumPy function `cosine_similarity(a, b)` for two 1D vectors, handling zero-norm vectors safely.',
     'import numpy as np\ndef cosine_similarity(a, b):\n    denom = np.linalg.norm(a) * np.linalg.norm(b)\n    return 0.0 if denom == 0 else float(np.dot(a, b) / denom)',
     'Computes dot product divided by product of L2 norms in O(D) vectorized time.'),
    (1, v_aiml_set_id, 'Scikit-learn', 'Feature Engineering & Encoding', 'Cross-Validation & Hyperparameter Tuning', 'CODING', 'Medium',
     'Write a Scikit-learn `Pipeline` with `ColumnTransformer` that imputes+scales numeric features, one-hot encodes categorical features, and fits a `LogisticRegression` classifier.',
     'pre = ColumnTransformer([("num", Pipeline([("imp", SimpleImputer()), ("sc", StandardScaler())]), num_cols), ("cat", OneHotEncoder(handle_unknown="ignore"), cat_cols)])\nclf = Pipeline([("pre", pre), ("model", LogisticRegression(max_iter=1000))])',
     'Prevents data leakage and cleanly handles unseen categories during inference.'),
    (1, v_aiml_set_id, 'Scikit-learn', 'Evaluation Metrics (ROC-AUC, F1, RMSE)', NULL, 'CODING', 'Easy',
     'Given binary ground truth `y_true` and predicted probabilities `y_prob`, write Python/NumPy code to compute Binary Cross-Entropy (Log Loss) with epsilon clipping.',
     'import numpy as np\ndef binary_cross_entropy(y_true, y_prob, eps=1e-15):\n    p = np.clip(y_prob, eps, 1 - eps)\n    return float(-np.mean(y_true * np.log(p) + (1 - y_true) * np.log(1 - p)))',
     'Clipping avoids `log(0)` NaNs when predicted probabilities hit 0 or 1.'),
    (1, v_aiml_set_id, 'Mathematics for ML', 'Optimization & Gradient Descent', 'Matrix Calculus & Gradients', 'CODING', 'Medium',
     'Implement one step of batch gradient descent for linear regression `y_hat = X @ w + b` minimizing MSE `(1/N) * sum((y_hat - y)^2)`.',
     'n = len(y)\ny_hat = X @ w + b\nerr = y_hat - y\ndw = (2.0 / n) * (X.T @ err)\ndb = (2.0 / n) * np.sum(err)\nw -= lr * dw\nb -= lr * db',
     'Vectorized matrix transpose `X.T @ err` computes all parameter gradients simultaneously.'),
    (1, v_aiml_set_id, 'Deep Learning', 'Neural Networks & Backpropagation', NULL, 'CODING', 'Medium',
     'Write a numerically stable `softmax(logits)` function in NumPy using the log-sum-exp / max-subtraction trick.',
     'import numpy as np\ndef softmax(logits):\n    shifted = logits - np.max(logits, axis=-1, keepdims=True)\n    exp = np.exp(shifted)\n    return exp / np.sum(exp, axis=-1, keepdims=True)',
     'Subtracting `max(logits)` prevents floating-point overflow in `exp()` without changing output probabilities.'),
    (1, v_aiml_set_id, 'Pandas', 'Data Cleaning, Missing Values & Merging', 'Pandas DataFrames, Indexing & Filtering', 'CODING', 'Easy',
     'Given a Pandas DataFrame `df` with a `timestamp` column and `target`, sort chronologically and split into the first 80% as train and last 20% as test (avoiding future leakage).',
     'df = df.sort_values("timestamp").reset_index(drop=True)\nsplit_idx = int(len(df) * 0.8)\ntrain_df, test_df = df.iloc[:split_idx], df.iloc[split_idx:]',
     'Random splitting on time-ordered data leaks future patterns into training.'),
    (1, v_aiml_set_id, 'Python', 'Object-Oriented Python', 'Python Data Structures & Comprehensions', 'CODING', 'Medium',
     'Implement an `LRUCache` or `FeatureStoreCache` in Python using `collections.OrderedDict` supporting `get(key)` and `put(key, value)` in O(1) time.',
     'from collections import OrderedDict\nclass SimpleLRU:\n    def __init__(self, cap): self.cap, self.od = cap, OrderedDict()\n    def get(self, k):\n        if k not in self.od: return -1\n        self.od.move_to_end(k); return self.od[k]\n    def put(self, k, v):\n        self.od[k] = v; self.od.move_to_end(k)\n        if len(self.od) > self.cap: self.od.popitem(last=False)',
     'Useful both for algorithmic coding and feature caching in ML services.'),
    (1, v_aiml_set_id, 'MLOps', 'Model Serving via FastAPI / REST', NULL, 'CODING', 'Medium',
     'Sketch a FastAPI `/predict` endpoint with Pydantic input validation that loads a model once at startup and returns a prediction JSON.',
     'from fastapi import FastAPI\nfrom pydantic import BaseModel\napp = FastAPI()\nclass Features(BaseModel): x1: float; x2: float\n@app.post("/predict")\ndef predict(f: Features):\n    pred = model.predict([[f.x1, f.x2]])[0]\n    return {"prediction": float(pred)}',
     'Loading the model once outside the request handler avoids reloading weights on every HTTP call.'),

    -- AI/ML TECHNICAL (9)
    (1, v_aiml_set_id, 'Machine Learning', 'Bias-Variance Tradeoff & Regularization', NULL, 'TECHNICAL', 'Medium',
     'Compare L1 (Lasso) vs L2 (Ridge) regularization geometrically and in terms of feature selection.',
     'L1 adds `lambda * sum(|w_i|)` (diamond constraint), encouraging exact zeros and sparse feature selection. L2 adds `lambda * sum(w_i^2)` (circular constraint), shrinking correlated weights smoothly toward zero without zeroing them.',
     'ElasticNet combines both L1 and L2 penalties.'),
    (1, v_aiml_set_id, 'Machine Learning', 'Unsupervised Learning: Clustering & PCA', 'Mathematics for ML', 'TECHNICAL', 'Medium',
     'How does Principal Component Analysis (PCA) reduce dimensionality, and why must features be standardized first?',
     'PCA finds orthogonal eigenvectors of the covariance matrix (directions of maximum variance) and projects data onto the top-k components. Without standardization, high-magnitude features (e.g., salary vs age) dominate variance artificially.',
     'Always fit standardization on training data before PCA.'),
    (1, v_aiml_set_id, 'Deep Learning', 'CNNs for Computer Vision', NULL, 'TECHNICAL', 'Medium',
     'Why do Convolutional Neural Networks (CNNs) work much better than dense MLPs on images?',
     'CNNs exploit spatial locality (small receptive fields), weight sharing (translation equivariance with far fewer parameters), and hierarchical feature extraction from edges to object parts.',
     'A dense MLP on a 224x224x3 image would explode in parameter count and lose 2D structure.'),
    (1, v_aiml_set_id, 'Deep Learning', 'RNNs, LSTMs & Sequence Modeling', NULL, 'TECHNICAL', 'Hard',
     'What causes the vanishing gradient problem in vanilla RNNs, and how do LSTMs mitigate it?',
     'Repeated multiplication of small Jacobian/weight matrices across many time steps shrinks gradients exponentially. LSTMs introduce an additive cell state ("constant error carousel") regulated by forget, input, and output gates.',
     'Transformers further improved long-range modeling via direct self-attention paths.'),
    (1, v_aiml_set_id, 'Advanced Machine Learning', 'LLM Prompting & RAG Fundamentals', 'Transfer Learning & Fine-Tuning', 'TECHNICAL', 'Medium',
     'When should you use Retrieval-Augmented Generation (RAG) vs fine-tuning an LLM?',
     'Use RAG when knowledge changes frequently, requires source citations, or is private/domain-specific (reducing hallucinations via grounded context). Use fine-tuning to adapt style, structured output formatting, or specialized domain reasoning.',
     'Often RAG and lightweight fine-tuning complement each other.'),
    (1, v_aiml_set_id, 'Advanced Machine Learning', 'Transfer Learning & Fine-Tuning', NULL, 'TECHNICAL', 'Medium',
     'Explain LoRA (Low-Rank Adaptation) and why it makes fine-tuning large pretrained models practical.',
     'LoRA freezes pretrained weights `W` and injects trainable low-rank decomposition matrices `A * B` (`r << d`) into attention layers, reducing trainable parameters and GPU VRAM by orders of magnitude.',
     'Enables parameter-efficient fine-tuning on modest GPUs.'),
    (1, v_aiml_set_id, 'Mathematics for ML', 'Probability Distributions for ML', 'Vectors, Matrices & Eigenvalues', 'TECHNICAL', 'Medium',
     'Explain Maximum Likelihood Estimation (MLE) vs Maximum A Posteriori (MAP) estimation and how MAP relates to regularization.',
     'MLE chooses parameters maximizing `P(Data | theta)`. MAP maximizes `P(theta | Data) ∝ P(Data | theta) * P(theta)`. A Gaussian prior on `theta` yields L2 regularization; a Laplace prior yields L1 regularization.',
     'Connects Bayesian priors directly to regularized loss functions.'),
    (1, v_aiml_set_id, 'MLOps', 'Experiment Tracking & Model Versioning', 'Reproducible ML Pipelines', 'TECHNICAL', 'Medium',
     'What artifacts and metadata must be logged to make an ML experiment reproducible?',
     'Log code Git commit hash, dataset version/hash, random seeds, preprocessing pipeline parameters, hyperparameters, evaluation metrics, and serialized model weights + environment dependencies.',
     'Tools like MLflow/DVC automate experiment lineage.'),
    (1, v_aiml_set_id, 'Statistics', 'Correlation & Regression Fundamentals', NULL, 'TECHNICAL', 'Easy',
     'What is multicollinearity in linear regression, how do you detect it (VIF), and why does it matter?',
     'Multicollinearity occurs when predictor features are highly correlated, inflating coefficient variance and making individual feature weights unstable. Detect via Variance Inflation Factor (VIF > 5–10) and mitigate via feature removal, PCA, or Ridge regularization.',
     'Note: multicollinearity hurts coefficient interpretability more than raw predictions.'),

    -- AI/ML HR (3)
    (1, v_aiml_set_id, 'MLOps', 'Reproducible ML Pipelines', NULL, 'HR', 'Medium',
     'Describe an ML project where offline validation metrics looked strong, but real-world or edge-case behavior revealed limitations.',
     'Use STAR: describe the dataset bias/leakage/edge-case discovered, how you diagnosed error slices, improved data/features, and validated realistically.',
     'Shows maturity beyond chasing leaderboard accuracy.'),
    (1, v_aiml_set_id, 'Machine Learning', 'Supervised Learning: Linear & Logistic Regression', NULL, 'HR', 'Easy',
     'How do you decide when a simple baseline (e.g., Logistic Regression or heuristics) is preferable to a deep neural network?',
     'Discuss latency, data size, interpretability requirements, maintenance cost, and starting with a fast baseline to establish whether added complexity actually improves business metrics.',
     'Reflects pragmatic applied ML engineering.'),
    (1, v_aiml_set_id, 'Advanced Machine Learning', 'LLM Prompting & RAG Fundamentals', NULL, 'HR', 'Easy',
     'How do you stay current with fast-moving AI/ML research while still shipping reliable fundamentals?',
     'Discuss balancing foundational math/engineering mastery with reading targeted papers/benchmarks and testing ideas in small reproducible experiments.',
     'Highlights grounded continuous learning.');

    -- Insert new questions idempotently (avoiding duplicates in the same question_set)
    INSERT INTO questions (question_set_id, skill_id, question_text, answer, explanation, difficulty, question_type)
    SELECT t.question_set_id, sk.id, t.question_text, t.answer, t.explanation, t.difficulty, t.question_type
    FROM tmp_new_questions t
    JOIN skills sk ON LOWER(sk.name) = LOWER(t.topic_name)
    WHERE NOT EXISTS (
        SELECT 1 FROM questions q
        WHERE q.question_set_id = t.question_set_id
          AND q.question_text = t.question_text
    );

    -- Record newly inserted questions in seed_manifest
    INSERT INTO seed_manifest (migration_version, table_name, record_key, record_id)
    SELECT 'V4', 'questions', q.question_set_id || ':' || MD5(q.question_text), q.id
    FROM tmp_new_questions t
    JOIN questions q ON q.question_set_id = t.question_set_id AND q.question_text = t.question_text
    ON CONFLICT (migration_version, table_name, record_key) DO NOTHING;

    -- Map subtopic 1 (enforcing that the subtopic belongs to the question's career curriculum in career_skills!)
    INSERT INTO question_subtopics (question_id, skill_id)
    SELECT q.id, s1.id
    FROM tmp_new_questions t
    JOIN questions q ON q.question_set_id = t.question_set_id AND q.question_text = t.question_text
    JOIN skills s1 ON LOWER(s1.name) = LOWER(t.subtopic1_name) AND s1.node_type = 'SUBTOPIC'
    JOIN career_skills cs ON cs.career_path_id = t.career_path_id AND cs.skill_id = s1.id
    ON CONFLICT (question_id, skill_id) DO NOTHING;

    -- Map subtopic 2 when present (also enforcing career_skills membership)
    INSERT INTO question_subtopics (question_id, skill_id)
    SELECT q.id, s2.id
    FROM tmp_new_questions t
    JOIN questions q ON q.question_set_id = t.question_set_id AND q.question_text = t.question_text
    JOIN skills s2 ON t.subtopic2_name IS NOT NULL AND LOWER(s2.name) = LOWER(t.subtopic2_name) AND s2.node_type = 'SUBTOPIC'
    JOIN career_skills cs ON cs.career_path_id = t.career_path_id AND cs.skill_id = s2.id
    ON CONFLICT (question_id, skill_id) DO NOTHING;
END $$;

-- Career Compass V6: Cybersecurity Engineer stream
-- Completes the fourth MVP career stream (SDE, Data Analyst, AI/ML Engineer, Cybersecurity Engineer).
-- Additive and idempotent. All rows are resolved by NAME (never by hard-coded ID).

DO $$
DECLARE
    v_cyber_id   BIGINT;
    v_creator_id BIGINT;
    v_set_id     BIGINT;
BEGIN
    -- ---------------------------------------------------------------------
    -- 0. Career path
    -- ---------------------------------------------------------------------
    INSERT INTO career_paths (name, description, difficulty, average_preparation_months)
    SELECT 'Cybersecurity Engineer',
           'Protect infrastructure, conduct vulnerability assessments, and secure enterprise networks and applications.',
           'Intermediate', 6
    WHERE NOT EXISTS (SELECT 1 FROM career_paths WHERE name = 'Cybersecurity Engineer');

    SELECT id INTO v_cyber_id FROM career_paths WHERE name = 'Cybersecurity Engineer';

    -- ---------------------------------------------------------------------
    -- 1. Topics (TOPIC nodes) and subtopics (SUBTOPIC nodes)
    -- ---------------------------------------------------------------------
    INSERT INTO skills (name, category, description, difficulty, parent_skill_id, node_type) VALUES
    ('Linux', 'Tools', 'Linux command line, file system, permissions and system administration.', 'Beginner', NULL, 'TOPIC'),
    ('Network Security', 'Cybersecurity', 'Defending networks with segmentation, firewalls, IDS/IPS and monitoring.', 'Intermediate', NULL, 'TOPIC'),
    ('Cryptography', 'Cybersecurity', 'Encryption, hashing, signatures and key management.', 'Intermediate', NULL, 'TOPIC'),
    ('Web Application Security', 'Cybersecurity', 'Finding and preventing common web vulnerabilities (OWASP Top 10).', 'Intermediate', NULL, 'TOPIC'),
    ('Security Operations & Incident Response', 'Cybersecurity', 'Monitoring, detection, triage and response to security incidents.', 'Intermediate', NULL, 'TOPIC')
    ON CONFLICT (name) DO NOTHING;

    CREATE TEMP TABLE tmp_cyber_subtopics (
        name VARCHAR(120) NOT NULL,
        parent_name VARCHAR(120) NOT NULL,
        difficulty VARCHAR(40) NOT NULL,
        description TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tmp_cyber_subtopics VALUES
    ('Linux File System & Permissions', 'Linux', 'Beginner', 'Directory hierarchy, users/groups, chmod/chown, SUID and sticky bits.'),
    ('Shell Scripting & Bash Automation', 'Linux', 'Beginner', 'Bash variables, loops, pipes, grep/awk/sed for automating admin tasks.'),
    ('Linux Processes, Services & Logs', 'Linux', 'Intermediate', 'ps/top, systemd services, cron, and reading /var/log and journalctl.'),
    ('Firewalls, IDS & IPS', 'Network Security', 'Intermediate', 'Stateful firewalls, iptables rules, Snort/Suricata detection and prevention.'),
    ('Network Scanning & Enumeration (Nmap)', 'Network Security', 'Intermediate', 'Host discovery, port/service scanning and safe enumeration practice.'),
    ('Packet Analysis with Wireshark', 'Network Security', 'Intermediate', 'Capturing traffic, display filters and following TCP streams.'),
    ('VPNs & Secure Network Architecture', 'Network Security', 'Intermediate', 'Segmentation, DMZs, zero trust, IPsec and TLS VPNs.'),
    ('Symmetric & Asymmetric Encryption', 'Cryptography', 'Beginner', 'AES, block cipher modes, RSA, ECC and key exchange (Diffie-Hellman).'),
    ('Hashing, MACs & Digital Signatures', 'Cryptography', 'Intermediate', 'SHA-2, salted password hashing (bcrypt/argon2), HMAC and signatures.'),
    ('PKI, Certificates & TLS', 'Cryptography', 'Intermediate', 'Certificate authorities, X.509 chains, revocation and TLS configuration.'),
    ('OWASP Top 10 Vulnerabilities', 'Web Application Security', 'Beginner', 'Broken access control, injection, misconfiguration and other top risks.'),
    ('SQL Injection & XSS Prevention', 'Web Application Security', 'Intermediate', 'Parameterized queries, output encoding and Content Security Policy.'),
    ('Authentication, Sessions & Access Control', 'Web Application Security', 'Intermediate', 'Secure login, session cookies, CSRF, MFA and authorization checks.'),
    ('SIEM & Log Monitoring', 'Security Operations & Incident Response', 'Intermediate', 'Centralized logging, correlation rules and alert triage.'),
    ('Incident Response Lifecycle', 'Security Operations & Incident Response', 'Intermediate', 'Preparation, detection, containment, eradication, recovery and lessons learned.'),
    ('Vulnerability Assessment & Risk Management', 'Security Operations & Incident Response', 'Intermediate', 'CVE/CVSS scoring, patch prioritization and risk treatment.');

    INSERT INTO skills (name, category, description, difficulty, parent_skill_id, node_type)
    SELECT t.name, 'Cybersecurity', t.description, t.difficulty, p.id, 'SUBTOPIC'
    FROM tmp_cyber_subtopics t
    JOIN skills p ON LOWER(p.name) = LOWER(t.parent_name)
    ON CONFLICT (name) DO NOTHING;

    -- ---------------------------------------------------------------------
    -- 2. Career curriculum with prerequisites
    -- ---------------------------------------------------------------------
    CREATE TEMP TABLE tmp_cyber_cs (
        skill_name VARCHAR(120) NOT NULL,
        importance INTEGER NOT NULL,
        required_level INTEGER NOT NULL,
        prereq_name VARCHAR(120)
    ) ON COMMIT DROP;

    INSERT INTO tmp_cyber_cs VALUES
    -- Topics
    ('Linux', 5, 4, NULL),
    ('Computer Networks', 5, 5, NULL),
    ('Operating Systems', 4, 4, 'Linux'),
    ('Python', 4, 3, NULL),
    ('Network Security', 5, 5, 'Computer Networks'),
    ('Cryptography', 4, 4, 'Computer Networks'),
    ('Web Application Security', 5, 4, 'Network Security'),
    ('Security Operations & Incident Response', 4, 4, 'Network Security'),
    -- Subtopics (shared with other streams)
    ('OSI & TCP/IP Layers', 5, 5, 'Computer Networks'),
    ('HTTP, HTTPS & TLS Handshake', 5, 4, 'OSI & TCP/IP Layers'),
    ('DNS, Routing & Load Balancing', 4, 4, 'OSI & TCP/IP Layers'),
    ('Processes, Threads & Scheduling', 3, 3, 'Operating Systems'),
    ('Memory Management & Paging', 3, 3, 'Processes, Threads & Scheduling'),
    ('Python Data Structures & Comprehensions', 3, 3, 'Python'),
    ('File I/O, JSON & CSV Processing', 3, 3, 'Python Data Structures & Comprehensions'),
    -- Subtopics (cybersecurity specific)
    ('Linux File System & Permissions', 5, 4, 'Linux'),
    ('Shell Scripting & Bash Automation', 4, 4, 'Linux File System & Permissions'),
    ('Linux Processes, Services & Logs', 4, 4, 'Linux File System & Permissions'),
    ('Firewalls, IDS & IPS', 5, 4, 'Network Security'),
    ('Network Scanning & Enumeration (Nmap)', 4, 4, 'OSI & TCP/IP Layers'),
    ('Packet Analysis with Wireshark', 4, 4, 'OSI & TCP/IP Layers'),
    ('VPNs & Secure Network Architecture', 4, 3, 'Firewalls, IDS & IPS'),
    ('Symmetric & Asymmetric Encryption', 5, 4, 'Cryptography'),
    ('Hashing, MACs & Digital Signatures', 5, 4, 'Symmetric & Asymmetric Encryption'),
    ('PKI, Certificates & TLS', 4, 4, 'Hashing, MACs & Digital Signatures'),
    ('OWASP Top 10 Vulnerabilities', 5, 5, 'Web Application Security'),
    ('SQL Injection & XSS Prevention', 5, 4, 'OWASP Top 10 Vulnerabilities'),
    ('Authentication, Sessions & Access Control', 5, 4, 'OWASP Top 10 Vulnerabilities'),
    ('SIEM & Log Monitoring', 4, 4, 'Linux Processes, Services & Logs'),
    ('Incident Response Lifecycle', 4, 4, 'Security Operations & Incident Response'),
    ('Vulnerability Assessment & Risk Management', 4, 4, 'Network Scanning & Enumeration (Nmap)');

    INSERT INTO career_skills (career_path_id, skill_id, importance, required_level, prerequisite_skill_id)
    SELECT v_cyber_id, s.id, t.importance, t.required_level, pr.id
    FROM tmp_cyber_cs t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    LEFT JOIN skills pr ON t.prereq_name IS NOT NULL AND LOWER(pr.name) = LOWER(t.prereq_name)
    ON CONFLICT (career_path_id, skill_id) DO NOTHING;

    UPDATE career_skills cs
    SET prerequisite_skill_id = pr.id
    FROM tmp_cyber_cs t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    JOIN skills pr ON t.prereq_name IS NOT NULL AND LOWER(pr.name) = LOWER(t.prereq_name)
    WHERE cs.career_path_id = v_cyber_id
      AND cs.skill_id = s.id
      AND cs.prerequisite_skill_id IS NULL;

    -- ---------------------------------------------------------------------
    -- 3. Company focus for Amazon, Microsoft and TCS (GENERAL evidence only)
    -- ---------------------------------------------------------------------
    CREATE TEMP TABLE tmp_cyber_ccs (
        company_name VARCHAR(120) NOT NULL,
        skill_name VARCHAR(120) NOT NULL,
        target_level INTEGER NOT NULL,
        importance INTEGER NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tmp_cyber_ccs VALUES
    ('Amazon', 'Network Security', 5, 5),
    ('Amazon', 'Web Application Security', 5, 5),
    ('Amazon', 'Authentication, Sessions & Access Control', 5, 5),
    ('Amazon', 'Linux', 4, 4),
    ('Amazon', 'Python', 4, 4),
    ('Amazon', 'Incident Response Lifecycle', 4, 4),
    ('Microsoft', 'Network Security', 5, 5),
    ('Microsoft', 'Cryptography', 4, 5),
    ('Microsoft', 'PKI, Certificates & TLS', 4, 4),
    ('Microsoft', 'SIEM & Log Monitoring', 4, 5),
    ('Microsoft', 'Security Operations & Incident Response', 4, 5),
    ('Microsoft', 'Operating Systems', 4, 4),
    ('TCS', 'Computer Networks', 4, 5),
    ('TCS', 'Linux', 4, 4),
    ('TCS', 'Firewalls, IDS & IPS', 4, 4),
    ('TCS', 'Vulnerability Assessment & Risk Management', 4, 5),
    ('TCS', 'OWASP Top 10 Vulnerabilities', 4, 4),
    ('TCS', 'SIEM & Log Monitoring', 3, 4);

    INSERT INTO company_career_skills (company_id, career_path_id, skill_id, target_level, importance, evidence_type, source_url, reviewed_at)
    SELECT c.id, v_cyber_id, s.id, t.target_level, t.importance, 'GENERAL', NULL, DATE '2026-11-26'
    FROM tmp_cyber_ccs t
    JOIN companies c ON LOWER(c.name) = LOWER(t.company_name)
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    JOIN career_skills cs ON cs.career_path_id = v_cyber_id AND cs.skill_id = s.id
    ON CONFLICT (company_id, career_path_id, skill_id) DO NOTHING;

    -- ---------------------------------------------------------------------
    -- 4. Learning resources
    -- ---------------------------------------------------------------------
    CREATE TEMP TABLE tmp_cyber_res (
        skill_name VARCHAR(120) NOT NULL,
        title VARCHAR(200) NOT NULL,
        description TEXT NOT NULL,
        url VARCHAR(2048) NOT NULL,
        rtype VARCHAR(40) NOT NULL,
        difficulty VARCHAR(40) NOT NULL,
        provider VARCHAR(120) NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tmp_cyber_res VALUES
    ('Linux', 'Linux Journey', 'Free beginner-friendly lessons on the Linux command line, permissions and processes.', 'https://linuxjourney.com/', 'COURSE', 'Beginner', 'Linux Journey'),
    ('Linux File System & Permissions', 'OverTheWire: Bandit Wargame', 'Hands-on wargame for practising Linux commands, permissions and basic security concepts.', 'https://overthewire.org/wargames/bandit/', 'PRACTICE', 'Beginner', 'OverTheWire'),
    ('Shell Scripting & Bash Automation', 'GNU Bash Reference Manual', 'Official reference for Bash syntax, expansions, redirection and scripting.', 'https://www.gnu.org/software/bash/manual/bash.html', 'DOCS', 'Intermediate', 'GNU Project'),
    ('Network Security', 'TryHackMe Learning Paths', 'Guided, browser-based cybersecurity labs from fundamentals to SOC and pentesting.', 'https://tryhackme.com/', 'PRACTICE', 'Beginner', 'TryHackMe'),
    ('Network Scanning & Enumeration (Nmap)', 'Nmap Reference Guide', 'Official Nmap manual covering host discovery, port scanning and service detection.', 'https://nmap.org/book/man.html', 'DOCS', 'Intermediate', 'Nmap Project'),
    ('Packet Analysis with Wireshark', 'Wireshark User''s Guide', 'Official guide to capturing packets, display filters and protocol analysis.', 'https://www.wireshark.org/docs/wsug_html_chunked/', 'DOCS', 'Intermediate', 'Wireshark Foundation'),
    ('Cryptography', 'Cloudflare Learning: What is Encryption?', 'Plain-language explanations of encryption, keys and TLS.', 'https://www.cloudflare.com/learning/ssl/what-is-encryption/', 'ARTICLE', 'Beginner', 'Cloudflare'),
    ('Hashing, MACs & Digital Signatures', 'OWASP Password Storage Cheat Sheet', 'Guidance on salted, slow password hashing (Argon2id, bcrypt, scrypt).', 'https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html', 'DOCS', 'Intermediate', 'OWASP'),
    ('PKI, Certificates & TLS', 'Cloudflare Learning: What is TLS?', 'How TLS, certificates and the handshake protect traffic.', 'https://www.cloudflare.com/learning/ssl/transport-layer-security-tls/', 'ARTICLE', 'Beginner', 'Cloudflare'),
    ('Web Application Security', 'PortSwigger Web Security Academy', 'Free interactive labs covering SQL injection, XSS, access control and more.', 'https://portswigger.net/web-security', 'PRACTICE', 'Intermediate', 'PortSwigger'),
    ('OWASP Top 10 Vulnerabilities', 'OWASP Top 10', 'The standard awareness document for the most critical web application security risks.', 'https://owasp.org/www-project-top-ten/', 'DOCS', 'Beginner', 'OWASP'),
    ('SQL Injection & XSS Prevention', 'OWASP SQL Injection Prevention Cheat Sheet', 'Parameterized queries, stored procedures and allow-list validation.', 'https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html', 'DOCS', 'Intermediate', 'OWASP'),
    ('Authentication, Sessions & Access Control', 'OWASP Authentication Cheat Sheet', 'Best practices for login, password policy, MFA and session handling.', 'https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html', 'DOCS', 'Intermediate', 'OWASP'),
    ('Security Operations & Incident Response', 'MITRE ATT&CK Framework', 'Knowledge base of adversary tactics and techniques used for detection engineering.', 'https://attack.mitre.org/', 'DOCS', 'Intermediate', 'MITRE'),
    ('Incident Response Lifecycle', 'NIST SP 800-61 Computer Security Incident Handling Guide', 'NIST guidance on preparing for, detecting and responding to incidents.', 'https://csrc.nist.gov/pubs/sp/800/61/r2/final', 'DOCS', 'Intermediate', 'NIST'),
    ('Vulnerability Assessment & Risk Management', 'FIRST CVSS v3.1 Specification', 'How vulnerabilities are scored for severity and prioritization.', 'https://www.first.org/cvss/v3.1/specification-document', 'DOCS', 'Intermediate', 'FIRST');

    INSERT INTO learning_resources (skill_id, title, description, resource_url, resource_type, difficulty, language, provider)
    SELECT s.id, t.title, t.description, t.url, t.rtype, t.difficulty, 'English', t.provider
    FROM tmp_cyber_res t
    JOIN skills s ON LOWER(s.name) = LOWER(t.skill_name)
    ON CONFLICT (skill_id, resource_url) DO NOTHING;

    -- ---------------------------------------------------------------------
    -- 5. General-practice question set (not attributed to any company)
    -- ---------------------------------------------------------------------
    SELECT id INTO v_creator_id
    FROM users
    ORDER BY CASE WHEN email = 'admin@careercompass.io' THEN 0 ELSE 1 END, id
    LIMIT 1;

    IF v_creator_id IS NULL THEN
        RAISE NOTICE 'V6: no users yet, skipping Cybersecurity question set';
        RETURN;
    END IF;

    INSERT INTO question_sets (title, company_id, career_path_id, branch, batch, difficulty, created_by_id)
    SELECT 'Cybersecurity Engineer Practice Bank', NULL, v_cyber_id, 'Computer Science / IT', 'General Practice', 'Mixed', v_creator_id
    WHERE NOT EXISTS (SELECT 1 FROM question_sets WHERE title = 'Cybersecurity Engineer Practice Bank');

    SELECT id INTO v_set_id FROM question_sets WHERE title = 'Cybersecurity Engineer Practice Bank' LIMIT 1;

    CREATE TEMP TABLE tmp_cyber_q (
        topic_name VARCHAR(120) NOT NULL,
        subtopic_name VARCHAR(120) NOT NULL,
        question_type VARCHAR(30) NOT NULL,
        difficulty VARCHAR(40) NOT NULL,
        question_text TEXT NOT NULL,
        answer TEXT NOT NULL,
        explanation TEXT NOT NULL
    ) ON COMMIT DROP;

    INSERT INTO tmp_cyber_q VALUES
    -- MCQ
    ('Computer Networks', 'OSI & TCP/IP Layers', 'MCQ', 'Easy',
     'At which OSI layer does a traditional stateful firewall filtering on IP addresses and ports primarily operate? A) Layer 2  B) Layers 3-4  C) Layer 6  D) Layer 7',
     'B', 'IP addresses are Layer 3 and TCP/UDP ports are Layer 4; application-aware (Layer 7) inspection is a next-generation firewall feature.'),
    ('Cryptography', 'Symmetric & Asymmetric Encryption', 'MCQ', 'Easy',
     'Which algorithm is symmetric? A) RSA  B) ECDSA  C) AES  D) Diffie-Hellman',
     'C', 'AES uses the same secret key to encrypt and decrypt. RSA/ECDSA are asymmetric and Diffie-Hellman is a key-exchange protocol.'),
    ('Cryptography', 'Hashing, MACs & Digital Signatures', 'MCQ', 'Medium',
     'Which is the most appropriate way to store user passwords? A) AES-encrypted  B) SHA-256 without salt  C) Base64-encoded  D) bcrypt/Argon2 with a per-user salt',
     'D', 'Passwords should be hashed with a slow, salted, adaptive function so stolen hashes are expensive to crack.'),
    ('Web Application Security', 'SQL Injection & XSS Prevention', 'MCQ', 'Easy',
     'What is the primary defence against SQL injection? A) Input length limits  B) Parameterized queries / prepared statements  C) HTTPS  D) Hiding error messages',
     'B', 'Parameterized queries keep data separate from SQL code so user input is never executed as SQL.'),
    ('Linux', 'Linux File System & Permissions', 'MCQ', 'Easy',
     'What does chmod 640 file.txt grant? A) Owner rw, group r, others none  B) Owner rwx, group rw, others none  C) Everyone read  D) Owner r, group rw, others none',
     'A', '6 = read+write for owner, 4 = read for group, 0 = no permissions for others.'),
    ('Network Security', 'Firewalls, IDS & IPS', 'MCQ', 'Medium',
     'What is the key difference between an IDS and an IPS? A) IDS encrypts traffic  B) IPS sits inline and can block traffic; IDS only alerts  C) IDS is hardware only  D) No difference',
     'B', 'An IPS is placed inline to actively drop malicious traffic, while an IDS passively monitors and raises alerts.'),
    ('Computer Networks', 'HTTP, HTTPS & TLS Handshake', 'MCQ', 'Medium',
     'Which TCP port is used by HTTPS by default? A) 21  B) 22  C) 80  D) 443',
     'D', 'HTTPS defaults to port 443; port 80 is plain HTTP, 22 is SSH and 21 is FTP.'),
    ('Security Operations & Incident Response', 'Incident Response Lifecycle', 'MCQ', 'Medium',
     'In the NIST incident response lifecycle, which phase comes immediately after Detection & Analysis? A) Preparation  B) Containment, Eradication & Recovery  C) Post-Incident Activity  D) Risk Acceptance',
     'B', 'NIST SP 800-61 phases: Preparation, Detection & Analysis, Containment/Eradication/Recovery, Post-Incident Activity.'),
    -- TECHNICAL
    ('Cryptography', 'PKI, Certificates & TLS', 'TECHNICAL', 'Medium',
     'Explain what happens during a TLS handshake at a high level.',
     'The client sends supported versions/ciphers; the server replies with its choice and its certificate; the client validates the certificate chain against trusted CAs; both sides perform an (EC)DHE key exchange to derive shared session keys; subsequent traffic is encrypted symmetrically.',
     'Asymmetric cryptography authenticates the server and agrees on keys; fast symmetric encryption protects the data.'),
    ('Web Application Security', 'SQL Injection & XSS Prevention', 'TECHNICAL', 'Medium',
     'What is the difference between stored, reflected and DOM-based XSS, and how do you prevent XSS?',
     'Stored XSS is saved on the server and served to other users; reflected XSS is echoed back from a request parameter; DOM-based XSS happens entirely in client-side JavaScript. Prevent with context-aware output encoding, avoiding innerHTML with untrusted data, input validation, and a Content Security Policy.',
     'Encoding output for the right context (HTML, attribute, JS, URL) is the core defence.'),
    ('Web Application Security', 'Authentication, Sessions & Access Control', 'TECHNICAL', 'Medium',
     'What is CSRF and what are two ways to prevent it?',
     'Cross-Site Request Forgery tricks a logged-in user''s browser into sending an unwanted state-changing request. Prevent it with synchronizer (anti-CSRF) tokens and SameSite cookies, plus re-authentication for sensitive actions.',
     'CSRF abuses automatic cookie sending; tokens prove the request came from your own page.'),
    ('Network Security', 'VPNs & Secure Network Architecture', 'TECHNICAL', 'Medium',
     'What is the principle of least privilege and how does network segmentation support defence in depth?',
     'Least privilege grants users and systems only the access they need. Segmentation (VLANs, subnets, DMZs, firewalls between zones) limits lateral movement so a compromise in one zone does not expose the whole network.',
     'Layered controls mean a single failure does not lead to full compromise.'),
    ('Security Operations & Incident Response', 'Vulnerability Assessment & Risk Management', 'TECHNICAL', 'Medium',
     'How would you prioritize patching 200 vulnerabilities found in a scan?',
     'Rank by risk, not just CVSS: consider CVSS severity, known exploitation (e.g. CISA KEV), asset criticality and exposure (internet-facing first), available compensating controls, and business impact. Patch critical exploitable internet-facing issues first.',
     'Risk = likelihood x impact; context matters more than raw scores.'),
    ('Linux', 'Linux Processes, Services & Logs', 'TECHNICAL', 'Easy',
     'Where would you look on a Linux server to investigate repeated failed SSH logins?',
     'Check /var/log/auth.log (Debian/Ubuntu) or /var/log/secure (RHEL), or use journalctl -u ssh / sshd. Look for "Failed password" entries and source IPs; consider fail2ban and key-only authentication.',
     'Authentication logs are the first source for brute-force detection.'),
    ('Network Security', 'Packet Analysis with Wireshark', 'TECHNICAL', 'Medium',
     'Describe the TCP three-way handshake and how a SYN flood abuses it.',
     'Client sends SYN, server replies SYN-ACK and allocates state, client sends ACK. A SYN flood sends many SYNs without completing the handshake, exhausting the server''s half-open connection table. Mitigations include SYN cookies and rate limiting.',
     'Understanding protocol state is key to recognizing DoS patterns in packet captures.'),
    -- CODING
    ('Python', 'File I/O, JSON & CSV Processing', 'CODING', 'Medium',
     'Write a Python function that reads an auth log file and returns the top 5 IP addresses with the most "Failed password" lines.',
     'import re\nfrom collections import Counter\ndef top_failed_ips(path):\n    pat = re.compile(r"Failed password.* from (\d+\.\d+\.\d+\.\d+)")\n    counts = Counter()\n    with open(path) as f:\n        for line in f:\n            m = pat.search(line)\n            if m:\n                counts[m.group(1)] += 1\n    return counts.most_common(5)',
     'Combines regex parsing and Counter, a common security-automation task. Practise on your own sample logs.'),
    ('Linux', 'Shell Scripting & Bash Automation', 'CODING', 'Easy',
     'Write a one-line Bash command that lists the 10 most frequent source IPs in an Nginx access.log.',
     'awk ''{print $1}'' access.log | sort | uniq -c | sort -rn | head -10',
     'Pipelines of awk, sort and uniq are standard for quick log triage.'),
    ('Cryptography', 'Hashing, MACs & Digital Signatures', 'CODING', 'Medium',
     'In Python, verify a webhook payload using HMAC-SHA256 given a shared secret and a received hex signature.',
     'import hmac, hashlib\ndef verify(secret: bytes, body: bytes, signature_hex: str) -> bool:\n    expected = hmac.new(secret, body, hashlib.sha256).hexdigest()\n    return hmac.compare_digest(expected, signature_hex)',
     'compare_digest prevents timing attacks when comparing signatures.'),
    -- HR
    ('Security Operations & Incident Response', 'Incident Response Lifecycle', 'HR', 'Easy',
     'Tell me about a time you found a security issue or bug. How did you report and handle it responsibly?',
     'Use STAR: describe the situation, how you verified the issue without causing harm, how you reported it through the proper channel, and the outcome or fix.',
     'Interviewers look for ethics, communication and responsible disclosure.'),
    ('Network Security', 'Firewalls, IDS & IPS', 'HR', 'Easy',
     'How do you keep up to date with new threats and vulnerabilities?',
     'Mention concrete sources (CISA advisories, vendor bulletins, OWASP, security blogs/podcasts), hands-on labs (TryHackMe, PortSwigger), and CTFs or home labs.',
     'Shows continuous learning, which is essential in security roles.');

    INSERT INTO questions (question_set_id, skill_id, question_text, answer, explanation, difficulty, question_type)
    SELECT v_set_id, sk.id, t.question_text, t.answer, t.explanation, t.difficulty, t.question_type
    FROM tmp_cyber_q t
    JOIN skills sk ON LOWER(sk.name) = LOWER(t.topic_name)
    WHERE NOT EXISTS (
        SELECT 1 FROM questions q WHERE q.question_set_id = v_set_id AND q.question_text = t.question_text
    );

    INSERT INTO question_subtopics (question_id, skill_id)
    SELECT q.id, s.id
    FROM tmp_cyber_q t
    JOIN questions q ON q.question_set_id = v_set_id AND q.question_text = t.question_text
    JOIN skills s ON LOWER(s.name) = LOWER(t.subtopic_name)
    ON CONFLICT (question_id, skill_id) DO NOTHING;
END $$;

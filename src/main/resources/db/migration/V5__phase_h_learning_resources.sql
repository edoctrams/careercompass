-- Career Compass Phase H: Verified Learning Resources Population
-- Additive, idempotent migration. Preserves all existing V1-V4 records and IDs.

INSERT INTO learning_resources (skill_id, title, description, resource_url, resource_type, difficulty, language, provider)
VALUES
-- =====================================================================
-- 1. AI/ML ENGINEER & SHARED PYTHON / DATA / ML SKILLS
-- =====================================================================
(4, 'The Official Python Tutorial', 'Comprehensive official introduction to Python syntax, data structures, modules, and standard library.', 'https://docs.python.org/3/tutorial/', 'DOCS', 'Beginner', 'English', 'Python Software Foundation'),
(4, 'Kaggle Learn: Free Micro-Courses (Python, Pandas, ML)', 'Interactive, free browser-based micro-courses with hands-on notebooks covering Python, Pandas, and Machine Learning.', 'https://www.kaggle.com/learn', 'COURSE', 'Beginner', 'English', 'Kaggle'),
(176, 'Python CSV File Reading and Writing', 'Official Python standard library documentation for parsing and writing CSV datasets.', 'https://docs.python.org/3/library/csv.html', 'DOCS', 'Beginner', 'English', 'Python Software Foundation'),
(21, 'NumPy: The Absolute Basics for Beginners', 'Official NumPy beginner guide covering ndarray creation, indexing, slicing, and basic array operations.', 'https://numpy.org/doc/stable/user/absolute_beginners.html', 'DOCS', 'Beginner', 'English', 'NumPy'),
(178, 'NumPy Broadcasting Guide', 'Official documentation explaining how NumPy performs vectorized arithmetic across arrays of different shapes.', 'https://numpy.org/doc/stable/user/basics.broadcasting.html', 'DOCS', 'Intermediate', 'English', 'NumPy'),
(17, '10 Minutes to Pandas', 'Official quickstart introduction to Series, DataFrames, selection, grouping, and reshaping in Pandas.', 'https://pandas.pydata.org/docs/user_guide/10min.html', 'DOCS', 'Beginner', 'English', 'Pandas'),
(180, 'Pandas Getting Started Tutorials', 'Step-by-step official tutorials on DataFrame indexing, filtering, plotting, and summary statistics.', 'https://pandas.pydata.org/docs/getting_started/intro_tutorials/index.html', 'DOCS', 'Beginner', 'English', 'Pandas'),
(181, 'Pandas: Working with Missing Data', 'Official user guide covering NaN handling, imputation, interpolation, and dropping missing values.', 'https://pandas.pydata.org/docs/user_guide/missing_data.html', 'DOCS', 'Intermediate', 'English', 'Pandas'),
(15, 'scikit-learn Getting Started Guide', 'Official introduction to scikit-learn estimators, transformers, pipelines, and model evaluation.', 'https://scikit-learn.org/stable/getting_started.html', 'DOCS', 'Beginner', 'English', 'scikit-learn'),
(209, 'scikit-learn Preprocessing & Feature Encoding', 'Official guide on scaling, normalization, one-hot encoding, and imputation pipelines.', 'https://scikit-learn.org/stable/modules/preprocessing.html', 'DOCS', 'Intermediate', 'English', 'scikit-learn'),
(210, 'scikit-learn Cross-Validation Guide', 'Official documentation on k-fold cross-validation, stratified splits, and preventing data leakage.', 'https://scikit-learn.org/stable/modules/cross_validation.html', 'DOCS', 'Intermediate', 'English', 'scikit-learn'),
(211, 'scikit-learn Metrics and Model Evaluation', 'Official reference for classification, regression, and clustering evaluation metrics (ROC-AUC, F1, RMSE).', 'https://scikit-learn.org/stable/modules/model_evaluation.html', 'DOCS', 'Intermediate', 'English', 'scikit-learn'),
(16, 'Google Machine Learning Crash Course', 'Free self-study ML course by Google covering gradient descent, loss functions, generalization, and neural nets.', 'https://developers.google.com/machine-learning/crash-course', 'COURSE', 'Beginner', 'English', 'Google'),
(205, 'Google ML Crash Course: Linear & Logistic Regression', 'Foundational supervised learning concepts, loss optimization, and classification thresholds.', 'https://developers.google.com/machine-learning/crash-course', 'COURSE', 'Beginner', 'English', 'Google'),
(206, 'scikit-learn Decision Trees Guide', 'Official guide to decision tree classification, regression, splitting criteria, and pruning.', 'https://scikit-learn.org/stable/modules/tree.html', 'DOCS', 'Intermediate', 'English', 'scikit-learn'),
(207, 'scikit-learn Clustering & Unsupervised Learning', 'Official guide comparing K-Means, DBSCAN, hierarchical clustering, and evaluation.', 'https://scikit-learn.org/stable/modules/clustering.html', 'DOCS', 'Intermediate', 'English', 'scikit-learn'),
(3, 'PyTorch: Learn the Basics', 'Official PyTorch tutorial series covering Tensors, Datasets, autograd, and building a neural network.', 'https://pytorch.org/tutorials/beginner/basics/intro.html', 'TUTORIAL', 'Beginner', 'English', 'PyTorch'),
(3, 'TensorFlow 2 Quickstart for Beginners', 'Official TensorFlow Keras tutorial for building, training, and evaluating a neural network.', 'https://www.tensorflow.org/tutorials/quickstart/beginner', 'TUTORIAL', 'Beginner', 'English', 'TensorFlow'),
(212, 'PyTorch Autograd & Neural Network Basics', 'Hands-on PyTorch walkthrough of tensors, automatic differentiation, and backpropagation.', 'https://pytorch.org/tutorials/beginner/basics/intro.html', 'TUTORIAL', 'Beginner', 'English', 'PyTorch'),
(213, 'PyTorch CIFAR-10 Image Classifier (CNNs)', 'Official PyTorch tutorial training a convolutional neural network on image data.', 'https://pytorch.org/tutorials/beginner/blitz/cifar10_tutorial.html', 'TUTORIAL', 'Intermediate', 'English', 'PyTorch'),
(213, 'TensorFlow Convolutional Neural Network (CNN) Tutorial', 'Official TensorFlow tutorial building and training a CNN using Keras Sequential API.', 'https://www.tensorflow.org/tutorials/images/cnn', 'TUTORIAL', 'Intermediate', 'English', 'TensorFlow'),
(37, 'Hugging Face NLP & Transformers Course', 'Free comprehensive course on transformer architectures, tokenization, and fine-tuning.', 'https://huggingface.co/learn/nlp-course/chapter1/1', 'COURSE', 'Intermediate', 'English', 'Hugging Face'),
(215, 'PyTorch Sequence Modeling with nn.Transformer', 'Official PyTorch tutorial implementing attention and transformer layers.', 'https://pytorch.org/tutorials/beginner/transformer_tutorial.html', 'TUTORIAL', 'Advanced', 'English', 'PyTorch'),
(41, 'FastAPI Official Tutorial for ML Model Serving', 'Step-by-step official FastAPI guide for building typed REST APIs to serve ML models.', 'https://fastapi.tiangolo.com/tutorial/', 'DOCS', 'Intermediate', 'English', 'FastAPI'),
(219, 'FastAPI Tutorial — Request Bodies & Validation', 'Official FastAPI documentation for Pydantic request validation and JSON inference endpoints.', 'https://fastapi.tiangolo.com/tutorial/', 'DOCS', 'Intermediate', 'English', 'FastAPI'),

-- =====================================================================
-- 2. DATA ANALYST SKILLS (SQL, VISUALIZATION, EXCEL, BI)
-- =====================================================================
(20, 'SQLBolt: Interactive SQL Lessons & Exercises', 'Free interactive browser lessons practicing SELECT, JOINs, aggregations, and filtering.', 'https://sqlbolt.com/', 'PRACTICE', 'Beginner', 'English', 'SQLBolt'),
(163, 'PostgreSQL Official Tutorial: Joins Between Tables', 'Official PostgreSQL guide to inner, left, right, and self joins.', 'https://www.postgresql.org/docs/current/tutorial-join.html', 'DOCS', 'Beginner', 'English', 'PostgreSQL'),
(165, 'PostgreSQL Official Tutorial: Window Functions', 'Official PostgreSQL documentation explaining OVER(), PARTITION BY, and ranking functions.', 'https://www.postgresql.org/docs/current/tutorial-window.html', 'DOCS', 'Intermediate', 'English', 'PostgreSQL'),
(167, 'PostgreSQL EXPLAIN & Query Execution Plans', 'Official PostgreSQL guide to reading EXPLAIN ANALYZE plans and optimizing slow queries.', 'https://www.postgresql.org/docs/current/using-explain.html', 'DOCS', 'Advanced', 'English', 'PostgreSQL'),
(167, 'Use The Index, Luke! — SQL Indexing & Tuning', 'Free engineering guide to B-tree indexes, composite indexes, and SQL performance tuning.', 'https://use-the-index-luke.com/', 'GUIDE', 'Intermediate', 'English', 'Use The Index, Luke'),
(10, 'Kaggle Learn: Data Analysis & Cleaning Micro-Courses', 'Free interactive Kaggle courses for data cleaning, exploratory analysis, and visualization.', 'https://www.kaggle.com/learn', 'COURSE', 'Beginner', 'English', 'Kaggle'),
(38, 'Matplotlib Pyplot Official Tutorial', 'Official Matplotlib tutorial covering line, bar, scatter plots, and figure formatting.', 'https://matplotlib.org/stable/tutorials/pyplot.html', 'DOCS', 'Beginner', 'English', 'Matplotlib'),
(193, 'Seaborn User Guide & Statistical Plotting Tutorial', 'Official Seaborn tutorial on distributions, categorical plots, and multi-panel grids.', 'https://seaborn.pydata.org/tutorial.html', 'DOCS', 'Beginner', 'English', 'Seaborn'),
(194, 'Microsoft Learn: Power BI Training Modules', 'Free official Microsoft Learn learning paths for Power BI data modeling and dashboards.', 'https://learn.microsoft.com/en-us/training/powerplatform/power-bi', 'COURSE', 'Beginner', 'English', 'Microsoft Learn'),
(35, 'Microsoft Support: Create a PivotTable in Excel', 'Official Microsoft guide to building PivotTables for summarizing and slicing tabular datasets.', 'https://support.microsoft.com/en-us/office/create-a-pivottable-to-analyze-worksheet-data-a9a84538-bfe9-40a9-a8e9-f99134456576', 'DOCS', 'Beginner', 'English', 'Microsoft'),
(189, 'Excel PivotTables Step-by-Step Guide', 'Official Microsoft reference for PivotTable fields, grouping, and calculated summaries.', 'https://support.microsoft.com/en-us/office/create-a-pivottable-to-analyze-worksheet-data-a9a84538-bfe9-40a9-a8e9-f99134456576', 'DOCS', 'Beginner', 'English', 'Microsoft'),

-- =====================================================================
-- 3. SOFTWARE DEVELOPMENT ENGINEER (SDE) SKILLS
-- =====================================================================
(13, 'NeetCode DSA Practice Roadmap', 'Structured pattern-based DSA roadmap covering arrays, trees, graphs, and dynamic programming.', 'https://neetcode.io/roadmap', 'PRACTICE', 'Intermediate', 'English', 'NeetCode'),
(13, 'GeeksforGeeks Data Structures Reference', 'Comprehensive reference articles and code examples for core data structures.', 'https://www.geeksforgeeks.org/data-structures/', 'GUIDE', 'Beginner', 'English', 'GeeksforGeeks'),
(13, 'CodeChef Competitive & DSA Practice', 'Free coding practice problems across difficulty tiers for algorithmic problem solving.', 'https://www.codechef.com/practice', 'PRACTICE', 'Beginner', 'English', 'CodeChef'),
(110, 'VisuAlgo: Interactive Data Structures & Algorithms Visualizer', 'Interactive visualizations for sorting, linked lists, trees, and graph algorithms.', 'https://visualgo.net/en', 'TUTORIAL', 'Beginner', 'English', 'VisuAlgo'),
(114, 'CP-Algorithms: Graph & Shortest Path Algorithms', 'In-depth explanations and implementations of BFS, DFS, Dijkstra, and topological sort.', 'https://cp-algorithms.com/', 'GUIDE', 'Advanced', 'English', 'CP-Algorithms'),
(6, 'Dev.java: Official Java Learning Portal', 'Official Oracle Java tutorials covering language basics, classes, streams, and modern Java.', 'https://dev.java/learn/', 'DOCS', 'Beginner', 'English', 'Oracle'),
(138, 'Oracle Java Collections Framework Trail', 'Official Java tutorial covering List, Set, Map, Queue, and comparator patterns.', 'https://docs.oracle.com/javase/tutorial/collections/', 'DOCS', 'Intermediate', 'English', 'Oracle'),
(12, 'Oracle Java Object-Oriented Programming Concepts', 'Official guide to encapsulation, inheritance, interfaces, and polymorphism.', 'https://docs.oracle.com/javase/tutorial/java/concepts/', 'DOCS', 'Beginner', 'English', 'Oracle'),
(7, 'LearnCpp.com: Comprehensive C++ Tutorial', 'Free, detailed modern C++ tutorial from fundamentals through STL and memory management.', 'https://www.learncpp.com/', 'TUTORIAL', 'Beginner', 'English', 'LearnCpp'),
(1, 'Operating Systems: Three Easy Pieces (OSTEP)', 'Free university textbook covering virtualization, concurrency, locks, and persistence.', 'https://pages.cs.wisc.edu/~remzi/OSTEP/', 'BOOK', 'Intermediate', 'English', 'UW-Madison'),
(24, 'PostgreSQL Official Tutorial: Transactions & ACID', 'Official PostgreSQL documentation on atomic transactions, COMMIT/ROLLBACK, and isolation.', 'https://www.postgresql.org/docs/current/tutorial-transactions.html', 'DOCS', 'Intermediate', 'English', 'PostgreSQL'),
(18, 'MDN Web Docs: An Overview of HTTP', 'Clear explanation of HTTP messages, status codes, headers, TLS, and client-server flows.', 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview', 'DOCS', 'Beginner', 'English', 'MDN Web Docs'),
(28, 'System Design Primer (GitHub)', 'Open-source guide to load balancing, caching, sharding, message queues, and scalability.', 'https://github.com/donnemartin/system-design-primer', 'GUIDE', 'Advanced', 'English', 'GitHub / Donne Martin'),
(22, 'Pro Git Book (Official Free Edition)', 'Complete official Git book covering branching, merging, rebasing, and team workflows.', 'https://git-scm.com/book/en/v2', 'BOOK', 'Beginner', 'English', 'Git'),

-- =====================================================================
-- 4. FULL STACK DEVELOPER SKILLS
-- =====================================================================
(19, 'MDN Web Docs: Learn HTML', 'Official MDN structured learning path for semantic HTML5, forms, and accessibility.', 'https://developer.mozilla.org/en-US/docs/Learn/HTML', 'DOCS', 'Beginner', 'English', 'MDN Web Docs'),
(23, 'MDN Web Docs: Flexbox Layout Guide', 'Official MDN guide to flexible box layout alignment and responsive containers.', 'https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Flexbox', 'DOCS', 'Beginner', 'English', 'MDN Web Docs'),
(145, 'MDN Web Docs: CSS Grid Layout', 'Official MDN reference and tutorial for two-dimensional CSS Grid layouts.', 'https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_grid_layout', 'DOCS', 'Intermediate', 'English', 'MDN Web Docs'),
(5, 'MDN Web Docs: JavaScript Guide', 'Official MDN reference guide to JavaScript syntax, closures, prototypes, and async programming.', 'https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide', 'DOCS', 'Beginner', 'English', 'MDN Web Docs'),
(5, 'The Modern JavaScript Tutorial (JavaScript.info)', 'Free comprehensive tutorial covering JavaScript fundamentals, DOM, promises, and async/await.', 'https://javascript.info/', 'TUTORIAL', 'Beginner', 'English', 'JavaScript.info'),
(150, 'MDN Web Docs: Using the Fetch API', 'Official MDN guide to making HTTP requests, handling JSON, and error handling in browsers.', 'https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch', 'DOCS', 'Intermediate', 'English', 'MDN Web Docs'),
(11, 'React Official Learn Documentation', 'Official React documentation covering components, JSX, state, effects, and thinking in React.', 'https://react.dev/learn', 'DOCS', 'Beginner', 'English', 'React'),
(152, 'React Official Built-in Hooks Reference', 'Official React API reference for useState, useEffect, useContext, useRef, and custom hooks.', 'https://react.dev/reference/react/hooks', 'DOCS', 'Intermediate', 'English', 'React'),
(11, 'Next.js Official Learn Course', 'Free official interactive Next.js course covering React foundations, routing, and full-stack rendering.', 'https://nextjs.org/learn', 'COURSE', 'Intermediate', 'English', 'Vercel / Next.js'),
(2, 'Spring Guide: Building a RESTful Web Service', 'Official Spring Boot guide to creating REST controllers and JSON endpoints.', 'https://spring.io/guides/gs/rest-service', 'GUIDE', 'Beginner', 'English', 'Spring'),
(156, 'Spring Guide: Accessing Data with JPA', 'Official Spring Data JPA guide to entities, repositories, and database persistence.', 'https://spring.io/guides/gs/accessing-data-jpa', 'GUIDE', 'Intermediate', 'English', 'Spring'),
(157, 'Spring Guide: Securing a Web Application', 'Official Spring Security guide to form login, route authorization, and password encoding.', 'https://spring.io/guides/gs/securing-web', 'GUIDE', 'Intermediate', 'English', 'Spring'),
(9, 'REST API Tutorial & Best Practices', 'Clear guide to REST architectural constraints, resource naming, HTTP verbs, and status codes.', 'https://restfulapi.net/', 'GUIDE', 'Beginner', 'English', 'RESTfulAPI.net'),
(39, 'Docker Official Get Started Guide', 'Official Docker guide to containers, images, Dockerfiles, and local development workflows.', 'https://docs.docker.com/get-started/', 'DOCS', 'Beginner', 'English', 'Docker'),
(172, 'Docker Compose Getting Started', 'Official Docker guide to running multi-container applications with Compose.', 'https://docs.docker.com/compose/gettingstarted/', 'DOCS', 'Intermediate', 'English', 'Docker')
ON CONFLICT (skill_id, resource_url) DO NOTHING;

-- Record all V5 seeded learning resources in seed_manifest
INSERT INTO seed_manifest (migration_version, table_name, record_key, record_id)
SELECT
    'V5',
    'learning_resources',
    lr.skill_id || '::' || lr.resource_url,
    lr.id
FROM learning_resources lr
WHERE lr.resource_url IN (
    'https://docs.python.org/3/tutorial/',
    'https://www.kaggle.com/learn',
    'https://docs.python.org/3/library/csv.html',
    'https://numpy.org/doc/stable/user/absolute_beginners.html',
    'https://numpy.org/doc/stable/user/basics.broadcasting.html',
    'https://pandas.pydata.org/docs/user_guide/10min.html',
    'https://pandas.pydata.org/docs/getting_started/intro_tutorials/index.html',
    'https://pandas.pydata.org/docs/user_guide/missing_data.html',
    'https://scikit-learn.org/stable/getting_started.html',
    'https://scikit-learn.org/stable/modules/preprocessing.html',
    'https://scikit-learn.org/stable/modules/cross_validation.html',
    'https://scikit-learn.org/stable/modules/model_evaluation.html',
    'https://developers.google.com/machine-learning/crash-course',
    'https://scikit-learn.org/stable/modules/tree.html',
    'https://scikit-learn.org/stable/modules/clustering.html',
    'https://pytorch.org/tutorials/beginner/basics/intro.html',
    'https://www.tensorflow.org/tutorials/quickstart/beginner',
    'https://pytorch.org/tutorials/beginner/blitz/cifar10_tutorial.html',
    'https://www.tensorflow.org/tutorials/images/cnn',
    'https://huggingface.co/learn/nlp-course/chapter1/1',
    'https://pytorch.org/tutorials/beginner/transformer_tutorial.html',
    'https://fastapi.tiangolo.com/tutorial/',
    'https://sqlbolt.com/',
    'https://www.postgresql.org/docs/current/tutorial-join.html',
    'https://www.postgresql.org/docs/current/tutorial-window.html',
    'https://www.postgresql.org/docs/current/using-explain.html',
    'https://use-the-index-luke.com/',
    'https://matplotlib.org/stable/tutorials/pyplot.html',
    'https://seaborn.pydata.org/tutorial.html',
    'https://learn.microsoft.com/en-us/training/powerplatform/power-bi',
    'https://support.microsoft.com/en-us/office/create-a-pivottable-to-analyze-worksheet-data-a9a84538-bfe9-40a9-a8e9-f99134456576',
    'https://neetcode.io/roadmap',
    'https://www.geeksforgeeks.org/data-structures/',
    'https://www.codechef.com/practice',
    'https://visualgo.net/en',
    'https://cp-algorithms.com/',
    'https://dev.java/learn/',
    'https://docs.oracle.com/javase/tutorial/collections/',
    'https://docs.oracle.com/javase/tutorial/java/concepts/',
    'https://www.learncpp.com/',
    'https://pages.cs.wisc.edu/~remzi/OSTEP/',
    'https://www.postgresql.org/docs/current/tutorial-transactions.html',
    'https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview',
    'https://github.com/donnemartin/system-design-primer',
    'https://git-scm.com/book/en/v2',
    'https://developer.mozilla.org/en-US/docs/Learn/HTML',
    'https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Flexbox',
    'https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_grid_layout',
    'https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide',
    'https://javascript.info/',
    'https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch',
    'https://react.dev/learn',
    'https://react.dev/reference/react/hooks',
    'https://nextjs.org/learn',
    'https://spring.io/guides/gs/rest-service',
    'https://spring.io/guides/gs/accessing-data-jpa',
    'https://spring.io/guides/gs/securing-web',
    'https://restfulapi.net/',
    'https://docs.docker.com/get-started/',
    'https://docs.docker.com/compose/gettingstarted/'
)
ON CONFLICT (migration_version, table_name, record_key) DO NOTHING;

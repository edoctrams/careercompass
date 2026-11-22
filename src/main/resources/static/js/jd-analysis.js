/**
 * Career Compass — Job Description (JD) Skill Gap Analyzer Engine
 */

document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('jd-form');
  const jdInput = document.getElementById('jd-description');
  const companyInput = document.getElementById('jd-company');
  const roleInput = document.getElementById('jd-role');
  const submitBtn = document.getElementById('analyze-jd-btn');
  const resultsSection = document.getElementById('jd-results-section');

  // Sample JD autofill buttons
  const sampleBtns = document.querySelectorAll('.sample-jd-btn');
  const sampleJDs = {
    sde: {
      company: 'Google',
      role: 'Software Development Engineer',
      text: 'Minimum qualifications:\n- Bachelor’s degree in Computer Science, Computer Engineering, or related technical field.\n- Proficiency in Java, C++, or Python.\n- Strong expertise in Data Structures, Algorithms (DSA), OOP, and system design.\n- Solid understanding of Operating Systems, DBMS, SQL, and distributed REST APIs.'
    },
    backend: {
      company: 'Amazon AWS',
      role: 'Backend Systems Engineer',
      text: 'Basic Qualifications:\n- Experience building scalable backend microservices with Java, Spring Boot, and REST APIs.\n- Strong mastery of relational databases (PostgreSQL/SQL), DBMS optimization, and caching.\n- Knowledge of Computer Networks, Git version control, and Linux systems architecture.'
    },
    frontend: {
      company: 'Atlassian',
      role: 'Frontend Engineer',
      text: 'Requirements:\n- 1+ years building modern client-side web applications using React, JavaScript, HTML5, and CSS3.\n- Deep understanding of browser performance, state management, and consuming REST APIs.\n- Proficient with Git and agile developer workflows.'
    }
  };

  sampleBtns.forEach(btn => {
    btn.addEventListener('click', function () {
      const type = this.getAttribute('data-sample');
      if (sampleJDs[type]) {
        companyInput.value = sampleJDs[type].company;
        roleInput.value = sampleJDs[type].role;
        jdInput.value = sampleJDs[type].text;
        window.showToast('Sample ' + sampleJDs[type].role + ' JD loaded!', 'info');
      }
    });
  });

  if (form) {
    form.addEventListener('submit', async function (e) {
      e.preventDefault();

      const text = jdInput.value.trim();
      if (!text) {
        window.showToast('Please paste a job description text to analyze.', 'warning');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.innerHTML = '<span class="spinner"></span> Parsing & Extracting Skills...';

      try {
        const response = await fetch('/api/job-descriptions/analyze', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            companyName: companyInput.value.trim() || 'Target Company',
            role: roleInput.value.trim() || 'Software Engineer',
            description: text
          })
        });

        if (!response.ok) {
          throw new Error('Analysis request failed with status ' + response.status);
        }

        const data = await response.json();
        renderAnalysisResults(data);
        window.showToast('Job description analysis complete!', 'success');
      } catch (err) {
        console.error('JD analysis error:', err);
        window.showToast('Analysis error: ' + err.message, 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-microscope me-2"></i> Analyze Job Description';
      }
    });
  }

  function renderAnalysisResults(data) {
    if (!resultsSection) return;
    resultsSection.style.display = 'block';
    resultsSection.scrollIntoView({ behavior: 'smooth' });

    // Match percentage circle/bar
    const matchVal = document.getElementById('jd-match-percentage');
    const matchBar = document.getElementById('jd-match-bar');
    if (matchVal) matchVal.textContent = data.matchPercentage + '%';
    if (matchBar) {
      matchBar.style.width = data.matchPercentage + '%';
      if (data.matchPercentage >= 70) {
        matchBar.className = 'progress-bar progress-bar-success';
      } else if (data.matchPercentage >= 40) {
        matchBar.className = 'progress-bar progress-bar-warning';
      } else {
        matchBar.className = 'progress-bar';
      }
    }

    // Skills breakdown
    const skillsContainer = document.getElementById('jd-skills-container');
    if (skillsContainer && data.skillsAnalysis) {
      skillsContainer.innerHTML = '';
      if (data.skillsAnalysis.length === 0) {
        skillsContainer.innerHTML = '<p class="text-muted">No catalog technical skills explicitly identified in this text.</p>';
      } else {
        data.skillsAnalysis.forEach(item => {
          const badge = document.createElement('div');
          badge.className = 'card card-elevated p-3 mb-2';
          badge.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <strong style="color: var(--text-primary);">${item.skillName}</strong>
                <span class="badge badge-purple ms-2">${item.category}</span>
              </div>
              <div>
                ${item.possessed
                  ? '<span class="badge badge-green"><i class="fas fa-check"></i> You Possess This (Lvl ' + item.currentLevel + ')</span>'
                  : '<span class="badge badge-red"><i class="fas fa-times"></i> Missing / Untrained</span>'}
              </div>
            </div>
          `;
          skillsContainer.appendChild(badge);
        });
      }
    }

    // Recommendations
    const recsList = document.getElementById('jd-recommendations-list');
    if (recsList && data.recommendations) {
      recsList.innerHTML = '';
      data.recommendations.forEach(rec => {
        const li = document.createElement('li');
        li.className = 'mb-2';
        li.style.color = 'var(--text-secondary)';
        li.innerHTML = '<i class="fas fa-arrow-right text-primary me-2"></i> ' + rec;
        recsList.appendChild(li);
      });
    }
  }
});

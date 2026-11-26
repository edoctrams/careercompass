/**
 * Career Compass — Career-Specific Skill Assessment & Gap Analyzer Engine
 */

document.addEventListener('DOMContentLoaded', function () {
  const ratings = {};
  let savedRatingsByName = {};
  let currentCurriculumSkills = [];
  let activeCategory = 'ALL';

  const proficiencyDescriptions = [
    '0 - No knowledge (Never used or studied)',
    '1 - Beginner (Basic syntax / conceptual awareness)',
    '2 - Basic (Can read and write simple solutions)',
    '3 - Intermediate (Comfortable with core patterns)',
    '4 - Advanced (Deep understanding & production use)',
    '5 - Proficient (Mastery, optimization & architecture)'
  ];

  const careerSelect = document.getElementById('assessment-career-select');
  const tabsNav = document.getElementById('skill-category-tabs');
  const skillsGrid = document.getElementById('dynamic-skills-grid');
  const submitBtn = document.getElementById('submit-assessment-btn');

  initializeAssessmentPage();

  async function initializeAssessmentPage() {
    let targetCareer = 'Software Development Engineer (SDE)';
    try {
      const profileRes = await fetch('/api/user/profile');
      if (profileRes.ok) {
        const profile = await profileRes.json();
        if (profile && profile.targetRoleName) {
          targetCareer = profile.targetRoleName;
        }
      }
    } catch (e) {
      console.warn('Could not load student profile:', e);
    }

    if (careerSelect) {
      if ([...careerSelect.options].some(o => o.value === targetCareer)) {
        careerSelect.value = targetCareer;
      }
      careerSelect.addEventListener('change', async function () {
        const selectedRole = this.value;
        try {
          await fetch('/api/user/profile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ targetRoleName: selectedRole })
          });
        } catch (err) {
          console.warn('Could not update target role:', err);
        }
        await loadCurriculumForCareer(selectedRole);
      });
    }

    await loadExistingRatings();
    await loadCurriculumForCareer(careerSelect ? careerSelect.value : targetCareer);
  }

  async function loadExistingRatings() {
    try {
      const res = await fetch('/api/user/skills');
      if (!res.ok) return;
      const savedSkills = await res.json();
      savedRatingsByName = {};
      savedSkills.forEach(s => {
        if (s.skillName) {
          savedRatingsByName[s.skillName.toLowerCase()] = Number(s.proficiency);
          ratings[s.skillName] = Number(s.proficiency);
        }
      });

      if (savedSkills.length > 0) {
        const skillDetails = savedSkills.map(s => {
          const currentLevel = Number(s.proficiency);
          const requiredLevel = Number(s.requiredLevel);
          return {
            skillName: s.skillName,
            currentLevel,
            requiredLevel,
            skillGap: Math.max(0, requiredLevel - currentLevel),
            category: s.category
          };
        });
        const totalGap = skillDetails.reduce((sum, s) => sum + s.skillGap, 0);
        renderAssessmentResults({
          skillDetails,
          totalSkillsAssessed: skillDetails.length,
          averageSkillGap: totalGap / skillDetails.length
        }, false);
      }
    } catch (e) {
      console.warn('Could not preload saved skill ratings:', e);
    }
  }

  async function loadCurriculumForCareer(careerName) {
    try {
      const res = await fetch(`/api/careers/curriculum?name=${encodeURIComponent(careerName)}`);
      if (!res.ok) return;
      const data = await res.json();
      const allSkills = data.skills || [];

      // Prefer TOPIC nodes first so the form is concise, or show all if few topics exist
      const topicSkills = allSkills.filter(s => s.nodeType === 'TOPIC');
      const subtopicSkills = allSkills.filter(s => s.nodeType === 'SUBTOPIC');
      currentCurriculumSkills = topicSkills.length >= 5
        ? [...topicSkills, ...subtopicSkills]
        : allSkills;

      activeCategory = 'ALL';
      renderCategoryTabs();
      renderSkillCards();
    } catch (e) {
      console.warn('Could not load career curriculum:', e);
    }
  }

  function renderCategoryTabs() {
    if (!tabsNav) return;
    const hasTopics = currentCurriculumSkills.some(s => s.nodeType === 'TOPIC');
    const hasSubtopics = currentCurriculumSkills.some(s => s.nodeType === 'SUBTOPIC');
    const categories = [...new Set(currentCurriculumSkills.map(s => s.category || 'General'))];

    tabsNav.innerHTML = '';

    const tabs = [];
    if (hasTopics && hasSubtopics) {
      tabs.push({ key: 'TOPICS', label: 'Core Topics' });
      tabs.push({ key: 'ALL', label: `All Topics & Subtopics (${currentCurriculumSkills.length})` });
    } else {
      tabs.push({ key: 'ALL', label: `All Career Skills (${currentCurriculumSkills.length})` });
    }
    categories.forEach(cat => tabs.push({ key: `CAT:${cat}`, label: cat }));

    if (hasTopics && hasSubtopics && activeCategory === 'ALL') {
      activeCategory = 'TOPICS';
    }

    tabs.forEach(tab => {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = `tab-btn ${activeCategory === tab.key ? 'active' : ''}`;
      btn.textContent = tab.label;
      btn.addEventListener('click', () => {
        activeCategory = tab.key;
        tabsNav.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        renderSkillCards();
      });
      tabsNav.appendChild(btn);
    });
  }

  function renderSkillCards() {
    if (!skillsGrid) return;
    skillsGrid.innerHTML = '';

    const visibleSkills = currentCurriculumSkills.filter(s => {
      if (activeCategory === 'TOPICS') return s.nodeType === 'TOPIC';
      if (activeCategory.startsWith('CAT:')) return (s.category || 'General') === activeCategory.slice(4);
      return true;
    });

    visibleSkills.forEach(skill => {
      const skillName = skill.name;
      const savedLevel = ratings[skillName] !== undefined
        ? ratings[skillName]
        : savedRatingsByName[skillName.toLowerCase()];
      if (savedLevel !== undefined) {
        ratings[skillName] = savedLevel;
      }

      const card = document.createElement('div');
      card.className = 'card card-elevated p-4 skill-rating-card';
      card.setAttribute('data-skill-name', skillName);

      const prereqNote = skill.prerequisiteSkillName
        ? `Prerequisite: ${skill.prerequisiteSkillName}`
        : (skill.parentSkillName ? `Topic: ${skill.parentSkillName}` : 'Core Career Requirement');

      card.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem; margin-bottom: 0.5rem;">
          <div>
            <strong style="color: var(--text-main); font-size: 1.05rem;">${escapeHtml(skillName)}</strong>
            <span class="badge badge-purple ms-2">${escapeHtml(skill.category || 'General')}</span>
            ${skill.nodeType === 'SUBTOPIC' ? '<span class="badge badge-gray ms-1">Subtopic</span>' : ''}
          </div>
          <span class="badge badge-yellow">Target: Lvl ${skill.requiredLevel || 4} (Imp ${skill.importance || 4}/5)</span>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.35rem;">
          ${escapeHtml(skill.description || `Assess your current proficiency in ${skillName}.`)}
        </p>
        <p style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 0.85rem;">
          <i class="fas fa-diagram-project me-1"></i> ${escapeHtml(prereqNote)}
        </p>
        <div class="proficiency-selector mb-2">
          ${[0, 1, 2, 3, 4, 5].map(lvl => `
            <div class="level-pill ${savedLevel === lvl ? 'active' : ''}" data-level="${lvl}">${lvl}</div>
          `).join('')}
        </div>
        <span class="skill-level-desc form-helper" style="color: ${savedLevel >= 3 ? 'var(--color-success)' : 'var(--text-secondary)'};">
          ${savedLevel !== undefined ? proficiencyDescriptions[savedLevel] : 'Click a rating level from 0 to 5'}
        </span>
      `;

      const pills = card.querySelectorAll('.level-pill');
      const descSpan = card.querySelector('.skill-level-desc');
      pills.forEach(pill => {
        pill.addEventListener('click', function () {
          const level = parseInt(this.getAttribute('data-level'), 10);
          ratings[skillName] = level;
          savedRatingsByName[skillName.toLowerCase()] = level;
          pills.forEach(p => p.classList.remove('active'));
          this.classList.add('active');
          if (descSpan) {
            descSpan.textContent = proficiencyDescriptions[level];
            descSpan.style.color = level >= 3 ? 'var(--color-success)' : 'var(--text-secondary)';
          }
          updateSummaryMetrics();
        });
      });

      skillsGrid.appendChild(card);
    });

    updateSummaryMetrics();
  }

  function updateSummaryMetrics() {
    const countEl = document.getElementById('assessed-count');
    if (countEl) {
      countEl.textContent = Object.keys(ratings).length;
    }
  }

  if (submitBtn) {
    submitBtn.addEventListener('click', async function () {
      if (Object.keys(ratings).length === 0) {
        if (window.showToast) window.showToast('Please rate at least one skill before analyzing gaps.', 'warning');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.innerHTML = '<span class="spinner"></span> Analyzing Skill Gaps...';

      try {
        const response = await fetch('/api/skill-assessment/assess', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(ratings)
        });

        if (!response.ok) {
          throw new Error('Assessment submission failed with status ' + response.status);
        }

        const data = await response.json();
        renderAssessmentResults(data, true);

        // Regenerate active roadmap so new ratings take effect immediately
        try {
          await fetch('/api/roadmaps/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              careerPathName: careerSelect ? careerSelect.value : undefined
            })
          });
        } catch (ignored) {}

        if (window.showToast) {
          window.showToast('Skill gap analysis & personalized roadmap updated!', 'success');
        }
      } catch (err) {
        console.error('Assessment error:', err);
        if (window.showToast) {
          window.showToast('Error communicating with assessment API: ' + err.message, 'error');
        }
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-chart-line me-2"></i> Submit & View Gap Analysis';
      }
    });
  }

  function renderAssessmentResults(data, shouldScroll = true) {
    const resultsContainer = document.getElementById('assessment-results-section');
    if (!resultsContainer) return;

    resultsContainer.style.display = 'block';
    if (shouldScroll) {
      resultsContainer.scrollIntoView({ behavior: 'smooth' });
    }

    const avgGapEl = document.getElementById('avg-gap-metric');
    if (avgGapEl) {
      avgGapEl.textContent = data.averageSkillGap != null
        ? Number(data.averageSkillGap).toFixed(1)
        : '0.0';
    }

    const totalAssessedEl = document.getElementById('total-assessed-metric');
    if (totalAssessedEl) {
      totalAssessedEl.textContent = data.totalSkillsAssessed || 0;
    }

    const listContainer = document.getElementById('skill-breakdown-list');
    if (listContainer && data.skillDetails) {
      listContainer.innerHTML = '';
      const sorted = [...data.skillDetails].sort((a, b) => b.skillGap - a.skillGap);

      sorted.forEach(item => {
        const gap = Number(item.skillGap);
        const current = Number(item.currentLevel);
        const required = Number(item.requiredLevel);

        let statusBadge = '<span class="badge badge-green"><i class="fas fa-check"></i> Target Met</span>';
        if (gap > 2) {
          statusBadge = `<span class="badge badge-red"><i class="fas fa-exclamation-triangle"></i> Critical Gap (${gap})</span>`;
        } else if (gap > 0) {
          statusBadge = `<span class="badge badge-yellow"><i class="fas fa-arrow-up"></i> Moderate Gap (${gap})</span>`;
        }

        const row = document.createElement('div');
        row.className = 'card card-elevated mb-3 p-3';
        row.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
            <div>
              <strong style="color: var(--text-primary); font-size: 1rem;">${escapeHtml(item.skillName)}</strong>
              <span class="badge badge-purple ms-2">${escapeHtml(item.category || 'General')}</span>
            </div>
            <div>${statusBadge}</div>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; font-size: 0.85rem;">
            <div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 0.25rem;">
                <span style="color: var(--text-secondary);">Your Current Proficiency</span>
                <span style="font-weight: 700; color: var(--accent-secondary);">${current} / 5</span>
              </div>
              <div class="progress-container">
                <div class="progress-bar" style="width: ${(current / 5) * 100}%; background: var(--accent-secondary);"></div>
              </div>
            </div>
            <div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 0.25rem;">
                <span style="color: var(--text-secondary);">Required Level for Target Role</span>
                <span style="font-weight: 700; color: var(--accent-primary);">${required} / 5</span>
              </div>
              <div class="progress-container">
                <div class="progress-bar" style="width: ${(required / 5) * 100}%; background: var(--accent-primary);"></div>
              </div>
            </div>
          </div>
        `;
        listContainer.appendChild(row);
      });
    }
  }

  function escapeHtml(str) {
    return String(str || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
});

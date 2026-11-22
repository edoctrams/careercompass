/**
 * Career Compass — Skill Assessment & Gap Analyzer Engine
 */

document.addEventListener('DOMContentLoaded', function () {
  const ratings = {};
  const proficiencyDescriptions = [
    '0 - No knowledge (Never used or studied)',
    '1 - Beginner (Basic syntax / conceptual awareness)',
    '2 - Basic (Can read and write simple solutions)',
    '3 - Intermediate (Comfortable with core patterns)',
    '4 - Advanced (Deep understanding & production use)',
    '5 - Proficient (Mastery, optimization & architecture)'
  ];

  // Initialize click handlers on level pills
  const skillCards = document.querySelectorAll('.skill-rating-card');
  skillCards.forEach(card => {
    const skillName = card.getAttribute('data-skill-name');
    const pills = card.querySelectorAll('.level-pill');
    const descSpan = card.querySelector('.skill-level-desc');

    pills.forEach(pill => {
      pill.addEventListener('click', function () {
        const level = parseInt(this.getAttribute('data-level'), 10);
        ratings[skillName] = level;

        pills.forEach(p => p.classList.remove('active'));
        this.classList.add('active');

        if (descSpan) {
          descSpan.textContent = proficiencyDescriptions[level];
          descSpan.style.color = level >= 3 ? 'var(--color-success)' : 'var(--text-secondary)';
        }

        updateSummaryMetrics();
      });
    });
  });

  function updateSummaryMetrics() {
    const assessedCount = Object.keys(ratings).length;
    const countEl = document.getElementById('assessed-count');
    if (countEl) countEl.textContent = assessedCount;
  }

  // Submit Assessment to Backend
  const submitBtn = document.getElementById('submit-assessment-btn');
  if (submitBtn) {
    submitBtn.addEventListener('click', async function () {
      if (Object.keys(ratings).length === 0) {
        window.showToast('Please rate at least one skill before analyzing gaps.', 'warning');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.innerHTML = '<span class="spinner"></span> Analyzing Skill Gaps...';

      try {
        const response = await fetch('/api/skill-assessment/assess', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(ratings)
        });

        if (!response.ok) {
          throw new Error('Assessment submission failed with status ' + response.status);
        }

        const data = await response.json();
        renderAssessmentResults(data);
        window.showToast('Skill gap analysis generated successfully!', 'success');
      } catch (err) {
        console.error('Assessment error:', err);
        window.showToast('Error communicating with assessment API: ' + err.message, 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-chart-line me-2"></i> Submit & View Gap Analysis';
      }
    });
  }

  function renderAssessmentResults(data) {
    const resultsContainer = document.getElementById('assessment-results-section');
    if (!resultsContainer) return;

    resultsContainer.style.display = 'block';
    resultsContainer.scrollIntoView({ behavior: 'smooth' });

    const avgGapEl = document.getElementById('avg-gap-metric');
    if (avgGapEl) avgGapEl.textContent = (data.averageSkillGap != null ? data.averageSkillGap.toFixed(1) : '0.0');

    const totalAssessedEl = document.getElementById('total-assessed-metric');
    if (totalAssessedEl) totalAssessedEl.textContent = data.totalSkillsAssessed || 0;

    const listContainer = document.getElementById('skill-breakdown-list');
    if (listContainer && data.skillDetails) {
      listContainer.innerHTML = '';

      // Sort by skill gap descending (highest weaknesses first)
      const sorted = [...data.skillDetails].sort((a, b) => b.skillGap - a.skillGap);

      sorted.forEach(item => {
        const gap = item.skillGap;
        const current = item.currentLevel;
        const required = item.requiredLevel;

        let statusBadge = '<span class="badge badge-green"><i class="fas fa-check"></i> Target Met</span>';
        if (gap > 2) {
          statusBadge = '<span class="badge badge-red"><i class="fas fa-exclamation-triangle"></i> Critical Gap (' + gap + ')</span>';
        } else if (gap > 0) {
          statusBadge = '<span class="badge badge-yellow"><i class="fas fa-arrow-up"></i> Moderate Gap (' + gap + ')</span>';
        }

        const row = document.createElement('div');
        row.className = 'card card-elevated mb-3 p-3';
        row.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
            <div>
              <strong style="color: var(--text-primary); font-size: 1rem;">${item.skillName}</strong>
              <span class="badge badge-purple ms-2">${item.category || 'General'}</span>
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
});

/**
 * Career Compass — Interactive Roadmap & Task Scheduling Engine
 */

document.addEventListener('DOMContentLoaded', function () {
  loadRoadmapData();

  async function loadRoadmapData() {
    try {
      const response = await fetch('/api/roadmaps/current');
      if (!response.ok) return;

      const data = await response.json();
      if (data.hasRoadmap) {
        renderRoadmap(data);
      } else {
        const emptyState = document.getElementById('roadmap-empty-state');
        const content = document.getElementById('roadmap-main-content');
        if (emptyState) emptyState.style.display = 'block';
        if (content) content.style.display = 'none';
      }
    } catch (err) {
      console.warn('Could not load roadmap data from API:', err);
    }
  }

  function renderRoadmap(data) {
    const emptyState = document.getElementById('roadmap-empty-state');
    const content = document.getElementById('roadmap-main-content');
    if (emptyState) emptyState.style.display = 'none';
    if (content) content.style.display = 'block';

    const titleEl = document.getElementById('roadmap-title');
    if (titleEl) titleEl.textContent = data.title;

    const roleEl = document.getElementById('roadmap-role-badge');
    if (roleEl) {
      roleEl.textContent = data.companyName
        ? `${data.careerPathName} • ${data.companyName}`
        : data.careerPathName;
    }

    const deadlineEl = document.getElementById('roadmap-deadline-val');
    if (deadlineEl) deadlineEl.textContent = data.targetDeadline || 'Flexible';

    const tasksCountEl = document.getElementById('roadmap-tasks-count-val');
    if (tasksCountEl) tasksCountEl.textContent = `${data.completedTasks || 0} / ${data.totalTasks || 0} Tasks`;

    const progressPercent = data.progressPercentage || 0;
    const progressEl = document.getElementById('roadmap-progress-val');
    const progressBar = document.getElementById('roadmap-progress-bar');
    if (progressEl) progressEl.textContent = progressPercent + '%';
    if (progressBar) progressBar.style.width = progressPercent + '%';

    const tasksList = document.getElementById('weekly-tasks-list');
    if (tasksList && data.tasks) {
      tasksList.innerHTML = '';
      data.tasks.forEach(task => {
        const isDone = task.status === 'COMPLETED';
        const card = document.createElement('div');
        card.className = `card card-elevated mb-3 ${isDone ? 'opacity-75' : ''}`;
        card.style.borderLeft = isDone ? '4px solid var(--color-success)' : '4px solid var(--accent-primary)';

        const gapBadge = task.skillGap !== undefined
          ? `<span class="badge ${task.skillGap >= 3 ? 'badge-red' : (task.skillGap >= 1 ? 'badge-yellow' : 'badge-green')}">Gap: ${task.skillGap} (Lvl ${task.currentLevel || 0} → ${task.requiredLevel || 4})</span>`
          : '';

        const priorityBadge = task.priorityScore !== undefined
          ? `<span class="badge badge-blue" title="Priority = (gap × 3) + importance + company bonus">Priority: ${task.priorityLabel || 'Medium'} (${task.priorityScore} pts / ${task.weightedPriorityPercent || 0}%)</span>`
          : '';

        const importanceBadge = task.importance !== undefined
          ? `<span class="badge badge-gray">Importance: ${task.importance}/5</span>`
          : '';

        const prereqBadge = task.prerequisiteSkillName
          ? `<span class="badge badge-gray"><i class="fas fa-diagram-project me-1"></i> Prereq: ${escapeHtml(task.prerequisiteSkillName)}</span>`
          : '';

        let resourceLinksHtml = '';
        if (task.resources && task.resources.length > 0) {
          resourceLinksHtml = `
            <div style="display: flex; gap: 0.4rem; flex-wrap: wrap; margin-top: 0.5rem;">
              ${task.resources.map(r => `
                <a href="${escapeHtml(r.url)}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline">
                  <i class="fas fa-external-link-alt me-1"></i> ${escapeHtml(r.title)} ${r.provider ? `(${escapeHtml(r.provider)})` : ''}
                </a>
              `).join('')}
            </div>
          `;
        } else if (task.resourceUrl) {
          resourceLinksHtml = `
            <div style="margin-top: 0.5rem;">
              <a href="${escapeHtml(task.resourceUrl)}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline">
                <i class="fas fa-external-link-alt me-1"></i> Learning Resource
              </a>
            </div>
          `;
        }

        const practiceLink = task.skillName
          ? `<a href="/question-sets?topic=${encodeURIComponent(task.skillName)}" class="btn btn-sm btn-secondary"><i class="fas fa-book-open me-1"></i> Questions</a>`
          : '';

        card.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; flex-wrap: wrap;">
            <div style="flex: 1; min-width: 260px;">
              <div style="display: flex; align-items: center; gap: 0.45rem; margin-bottom: 0.45rem; flex-wrap: wrap;">
                <span class="badge ${isDone ? 'badge-green' : 'badge-purple'}">Week ${task.weekNumber || 1}</span>
                <span class="badge badge-gray"><i class="far fa-clock"></i> ${task.estimatedHours || 2}h</span>
                ${gapBadge}
                ${priorityBadge}
                ${importanceBadge}
                ${prereqBadge}
                <span class="badge ${isDone ? 'badge-green' : 'badge-yellow'}">${task.status}</span>
              </div>
              <h4 style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); margin-bottom: 0.25rem; text-decoration: ${isDone ? 'line-through' : 'none'};">
                ${escapeHtml(task.title)}
              </h4>
              <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.35rem;">
                ${escapeHtml(task.description || 'Focus on theory, practice problem solving, and build applied projects.')}
              </p>
              ${resourceLinksHtml}
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
              ${practiceLink}
              ${!isDone ? `
                <button class="btn btn-sm btn-success mark-task-btn" data-task-id="${task.id}">
                  <i class="fas fa-check"></i> Complete
                </button>
              ` : `
                <span class="badge badge-green p-2"><i class="fas fa-check-double"></i> Done</span>
              `}
            </div>
          </div>
        `;
        tasksList.appendChild(card);
      });

      tasksList.querySelectorAll('.mark-task-btn').forEach(btn => {
        btn.addEventListener('click', async function () {
          const taskId = this.getAttribute('data-task-id');
          this.disabled = true;
          this.innerHTML = '<span class="spinner"></span>';
          try {
            const res = await fetch(`/api/roadmaps/tasks/${taskId}/status`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ status: 'COMPLETED' })
            });
            if (res.ok) {
              window.showToast('Task marked as completed! Activity streak updated.', 'success');
              loadRoadmapData();
            }
          } catch (e) {
            window.showToast('Failed to update task: ' + e.message, 'error');
          }
        });
      });
    }
  }

  const generateBtn = document.getElementById('generate-roadmap-btn');
  if (generateBtn) {
    generateBtn.addEventListener('click', async function () {
      generateBtn.disabled = true;
      generateBtn.innerHTML = '<span class="spinner"></span> Generating Roadmap...';
      try {
        const res = await fetch('/api/roadmaps/generate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({})
        });
        if (res.ok) {
          window.showToast('Your personalized roadmap has been generated!', 'success');
          loadRoadmapData();
        } else {
          window.showToast('Failed to generate roadmap. Please select a career role in Profile.', 'error');
        }
      } catch (e) {
        window.showToast('Error: ' + e.message, 'error');
      } finally {
        generateBtn.disabled = false;
        generateBtn.innerHTML = '<i class="fas fa-wand-magic-sparkles me-2"></i> Generate My Roadmap';
      }
    });
  }

  const saveCustomizeBtn = document.getElementById('save-customize-roadmap-btn');
  if (saveCustomizeBtn) {
    saveCustomizeBtn.addEventListener('click', async function () {
      const hoursInput = document.getElementById('custom-study-hours');
      const deadlineInput = document.getElementById('custom-deadline');
      const companySelect = document.getElementById('custom-company-select');

      const dailyHours = hoursInput ? parseInt(hoursInput.value, 10) : null;
      const targetDeadline = deadlineInput && deadlineInput.value ? deadlineInput.value : null;
      const companyName = companySelect && companySelect.value ? companySelect.value : null;

      saveCustomizeBtn.disabled = true;
      try {
        if (dailyHours && dailyHours > 0) {
          await fetch('/api/user/profile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ dailyAvailableHours: dailyHours })
          });
        }

        const genRes = await fetch('/api/roadmaps/generate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ companyName, targetDeadline })
        });

        if (genRes.ok) {
          window.closeModal('customize-roadmap-modal');
          window.showToast('Personalized roadmap updated!', 'success');
          loadRoadmapData();
        } else {
          window.showToast('Could not update roadmap.', 'error');
        }
      } catch (e) {
        window.showToast('Error: ' + e.message, 'error');
      } finally {
        saveCustomizeBtn.disabled = false;
      }
    });
  }

  function escapeHtml(str) {
    return String(str || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
});

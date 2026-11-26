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
        const resourceBtn = task.resourceUrl
          ? `<a href="${task.resourceUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline"><i class="fas fa-external-link-alt me-1"></i> Resource</a>`
          : '';
        card.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem;">
            <div style="flex: 1;">
              <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem; flex-wrap: wrap;">
                <span class="badge ${isDone ? 'badge-green' : 'badge-purple'}">Week ${task.weekNumber || 1}</span>
                <span class="badge badge-gray"><i class="far fa-clock"></i> ${task.estimatedHours || 2}h</span>
                <span class="badge ${isDone ? 'badge-green' : 'badge-yellow'}">${task.status}</span>
              </div>
              <h4 style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); margin-bottom: 0.25rem; text-decoration: ${isDone ? 'line-through' : 'none'};">
                ${escapeHtml(task.title)}
              </h4>
              <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.5rem;">
                ${escapeHtml(task.description || 'Focus on theory, practice problem solving, and build applied projects.')}
              </p>
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              ${resourceBtn}
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

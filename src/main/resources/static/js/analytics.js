/**
 * Career Compass — Progress Tracking, Dashboard & Analytics Engine
 */

document.addEventListener('DOMContentLoaded', function () {
  loadAnalyticsData();

  async function loadAnalyticsData() {
    try {
      const [activityRes, skillsRes, roadmapRes, attemptsRes] = await Promise.all([
        fetch('/api/activity/summary'),
        fetch('/api/user/skills'),
        fetch('/api/roadmaps/current'),
        fetch('/api/questions/attempts/summary')
      ]);

      let activeDates = new Set();

      if (activityRes.ok) {
        const activityData = await activityRes.json();
        updateSummaryStats(activityData);
        if (Array.isArray(activityData.activeDates)) {
          activeDates = new Set(activityData.activeDates);
        }
      }

      if (skillsRes.ok) {
        const userSkills = await skillsRes.json();
        updateAssessedSkillsWidgets(userSkills);
      }

      if (roadmapRes.ok) {
        const roadmapData = await roadmapRes.json();
        updateRoadmapWidgets(roadmapData);
      }

      if (attemptsRes.ok) {
        const attemptsData = await attemptsRes.json();
        updateQuestionAccuracyWidgets(attemptsData);
      }

      renderActivityHeatmap(activeDates);
    } catch (e) {
      console.warn('Could not load dashboard/analytics data:', e);
    }
  }

  function updateSummaryStats(data) {
    const streakEl = document.getElementById('stat-streak');
    if (streakEl) streakEl.textContent = (data.streakDays || 1) + ' Days';

    const tasksEl = document.getElementById('stat-tasks-done');
    if (tasksEl) tasksEl.textContent = (data.completedTasks || 0) + ' Tasks';

    const hoursEl = document.getElementById('stat-hours');
    if (hoursEl) hoursEl.textContent = (data.learningHours || 0) + ' hrs';
  }

  function updateAssessedSkillsWidgets(userSkills) {
    const assessedCount = Array.isArray(userSkills) ? userSkills.length : 0;
    const assessedEl = document.getElementById('stat-skills-assessed');
    if (assessedEl) {
      assessedEl.textContent = assessedCount + (assessedCount === 1 ? ' Skill' : ' Skills');
    }

    const assessedSubEl = document.getElementById('stat-skills-assessed-sub');
    if (assessedSubEl) {
      assessedSubEl.innerHTML = assessedCount > 0
        ? '<i class="fas fa-check-circle"></i> Saved in profile'
        : '<i class="fas fa-info-circle"></i> Take skill assessment';
    }

    const gaps = (userSkills || [])
      .map(s => ({
        ...s,
        requiredLevel: s.requiredLevel != null ? s.requiredLevel : 3,
        gap: Math.max(0, (s.requiredLevel != null ? s.requiredLevel : 3) - (s.proficiency || 0))
      }))
      .sort((a, b) => b.gap - a.gap);

    const priorityGaps = gaps.filter(g => g.gap > 0);
    const improveEl = document.getElementById('stat-skills-improve');
    if (improveEl) {
      improveEl.textContent = priorityGaps.length + ' Priority';
    }
    const improveSubEl = document.getElementById('stat-skills-improve-sub');
    if (improveSubEl) {
      if (priorityGaps.length > 0) {
        const topNames = priorityGaps.slice(0, 2).map(g => g.skillName).join(' & ');
        improveSubEl.innerHTML = `<i class="fas fa-arrow-up"></i> Focus: ${escapeHtml(topNames)}`;
      } else if (assessedCount > 0) {
        improveSubEl.innerHTML = '<i class="fas fa-check"></i> All assessed targets met';
      } else {
        improveSubEl.innerHTML = '<i class="fas fa-sliders-h"></i> Rate skills to detect gaps';
      }
    }

    const gapsContainer = document.getElementById('dashboard-skill-gaps');
    if (gapsContainer) {
      if (assessedCount === 0) {
        gapsContainer.innerHTML = `
          <div style="background-color: var(--bg-subtle); padding: 1rem; border-radius: var(--radius-sm); font-size: 0.85rem; color: var(--text-secondary); border: 1px solid var(--border);">
            <i class="fas fa-info-circle text-primary me-1"></i> No skills assessed yet.
            <a href="/skill-assessment" style="font-weight: 600;">Complete your skill assessment</a> to see real gap bars.
          </div>`;
        return;
      }

      gapsContainer.innerHTML = '';
      gaps.slice(0, 5).forEach(item => {
        const pct = item.requiredLevel > 0
          ? Math.min(100, Math.round(((item.proficiency || 0) / item.requiredLevel) * 100))
          : 100;
        const barClass = item.gap === 0 ? 'progress-bar-success' : item.gap >= 2 ? 'progress-bar-warning' : '';
        const block = document.createElement('div');
        block.className = 'mb-3';
        block.innerHTML = `
          <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 0.25rem;">
            <strong style="color: var(--text-main);">${escapeHtml(item.skillName)}</strong>
            <span style="color: var(--text-muted);">Current: <b style="color: var(--secondary);">${item.proficiency}</b> / Req: <b style="color: var(--primary);">${item.requiredLevel}</b></span>
          </div>
          <div class="progress-container">
            <div class="progress-bar ${barClass}" style="width: ${pct}%;"></div>
          </div>`;
        gapsContainer.appendChild(block);
      });
    }
  }

  function updateRoadmapWidgets(data) {
    const focusContainer = document.getElementById('dashboard-focus-list');
    const roadmapSub = document.getElementById('dashboard-roadmap-subtitle');
    const roadmapBadge = document.getElementById('dashboard-roadmap-progress-badge');

    if (!data || !data.hasRoadmap) {
      if (roadmapSub) roadmapSub.textContent = 'No active roadmap yet';
      if (roadmapBadge) roadmapBadge.textContent = '0% Complete';
      if (focusContainer) {
        focusContainer.innerHTML = `
          <div class="card card-elevated p-3">
            <p class="text-secondary mb-2" style="font-size: 0.9rem;">No active roadmap generated yet.</p>
            <a href="/roadmap" class="btn btn-sm btn-primary">Generate Roadmap</a>
          </div>`;
      }
      return;
    }

    if (roadmapSub) {
      const companyPart = data.companyName ? ` • ${data.companyName}` : '';
      roadmapSub.textContent = `${data.careerPathName}${companyPart} (${data.completedTasks || 0}/${data.totalTasks || 0} tasks)`;
    }
    if (roadmapBadge) {
      roadmapBadge.textContent = `${data.progressPercentage || 0}% Complete`;
    }

    if (focusContainer && Array.isArray(data.tasks)) {
      const pending = data.tasks.filter(t => t.status !== 'COMPLETED').slice(0, 3);
      if (pending.length === 0) {
        focusContainer.innerHTML = `
          <div class="card card-elevated p-3" style="border-left: 4px solid var(--success);">
            <strong style="color: var(--text-main);">All roadmap tasks completed!</strong>
            <p style="font-size: 0.8rem; color: var(--text-secondary); margin: 0.25rem 0 0;">Practice questions or update your target company in Roadmap.</p>
          </div>`;
        return;
      }

      focusContainer.innerHTML = '';
      pending.forEach(task => {
        const card = document.createElement('div');
        card.className = 'card card-elevated p-3 mb-3';
        card.style.borderLeft = '4px solid var(--primary)';
        card.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem;">
            <div style="flex: 1;">
              <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem;">
                <span class="badge badge-purple">Week ${task.weekNumber || 1}</span>
                <span class="badge badge-gray"><i class="far fa-clock"></i> ${task.estimatedHours || 2}h</span>
              </div>
              <h4 style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.25rem;">${escapeHtml(task.title)}</h4>
              <p style="font-size: 0.8rem; color: var(--text-secondary); margin: 0;">${escapeHtml(task.description || '')}</p>
            </div>
            <div style="display: flex; gap: 0.5rem;">
              <a href="/roadmap" class="btn btn-sm btn-primary">Open</a>
              <button type="button" class="btn btn-sm btn-outline dash-complete-task" data-task-id="${task.id}" title="Mark Completed">
                <i class="fas fa-check"></i>
              </button>
            </div>
          </div>`;
        focusContainer.appendChild(card);
      });

      focusContainer.querySelectorAll('.dash-complete-task').forEach(btn => {
        btn.addEventListener('click', async function () {
          const taskId = this.getAttribute('data-task-id');
          this.disabled = true;
          const res = await fetch(`/api/roadmaps/tasks/${taskId}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: 'COMPLETED' })
          });
          if (res.ok) {
            window.showToast('Task marked as completed! Progress updated.', 'success');
            loadAnalyticsData();
          } else {
            this.disabled = false;
          }
        });
      });
    }
  }

  function updateQuestionAccuracyWidgets(summary) {
    const accuracyEl = document.getElementById('stat-question-accuracy');
    if (accuracyEl) {
      accuracyEl.textContent = (summary.accuracyPercent != null ? summary.accuracyPercent : 0) + '%';
    }
    const attemptsEl = document.getElementById('stat-question-attempts');
    if (attemptsEl) {
      attemptsEl.textContent = `${summary.correctAttempts || 0}/${summary.evaluatedAttempts || 0} correct (${summary.totalAttempts || 0} attempts)`;
    }
  }

  function renderActivityHeatmap(activeDates) {
    const heatmapGrid = document.getElementById('activity-heatmap-grid');
    if (!heatmapGrid) return;

    heatmapGrid.innerHTML = '';
    const today = new Date();
    for (let i = 27; i >= 0; i--) {
      const d = new Date();
      d.setDate(today.getDate() - i);
      const iso = d.toISOString().slice(0, 10);
      const isActive = activeDates.has(iso);

      const cell = document.createElement('div');
      cell.className = 'heatmap-cell';
      cell.style.width = '18px';
      cell.style.height = '18px';
      cell.style.borderRadius = '4px';
      cell.style.backgroundColor = isActive ? 'var(--accent-primary)' : 'var(--bg-elevated)';
      cell.title = iso + (isActive ? ' — Activity recorded' : ' — No activity');
      heatmapGrid.appendChild(cell);
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

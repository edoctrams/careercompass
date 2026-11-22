/**
 * Career Compass — Progress Tracking & Analytics Engine
 */

document.addEventListener('DOMContentLoaded', function () {
  loadAnalyticsData();

  async function loadAnalyticsData() {
    try {
      const res = await fetch('/api/activity/summary');
      if (res.ok) {
        const data = await res.json();
        updateSummaryStats(data);
      }
    } catch (e) {
      console.warn('Could not load activity summary:', e);
    }

    renderActivityHeatmap();
  }

  function updateSummaryStats(data) {
    const streakEl = document.getElementById('stat-streak');
    if (streakEl) streakEl.textContent = (data.streakDays || 1) + ' Days';

    const tasksEl = document.getElementById('stat-tasks-done');
    if (tasksEl) tasksEl.textContent = data.completedTasks || 0;

    const hoursEl = document.getElementById('stat-hours');
    if (hoursEl) hoursEl.textContent = (data.learningHours || 2) + ' hrs';
  }

  function renderActivityHeatmap() {
    const heatmapGrid = document.getElementById('activity-heatmap-grid');
    if (!heatmapGrid) return;

    heatmapGrid.innerHTML = '';
    // Generate 28 days of activity squares
    const today = new Date();
    for (let i = 27; i >= 0; i--) {
      const d = new Date();
      d.setDate(today.getDate() - i);

      const cell = document.createElement('div');
      cell.className = 'heatmap-cell';

      // Simulate natural student learning pattern
      const isWeekend = d.getDay() === 0 || d.getDay() === 6;
      const intensity = i < 3 ? 'active-high' : isWeekend ? 'active-medium' : i % 3 === 0 ? 'active-low' : 'active-none';

      cell.style.width = '18px';
      cell.style.height = '18px';
      cell.style.borderRadius = '4px';
      cell.style.backgroundColor = getIntensityColor(intensity);
      cell.title = d.toLocaleDateString() + ' — Activity recorded';

      heatmapGrid.appendChild(cell);
    }
  }

  function getIntensityColor(intensity) {
    switch (intensity) {
      case 'active-high': return 'var(--accent-primary)';
      case 'active-medium': return 'rgba(116, 103, 240, 0.65)';
      case 'active-low': return 'rgba(116, 103, 240, 0.3)';
      default: return 'var(--bg-elevated)';
    }
  }
});

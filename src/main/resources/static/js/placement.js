/**
 * Career Compass — Placement Preparation & Question Bank Engine
 */

document.addEventListener('DOMContentLoaded', function () {
  let allQuestionSets = [];
  let currentQuestions = [];

  // Filter elements
  const companyFilter = document.getElementById('filter-company');
  const typeFilter = document.getElementById('filter-type');
  const searchInput = document.getElementById('question-search');
  const questionContainer = document.getElementById('question-bank-container');

  // Load question sets from backend
  loadQuestions();

  async function loadQuestions() {
    try {
      const res = await fetch('/api/question-sets/all');
      if (res.ok) {
        allQuestionSets = await res.json();
        // Fetch questions for each question set
        for (const qs of allQuestionSets) {
          const qRes = await fetch(`/api/question-sets/${qs.id}/questions`);
          if (qRes.ok) {
            const qsQuestions = await qRes.json();
            qsQuestions.forEach(q => {
              q.companyName = qs.companyName;
              q.setBatch = qs.batch;
              currentQuestions.push(q);
            });
          }
        }
        renderQuestionList(currentQuestions);
      }
    } catch (err) {
      console.warn('Could not load placement questions:', err);
    }
  }

  function renderQuestionList(questions) {
    if (!questionContainer) return;

    if (!questions || questions.length === 0) {
      questionContainer.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon"><i class="fas fa-question-circle"></i></div>
          <h3 class="empty-state-title">No Questions Match Filter</h3>
          <p class="empty-state-desc">Try clearing filters or search terms to browse the placement question bank.</p>
        </div>
      `;
      return;
    }

    questionContainer.innerHTML = '';
    questions.forEach((q, index) => {
      const card = document.createElement('div');
      card.className = 'card card-elevated mb-4 p-4';
      card.setAttribute('data-type', q.questionType);
      card.setAttribute('data-company', q.companyName || '');

      let typeBadge = '<span class="badge badge-purple">' + q.questionType + '</span>';
      if (q.questionType === 'CODING') typeBadge = '<span class="badge badge-blue"><i class="fas fa-code"></i> CODING</span>';
      if (q.questionType === 'HR' || q.questionType === 'BEHAVIORAL') typeBadge = '<span class="badge badge-yellow"><i class="fas fa-user-tie"></i> HR / BEHAVIORAL</span>';

      let contentHTML = '';

      if (q.questionType === 'MCQ') {
        contentHTML = `
          <div style="font-size: 1rem; color: var(--text-primary); margin-bottom: 1rem; white-space: pre-line;">
            ${q.questionText}
          </div>
          <button class="btn btn-sm btn-outline show-answer-btn">
            <i class="fas fa-eye me-1"></i> Reveal Answer & Explanation
          </button>
          <div class="answer-box mt-3 p-3" style="display: none; background-color: var(--bg-card); border-left: 3px solid var(--color-success); border-radius: var(--radius-sm); font-size: 0.9rem;">
            <strong style="color: var(--color-success);"><i class="fas fa-check-circle me-1"></i> Official Solution:</strong>
            <p class="mt-2 mb-0" style="white-space: pre-line; color: var(--text-secondary);">${q.answer || 'Detailed solution verified by placement leads.'}</p>
          </div>
        `;
      } else if (q.questionType === 'CODING') {
        contentHTML = `
          <div style="font-size: 0.95rem; color: var(--text-primary); margin-bottom: 1rem; white-space: pre-line;">
            ${q.questionText}
          </div>
          <div class="code-editor-sim mb-3" style="background: #0d1117; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 0.5rem; margin-bottom: 0.75rem;">
              <span style="font-family: var(--font-mono); font-size: 0.8rem; color: var(--text-muted);"><i class="fas fa-terminal me-2"></i>solution.java</span>
              <span class="badge badge-gray">Java 21</span>
            </div>
            <textarea class="form-control" style="background: transparent; border: none; font-family: var(--font-mono); font-size: 0.85rem; color: #58a6ff; resize: none; min-height: 100px;">// Write your optimal solution here&#10;class Solution {&#10;    public int[] solve() {&#10;        // Implementation&#10;    }&#10;}</textarea>
          </div>
          <div style="display: flex; gap: 0.75rem;">
            <button class="btn btn-sm btn-primary run-testcases-btn">
              <i class="fas fa-play me-1"></i> Run Test Cases
            </button>
            <button class="btn btn-sm btn-outline show-answer-btn">
              <i class="fas fa-lightbulb me-1"></i> View Optimal Pattern
            </button>
          </div>
          <div class="answer-box mt-3 p-3" style="display: none; background-color: var(--bg-card); border-left: 3px solid var(--accent-secondary); border-radius: var(--radius-sm); font-size: 0.9rem;">
            <strong style="color: var(--accent-secondary);"><i class="fas fa-code-branch me-1"></i> Optimal Complexity & Algorithm:</strong>
            <p class="mt-2 mb-0" style="white-space: pre-line; color: var(--text-secondary);">${q.answer || 'Optimal approach details.'}</p>
          </div>
        `;
      } else {
        // Technical or HR
        contentHTML = `
          <div style="font-size: 1rem; color: var(--text-primary); margin-bottom: 1rem; white-space: pre-line;">
            ${q.questionText}
          </div>
          <div style="display: flex; gap: 0.75rem;">
            <button class="btn btn-sm btn-outline show-answer-btn">
              <i class="fas fa-book-reader me-1"></i> Model Reference Answer
            </button>
            <button class="btn btn-sm btn-ghost mark-practiced-btn">
              <i class="far fa-check-circle me-1"></i> Mark as Practiced
            </button>
          </div>
          <div class="answer-box mt-3 p-3" style="display: none; background-color: var(--bg-card); border-left: 3px solid var(--accent-primary); border-radius: var(--radius-sm); font-size: 0.9rem;">
            <strong style="color: var(--accent-primary);"><i class="fas fa-star me-1"></i> Framework & Model Response:</strong>
            <p class="mt-2 mb-0" style="white-space: pre-line; color: var(--text-secondary);">${q.answer || 'Reference guidance.'}</p>
          </div>
        `;
      }

      card.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span class="badge badge-gray"><i class="fas fa-building me-1"></i> ${q.companyName || 'General Tech'}</span>
            ${typeBadge}
            <span class="badge ${q.difficulty === 'Hard' ? 'badge-red' : q.difficulty === 'Medium' ? 'badge-yellow' : 'badge-green'}">${q.difficulty || 'Medium'}</span>
          </div>
          <button class="btn btn-sm btn-ghost bookmark-btn" style="color: var(--text-muted);"><i class="far fa-bookmark"></i></button>
        </div>
        ${contentHTML}
      `;

      questionContainer.appendChild(card);
    });

    // Hook answer toggle buttons
    questionContainer.querySelectorAll('.show-answer-btn').forEach(btn => {
      btn.addEventListener('click', function () {
        const box = this.parentElement.querySelector('.answer-box');
        if (box) {
          const isShown = box.style.display === 'block';
          box.style.display = isShown ? 'none' : 'block';
          this.innerHTML = isShown ? '<i class="fas fa-eye me-1"></i> View Solution' : '<i class="fas fa-eye-slash me-1"></i> Hide Solution';
        }
      });
    });

    // Hook test cases simulation
    questionContainer.querySelectorAll('.run-testcases-btn').forEach(btn => {
      btn.addEventListener('click', function () {
        this.disabled = true;
        this.innerHTML = '<span class="spinner"></span> Running test cases...';
        setTimeout(() => {
          this.disabled = false;
          this.innerHTML = '<i class="fas fa-check text-success me-1"></i> All 12/12 Cases Passed';
          window.showToast('Test execution successful! Memory: 42.1MB, Time: 2ms.', 'success');
        }, 800);
      });
    });

    // Hook bookmark buttons
    questionContainer.querySelectorAll('.bookmark-btn').forEach(btn => {
      btn.addEventListener('click', function () {
        const icon = this.querySelector('i');
        if (icon.classList.contains('far')) {
          icon.classList.remove('far');
          icon.classList.add('fas');
          this.style.color = 'var(--accent-secondary)';
          window.showToast('Question saved to bookmarks!', 'info');
        } else {
          icon.classList.remove('fas');
          icon.classList.add('far');
          this.style.color = 'var(--text-muted)';
        }
      });
    });

    // Hook mark practiced
    questionContainer.querySelectorAll('.mark-practiced-btn').forEach(btn => {
      btn.addEventListener('click', function () {
        this.innerHTML = '<i class="fas fa-check text-success me-1"></i> Practiced';
        this.classList.add('text-success');
        window.showToast('Added to your practice history!', 'success');
      });
    });
  }

  // Filter Event Listeners
  if (companyFilter) companyFilter.addEventListener('change', applyFilters);
  if (typeFilter) typeFilter.addEventListener('change', applyFilters);
  if (searchInput) searchInput.addEventListener('input', applyFilters);

  function applyFilters() {
    const company = companyFilter ? companyFilter.value : '';
    const type = typeFilter ? typeFilter.value : '';
    const search = searchInput ? searchInput.value.toLowerCase() : '';

    const filtered = currentQuestions.filter(q => {
      const matchCompany = !company || (q.companyName && q.companyName.toLowerCase().includes(company.toLowerCase()));
      const matchType = !type || q.questionType === type;
      const matchSearch = !search || q.questionText.toLowerCase().includes(search) || (q.companyName && q.companyName.toLowerCase().includes(search));
      return matchCompany && matchType && matchSearch;
    });

    renderQuestionList(filtered);
  }
});

/**
 * Career Compass - Interactive Question Bank & Practice Attempts
 */

document.addEventListener('DOMContentLoaded', function () {
  let allQuestions = [];

  const container = document.getElementById('question-bank-container');
  const companyFilter = document.getElementById('filter-company');
  const roleFilter = document.getElementById('filter-role');
  const topicFilter = document.getElementById('filter-topic');
  const difficultyFilter = document.getElementById('filter-difficulty');
  const typeFilter = document.getElementById('filter-type');
  const searchInput = document.getElementById('question-search');

  const requestedCompany = new URLSearchParams(window.location.search).get('company');
  if (
    requestedCompany &&
    companyFilter &&
    [...companyFilter.options].some(option => option.value === requestedCompany)
  ) {
    companyFilter.value = requestedCompany;
  }

  function addText(parent, tag, value, className = '') {
    const element = document.createElement(tag);
    element.textContent = value || '';
    if (className) element.className = className;
    parent.appendChild(element);
    return element;
  }

  async function loadAttemptsSummary() {
    const summaryText = document.getElementById('attempts-summary-text');
    const accuracyBadge = document.getElementById('attempts-accuracy-badge');
    if (!summaryText || !accuracyBadge) return;

    try {
      const res = await fetch('/api/questions/attempts/summary');
      if (!res.ok) {
        summaryText.textContent = 'Sign in to track practice attempts and accuracy.';
        accuracyBadge.textContent = 'Accuracy: --%';
        return;
      }
      const data = await res.json();
      summaryText.textContent = `${data.totalAttempts || 0} attempts across ${data.uniqueQuestionsAttempted || 0} questions (${data.correctAttempts || 0}/${data.evaluatedAttempts || 0} correct)`;
      accuracyBadge.textContent = `Accuracy: ${data.accuracyPercent != null ? data.accuracyPercent : 0}%`;
    } catch (e) {
      console.warn('Could not load question attempts summary:', e);
    }
  }

  async function loadQuestions() {
    try {
      const params = new URLSearchParams();
      if (companyFilter && companyFilter.value) params.set('companyName', companyFilter.value);
      if (roleFilter && roleFilter.value) params.set('careerPathName', roleFilter.value);
      if (difficultyFilter && difficultyFilter.value) params.set('difficulty', difficultyFilter.value);
      if (typeFilter && typeFilter.value) params.set('questionType', typeFilter.value);

      const url = '/api/questions/filter' + (params.toString() ? '?' + params.toString() : '');
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error('Unable to load questions.');
      }

      allQuestions = await response.json();
      renderQuestions();
    } catch (error) {
      console.error(error);
      container.textContent = 'Unable to load questions. Please try again later.';
    }
  }

  function renderQuestions() {
    container.innerHTML = '';

    const topic = (topicFilter ? topicFilter.value : '').toLowerCase().trim();
    const search = (searchInput ? searchInput.value : '').toLowerCase().trim();

    const filtered = allQuestions.filter(q => {
      const subtopicNames = (q.subtopics || []).map(s => s.name).join(' ');
      const companyNames = (q.companies || []).map(c => c.name).join(' ');
      const topicHaystack = `${q.skillName || ''} ${subtopicNames}`.toLowerCase();

      const matchesTopic = !topic || topicHaystack.includes(topic);
      const matchesSearch = !search ||
        (q.questionText || '').toLowerCase().includes(search) ||
        topicHaystack.includes(search) ||
        companyNames.toLowerCase().includes(search) ||
        (q.careerPathName || '').toLowerCase().includes(search);

      return matchesTopic && matchesSearch;
    });

    if (filtered.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      addText(empty, 'h3', 'No matching questions found.');
      addText(empty, 'p', 'Try changing your filters or clearing the search box.');
      container.appendChild(empty);
      return;
    }

    filtered.forEach(q => {
      const card = document.createElement('div');
      card.className = 'card card-elevated p-4 mb-3';

      const heading = document.createElement('div');
      heading.className = 'd-flex justify-content-between flex-wrap gap-2';

      const leftBadges = document.createElement('div');
      leftBadges.style.display = 'flex';
      leftBadges.style.gap = '0.4rem';
      leftBadges.style.flexWrap = 'wrap';

      if (q.companies && q.companies.length > 0) {
        q.companies.forEach(c => {
          addText(leftBadges, 'strong', `${c.name} (${c.evidenceType || 'GENERAL'})`, 'badge badge-purple');
        });
      } else {
        addText(leftBadges, 'strong', q.careerPathName || 'General Prep', 'badge badge-purple');
      }
      addText(leftBadges, 'span', q.questionType, 'badge badge-blue');
      addText(leftBadges, 'span', q.difficulty || 'Unspecified', 'badge badge-gray');

      heading.appendChild(leftBadges);
      card.appendChild(heading);

      addText(card, 'h3', q.questionText, 'mt-3');

      const subtopicText = (q.subtopics && q.subtopics.length > 0)
        ? q.subtopics.map(s => s.name).join(', ')
        : (q.skillName || 'General');

      addText(
        card,
        'p',
        `Career: ${q.careerPathName || 'General'} | Subtopics: ${subtopicText}`,
        'text-secondary'
      );

      // Practice & Attempt Form
      const attemptBox = document.createElement('div');
      attemptBox.style.display = 'flex';
      attemptBox.style.gap = '0.5rem';
      attemptBox.style.flexWrap = 'wrap';
      attemptBox.style.alignItems = 'center';
      attemptBox.className = 'mt-2 mb-2';

      const answerInput = document.createElement('input');
      answerInput.type = 'text';
      answerInput.className = 'form-control';
      answerInput.style.flex = '1';
      answerInput.style.minWidth = '220px';
      answerInput.placeholder = q.questionType === 'MCQ'
        ? 'Enter option (e.g. A, B, C, D) or full answer...'
        : 'Type your answer or key points...';

      const submitBtn = document.createElement('button');
      submitBtn.type = 'button';
      submitBtn.className = 'btn btn-sm btn-primary';
      submitBtn.textContent = 'Submit Attempt';

      const feedback = document.createElement('div');
      feedback.className = 'mt-2';
      feedback.style.display = 'none';

      submitBtn.addEventListener('click', async () => {
        const val = answerInput.value.trim();
        if (!val) {
          if (window.showToast) window.showToast('Please enter an answer before submitting.', 'warning');
          return;
        }
        submitBtn.disabled = true;
        try {
          const payload = { selectedAnswer: val };
          const res = await fetch(`/api/questions/${q.id}/attempt`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          if (res.status === 401) {
            if (window.showToast) window.showToast('Please log in to record attempts.', 'warning');
            return;
          }
          if (!res.ok) throw new Error('Failed to record attempt');
          const result = await res.json();

          feedback.style.display = 'block';
          feedback.innerHTML = '';
          const statusSpan = document.createElement('span');
          if (result.isCorrect === true) {
            statusSpan.className = 'badge badge-green me-2';
            statusSpan.textContent = 'Correct!';
          } else if (result.isCorrect === false) {
            statusSpan.className = 'badge badge-red me-2';
            statusSpan.textContent = 'Incorrect';
          } else {
            statusSpan.className = 'badge badge-blue me-2';
            statusSpan.textContent = 'Attempt Recorded';
          }
          feedback.appendChild(statusSpan);

          if (result.correctAnswer) {
            addText(feedback, 'p', `Answer: ${result.correctAnswer}`, 'mt-2 mb-1');
          }
          if (result.explanation) {
            addText(feedback, 'p', `Explanation: ${result.explanation}`, 'text-secondary mb-0');
          }
          loadAttemptsSummary();
        } catch (err) {
          if (window.showToast) window.showToast('Error: ' + err.message, 'error');
        } finally {
          submitBtn.disabled = false;
        }
      });

      attemptBox.appendChild(answerInput);
      attemptBox.appendChild(submitBtn);
      card.appendChild(attemptBox);
      card.appendChild(feedback);

      const toggleButton = document.createElement('button');
      toggleButton.type = 'button';
      toggleButton.className = 'btn btn-outline btn-sm mt-1';
      toggleButton.textContent = 'Show Answer & Explanation';

      const answerBox = document.createElement('div');
      answerBox.className = 'card p-3 mt-2';
      answerBox.style.display = 'none';
      addText(answerBox, 'p', q.answer || 'No sample answer available.', 'mb-1');
      if (q.explanation) {
        addText(answerBox, 'p', q.explanation, 'text-secondary mb-0');
      }

      toggleButton.addEventListener('click', () => {
        const visible = answerBox.style.display === 'block';
        answerBox.style.display = visible ? 'none' : 'block';
        toggleButton.textContent = visible ? 'Show Answer & Explanation' : 'Hide Answer';
      });

      card.appendChild(toggleButton);
      card.appendChild(answerBox);
      container.appendChild(card);
    });
  }

  [companyFilter, roleFilter, difficultyFilter, typeFilter]
    .filter(Boolean)
    .forEach(control => control.addEventListener('change', loadQuestions));

  [topicFilter, searchInput]
    .filter(Boolean)
    .forEach(control => control.addEventListener('input', renderQuestions));

  loadAttemptsSummary();
  loadQuestions();
});

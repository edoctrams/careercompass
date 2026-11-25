
/**
 * Career Compass - Read-only Company Question Bank
 */

document.addEventListener('DOMContentLoaded', function () {

  let allQuestions = [];

  const container = document.getElementById('question-bank-container');

  const companyFilter = document.getElementById('filter-company');
  const requestedCompany =
    new URLSearchParams(window.location.search).get('company');

if (
    requestedCompany &&
    [...companyFilter.options].some(
        option => option.value === requestedCompany
    )
) {
    companyFilter.value = requestedCompany;
}
  const roleFilter = document.getElementById('filter-role');
  const topicFilter = document.getElementById('filter-topic');
  const difficultyFilter = document.getElementById('filter-difficulty');
  const typeFilter = document.getElementById('filter-type');
  const searchInput = document.getElementById('question-search');

  function addText(parent, tag, value, className = '') {
    const element = document.createElement(tag);
    element.textContent = value || '';
    if (className) element.className = className;
    parent.appendChild(element);
    return element;
  }

  async function loadQuestions() {

    try {

      const response = await fetch('/api/question-sets/all');

      if (!response.ok) {
        throw new Error('Unable to load question sets.');
      }

      const sets = await response.json();

      const allowedCompanies = ['Amazon', 'Microsoft', 'TCS'];

      for (const set of sets) {

        if (!allowedCompanies.includes(set.companyName)) continue;

        const questionResponse = await fetch(
          `/api/question-sets/${set.id}/questions`
        );

        if (!questionResponse.ok) continue;

        const questions = await questionResponse.json();

        questions.forEach(question => {

          allQuestions.push({
            ...question,
            companyName: set.companyName,
            careerPathName: set.careerPathName || '',
            topicName: question.skillName ||
              (question.skill && question.skill.name) || 'General'
          });

        });
      }

      renderQuestions();

    } catch (error) {

      console.error(error);

      container.textContent =
        'Unable to load questions. Please try again later.';
    }
  }

  function renderQuestions() {

    container.innerHTML = '';

    const company = companyFilter.value;
    const role = roleFilter.value;
    const topic = topicFilter.value.toLowerCase().trim();
    const difficulty = difficultyFilter.value;
    const type = typeFilter.value;
    const search = searchInput.value.toLowerCase().trim();

    const filtered = allQuestions.filter(q => {

      return (
        (!company || q.companyName === company) &&
        (!role || q.careerPathName === role) &&
        (!topic || q.topicName.toLowerCase().includes(topic)) &&
        (!difficulty || q.difficulty === difficulty) &&
        (!type || q.questionType === type) &&
        (!search ||
          q.questionText.toLowerCase().includes(search) ||
          q.companyName.toLowerCase().includes(search) ||
          q.topicName.toLowerCase().includes(search))
      );
    });

    if (filtered.length === 0) {

      const empty = document.createElement('div');
      empty.className = 'empty-state';

      addText(empty, 'h3', 'No matching questions found.');
      addText(empty, 'p',
        'Try changing your filters or selecting another company.');

      container.appendChild(empty);
      return;
    }

    filtered.forEach(q => {

      const card = document.createElement('div');
      card.className = 'card card-elevated p-4 mb-3';

      const heading = document.createElement('div');
      heading.className = 'd-flex justify-content-between flex-wrap gap-2';

      addText(heading, 'strong', q.companyName, 'badge badge-purple');
      addText(heading, 'span', q.questionType, 'badge badge-blue');
      addText(heading, 'span', q.difficulty || 'Unspecified',
        'badge badge-gray');

      card.appendChild(heading);

      addText(card, 'h3', q.questionText, 'mt-3');

      addText(
        card,
        'p',
        `Career: ${q.careerPathName || 'General'} | Topic: ${q.topicName}`,
        'text-secondary'
      );

      addText(
        card,
        'p',
        'Source: General practice — not verified as a past question from this company.',
        'text-secondary'
      );

      if (q.answer) {

        const answerButton = document.createElement('button');

        answerButton.type = 'button';
        answerButton.className = 'btn btn-sm btn-outline';
        answerButton.textContent = 'View Suggested Explanation';

        const answer = document.createElement('div');
        answer.className = 'card p-3 mt-3';
        answer.style.display = 'none';

        addText(answer, 'strong', 'Suggested Explanation');
        addText(answer, 'p', q.answer);

        answerButton.addEventListener('click', function () {

          const visible = answer.style.display !== 'none';

          answer.style.display = visible ? 'none' : 'block';

          answerButton.textContent = visible
            ? 'View Suggested Explanation'
            : 'Hide Explanation';
        });

        card.appendChild(answerButton);
        card.appendChild(answer);
      }

      container.appendChild(card);
    });
  }

  [
    companyFilter,
    roleFilter,
    topicFilter,
    difficultyFilter,
    typeFilter
  ].forEach(control => control.addEventListener('change', renderQuestions));

  searchInput.addEventListener('input', renderQuestions);

  loadQuestions();

});


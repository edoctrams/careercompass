
/**
 * Career Compass — Student Onboarding & Career Interest Assessment
 */

document.addEventListener('DOMContentLoaded', function () {

  let currentStep = 1;
  const totalSteps = 4;

  const CAREER_STREAMS = [
    'Software Development Engineer (SDE)',
    'Full Stack Developer',
    'AI/ML Engineer',
    'Data Analyst'
  ];

  const onboardingData = {
    branch: '',
    graduationYear: 2026,
    currentYear: 3,
    dailyAvailableHours: 3,
    targetRoleName: '',
    careerGoal: ''
  };

  let assessmentQuestions = [];
  let assessmentAnswers = {};
  let assessmentEvaluated = false;

  const nextBtn = document.getElementById('onboarding-next-btn');
  const prevBtn = document.getElementById('onboarding-prev-btn');
  const submitBtn = document.getElementById('onboarding-submit-btn');

  const stepIndicator =
    document.getElementById('onboarding-step-indicator');

  const progressBar =
    document.getElementById('onboarding-progress-bar');

  const loadingEl =
    document.getElementById('assessment-loading');

  const errorEl =
    document.getElementById('assessment-error');

  const questionsContainer =
    document.getElementById('assessment-questions');

  const analyzeBtn =
    document.getElementById('analyze-career-btn');

  const resultsSection =
    document.getElementById('assessment-results-section');

  const recommendationsList =
    document.getElementById('recommendations-list');

  const selectedCareerSelect =
    document.getElementById('selected-career');

  function notify(message, type = 'info') {
    if (typeof window.showToast === 'function') {
      window.showToast(message, type);
    } else {
      console.log(message);
    }
  }

  function showAssessmentError(message) {
    if (errorEl) {
      errorEl.textContent = message;
      errorEl.style.display = 'block';
    }

    notify(message, 'error');
  }

  function clearAssessmentError() {
    if (errorEl) {
      errorEl.textContent = '';
      errorEl.style.display = 'none';
    }
  }

  // Load assessment questions from backend.
  async function loadAssessmentQuestions() {

    if (assessmentQuestions.length > 0) return;

    if (loadingEl) {
      loadingEl.style.display = 'block';
      loadingEl.textContent = 'Loading career assessment questions...';
    }

    if (analyzeBtn) analyzeBtn.disabled = true;

    clearAssessmentError();

    try {

      const response = await fetch(
        '/api/career-interest-assessment/questions',
        {
          method: 'GET',
          credentials: 'same-origin',
          headers: { 'Accept': 'application/json' }
        }
      );

      if (!response.ok) {
        throw new Error(
          'Unable to load questions. Please check your login.'
        );
      }

      const questions = await response.json();

      if (!Array.isArray(questions) || questions.length !== 8) {
        throw new Error(
          'The assessment must contain exactly 8 questions.'
        );
      }

      assessmentQuestions = questions;

      renderAssessmentQuestions();

      if (loadingEl) loadingEl.style.display = 'none';
      if (analyzeBtn) analyzeBtn.disabled = false;

    } catch (error) {

      console.error('Assessment loading error:', error);

      if (loadingEl) {
        loadingEl.textContent =
          'Could not load assessment questions. Please retry.';
      }

      showAssessmentError(error.message);
    }
  }

  // Display questions and their four answer options.
  function renderAssessmentQuestions() {

    if (!questionsContainer) return;

    questionsContainer.innerHTML = '';

    assessmentQuestions.forEach((question, index) => {

      const card = document.createElement('div');
      card.className = 'card p-3 mb-3';

      const heading = document.createElement('h3');
      heading.style.fontSize = '1rem';
      heading.style.fontWeight = '700';

      heading.textContent =
        `${index + 1}. ${question.prompt}`;

      card.appendChild(heading);

      const optionsContainer = document.createElement('div');
      optionsContainer.style.marginTop = '0.75rem';

      question.options.forEach(option => {

        const label = document.createElement('label');

        label.style.display = 'flex';
        label.style.alignItems = 'flex-start';
        label.style.gap = '0.65rem';
        label.style.padding = '0.7rem';
        label.style.marginBottom = '0.5rem';
        label.style.border = '1px solid var(--border)';
        label.style.borderRadius = 'var(--radius-md)';
        label.style.cursor = 'pointer';

        const radio = document.createElement('input');

        radio.type = 'radio';
        radio.name = question.id;
        radio.value = option.id;
        radio.style.marginTop = '0.2rem';
        radio.required = true;

        if (assessmentAnswers[question.id] === option.id) {
          radio.checked = true;
        }

        radio.addEventListener('change', function () {
          assessmentAnswers[question.id] = option.id;
          clearAssessmentError();

          assessmentEvaluated = false;

          if (resultsSection) {
            resultsSection.style.display = 'none';
          }

          if (nextBtn && currentStep === 2) {
            nextBtn.disabled = true;
          }
        });

        const text = document.createElement('span');
        text.textContent = option.text;

        label.appendChild(radio);
        label.appendChild(text);

        optionsContainer.appendChild(label);
      });

      card.appendChild(optionsContainer);
      questionsContainer.appendChild(card);
    });
  }

  // Collect all selected answers.
  function collectAnswers() {

    const answers = {};

    for (const question of assessmentQuestions) {

      const selected = document.querySelector(
        `input[name="${question.id}"]:checked`
      );

      if (!selected) {
        throw new Error(
          `Please answer question ${question.id.replace('Q', '')}.`
        );
      }

      answers[question.id] = selected.value;
    }

    return answers;
  }

  // Analyze student interests.
  if (analyzeBtn) {

    analyzeBtn.addEventListener('click', async function () {

      clearAssessmentError();

      let answers;

      try {
        answers = collectAnswers();
      } catch (error) {
        showAssessmentError(error.message);
        return;
      }

      assessmentAnswers = answers;

      analyzeBtn.disabled = true;
      analyzeBtn.textContent = 'Analyzing your interests...';

      try {

        const response = await fetch(
          '/api/career-interest-assessment/evaluate',
          {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
              'Content-Type': 'application/json',
              'Accept': 'application/json'
            },
            body: JSON.stringify({
              answers: answers,
              selectedCareerName: null
            })
          }
        );

        const data = await response.json();

        if (!response.ok) {
          throw new Error(
            data.error || 'Career assessment failed.'
          );
        }

        renderRecommendations(data);

        assessmentEvaluated = true;

        if (nextBtn) nextBtn.disabled = false;

        notify(
          'Your career recommendations are ready!',
          'success'
        );

      } catch (error) {

        console.error('Assessment evaluation error:', error);

        showAssessmentError(error.message);

      } finally {

        analyzeBtn.disabled = false;

        analyzeBtn.innerHTML =
          '<i class="fas fa-chart-line me-2"></i> Analyze My Interests';
      }
    });
  }

  // Display the top-three recommendations.
  function renderRecommendations(data) {

    if (!recommendationsList || !resultsSection) return;

    recommendationsList.innerHTML = '';

    const recommendations = data.recommendations || [];

    if (recommendations.length !== 3) {
      throw new Error(
        'The system did not return three career recommendations.'
      );
    }

    recommendations.forEach((item, index) => {

      const card = document.createElement('div');
      card.className = 'card card-elevated p-3 mb-3';

      const title = document.createElement('h4');
      title.style.fontWeight = '700';
      title.textContent =
        `${index + 1}. ${item.careerName}`;

      const score = document.createElement('p');
      score.style.fontWeight = '700';
      score.style.color = 'var(--success)';
      score.textContent =
        `Interest Match: ${item.score}%`;

      const reason = document.createElement('p');
      reason.style.color = 'var(--text-secondary)';
      reason.textContent = item.reason;

      const chooseBtn = document.createElement('button');
      chooseBtn.type = 'button';
      chooseBtn.className = 'btn btn-sm btn-outline';
      chooseBtn.textContent = 'Choose this career';

      chooseBtn.addEventListener('click', function () {
        selectedCareerSelect.value = item.careerName;
        onboardingData.targetRoleName = item.careerName;

        notify(
          `${item.careerName} selected.`,
          'success'
        );
      });

      card.appendChild(title);
      card.appendChild(score);
      card.appendChild(reason);
      card.appendChild(chooseBtn);

      recommendationsList.appendChild(card);
    });

    // Allow manual selection of any of the four careers.
    selectedCareerSelect.innerHTML =
      '<option value="">Select a career</option>';

    CAREER_STREAMS.forEach(career => {

      const option = document.createElement('option');

      option.value = career;
      option.textContent = career;

      selectedCareerSelect.appendChild(option);
    });

    selectedCareerSelect.value =
      data.recommendedCareerName;

    onboardingData.targetRoleName =
      data.recommendedCareerName;

    selectedCareerSelect.addEventListener('change', function () {

      onboardingData.targetRoleName = this.value;
    });

    resultsSection.style.display = 'block';

    resultsSection.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    });
  }

  // Validate the current onboarding step.
  function validateCurrentStep(step) {

    if (step === 1) {

      onboardingData.branch =
        document.getElementById('onboarding-branch').value;

      onboardingData.graduationYear = parseInt(
        document.getElementById('onboarding-grad-year').value,
        10
      );

      onboardingData.currentYear = parseInt(
        document.getElementById('onboarding-curr-year').value,
        10
      );

    } else if (step === 2) {

      if (!assessmentEvaluated) {
        notify(
          'Please complete and analyze the career assessment.',
          'warning'
        );
        return false;
      }

      onboardingData.targetRoleName =
        selectedCareerSelect.value;

      if (!CAREER_STREAMS.includes(onboardingData.targetRoleName)) {
        notify('Please select a valid career stream.', 'warning');
        return false;
      }

    } else if (step === 3) {

      onboardingData.dailyAvailableHours = parseInt(
        document.getElementById('onboarding-hours').value,
        10
      );

      onboardingData.careerGoal =
        document.getElementById('onboarding-goal').value.trim();

      if (
        !Number.isInteger(onboardingData.dailyAvailableHours)
        || onboardingData.dailyAvailableHours < 1
        || onboardingData.dailyAvailableHours > 14
      ) {
        notify(
          'Daily preparation hours must be between 1 and 14.',
          'warning'
        );
        return false;
      }
    }

    return true;
  }

  // Update the four-step wizard.
  function updateStepView() {

    document.querySelectorAll('.onboarding-step-pane')
      .forEach((pane, index) => {

        pane.style.display =
          index + 1 === currentStep ? 'block' : 'none';
      });

    const percentage =
      (currentStep / totalSteps) * 100;

    if (progressBar) {
      progressBar.style.width = percentage + '%';
    }

    if (stepIndicator) {
      stepIndicator.textContent =
        `Step ${currentStep} of ${totalSteps}`;
    }

    if (prevBtn) {
      prevBtn.style.display =
        currentStep > 1 ? 'inline-flex' : 'none';
    }

    if (nextBtn) {

      nextBtn.style.display =
        currentStep < totalSteps ? 'inline-flex' : 'none';

      nextBtn.disabled =
        currentStep === 2 && !assessmentEvaluated;
    }

    if (submitBtn) {
      submitBtn.style.display =
        currentStep === totalSteps ? 'inline-flex' : 'none';
    }

    if (currentStep === 2) {
      loadAssessmentQuestions();
    }
  }

  // Next button.
  if (nextBtn) {

    nextBtn.addEventListener('click', function () {

      if (validateCurrentStep(currentStep)) {
        currentStep++;
        updateStepView();
      }
    });
  }

  // Previous button.
  if (prevBtn) {

    prevBtn.addEventListener('click', function () {

      if (currentStep > 1) {
        currentStep--;
        updateStepView();
      }
    });
  }

  // Submit profile, assessment and generate roadmap.
  if (submitBtn) {

    submitBtn.addEventListener('click', async function () {

      if (!onboardingData.targetRoleName) {
        notify('Please select your career stream.', 'warning');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Saving Your Career Profile...';

      try {

        // Save academic and student profile information.
        const profileResponse = await fetch('/api/user/profile', {
          method: 'POST',
          credentials: 'same-origin',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(onboardingData)
        });

        if (!profileResponse.ok) {
          throw new Error('Unable to save your student profile.');
        }

        // Persist assessment history and selected career.
        const assessmentResponse = await fetch(
          '/api/career-interest-assessment/submit',
          {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              answers: assessmentAnswers,
              selectedCareerName: onboardingData.targetRoleName
            })
          }
        );

        const assessmentData = await assessmentResponse.json();

        if (!assessmentResponse.ok) {
          throw new Error(
            assessmentData.error || 'Unable to save career assessment.'
          );
        }

        // Generate the roadmap for the selected career.
        const roadmapResponse = await fetch('/api/roadmaps/generate', {
          method: 'POST',
          credentials: 'same-origin',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            careerPathName: onboardingData.targetRoleName
          })
        });

        if (!roadmapResponse.ok) {
          console.warn(
            'Profile and assessment saved, but roadmap generation failed.'
          );

          notify(
            'Assessment saved. Roadmap generation needs attention.',
            'warning'
          );

        } else {

          notify(
            'Profile, assessment and roadmap saved successfully!',
            'success'
          );
        }

        submitBtn.textContent = 'Opening Dashboard...';

        setTimeout(function () {
          window.location.href = '/dashboard';
        }, 800);

      } catch (error) {

        console.error('Onboarding submission error:', error);

        notify(error.message, 'error');

        submitBtn.disabled = false;
        submitBtn.innerHTML =
          '<i class="fas fa-compass me-2"></i> Launch My Dashboard';
      }
    });
  }

  // Initial wizard state.
  updateStepView();

});

/**
 * Career Compass — Student Onboarding Wizard Controller
 */

document.addEventListener('DOMContentLoaded', function () {
  let currentStep = 1;
  const totalSteps = 4;
  const onboardingData = {
    branch: '',
    graduationYear: 2026,
    currentYear: 3,
    dailyAvailableHours: 3,
    targetRoleName: 'Software Development Engineer (SDE)',
    careerGoal: ''
  };

  const nextBtn = document.getElementById('onboarding-next-btn');
  const prevBtn = document.getElementById('onboarding-prev-btn');
  const submitBtn = document.getElementById('onboarding-submit-btn');
  const stepIndicator = document.getElementById('onboarding-step-indicator');
  const progressBar = document.getElementById('onboarding-progress-bar');

  // Career Selection Cards
  const careerCards = document.querySelectorAll('.career-select-card');
  careerCards.forEach(card => {
    card.addEventListener('click', function () {
      careerCards.forEach(c => c.classList.remove('active'));
      this.classList.add('active');
      const selected = this.getAttribute('data-career');
      if (selected) {
        onboardingData.targetRoleName = selected;
      }
    });
  });

  if (nextBtn) {
    nextBtn.addEventListener('click', function () {
      if (validateCurrentStep(currentStep)) {
        currentStep++;
        updateStepView();
      }
    });
  }

  if (prevBtn) {
    prevBtn.addEventListener('click', function () {
      if (currentStep > 1) {
        currentStep--;
        updateStepView();
      }
    });
  }

  function validateCurrentStep(step) {
    if (step === 1) {
      const branchSelect = document.getElementById('onboarding-branch');
      if (branchSelect) onboardingData.branch = branchSelect.value;
      const gradYear = document.getElementById('onboarding-grad-year');
      if (gradYear) onboardingData.graduationYear = parseInt(gradYear.value, 10);
      const curYear = document.getElementById('onboarding-curr-year');
      if (curYear) onboardingData.currentYear = parseInt(curYear.value, 10);
    } else if (step === 2) {
      if (!onboardingData.targetRoleName) {
        window.showToast('Please select your target career goal.', 'warning');
        return false;
      }
    } else if (step === 3) {
      const hours = document.getElementById('onboarding-hours');
      if (hours) onboardingData.dailyAvailableHours = parseInt(hours.value, 10);
      const goal = document.getElementById('onboarding-goal');
      if (goal) onboardingData.careerGoal = goal.value.trim();
    }
    return true;
  }

  function updateStepView() {
    // Hide all steps, show current
    document.querySelectorAll('.onboarding-step-pane').forEach((pane, idx) => {
      pane.style.display = (idx + 1 === currentStep) ? 'block' : 'none';
    });

    // Update progress
    const percent = ((currentStep) / totalSteps) * 100;
    if (progressBar) progressBar.style.width = percent + '%';
    if (stepIndicator) stepIndicator.textContent = `Step ${currentStep} of ${totalSteps}`;

    // Update buttons
    if (prevBtn) prevBtn.style.display = currentStep > 1 ? 'inline-flex' : 'none';
    if (nextBtn) nextBtn.style.display = currentStep < totalSteps ? 'inline-flex' : 'none';
    if (submitBtn) submitBtn.style.display = currentStep === totalSteps ? 'inline-flex' : 'none';
  }

  if (submitBtn) {
    submitBtn.addEventListener('click', async function () {
      submitBtn.disabled = true;
      submitBtn.innerHTML = '<span class="spinner"></span> Personalizing Your Compass...';

      try {
        const response = await fetch('/api/user/profile', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(onboardingData)
        });

        if (response.ok) {
          // Trigger initial roadmap generation
          await fetch('/api/roadmaps/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ careerPathName: onboardingData.targetRoleName })
          });

          window.showToast('Profile and roadmap configured successfully!', 'success');
          setTimeout(() => {
            window.location.href = '/dashboard';
          }, 600);
        } else {
          window.showToast('Failed to save profile. Redirecting to dashboard.', 'warning');
          setTimeout(() => { window.location.href = '/dashboard'; }, 800);
        }
      } catch (err) {
        console.error('Onboarding submit error:', err);
        window.location.href = '/dashboard';
      }
    });
  }

  // Initial step view
  updateStepView();
});

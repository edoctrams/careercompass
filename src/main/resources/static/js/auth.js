/**
 * Career Compass — Authentication & Form Handling
 */

document.addEventListener('DOMContentLoaded', function () {
  // Password Visibility Toggle
  const toggleButtons = document.querySelectorAll('.input-password-toggle');
  toggleButtons.forEach(button => {
    button.addEventListener('click', function () {
      const targetInput = this.parentElement.querySelector('input');
      const icon = this.querySelector('i');
      if (targetInput.type === 'password') {
        targetInput.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
      } else {
        targetInput.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
      }
    });
  });

  // Password Strength Evaluation
  const passwordInput = document.getElementById('reg-password');
  const strengthBar = document.getElementById('password-strength-bar');
  const strengthLabel = document.getElementById('password-strength-label');

  if (passwordInput && strengthBar && strengthLabel) {
    passwordInput.addEventListener('input', function () {
      const val = this.value;
      let score = 0;
      if (val.length >= 8) score++;
      if (/[A-Z]/.test(val)) score++;
      if (/[0-9]/.test(val)) score++;
      if (/[^A-Za-z0-9]/.test(val)) score++;

      let width = '0%';
      let color = 'var(--border-color)';
      let text = 'Too weak';

      if (val.length > 0) {
        if (score <= 1) {
          width = '25%';
          color = 'var(--color-error)';
          text = 'Weak (Needs 8+ chars, upper & number)';
        } else if (score === 2) {
          width = '50%';
          color = 'var(--color-warning)';
          text = 'Fair (Add special character)';
        } else if (score === 3) {
          width = '75%';
          color = 'var(--accent-secondary)';
          text = 'Good';
        } else if (score >= 4) {
          width = '100%';
          color = 'var(--color-success)';
          text = 'Strong password';
        }
      }

      strengthBar.style.width = width;
      strengthBar.style.backgroundColor = color;
      strengthLabel.textContent = text;
      strengthLabel.style.color = color;
    });
  }

  // Registration Form Validation & Submit
  const regForm = document.getElementById('register-form');
  if (regForm) {
    regForm.addEventListener('submit', function (e) {
      const password = document.getElementById('reg-password').value;
      const confirm = document.getElementById('reg-confirm-password').value;
      const submitBtn = regForm.querySelector('button[type="submit"]');

      if (password !== confirm) {
        e.preventDefault();
        window.showToast('Passwords do not match. Please re-enter.', 'error');
        return;
      }

      if (password.length < 8) {
        e.preventDefault();
        window.showToast('Password must be at least 8 characters long.', 'warning');
        return;
      }

      // Add loading spinner to submit button
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner"></span> Creating Account...';
      }
    });
  }

  // Login Form Submit Loading
  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    loginForm.addEventListener('submit', function () {
      const submitBtn = loginForm.querySelector('button[type="submit"]');
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner"></span> Signing in...';
      }
    });
  }
});

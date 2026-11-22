/**
 * Career Compass — Core UI Framework & Client Controller
 * Bulletproof Event Handling, Modals, Toasts, and Drawer Navigation
 */

(function () {
  'use strict';

  // Global Toast Notifications
  window.showToast = function (message, type, duration) {
    type = type || 'info';
    duration = duration || 3500;

    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;

    let icon = 'fa-info-circle';
    if (type === 'success') icon = 'fa-check-circle';
    if (type === 'error') icon = 'fa-exclamation-circle';
    if (type === 'warning') icon = 'fa-exclamation-triangle';

    toast.innerHTML =
      '<div style="font-size: 1.1rem; color: var(--' + (type === 'error' ? 'danger' : type) + ');">' +
        '<i class="fas ' + icon + '"></i>' +
      '</div>' +
      '<div class="toast-content">' +
        '<div class="toast-title">' + type.toUpperCase() + '</div>' +
        '<div class="toast-message">' + message + '</div>' +
      '</div>' +
      '<button type="button" class="toast-close" aria-label="Close">&times;</button>';

    container.appendChild(toast);

    const closeBtn = toast.querySelector('.toast-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        toast.remove();
      });
    }

    setTimeout(function () {
      if (toast.parentElement) {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(function () { toast.remove(); }, 200);
      }
    }, duration);
  };

  // Global Modals
  window.openModal = function (modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('open');
      document.body.style.overflow = 'hidden';
    }
  };

  window.closeModal = function (modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('open');
      document.body.style.overflow = '';
    }
  };

  // Close modals on clicking overlay backdrop
  document.addEventListener('click', function (e) {
    if (e.target && e.target.classList && e.target.classList.contains('modal-overlay')) {
      e.target.classList.remove('open');
      document.body.style.overflow = '';
    }
  });

  // Close modals on ESC key
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      const openModal = document.querySelector('.modal-overlay.open');
      if (openModal) {
        openModal.classList.remove('open');
        document.body.style.overflow = '';
      }
      const userDropdown = document.getElementById('user-dropdown');
      if (userDropdown) {
        userDropdown.classList.remove('show');
      }
    }
  });

  // DOM Content Loaded Handler
  document.addEventListener('DOMContentLoaded', function () {
    // Mobile Sidebar Drawer
    const mobileToggle = document.getElementById('mobile-sidebar-toggle');
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.getElementById('sidebar-backdrop');
    const closeBtn = document.getElementById('sidebar-close-btn');

    function closeSidebar() {
      if (sidebar) sidebar.classList.remove('mobile-open');
      if (backdrop) backdrop.classList.remove('active');
    }

    if (closeBtn) {
      closeBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        closeSidebar();
      });
    }

    if (mobileToggle && sidebar) {
      mobileToggle.addEventListener('click', function (e) {
        e.stopPropagation();
        sidebar.classList.toggle('mobile-open');
        if (backdrop) backdrop.classList.toggle('active');
      });
    }

    if (backdrop) {
      backdrop.addEventListener('click', function (e) {
        e.stopPropagation();
        closeSidebar();
      });
    }

    // Auto-close sidebar on mobile when a navigation link is clicked
    if (sidebar) {
      const navLinks = sidebar.querySelectorAll('.sidebar-link');
      navLinks.forEach(function (link) {
        link.addEventListener('click', function () {
          if (window.innerWidth < 992) {
            closeSidebar();
          }
        });
      });
    }

    // User Profile Dropdown Toggle
    const userMenuBtn = document.getElementById('user-menu-btn');
    const userDropdown = document.getElementById('user-dropdown');

    if (userMenuBtn && userDropdown) {
      userMenuBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        userDropdown.classList.toggle('show');
      });
    }

    // Close dropdowns when clicking outside
    document.addEventListener('click', function (e) {
      if (userDropdown && userDropdown.classList.contains('show')) {
        if (!userDropdown.contains(e.target) && (!userMenuBtn || !userMenuBtn.contains(e.target))) {
          userDropdown.classList.remove('show');
        }
      }
    });

    // Tab Navigation Switchers
    const tabWrappers = document.querySelectorAll('.tabs-wrapper');
    tabWrappers.forEach(function (wrapper) {
      const tabButtons = wrapper.querySelectorAll('.tab-btn');
      const tabPanes = wrapper.querySelectorAll('.tab-pane');

      tabButtons.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
          e.preventDefault();
          const targetId = this.getAttribute('data-tab');

          tabButtons.forEach(function (b) { b.classList.remove('active'); });
          tabPanes.forEach(function (p) { p.classList.remove('active'); });

          this.classList.add('active');
          const targetPane = wrapper.querySelector('#' + targetId);
          if (targetPane) {
            targetPane.classList.add('active');
          }
        });
      });
    });

    // Check URL query parameters for feedback notifications
    const params = new URLSearchParams(window.location.search);
    if (params.get('error')) {
      window.showToast('Invalid email or password. Please verify your credentials.', 'error');
    }
    if (params.get('logout')) {
      window.showToast('You have been signed out successfully.', 'info');
    }
    if (params.get('registered')) {
      window.showToast('Account created successfully! Please sign in.', 'success');
    }
  });
})();

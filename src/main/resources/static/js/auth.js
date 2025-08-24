document.addEventListener('DOMContentLoaded', function() {
    const toggle = document.getElementById('auth-toggle');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginLabel = document.querySelector('.login-label');
    const registerLabel = document.querySelector('.register-label');
    const passwordInput = document.getElementById('register-password');

    // Initialize based on URL parameters or default to login
    const urlParams = new URLSearchParams(window.location.search);
    const showRegister = urlParams.get('register') === 'true';
    
    if (showRegister) {
        switchToRegister();
    }

    // Toggle event listener
    toggle.addEventListener('change', function() {
        if (this.checked) {
            switchToRegister();
        } else {
            switchToLogin();
        }
    });

    // Password strength checker
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            updatePasswordStrength(this.value);
        });
    }

    // Handle login form submission
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        handleLogin(this);
    });

    // Handle register form submission
    registerForm.addEventListener('submit', function(e) {
        e.preventDefault();
        handleRegister(this);
    });

    // Auto-hide messages after 5 seconds
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        setTimeout(() => {
            message.style.animation = 'slideOut 0.3s ease forwards';
            setTimeout(() => {
                message.remove();
            }, 300);
        }, 5000);
    });
});

function handleLogin(form) {
    const submitBtn = form.querySelector('.submit-btn');
    const formData = new FormData(form);
    
    submitBtn.classList.add('loading');
    
    fetch('/login', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            showMessage('success', 'Login successful! Redirecting...');
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            showMessage('error', data.message || 'Login failed');
        }
    })
    .catch(error => {
        showMessage('error', 'Network error. Please try again.');
        console.error('Login error:', error);
    })
    .finally(() => {
        submitBtn.classList.remove('loading');
    });
}

function handleRegister(form) {
    const submitBtn = form.querySelector('.submit-btn');
    const formData = new FormData(form);
    
    submitBtn.classList.add('loading');
    
    fetch('/register', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            showMessage('success', 'Registration successful! Redirecting...');
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            showMessage('error', data.message || 'Registration failed');
        }
    })
    .catch(error => {
        showMessage('error', 'Network error. Please try again.');
        console.error('Registration error:', error);
    })
    .finally(() => {
        submitBtn.classList.remove('loading');
    });
}

function showMessage(type, text) {
    // Remove existing messages
    const existingMessages = document.querySelectorAll('.message');
    existingMessages.forEach(msg => msg.remove());
    
    // Create new message
    const messageContainer = document.querySelector('.message-container');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}-message`;
    messageDiv.innerHTML = `
        <i class="icon">${type === 'success' ? '✅' : '⚠️'}</i>
        <span>${text}</span>
    `;
    
    messageContainer.appendChild(messageDiv);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        messageDiv.style.animation = 'slideOut 0.3s ease forwards';
        setTimeout(() => {
            messageDiv.remove();
        }, 300);
    }, 5000);
}

function switchToLogin() {
    const toggle = document.getElementById('auth-toggle');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginLabel = document.querySelector('.login-label');
    const registerLabel = document.querySelector('.register-label');

    toggle.checked = false;
    loginForm.classList.add('active');
    registerForm.classList.remove('active');
    loginLabel.classList.add('active');
    registerLabel.classList.remove('active');

    // Update URL without reload
    const url = new URL(window.location);
    url.searchParams.delete('register');
    window.history.replaceState({}, '', url);
}

function switchToRegister() {
    const toggle = document.getElementById('auth-toggle');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const loginLabel = document.querySelector('.login-label');
    const registerLabel = document.querySelector('.register-label');

    toggle.checked = true;
    loginForm.classList.remove('active');
    registerForm.classList.add('active');
    loginLabel.classList.remove('active');
    registerLabel.classList.add('active');

    // Update URL without reload
    const url = new URL(window.location);
    url.searchParams.set('register', 'true');
    window.history.replaceState({}, '', url);
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const eyeIcon = input.parentElement.querySelector('.eye-icon');
    
    if (input.type === 'password') {
        input.type = 'text';
        eyeIcon.textContent = '🙈';
    } else {
        input.type = 'password';
        eyeIcon.textContent = '👁️';
    }
}

function updatePasswordStrength(password) {
    const strengthBar = document.querySelector('.strength-bar');
    const strengthText = document.querySelector('.strength-text');
    
    if (!strengthBar || !strengthText) return;
    
    let strength = 0;
    let feedback = '';
    
    if (password.length >= 8) strength += 25;
    if (/[a-z]/.test(password)) strength += 25;
    if (/[A-Z]/.test(password)) strength += 25;
    if (/[0-9]/.test(password) || /[^A-Za-z0-9]/.test(password)) strength += 25;
    
    if (strength === 0) {
        feedback = 'Enter a password';
        strengthBar.style.background = '#e2e8f0';
    } else if (strength <= 25) {
        feedback = 'Weak password';
        strengthBar.style.background = 'linear-gradient(90deg, #ef4444 ' + strength + '%, #e2e8f0 ' + strength + '%)';
    } else if (strength <= 50) {
        feedback = 'Fair password';
        strengthBar.style.background = 'linear-gradient(90deg, #f59e0b ' + strength + '%, #e2e8f0 ' + strength + '%)';
    } else if (strength <= 75) {
        feedback = 'Good password';
        strengthBar.style.background = 'linear-gradient(90deg, #3b82f6 ' + strength + '%, #e2e8f0 ' + strength + '%)';
    } else {
        feedback = 'Strong password';
        strengthBar.style.background = 'linear-gradient(90deg, #10b981 ' + strength + '%, #e2e8f0 ' + strength + '%)';
    }
    
    strengthText.textContent = feedback;
}

// Add CSS animation for slide out
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOut {
        to {
            opacity: 0;
            transform: translateX(-20px);
        }
    }
`;
document.head.appendChild(style);
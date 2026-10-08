if (window.location.pathname.endsWith("/admin-login.html")) {
  window.history.replaceState(null, "", "/admin/login");
}

let isSubmitting = false;
let isRedirecting = false;

// Toggle Password Visibility (Eye Icon)
function togglePasswordVisibility() {
  const pwdField = document.getElementById("staff-password");
  const eyeShow = document.getElementById("eye-icon-show");
  const eyeHide = document.getElementById("eye-icon-hide");
  const toggleBtn = document.getElementById("toggle-password-btn");

  if (!pwdField) return;

  if (pwdField.type === "password") {
    pwdField.type = "text";
    if (eyeShow) eyeShow.style.display = "none";
    if (eyeHide) eyeHide.style.display = "block";
    if (toggleBtn) {
      toggleBtn.title = "Hide password";
      toggleBtn.setAttribute("aria-label", "Hide password");
    }
  } else {
    pwdField.type = "password";
    if (eyeShow) eyeShow.style.display = "block";
    if (eyeHide) eyeHide.style.display = "none";
    if (toggleBtn) {
      toggleBtn.title = "Show password";
      toggleBtn.setAttribute("aria-label", "Show password");
    }
  }
  pwdField.focus();
}

// Auto-recover and re-enable form controls if the window lost/regained focus or desktop changed

async function handleStaffLogin(e) {
  if (e && typeof e.preventDefault === "function") {
    e.preventDefault();
  }

  if (isSubmitting || isRedirecting) {
    return;
  }

  const alertBox = document.getElementById("login-alert");
  if (alertBox) alertBox.style.display = "none";

  const btn = document.getElementById("staff-submit-btn");
  const userField = document.getElementById("staff-username");
  const pwdField = document.getElementById("staff-password");

  const username = userField ? userField.value.trim() : "";
  const password = pwdField ? pwdField.value : "";

  if (!username) {
    if (alertBox) {
      alertBox.className = "alert-box alert-danger";
      alertBox.textContent = "Please enter your staff username or email.";
      alertBox.style.display = "block";
    }
    if (userField) userField.focus();
    return;
  }

  if (!password) {
    if (alertBox) {
      alertBox.className = "alert-box alert-danger";
      alertBox.textContent = "Please enter your secure password.";
      alertBox.style.display = "block";
    }
    if (pwdField) pwdField.focus();
    return;
  }

  isSubmitting = true;
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Verifying Staff Credentials...";
  }

  // Timeout safety: 10 seconds max so the UI never hangs indefinitely
  const timeoutPromise = new Promise((_, reject) =>
    setTimeout(() => reject(new Error("Request timed out. Please check your connection or server status.")), 10000)
  );

  try {
    const loginPromise = API.auth.login(username, password);
    const response = await Promise.race([loginPromise, timeoutPromise]);
    const user = response;

    // Security Gatekeeper: Ensure user is an internal staff member
    if (!user || user.role === "CUSTOMER") {
      throw new Error("Access Denied: Customer accounts are not permitted in the internal staff console. Please visit the public storefront.");
    }

    isRedirecting = true;

    // Store staff session
    localStorage.setItem("shopease_user", JSON.stringify(user));
    if (user.token) {
      localStorage.setItem("shopease_token", user.token);
    }

    if (alertBox) {
      alertBox.className = "alert-box alert-success";
      alertBox.textContent = `Authentication Successful! Welcome, ${user.fullName || user.username} (${user.role}). Redirecting...`;
      alertBox.style.display = "block";
    }

    if (btn) {
      btn.textContent = "Redirecting to Management Console...";
    }

    setTimeout(() => {
      window.location.href = "/admin-dashboard.html";
    }, 600);

  } catch (err) {
    console.error("Staff login failed:", err);
    if (alertBox) {
      alertBox.className = "alert-box alert-danger";
      alertBox.textContent = err.message || "Invalid staff credentials. Please check your username and password.";
      alertBox.style.display = "block";
    }
  } finally {
    isSubmitting = false;
    if (!isRedirecting && btn) {
      btn.disabled = false;
      btn.textContent = "Authenticate & Access Console →";
    }
  }
}

// Ensure Enter key always triggers login smoothly
document.addEventListener("DOMContentLoaded", () => {
  const userField = document.getElementById("staff-username");
  const pwdField = document.getElementById("staff-password");

  [userField, pwdField].forEach(field => {
    if (field) {
      field.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          handleStaffLogin(e);
        }
      });
    }
  });
});

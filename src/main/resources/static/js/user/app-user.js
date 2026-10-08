/**
 * ShopEase Lanka - Login, registration, password reset, profile, addresses, activity log (User module)
 * Extends the shared App object from js/app-core.js
 */
Object.assign(App, {
  // ===================================================================
  // LOGIN MODAL
  // ===================================================================
  
  togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    btn.title = isPassword ? 'Hide password' : 'Show password';
    btn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
    btn.innerHTML = isPassword ? (Icons.eyeOff || 'Hide') : (Icons.eye || 'Show');
  },

  openLoginModal() {
    let modal = document.getElementById("login-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "login-modal";
      modal.className = "modal-overlay";
      modal.innerHTML = `
        <div class="modal-card" style="max-width: 440px;">
          <div class="modal-header">
            <h3 style="font-weight: 800; display: flex; align-items: center; gap: 8px;">
              ${Icons.lock} Account Sign In
            </h3>
            <button class="close-btn" onclick="document.getElementById('login-modal').classList.remove('open')">&times;</button>
          </div>
          <div class="modal-body">
            <form id="login-form" onsubmit="App.handleLoginSubmit(event)">
              <div class="form-group" style="margin-bottom: 14px;">
                <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Username or Registered Email</label>
                <input type="text" id="login-username" class="form-control" placeholder="e.g. customer_kasun or email" required>
              </div>
              <div class="form-group" style="margin-bottom: 14px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600; margin: 0;">Password</label>
                  <a href="javascript:void(0)" onclick="App.openForgotPasswordModal()" style="font-size: 0.78rem; color: var(--primary); text-decoration: none;">Forgot Password?</a>
                </div>
                <div style="position: relative;">
                  <input type="password" id="login-password" class="form-control" placeholder="••••••••" required style="padding-right: 42px;">
                  <button type="button" onclick="App.togglePasswordVisibility('login-password', this)" style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: var(--text-muted); cursor: pointer; display: flex; align-items: center; justify-content: center; padding: 4px;" title="Show password" aria-label="Toggle password visibility">
                    ${Icons.eye || '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'}
                  </button>
                </div>
              </div>
              <button type="submit" class="primary-btn" id="login-submit-btn" style="width: 100%; padding: 10px; margin-top: 6px;">Sign In</button>
              </form>
            <div style="text-align: center; margin-top: 16px; font-size: 0.85rem; color: var(--text-muted); border-top: 1px solid var(--border-color); padding-top: 12px;">
              Don't have an account? 
              <a href="javascript:void(0)" onclick="App.openRegisterModal()" style="color: var(--primary); font-weight: 700;">Create Account</a>
            </div>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }
    const regModal = document.getElementById("register-modal");
    if (regModal) regModal.classList.remove("open");
    const pwdModal = document.getElementById("forgot-password-modal");
    if (pwdModal) pwdModal.classList.remove("open");
    modal.classList.add("open");
  },

  async handleLoginSubmit(event) {
    event.preventDefault();
    const btn = document.getElementById("login-submit-btn");
    const username = document.getElementById("login-username").value.trim();
    const password = document.getElementById("login-password").value;

    btn.disabled = true;
    btn.textContent = "Signing In...";
    try {
      const response = await API.auth.login(username, password);
      document.getElementById("login-modal").classList.remove("open");
      this.setUser({
        userId: response.userId,
        username: response.username,
        email: response.email,
        fullName: response.fullName,
        role: response.role
      });
      if (response.role !== 'CUSTOMER') {
        window.location.href = "admin-dashboard.html";
      } else {
        window.location.reload();
      }
    } catch (err) {
      this.showToast(err.message || "Invalid username or password", "error");
    } finally {
      btn.disabled = false;
      btn.textContent = "Sign In";
    }
  },

  // ===================================================================
  // REGISTER MODAL
  // ===================================================================
  openRegisterModal() {
    let modal = document.getElementById("register-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "register-modal";
      modal.className = "modal-overlay";
      modal.innerHTML = `
        <div class="modal-card" style="max-width: 480px;">
          <div class="modal-header">
            <h3 style="font-weight: 800; display: flex; align-items: center; gap: 8px;">
              ${Icons.user} Register New Account
            </h3>
            <button class="close-btn" onclick="document.getElementById('register-modal').classList.remove('open')">&times;</button>
          </div>
          <div class="modal-body">
            <form id="register-form" onsubmit="App.handleRegisterSubmit(event)">
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px;">
                <div class="form-group">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Full Name *</label>
                  <input type="text" id="reg-fullname" class="form-control" placeholder="e.g. Sunil Perera" required>
                </div>
                <div class="form-group">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Username *</label>
                  <input type="text" id="reg-username" class="form-control" placeholder="e.g. sunil_p" minlength="3" required>
                </div>
              </div>
              <div class="form-group" style="margin-bottom: 12px;">
                <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Email Address *</label>
                <input type="email" id="reg-email" class="form-control" placeholder="sunil@example.com" required>
              </div>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px;">
                <div class="form-group">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Password *</label>
                  <div style="position: relative;">
                    <input type="password" id="reg-password" class="form-control" placeholder="Min 6 characters" minlength="6" required style="padding-right: 42px;">
                    <button type="button" onclick="App.togglePasswordVisibility('reg-password', this)" style="position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: var(--text-muted); cursor: pointer; display: flex; align-items: center; justify-content: center; padding: 4px;" title="Show password" aria-label="Toggle password visibility">
                      ${Icons.eye || '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'}
                    </button>
                  </div>
                </div>
                <div class="form-group">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Phone Number</label>
                  <input type="tel" id="reg-phone" class="form-control" placeholder="+94 77 123 4567">
                </div>
              </div>
              <div class="form-group" style="margin-bottom: 14px;">
                <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Default Delivery Address</label>
                <input type="text" id="reg-address" class="form-control" placeholder="e.g. 45 Galle Road, Colombo 03">
              </div>
              <button type="submit" class="primary-btn" id="reg-submit-btn" style="width: 100%; padding: 10px;">Create Account</button>
            </form>
            <div style="text-align: center; margin-top: 16px; font-size: 0.85rem; color: var(--text-muted); border-top: 1px solid var(--border-color); padding-top: 12px;">
              Already have an account? 
              <a href="javascript:void(0)" onclick="App.openLoginModal()" style="color: var(--primary); font-weight: 700;">Sign In</a>
            </div>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }
    const loginModal = document.getElementById("login-modal");
    if (loginModal) loginModal.classList.remove("open");
    modal.classList.add("open");
  },

  async handleRegisterSubmit(event) {
    event.preventDefault();
    const btn = document.getElementById("reg-submit-btn");
    const payload = {
      fullName: document.getElementById("reg-fullname").value.trim(),
      username: document.getElementById("reg-username").value.trim(),
      email: document.getElementById("reg-email").value.trim(),
      password: document.getElementById("reg-password").value,
      phoneNumber: document.getElementById("reg-phone").value.trim(),
      deliveryAddress: document.getElementById("reg-address").value.trim()
    };

    btn.disabled = true;
    btn.textContent = "Creating Account...";
    try {
      const response = await API.auth.register(payload);
      document.getElementById("register-modal").classList.remove("open");
      this.setUser({
        userId: response.userId,
        username: response.username,
        email: response.email,
        fullName: response.fullName,
        role: response.role
      });
      this.showToast("Account created successfully! Welcome to ShopEase Lanka.", "success");
      setTimeout(() => window.location.reload(), 600);
    } catch (err) {
      this.showToast(err.message || "Failed to register account", "error");
    } finally {
      btn.disabled = false;
      btn.textContent = "Create Account";
    }
  },

  // ===================================================================
  // FORGOT / RESET PASSWORD MODAL
  // ===================================================================
  openForgotPasswordModal() {
    let modal = document.getElementById("forgot-password-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "forgot-password-modal";
      modal.className = "modal-overlay";
      modal.innerHTML = `
        <div class="modal-card" style="max-width: 440px;">
          <div class="modal-header">
            <h3 style="font-weight: 800; display:flex; align-items:center; gap:8px;">${Icons.lock} Reset Forgotten Password</h3>
            <button class="close-btn" onclick="document.getElementById('forgot-password-modal').classList.remove('open')">&times;</button>
          </div>
          <div class="modal-body">
            <form onsubmit="App.handleResetPasswordSubmit(event)">
              <div class="form-group" style="margin-bottom: 12px;">
                <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Username or Registered Email</label>
                <input type="text" id="reset-user-email" class="form-control" required placeholder="Enter username or email">
              </div>
              <div class="form-group" style="margin-bottom: 12px;">
                <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">New Password (min 6 chars)</label>
                <input type="password" id="reset-new-password" class="form-control" minlength="6" required placeholder="••••••••">
              </div>
              <button type="submit" class="primary-btn" id="reset-submit-btn" style="width: 100%; padding: 10px; margin-top: 6px;">Update Password</button>
            </form>
            <div style="text-align: center; margin-top: 14px;">
              <a href="javascript:void(0)" onclick="App.openLoginModal()" style="font-size: 0.85rem; color: var(--primary); text-decoration: none;">← Back to Sign In</a>
            </div>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }
    const loginModal = document.getElementById("login-modal");
    if (loginModal) loginModal.classList.remove("open");
    modal.classList.add("open");
  },

  async handleResetPasswordSubmit(event) {
    event.preventDefault();
    const btn = document.getElementById("reset-submit-btn");
    const usernameOrEmail = document.getElementById("reset-user-email").value.trim();
    const newPassword = document.getElementById("reset-new-password").value;

    btn.disabled = true;
    btn.textContent = "Updating Password...";
    try {
      await API.auth.resetPassword(usernameOrEmail, newPassword);
      this.showToast("Password updated successfully! Please sign in with your new password.", "success");
      document.getElementById("forgot-password-modal").classList.remove("open");
      this.openLoginModal();
    } catch (err) {
      this.showToast(err.message || "Failed to reset password", "error");
    } finally {
      btn.disabled = false;
      btn.textContent = "Update Password";
    }
  },

  // ===================================================================
  // CUSTOMER PROFILE & SECURITY AUDIT MODAL
  // ===================================================================
  async openProfileModal() {
    if (!this.currentUser) {
      this.openLoginModal();
      return;
    }

    let modal = document.getElementById("profile-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "profile-modal";
      modal.className = "modal-overlay";
      modal.innerHTML = `
        <div class="modal-card" style="max-width: 650px;">
          <div class="modal-header">
            <h3 style="font-weight: 800; display: flex; align-items: center; gap: 8px;">
              <span style="display:inline-flex; align-items:center; justify-content:center; width:22px; height:22px; color:var(--primary); flex-shrink:0;">
                ${Icons.user}
              </span>
              <span>My Profile & Account Settings</span>
            </h3>
            <button class="close-btn" onclick="document.getElementById('profile-modal').classList.remove('open')">&times;</button>
          </div>
          <div class="modal-body">
            <div style="display: flex; gap: 8px; border-bottom: 2px solid var(--border-color); margin-bottom: 18px;">
              <button class="cat-chip active" id="prof-tab-info-btn" onclick="App.switchProfileTab('info')" style="border-radius: 6px 6px 0 0;">Profile Details</button>
              <button class="cat-chip" id="prof-tab-addrs-btn" onclick="App.switchProfileTab('addrs')" style="border-radius: 6px 6px 0 0;">Delivery Addresses</button>
              <button class="cat-chip" id="prof-tab-sec-btn" onclick="App.switchProfileTab('sec')" style="border-radius: 6px 6px 0 0;">Recent Security Logins</button>
            </div>

            <!-- TAB 1: Profile Info -->
            <div id="prof-tab-info">
              <form onsubmit="App.handleProfileSave(event)">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px;">
                  <div class="form-group">
                    <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Username</label>
                    <input type="text" id="prof-username" class="form-control" required placeholder="Username">
                  </div>
                  <div class="form-group">
                    <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Email</label>
                    <input type="email" id="prof-email" class="form-control" required placeholder="user@example.com">
                  </div>
                </div>
                <div class="form-group" style="margin-bottom: 12px;">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Full Name</label>
                  <input type="text" id="prof-fullname" class="form-control" required>
                </div>
                <div class="form-group" style="margin-bottom: 12px;">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Phone Number</label>
                  <input type="tel" id="prof-phone" class="form-control" placeholder="+94 7X XXX XXXX">
                </div>
                <div class="form-group" style="margin-bottom: 14px;">
                  <label class="form-label" style="font-size: 0.85rem; font-weight: 600;">Default Delivery Address</label>
                  <textarea id="prof-address" class="form-control" rows="2" placeholder="Street, City, Postal Code"></textarea>
                </div>

                <!-- Saved Card Section -->
                <div style="margin-bottom: 16px; padding: 14px; background: var(--bg-elevated); border: 1.5px dashed var(--border-color); border-radius: var(--radius-md); color: var(--text-main);">
                  <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;">
                    <span style="font-size: 0.9rem; font-weight: 700; display: flex; align-items: center; gap: 8px;">
                      <span style="display:inline-flex; align-items:center; justify-content:center; width:20px; height:20px; color:var(--primary); flex-shrink:0;">
                        ${Icons.creditCard}
                      </span>
                      <span>Saved Card Details (for Quick Checkout)</span>
                    </span>
                    <span style="font-size: 0.72rem; color: var(--text-muted);">Auto-populates at Checkout</span>
                  </div>
                  <div class="form-group" style="margin-bottom: 10px;">
                    <label class="form-label" style="font-size: 0.8rem; font-weight: 600;">Cardholder Name</label>
                    <input type="text" id="prof-card-holder" class="form-control" placeholder="Name on Card (e.g. Kasun Perera)">
                  </div>
                  <div style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 10px;">
                    <div class="form-group">
                      <label class="form-label" style="font-size: 0.8rem; font-weight: 600;">Card Number</label>
                      <input type="text" id="prof-card-number" class="form-control" placeholder="16-digit card number" maxlength="19">
                    </div>
                    <div class="form-group">
                      <label class="form-label" style="font-size: 0.8rem; font-weight: 600;">Expiry (MM/YY)</label>
                      <input type="text" id="prof-card-expiry" class="form-control" placeholder="MM/YY" maxlength="5">
                    </div>
                    <div class="form-group">
                      <label class="form-label" style="font-size: 0.8rem; font-weight: 600;">CVV</label>
                      <input type="password" id="prof-card-cvv" class="form-control" placeholder="CVV" maxlength="4">
                    </div>
                  </div>
                </div>

                <button type="submit" class="primary-btn" id="prof-save-btn" style="width: 100%; padding: 10px;">Save Profile Updates</button>
              </form>
            </div>

            <!-- TAB 2: Addresses -->
            <div id="prof-tab-addrs" style="display: none;">
              <div id="prof-addrs-list" style="margin-bottom: 18px;">Loading addresses...</div>
              <h4 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 10px;">+ Add Additional Address</h4>
              <form onsubmit="App.handleAddAddress(event)">
                <div class="form-group" style="margin-bottom: 8px;">
                  <input type="text" id="new-addr-line" class="form-control" placeholder="Street Address / Building" required>
                </div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 10px;">
                  <input type="text" id="new-addr-city" class="form-control" placeholder="City (e.g. Kandy, Galle)" required>
                  <input type="text" id="new-addr-postal" class="form-control" placeholder="Postal Code (e.g. 20000)" required>
                </div>
                <button type="submit" class="secondary-btn" style="width: 100%;">Add Address</button>
              </form>
            </div>

            <!-- TAB 3: Recent Security Activity -->
            <div id="prof-tab-sec" style="display: none;">
              <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 12px;">
                Review recent login attempts on your account to verify your security:
              </p>
              <div class="data-table-container" style="max-height: 250px; overflow-y: auto;">
                <table class="data-table" style="font-size: 0.82rem;">
                  <thead>
                    <tr>
                      <th>Time</th>
                      <th>IP Address</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody id="prof-login-logs-body">
                    <tr><td colspan="3" style="text-align: center;">Loading login logs...</td></tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }

    document.getElementById("prof-username").value = this.currentUser.username || "";
    document.getElementById("prof-email").value = this.currentUser.email || (this.currentUser.username + "@example.com");
    document.getElementById("prof-fullname").value = this.currentUser.fullName || "";

    try {
      const profile = await API.auth.getUser(this.currentUser.userId);
      if (profile) {
        if (profile.username) document.getElementById("prof-username").value = profile.username;
        if (profile.email) document.getElementById("prof-email").value = profile.email;
        document.getElementById("prof-fullname").value = profile.fullName || this.currentUser.fullName || "";
        document.getElementById("prof-phone").value = profile.phoneNumber || "";
        document.getElementById("prof-address").value = profile.defaultDeliveryAddress || "";
        document.getElementById("prof-card-holder").value = profile.cardholderName || "";
        document.getElementById("prof-card-number").value = profile.cardNumber || "";
        document.getElementById("prof-card-expiry").value = profile.cardExpiry || "";
        document.getElementById("prof-card-cvv").value = profile.cardCvv || "";
      }
    } catch (e) {
      console.warn("Could not load customer profile details:", e);
    }

    this.switchProfileTab('info');
    modal.classList.add("open");
  },

  switchProfileTab(tab) {
    document.getElementById("prof-tab-info").style.display = tab === 'info' ? 'block' : 'none';
    document.getElementById("prof-tab-addrs").style.display = tab === 'addrs' ? 'block' : 'none';
    document.getElementById("prof-tab-sec").style.display = tab === 'sec' ? 'block' : 'none';

    document.getElementById("prof-tab-info-btn").className = `cat-chip ${tab === 'info' ? 'active' : ''}`;
    document.getElementById("prof-tab-addrs-btn").className = `cat-chip ${tab === 'addrs' ? 'active' : ''}`;
    document.getElementById("prof-tab-sec-btn").className = `cat-chip ${tab === 'sec' ? 'active' : ''}`;

    if (tab === 'addrs') this.loadProfileAddresses();
    if (tab === 'sec') this.loadProfileSecurityLogs();
  },

  async handleProfileSave(event) {
    event.preventDefault();
    const btn = document.getElementById("prof-save-btn");
    const payload = {
      username: document.getElementById("prof-username").value.trim(),
      email: document.getElementById("prof-email").value.trim(),
      fullName: document.getElementById("prof-fullname").value.trim(),
      phoneNumber: document.getElementById("prof-phone").value.trim(),
      defaultDeliveryAddress: document.getElementById("prof-address").value.trim(),
      cardholderName: document.getElementById("prof-card-holder").value.trim(),
      cardNumber: document.getElementById("prof-card-number").value.trim(),
      cardExpiry: document.getElementById("prof-card-expiry").value.trim(),
      cardCvv: document.getElementById("prof-card-cvv").value.trim()
    };
    btn.disabled = true;
    try {
      await API.auth.updateProfile(this.currentUser.userId, payload);
      this.currentUser.username = payload.username;
      this.currentUser.email = payload.email;
      this.currentUser.fullName = payload.fullName;
      sessionStorage.setItem("shopease_user", JSON.stringify(this.currentUser));
      this.renderHeaderNav();
      this.showToast("Profile details & card info updated successfully!", "success");
    } catch (err) {
      this.showToast("Failed to update profile: " + err.message, "error");
    } finally {
      btn.disabled = false;
    }
  },

  async loadProfileAddresses() {
    const listEl = document.getElementById("prof-addrs-list");
    try {
      const addrs = await API.auth.getAddresses(this.currentUser.userId);
      if (!addrs || addrs.length === 0) {
        listEl.innerHTML = `<p style="font-size: 0.85rem; color: var(--text-muted);">No additional delivery addresses saved yet.</p>`;
        return;
      }
      listEl.innerHTML = addrs.map(a => `
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 10px; border: 1px solid var(--border-color); border-radius: 6px; margin-bottom: 8px;">
          <div>
            <div style="font-weight: 700; font-size: 0.88rem;">${a.addressLine}</div>
            <div style="font-size: 0.78rem; color: var(--text-muted);">${a.city} - ${a.postalCode} ${a.isPrimary ? '<span class="badge badge-success">Default</span>' : ''}</div>
          </div>
          <button onclick="App.handleDeleteAddress(${a.addressId})" class="secondary-btn" style="color: var(--danger); font-size: 0.75rem; padding: 4px 8px;">Delete</button>
        </div>
      `).join("");
    } catch (err) {
      listEl.innerHTML = `<p style="color: var(--danger); font-size: 0.85rem;">Failed to load addresses.</p>`;
    }
  },

  async handleAddAddress(event) {
    event.preventDefault();
    const payload = {
      addressLine: document.getElementById("new-addr-line").value.trim(),
      city: document.getElementById("new-addr-city").value.trim(),
      postalCode: document.getElementById("new-addr-postal").value.trim(),
      isPrimary: false
    };
    try {
      await API.auth.addAddress(this.currentUser.userId, payload);
      this.showToast("Address added successfully", "success");
      document.getElementById("new-addr-line").value = "";
      document.getElementById("new-addr-city").value = "";
      document.getElementById("new-addr-postal").value = "";
      this.loadProfileAddresses();
    } catch (err) {
      this.showToast(err.message, "error");
    }
  },

  async handleDeleteAddress(addressId) {
    if (!confirm("Are you sure you want to delete this address?")) return;
    try {
      await API.auth.deleteAddress(addressId);
      this.showToast("Address deleted", "success");
      this.loadProfileAddresses();
    } catch (err) {
      this.showToast(err.message, "error");
    }
  },

  async loadProfileSecurityLogs() {
    const tbody = document.getElementById("prof-login-logs-body");
    try {
      const logs = await API.auth.getUserSecurityLogs(this.currentUser.userId);
      if (!logs || logs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-muted);">No login logs recorded yet.</td></tr>`;
        return;
      }
      tbody.innerHTML = logs.slice(0, 10).map(l => `
        <tr>
          <td>${new Date(l.loginTime).toLocaleString("en-LK")}</td>
          <td><code>${l.ipAddress}</code></td>
          <td>
            <span class="badge ${l.status === 'SUCCESS' ? 'badge-success' : 'badge-danger'}">
              ${l.status}
            </span>
          </td>
        </tr>
      `).join("");
    } catch (err) {
      tbody.innerHTML = `<tr><td colspan="3" style="color: var(--danger); text-align: center;">Failed to load activity logs.</td></tr>`;
    }
  }
});

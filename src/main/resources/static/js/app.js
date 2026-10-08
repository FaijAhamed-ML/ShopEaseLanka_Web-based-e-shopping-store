/**
 * ShopEase Lanka - Global Application UI & State Controller
 * Manages Cart, Auth session, Demo role switcher, Toast notifications, and Modals
 */

const App = {
  // Current user state (stored in localStorage)
  currentUser: null,

  // Cart State (stored in localStorage)
  cart: [],

  init() {
    this.loadUserSession();
    this.loadCart();
    this.renderHeaderNav();
    this.renderCartDrawer();
  },

  // ===================================================================
  // AUTH & SESSION STATE
  // ===================================================================
  loadUserSession() {
    // Session isolation per browser tab: check sessionStorage
    const saved = sessionStorage.getItem("shopease_user");
    if (saved) {
      try {
        this.currentUser = JSON.parse(saved);
        return;
      } catch (e) {
        this.currentUser = null;
      }
    }

    // Clean session on fresh localhost window: do NOT automatically force-login as any user
    this.currentUser = null;
  },

  setUser(user) {
    this.currentUser = user;
    sessionStorage.setItem("shopease_user", JSON.stringify(user));
    localStorage.removeItem("shopease_logged_out");
    this.renderHeaderNav();
    this.showToast(`Logged in as ${user.fullName || user.username} (${user.role})`, "success");
  },

  logout() {
    sessionStorage.removeItem("shopease_user");
    localStorage.removeItem("shopease_user");
    localStorage.setItem("shopease_logged_out", "true");
    this.currentUser = null;
    this.showToast("Logged out successfully", "info");
    setTimeout(() => {
      window.location.reload();
    }, 500);
  },

  // Switch demo account conveniently for demonstration/testing
  switchDemoRole(roleKey) {
    const demoAccounts = {
      admin: { userId: 1, username: "admin", fullName: "System Administrator", role: "ADMIN" },
      catalog: { userId: 2, username: "catalog_mgr", fullName: "Catalog Manager", role: "CATALOG_MANAGER" },
      inventory: { userId: 3, username: "inv_officer", fullName: "Inventory Officer", role: "INVENTORY_OFFICER" },
      sales: { userId: 4, username: "sales_mgr", fullName: "Sales Manager", role: "SALES_MANAGER" },
      delivery_coord: { userId: 5, username: "delivery_coord", fullName: "Logistics Coordinator", role: "DELIVERY_COORDINATOR" },
      cx: { userId: 6, username: "cx_officer", fullName: "CX Officer", role: "CUSTOMER_EXPERIENCE_OFFICER" },
      rider: { userId: 7, username: "rider_kamal", fullName: "Kamal (Delivery Rider)", role: "DELIVERY_PERSON" },
      kasun: { userId: 8, username: "customer_kasun", fullName: "Kasun Perera (Verified Buyer)", role: "CUSTOMER" },
      nimalka: { userId: 9, username: "customer_nimalka", fullName: "Nimalka Fernando", role: "CUSTOMER" }
    };

    const target = demoAccounts[roleKey];
    if (target) {
      this.setUser(target);
      if (target.role !== 'CUSTOMER') {
        window.location.href = "admin-dashboard.html";
      } else {
        window.location.reload();
      }
    }
  },

  renderHeaderNav() {
    const userContainer = document.getElementById("user-nav-container");
    if (!userContainer) return;

    const bellSvg    = `<svg style="width:17px;height:17px" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>`;
    const packageSvg = `<svg style="width:15px;height:15px" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="16.5" y1="9.4" x2="7.5" y2="4.21"/><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 002 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>`;
    const userSvg    = `<svg style="width:15px;height:15px" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`;

    if (this.currentUser) {
      const isStaff = this.currentUser.role !== "CUSTOMER";
      userContainer.innerHTML = `
        <div style="display: flex; align-items: center; gap: 8px;">
          <div style="text-align: right; line-height: 1.2;">
            <div style="font-weight: 700; font-size: 0.88rem;">${this.currentUser.fullName || this.currentUser.username}</div>
            <div style="font-size: 0.72rem; color: var(--primary); font-weight: 600;">${this.currentUser.role}</div>
          </div>
          <button onclick="App.openNotificationsModal()" class="nav-btn" id="notif-bell-btn" title="My Notifications" style="font-size: 0.85rem; padding: 6px 10px; position: relative;">
            ${bellSvg}
            <span id="notif-unread-badge" style="display:none; position:absolute; top:2px; right:2px; background:var(--danger); color:#fff; border-radius:50%; font-size:0.6rem; font-weight:800; width:16px; height:16px; line-height:16px; text-align:center;">0</span>
          </button>
          <button onclick="App.openCustomerOrdersModal()" class="nav-btn" title="View My Orders &amp; Track Shipments" style="font-size: 0.8rem; padding: 6px 10px; font-weight: 700; display: flex; align-items: center; gap: 5px; color: var(--primary);">${packageSvg} My Orders</button>
          <button onclick="App.openProfileModal()" class="nav-btn" title="View &amp; Edit Profile" style="font-size: 0.8rem; padding: 6px 10px; display: flex; align-items: center; gap: 5px;">${userSvg} Profile</button>
          ${isStaff ? `<a href="admin-dashboard.html" class="nav-btn" style="background: #0f172a; color: #fff; font-size: 0.8rem; padding: 6px 12px;">Staff Console</a>` : ''}
          
          <button onclick="App.logout()" class="nav-btn" style="color: var(--danger); font-size: 0.8rem; padding: 4px 8px;">Logout</button>
        </div>
      `;
    } else {
      userContainer.innerHTML = `
        <button onclick="App.openLoginModal()" class="nav-btn">Sign In</button>
        <button onclick="App.openRegisterModal()" class="primary-btn" style="padding: 6px 14px; font-size: 0.85rem;">Register</button>
        
        `;
    }
    // Load notification count badge after nav renders
    if (this.currentUser) setTimeout(() => this.loadNotificationBadge(), 300);
  },


  // ===================================================================
  // CART STATE & ACTIONS
  // ===================================================================
  loadCart() {
    const saved = localStorage.getItem("shopease_cart");
    if (saved) {
      try {
        this.cart = JSON.parse(saved);
      } catch (e) {
        this.cart = [];
      }
    }
    this.updateCartCount();
  },

  saveCart() {
    localStorage.setItem("shopease_cart", JSON.stringify(this.cart));
    this.updateCartCount();
    this.renderCartDrawer();
  },

  addToCart(product, quantity = 1) {
    const existing = this.cart.find(item => item.productId === product.productId);
    const effPrice = product.discountPercentage > 0
      ? product.price * (1 - product.discountPercentage / 100)
      : product.price;

    if (existing) {
      existing.quantity += quantity;
    } else {
      this.cart.push({
        productId: product.productId,
        name: product.name,
        price: effPrice,
        originalPrice: product.price,
        discountPercentage: product.discountPercentage,
        mainImageUrl: product.mainImageUrl,
        quantity: quantity
      });
    }

    this.saveCart();
    this.showToast(`Added "${product.name}" to cart!`, "success");
    this.openCartDrawer();
  },

  updateQuantity(productId, delta) {
    const item = this.cart.find(i => i.productId === productId);
    if (!item) return;
    item.quantity += delta;
    if (item.quantity <= 0) {
      this.cart = this.cart.filter(i => i.productId !== productId);
    }
    this.saveCart();
  },

  removeFromCart(productId) {
    this.cart = this.cart.filter(i => i.productId !== productId);
    this.saveCart();
    this.showToast("Item removed from cart", "info");
  },

  clearCart() {
    this.cart = [];
    this.saveCart();
  },

  getCartTotal() {
    return this.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  },

  updateCartCount() {
    const count = this.cart.reduce((sum, i) => sum + i.quantity, 0);
    const badges = document.querySelectorAll(".cart-count-badge");
    badges.forEach(b => {
      b.textContent = count;
    });
  },

  openCartDrawer() {
    const drawer = document.getElementById("cart-drawer");
    const overlay = document.getElementById("cart-overlay");
    if (drawer) drawer.classList.add("open");
    if (overlay) overlay.classList.add("open");
  },

  closeCartDrawer() {
    const drawer = document.getElementById("cart-drawer");
    const overlay = document.getElementById("cart-overlay");
    if (drawer) drawer.classList.remove("open");
    if (overlay) overlay.classList.remove("open");
  },

  renderCartDrawer() {
    const container = document.getElementById("drawer-items-list");
    const totalEl = document.getElementById("drawer-subtotal");
    if (!container) return;

    if (this.cart.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 40px 10px; color: var(--text-muted);">
          <div style="width: 48px; height: 48px; margin: 0 auto 12px; color: var(--text-light);">${Icons.shoppingBag}</div>
          <p style="font-weight: 600;">Your shopping cart is empty</p>
          <p style="font-size: 0.85rem; margin-top: 4px;">Explore our curated Sri Lankan collection!</p>
        </div>
      `;
      if (totalEl) totalEl.textContent = API.formatLKR(0);
      return;
    }

    let html = "";
    this.cart.forEach(item => {
      html += `
        <div class="cart-item">
          <img src="${item.mainImageUrl || 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=150'}" alt="${item.name}">
          <div style="flex: 1;">
            <div style="font-weight: 700; font-size: 0.92rem; margin-bottom: 4px;">${item.name}</div>
            <div style="font-size: 0.9rem; color: var(--primary-dark); font-weight: 800; margin-bottom: 6px;">
              ${API.formatLKR(item.price)}
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div class="qty-stepper" style="transform: scale(0.85); transform-origin: left;">
                <button class="qty-btn" onclick="App.updateQuantity(${item.productId}, -1)">-</button>
                <input class="qty-input" type="text" value="${item.quantity}" readonly>
                <button class="qty-btn" onclick="App.updateQuantity(${item.productId}, 1)">+</button>
              </div>
              <button onclick="App.removeFromCart(${item.productId})" style="background: none; border: none; color: var(--danger); font-size: 0.8rem; cursor: pointer;">Remove</button>
            </div>
          </div>
        </div>
      `;
    });

    container.innerHTML = html;
    if (totalEl) totalEl.textContent = API.formatLKR(this.getCartTotal());
  },

  // ===================================================================
  // TOAST NOTIFICATIONS
  // ===================================================================
  showToast(message, type = "info") {
    let container = document.getElementById("toast-container");
    if (!container) {
      container = document.createElement("div");
      container.id = "toast-container";
      document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    const iconSvg = type === "success"
      ? `<svg style="width:16px;height:16px" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`
      : type === "error"
      ? `<svg style="width:16px;height:16px" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`
      : `<svg style="width:16px;height:16px" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`;
    toast.innerHTML = `${iconSvg} <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = "0";
      toast.style.transform = "translateX(100%)";
      toast.style.transition = "all 0.3s ease";
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  },

  // ===================================================================
  // NOTIFICATIONS MODAL
  // ===================================================================
  async openNotificationsModal() {
    let modal = document.getElementById("notifications-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "notifications-modal";
      modal.className = "modal-overlay";
      modal.innerHTML = `
        <div class="modal-card" style="max-width: 500px;">
          <div class="modal-header">
            <h3 style="font-weight: 800; font-family: var(--font-headline); display:flex; align-items:center; gap:8px;">
              <svg style="width:18px;height:18px;color:var(--primary)" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>
              My Notifications
            </h3>
            <button class="close-btn" onclick="document.getElementById('notifications-modal').classList.remove('open')">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
            <div id="notif-modal-loading" style="text-align:center; padding:30px; color:var(--text-muted);">Loading notifications...</div>
            <div id="notif-modal-list"></div>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }
    modal.classList.add("open");

    const loading = document.getElementById("notif-modal-loading");
    const list = document.getElementById("notif-modal-list");
    loading.style.display = "block";
    list.innerHTML = "";

    try {
      const notifs = await API.notifications.getByUser(this.currentUser.userId);
      loading.style.display = "none";

      if (!notifs || notifs.length === 0) {
        list.innerHTML = `<div style="text-align:center; padding:40px 20px;"><div style="width: 40px; height: 40px; margin: 0 auto 10px; color: var(--text-light);">${Icons.info}</div><p style="color:var(--text-muted);">No notifications yet.</p></div>`;
        return;
      }

      // Newest first by actual execution timestamp
      const sorted = [...notifs].sort((a, b) => {
        const timeA = new Date(a.sentTime || a.sent_time || a.createdAt || 0).getTime();
        const timeB = new Date(b.sentTime || b.sent_time || b.createdAt || 0).getTime();
        return timeB - timeA;
      });

      list.innerHTML = sorted.map(n => {
        const icon = n.messageType === "ORDER" ? Icons.package : n.messageType === "DELIVERY" ? Icons.truck : n.messageType === "STOCK" ? Icons.barChart : Icons.info;
        const badgeClass = n.messageType === "ORDER" ? "badge-info" : n.messageType === "DELIVERY" ? "badge-success" : "badge-warning";
        const rawTime = n.sentTime || n.sent_time || n.createdAt || n.created_at || n.sentAt;
        const time = rawTime ? new Date(rawTime).toLocaleString("en-LK") : new Date().toLocaleString("en-LK");
        return `
          <div style="display:flex; gap:12px; padding:12px 0; border-bottom:1px solid var(--border-color); align-items:flex-start;">
            <div style="width:24px; height:24px; display:flex; align-items:center; justify-content:center; flex-shrink:0; color:var(--primary); margin-top:2px;">
              <span style="display:inline-flex; width:18px; height:18px; align-items:center; justify-content:center;">${icon}</span>
            </div>
            <div style="flex:1;">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px; gap:8px;">
                <span class="badge ${badgeClass}" style="font-size:0.68rem; padding:2px 7px;">${n.messageType}</span>
                <span style="font-size:0.72rem; color:var(--text-muted);">${time}</span>
              </div>
              <div style="font-size:0.86rem; line-height:1.5; color:var(--text-main);">${n.content || n.message || ''}</div>
            </div>
          </div>
        `;
      }).join("");

      // Clear the badge
      const badge = document.getElementById("notif-unread-badge");
      if (badge) badge.style.display = "none";

    } catch (e) {
      loading.style.display = "none";
      list.innerHTML = `<div style="color:var(--danger); text-align:center; padding:20px;">Failed to load notifications.</div>`;
    }
  },

  async loadNotificationBadge() {
    if (!this.currentUser) return;
    try {
      const notifs = await API.notifications.getByUser(this.currentUser.userId);
      const badge = document.getElementById("notif-unread-badge");
      if (badge && notifs && notifs.length > 0) {
        badge.textContent = notifs.length > 9 ? "9+" : notifs.length;
        badge.style.display = "block";
      }
    } catch (e) { /* silently ignore */ }
  },

  // ===================================================================
  // ROLE SWITCHER MODAL (For demo testing 5 roles)
  // ===================================================================
  openRoleSwitchModal() {
    let modal = document.getElementById("role-switch-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "role-switch-modal";
      modal.className = "modal-overlay";
      modal.innerHTML = `
        <div class="modal-card">
          <div class="modal-header">
            <h3 style="font-weight: 800;">Switch Demo Persona</h3>
            <button class="close-btn" onclick="document.getElementById('role-switch-modal').classList.remove('open')">&times;</button>
          </div>
          <div class="modal-body">
            <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 16px;">
              Easily toggle between the 5 Member functional roles to test permissions and features:
            </p>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
              <button onclick="App.switchDemoRole('admin')" class="secondary-btn" style="text-align: left;">
                 <strong>Member 1: Admin</strong><br><small style="color: var(--text-muted);">Security & Users</small>
              </button>
              <button onclick="App.switchDemoRole('catalog')" class="secondary-btn" style="text-align: left;">
                 <strong>Member 2: Catalog Mgr</strong><br><small style="color: var(--text-muted);">Products & Categories</small>
              </button>
              <button onclick="App.switchDemoRole('inventory')" class="secondary-btn" style="text-align: left;">
                 <strong>Member 3: Inventory Officer</strong><br><small style="color: var(--text-muted);">Batches & Low-Stock</small>
              </button>
              <button onclick="App.switchDemoRole('sales')" class="secondary-btn" style="text-align: left;">
                 <strong>Member 4: Sales Mgr</strong><br><small style="color: var(--text-muted);">Order Management</small>
              </button>
              <button onclick="App.switchDemoRole('delivery_coord')" class="secondary-btn" style="text-align: left;">
                 <strong>Member 5: Delivery Co-ordinator</strong><br><small style="color: var(--text-muted);">Logistics &amp; Tracking</small>
              </button>
              <button onclick="App.switchDemoRole('cx')" class="secondary-btn" style="text-align: left;">
                ⭐ <strong>Member 6: CX Officer</strong><br><small style="color: var(--text-muted);">Review Moderation</small>
              </button>
              <button onclick="App.switchDemoRole('kasun')" class="secondary-btn" style="text-align: left; background: #ecfdf5; border-color: var(--primary);">
                 <strong>Kasun (Customer)</strong><br><small style="color: var(--primary-dark);">Verified Buyer Seed</small>
              </button>
            </div>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }
    modal.classList.add("open");
  },

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
  },

  // ===================================================================
  // CUSTOMER ORDERS MODAL & TRACKING
  // ===================================================================
  async openCustomerOrdersModal() {
    if (!this.currentUser) {
      this.openLoginModal();
      return;
    }

    let modal = document.getElementById("customer-orders-modal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "customer-orders-modal";
      modal.className = "modal-overlay";
      modal.innerHTML = `
        <div class="modal-card" style="max-width: 780px;">
          <div class="modal-header">
            <h3 style="font-weight: 800; display: flex; align-items: center; gap: 8px;">
              <span style="display:inline-flex; align-items:center; justify-content:center; width:22px; height:22px; color:var(--primary); flex-shrink:0;">
                ${Icons.package}
              </span>
              <span>My Orders & Shipment History</span>
            </h3>
            <button class="close-btn" onclick="document.getElementById('customer-orders-modal').classList.remove('open')">&times;</button>
          </div>
          <div class="modal-body" style="max-height: 75vh; overflow-y: auto;">
            <div id="cust-orders-modal-loading" style="text-align: center; padding: 30px; color: var(--text-muted);">
              Loading your orders...
            </div>
            <div id="cust-orders-modal-list"></div>
          </div>
        </div>
      `;
      document.body.appendChild(modal);
    }

    modal.classList.add("open");
    await this.loadCustomerOrdersModal();
  },

  async loadCustomerOrdersModal() {
    const listContainer = document.getElementById("cust-orders-modal-list");
    const loading = document.getElementById("cust-orders-modal-loading");
    if (!listContainer || !loading) return;

    loading.style.display = "block";
    listContainer.innerHTML = "";

    try {
      const orders = await API.orders.getCustomerOrders(this.currentUser.userId);
      loading.style.display = "none";

      if (!orders || orders.length === 0) {
        listContainer.innerHTML = `
          <div style="text-align: center; padding: 40px 20px;">
            <div class="empty-orders-icon" style="width: 48px; height: 48px; margin: 0 auto 12px; color: var(--text-light); display:flex; align-items:center; justify-content:center;">
              <span style="display:inline-flex; width:44px; height:44px; align-items:center; justify-content:center;">${Icons.package}</span>
            </div>
            <h4 style="font-weight: 700; margin-bottom: 6px;">No Orders Yet</h4>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 20px;">You have not placed any orders yet. Discover our curated Sri Lankan catalogue!</p>
            <a href="index.html" class="primary-btn" onclick="document.getElementById('customer-orders-modal').classList.remove('open')">Browse Products</a>
          </div>
        `;
        return;
      }

      listContainer.innerHTML = orders.map(o => {
        const canCancel = o.orderStatus === 'PENDING' || o.orderStatus === 'CONFIRMED';
        const canDelete = o.orderStatus === 'CANCELLED' || o.orderStatus === 'DELIVERED';
        const paymentMethod = o.payment ? o.payment.paymentMethod : 'CARD';
        const paymentStatus = o.payment ? o.payment.paymentStatus : 'PENDING';

        const orderStatusBadgeClass = 
          o.orderStatus === 'DELIVERED' ? 'badge-success' :
          o.orderStatus === 'CANCELLED' ? 'badge-danger' :
          o.orderStatus === 'SHIPPED' ? 'badge-info' : 'badge-warning';

        const paymentStatusBadgeClass = 
          paymentStatus === 'COMPLETED' ? 'badge-success' :
          paymentStatus === 'REFUNDED' ? 'badge-info' :
          paymentStatus === 'FAILED' ? 'badge-danger' : 'badge-warning';

        return `
          <div style="background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 18px; margin-bottom: 16px; box-shadow: var(--shadow-sm);">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 10px; border-bottom: 1px solid var(--border-color); padding-bottom: 12px; margin-bottom: 12px;">
              <div>
                <div style="display: flex; align-items: center; gap: 8px;">
                  <strong style="font-size: 1.05rem;">Order #${o.orderId}</strong>
                  <span class="badge ${orderStatusBadgeClass}">${o.orderStatus}</span>
                </div>
                <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 3px;">
                  Placed on: ${new Date(o.createdAt).toLocaleString("en-LK")}
                </div>
              </div>
              <div style="text-align: right;">
                <div style="font-size: 1.15rem; font-weight: 800; color: var(--primary);">${API.formatLKR(o.totalAmount)}</div>
                <div style="font-size: 0.78rem; margin-top: 3px;">
                  ${paymentMethod} • <span class="badge ${paymentStatusBadgeClass}" style="padding: 2px 6px; font-size: 0.72rem;">${paymentStatus}</span>
                </div>
              </div>
            </div>

            <!-- Items -->
            <div style="margin-bottom: 14px;">
              ${(o.orderItems || []).map(item => `
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 0.88rem;">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    ${item.product && item.product.mainImageUrl ? `<img src="${item.product.mainImageUrl}" style="width: 32px; height: 32px; border-radius: 4px; object-fit: cover;">` : ''}
                    <div>
                      <span>${item.product ? item.product.name : 'Item #' + item.orderItemId}</span>
                      <span style="color: var(--text-muted); font-size: 0.8rem;"> × ${item.quantity}</span>
                    </div>
                  </div>
                  <strong style="font-size: 0.88rem;">${API.formatLKR(item.subtotal)}</strong>
                </div>
              `).join("")}
            </div>

            <div class="cust-order-address" style="font-size: 0.84rem; color: var(--text-main); margin-bottom: 14px; background: var(--bg-elevated); padding: 9px 12px; border-radius: 6px; border: 1px solid var(--border-color); display: flex; align-items: center; gap: 8px;">
              <span class="order-address-icon" style="display: inline-flex; align-items: center; justify-content: center; width: 16px; height: 16px; min-width: 16px; min-height: 16px; color: var(--primary); flex-shrink: 0;">
                ${Icons.mapPin}
              </span>
              <div style="line-height: 1.4;"><strong>Delivery Address:</strong> ${o.shippingAddress}</div>
            </div>

            <!-- Actions -->
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;">
              <a href="order-tracking.html?orderId=${o.orderId}" target="_blank" class="primary-btn" style="padding: 7px 14px; font-size: 0.82rem; text-decoration: none; display: inline-flex; align-items: center; gap: 6px;">
                <span style="display: inline-flex; align-items: center; width: 15px; height: 15px; flex-shrink: 0;">${Icons.truck}</span>
                <span>Live Track Shipment</span>
              </a>

              <div style="display: flex; align-items: center; gap: 8px;">
                ${canCancel ? `
                  <button class="secondary-btn" style="color: var(--danger); border-color: #fca5a5; padding: 7px 14px; font-size: 0.82rem; display: inline-flex; align-items: center; gap: 6px;" onclick="App.handleCancelOrderFromModal(${o.orderId})">
                    <span style="display: inline-flex; align-items: center; width: 14px; height: 14px; flex-shrink: 0;">${Icons.close}</span>
                    <span>Cancel Order</span>
                  </button>
                ` : ''}

                ${canDelete ? `
                  <button class="secondary-btn" style="color: #b91c1c; border-color: #fca5a5; padding: 7px 14px; font-size: 0.82rem; background: #fef2f2; display: inline-flex; align-items: center; gap: 6px;" onclick="App.handleDeleteOrderFromModal(${o.orderId})" title="Remove this ${o.orderStatus.toLowerCase()} order from history">
                    <span style="display: inline-flex; align-items: center; width: 14px; height: 14px; flex-shrink: 0;">${Icons.ban}</span>
                    <span>Delete from History</span>
                  </button>
                ` : ''}

                ${!canCancel && !canDelete ? `
                  <span style="font-size: 0.78rem; color: var(--text-muted);">
                    Fulfillment in progress (Locked)
                  </span>
                ` : ''}
              </div>
            </div>
          </div>
        `;
      }).join("");

    } catch (err) {
      loading.style.display = "none";
      listContainer.innerHTML = `<div style="color: var(--danger); text-align: center; padding: 20px;">Failed to load orders: ${err.message}</div>`;
    }
  },

  async handleCancelOrderFromModal(orderId) {
    if (!confirm(`Are you sure you want to cancel Order #${orderId}? Reserved warehouse stock will be restored.`)) {
      return;
    }
    try {
      await API.orders.cancelOrder(orderId, this.currentUser.userId);
      this.showToast(`Order #${orderId} cancelled and warehouse stock restored!`, "success");
      this.loadCustomerOrdersModal();
    } catch (err) {
      this.showToast(err.message, "error");
    }
  },

  async handleDeleteOrderFromModal(orderId) {
    if (!confirm(`Are you sure you want to remove Order #${orderId} from your order history?`)) {
      return;
    }
    try {
      await API.orders.deleteCustomerOrder(orderId, this.currentUser.userId);
      this.showToast(`Order #${orderId} removed from your order history!`, "success");
      await this.loadCustomerOrdersModal();
    } catch (err) {
      this.showToast(err.message, "error");
    }
  }
};

// =====================================================================
// THEME MANAGER - Unified Light / Dark Mode System (Storefront & Staff)
// =====================================================================
const ThemeManager = {
  STORAGE_KEY: 'shopease_theme',

  /** Apply theme to <html> and update toggle button icons & labels */
  apply(theme) {
    if (theme !== 'light' && theme !== 'dark') theme = 'dark';
    document.documentElement.setAttribute('data-theme', theme);
    try {
      localStorage.setItem(this.STORAGE_KEY, theme);
      localStorage.setItem('theme', theme);
    } catch(e) {}

    // Update Sun/Moon icon visibility across all pages (storefront & staff)
    const sunIcons = document.querySelectorAll('.theme-icon-sun, #theme-icon-sun');
    const moonIcons = document.querySelectorAll('.theme-icon-moon, #theme-icon-moon');
    sunIcons.forEach(el => { el.style.display = theme === 'dark' ? 'inline-block' : 'none'; });
    moonIcons.forEach(el => { el.style.display = theme === 'dark' ? 'none' : 'inline-block'; });

    // Update text labels on dedicated buttons
    const textLabels = document.querySelectorAll('.theme-text-label, #theme-text-label');
    textLabels.forEach(el => {
      el.textContent = theme === 'dark' ? 'Dark' : 'Light';
    });

    // Update button titles and aria-labels
    const btns = document.querySelectorAll('.theme-toggle-btn, .staff-theme-toggle, #theme-toggle-btn');
    btns.forEach(b => {
      b.setAttribute('title', `Currently ${theme === 'dark' ? 'Dark' : 'Light'} Mode. Click to switch to ${theme === 'dark' ? 'Light' : 'Dark'} Mode.`);
      b.setAttribute('aria-label', `Switch to ${theme === 'dark' ? 'Light' : 'Dark'} Mode`);
    });

    if (typeof ChromaWaves !== 'undefined') ChromaWaves.onThemeChange();
    window.dispatchEvent(new CustomEvent('shopease:themechange', { detail: { theme } }));
  },

  /** Load persisted preference (defaults to dark for Luxe Ceylon signature) */
  load() {
    try {
      const saved = localStorage.getItem(this.STORAGE_KEY) || localStorage.getItem('theme');
      if (saved === 'light' || saved === 'dark') return saved;
    } catch(e) {}
    return 'dark';
  },

  /** Toggle between light and dark */
  toggle() {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const next = current === 'dark' ? 'light' : 'dark';
    this.apply(next);
  },

  /** Initialise: apply saved theme and wire all toggle buttons safely without duplicates */
  init() {
    const theme = this.load();
    this.apply(theme);

    const btns = document.querySelectorAll('.theme-toggle-btn, .staff-theme-toggle, #theme-toggle-btn');
    btns.forEach(btn => {
      if (!btn.dataset.themeBound) {
        btn.dataset.themeBound = 'true';
        btn.addEventListener('click', (e) => {
          e.preventDefault();
          ThemeManager.toggle();
        });
      }
    });
  }
};

// Global init on DOM ready
document.addEventListener("DOMContentLoaded", () => {
  App.init();
  ThemeManager.init();
});


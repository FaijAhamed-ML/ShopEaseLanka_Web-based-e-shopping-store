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

/* Staff console shell: route guard, role-based tab permissions, tab switching (shared) */
let productsList = [];
let cachedCatalog = [];

// Route Guard: enforce staff session
const userJson = localStorage.getItem("shopease_user");
const currentStaffUser = userJson ? JSON.parse(userJson) : null;
if (!currentStaffUser || currentStaffUser.role === "CUSTOMER") {
  window.location.replace("/admin/login");
}

document.addEventListener("DOMContentLoaded", async () => {
  await Partials.loadAll();   // inject every member's tab + modal HTML first
  initStaffConsoleRBAC();
  // Pre-load common products
  API.catalog.getProducts().then(res => {
    productsList = res;
    cachedCatalog = res;
  });
});

    const ROLE_MODULE_PERMISSIONS = {
  ADMIN: ["security", "catalog", "inventory", "orders", "deliveries", "reviews"],
  SALES_MANAGER: ["orders"],
  DELIVERY_COORDINATOR: ["deliveries"],
  INVENTORY_OFFICER: ["inventory"],
  CATALOG_MANAGER: ["catalog"],
  CUSTOMER_EXPERIENCE_OFFICER: ["reviews"],
  DELIVERY_PERSON: ["deliveries"]
};

const ROLE_DEFAULT_TAB = {
  ADMIN: "security",
  SALES_MANAGER: "orders",
  DELIVERY_COORDINATOR: "deliveries",
  INVENTORY_OFFICER: "inventory",
  CATALOG_MANAGER: "catalog",
  CUSTOMER_EXPERIENCE_OFFICER: "reviews",
  DELIVERY_PERSON: "deliveries"
};

function initStaffConsoleRBAC() {
  if (!currentStaffUser) return;

  const nameEl = document.getElementById("admin-user-name");
  const roleEl = document.getElementById("admin-user-role");
  if (nameEl) nameEl.textContent = currentStaffUser.fullName || currentStaffUser.username;
  if (roleEl) roleEl.textContent = currentStaffUser.role;

  const role = currentStaffUser.role;
  const allowedTabs = ROLE_MODULE_PERMISSIONS[role] || [ROLE_DEFAULT_TAB[role] || "orders"];
  const allTabs = ["security", "catalog", "inventory", "orders", "deliveries", "reviews"];

  allTabs.forEach(tab => {
    const btn = document.getElementById(`tab-btn-${tab}`);
    if (btn) {
      btn.style.display = allowedTabs.includes(tab) ? "inline-flex" : "none";
    }
  });

  // Customize deliveries tab button for delivery driver
  const delTabBtn = document.getElementById("tab-btn-deliveries");
  if (delTabBtn) {
    if (role === "DELIVERY_PERSON") {
      delTabBtn.innerHTML = " My Assigned Deliveries";
    } else {
      delTabBtn.innerHTML = " M5: Deliveries";
    }
  }

  const defaultTab = ROLE_DEFAULT_TAB[role] || allowedTabs[0] || "orders";
  switchAdminTab(defaultTab);
}

function hideTab(btnId) {
  const btn = document.getElementById(btnId);
  if (btn) btn.style.display = "none";
}

function handleStaffLogout() {
  localStorage.removeItem("shopease_user");
  localStorage.removeItem("shopease_token");
  window.location.href = "/admin/login";
}

function switchAdminTab(tabName) {
  const role = currentStaffUser ? currentStaffUser.role : null;
  const allowedTabs = (role && ROLE_MODULE_PERMISSIONS[role]) ? ROLE_MODULE_PERMISSIONS[role] : ["orders"];

  // Guard: If role is not authorized for this tab, redirect to default
  if (!allowedTabs.includes(tabName)) {
    tabName = (role && ROLE_DEFAULT_TAB[role]) ? ROLE_DEFAULT_TAB[role] : allowedTabs[0];
  }

  document.querySelectorAll(".admin-tab-btn").forEach(b => b.classList.remove("active"));
  document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));

  const targetBtn = document.getElementById(`tab-btn-${tabName}`);
  const targetContent = document.getElementById(`tab-${tabName}`);

  if (targetBtn) targetBtn.classList.add("active");
  if (targetContent) targetContent.classList.add("active");

  if (tabName === "security") loadSecurityTab();
  if (tabName === "catalog") loadCatalogTab();
  if (tabName === "inventory") loadInventoryTab();
  if (tabName === "orders") loadOrdersTab();
  if (tabName === "deliveries") loadDeliveriesTab();
  if (tabName === "reviews") loadReviewsTab();
}

function closeModal(modalId) {
  document.getElementById(modalId).classList.remove("open");
}
  

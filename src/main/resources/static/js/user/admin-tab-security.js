/* Staff console tab script (user module). Loaded by admin-dashboard.html */
let cachedUsers = [];
let cachedLogs = [];
// =================================================================
// TAB 1: SECURITY
// =================================================================
async function loadSecurityTab() {
  try {
    cachedUsers = await API.auth.getAllUsers();
    cachedLogs = await API.auth.getSecurityLogs();
    filterUsersTable();
    filterLogsTable();
  } catch (err) {
    console.error("Failed loading security:", err);
  }
}

function filterUsersTable() {
  const query = (document.getElementById("users-search-input").value || "").trim().toLowerCase();
  const roleFilter = document.getElementById("users-role-filter").value;

  const filtered = (cachedUsers || []).filter(u => {
    const matchesQuery = !query || 
      u.userId.toString().includes(query) || 
      (u.username && u.username.toLowerCase().includes(query)) || 
      (u.email && u.email.toLowerCase().includes(query));
    const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
    return matchesQuery && matchesRole;
  });

  const usersTbody = document.getElementById("users-table-body");
  if (filtered.length === 0) {
    usersTbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 20px;">No users match your criteria.</td></tr>`;
    return;
  }

  usersTbody.innerHTML = filtered.map(u => `
    <tr>
      <td>#${u.userId}</td>
      <td><strong>${u.username}</strong></td>
      <td>${u.email}</td>
      <td><span class="badge badge-info">${u.role}</span></td>
      <td>
        <span class="badge ${u.accountStatus === 'ACTIVE' ? 'badge-success' : u.accountStatus === 'SUSPENDED' ? 'badge-danger' : 'badge-warning'}">
          ${u.accountStatus}
        </span>
      </td>
      <td>
        <div style="display: flex; gap: 6px; flex-wrap: wrap;">
          <button class="secondary-btn" style="padding: 4px 8px; font-size: 0.78rem;" onclick='openEditUserModal(${JSON.stringify(u).replace(/'/g, "&#39;")})'>
             Edit Role
          </button>
          <button class="secondary-btn" style="padding: 4px 8px; font-size: 0.78rem;" onclick="toggleUserLock(${u.userId})">
            ${u.accountStatus === 'ACTIVE' ? ' Lock' : ' Unlock'}
          </button>
          <button class="secondary-btn" style="padding: 4px 8px; font-size: 0.78rem; color: var(--danger);" onclick="deleteUserAccount(${u.userId}, '${u.username}')" title="Delete User">
            <svg style="width:13px;height:13px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
          </button>
        </div>
      </td>
    </tr>
  `).join("");
}

function filterLogsTable() {
  const query = (document.getElementById("logs-search-input").value || "").trim().toLowerCase();
  const statusFilter = document.getElementById("logs-status-filter").value;

  let suspCount = 0;
  (cachedLogs || []).forEach(l => { if (l.suspiciousFlag) suspCount++; });
  document.getElementById("suspicious-count-badge").textContent = `${suspCount} Suspicious Logs`;

  const filtered = (cachedLogs || []).filter(l => {
    const username = l.user ? l.user.username.toLowerCase() : "";
    const ip = l.ipAddress ? l.ipAddress.toLowerCase() : "";
    const matchesQuery = !query || username.includes(query) || ip.includes(query) || l.activityId.toString() === query;

    if (!matchesQuery) return false;
    if (statusFilter === "SUCCESS") return l.status === "SUCCESS";
    if (statusFilter === "FAILURE") return l.status === "FAILURE";
    if (statusFilter === "SUSPICIOUS") return l.suspiciousFlag === true;
    return true;
  });

  const logsTbody = document.getElementById("login-logs-table-body");
  if (filtered.length === 0) {
    logsTbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 20px;">No audit log entries match your filter.</td></tr>`;
    return;
  }

  logsTbody.innerHTML = filtered.map(l => `
    <tr class="${l.suspiciousFlag ? 'log-row-suspicious' : ''}">
      <td>#${l.activityId}</td>
      <td>${l.user ? l.user.username : '<span style="color: var(--danger); font-weight: 600;">(Non-existent)</span>'}</td>
      <td><code>${l.ipAddress}</code></td>
      <td>${new Date(l.loginTime).toLocaleString("en-LK")}</td>
      <td>
        <span class="badge ${l.status === 'SUCCESS' ? 'badge-success' : 'badge-danger'}">
          ${l.status}
        </span>
      </td>
      <td>
        ${l.suspiciousFlag ? '<span class="badge badge-danger"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="display:inline-block;vertical-align:-1px;margin-right:3px;"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>SUSPICIOUS</span>' 
          : '<span style="color: var(--text-muted); font-size: 0.8rem;">Normal</span>'}
      </td>
      <td>
        <div style="display: flex; gap: 6px;">
          <button class="secondary-btn" style="padding: 4px 8px; font-size: 0.75rem;" onclick="toggleSuspiciousFlag(${l.activityId})">
            ${l.suspiciousFlag ? 'Clear Flag' : 'Flag'}
          </button>
          <button class="secondary-btn" style="padding: 4px 8px; font-size: 0.75rem; color: var(--danger);" onclick="deleteSecurityLogEntry(${l.activityId})">
            <svg style="width:13px;height:13px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg> Delete
          </button>
        </div>
      </td>
    </tr>
  `).join("");
}

function openEditUserModal(user) {
  document.getElementById("edit-user-id-input").value = user.userId;
  document.getElementById("edit-user-username-input").value = user.username;
  document.getElementById("edit-user-role-select").value = user.role;
  document.getElementById("edit-user-status-select").value = user.accountStatus;
  document.getElementById("edit-user-modal").classList.add("open");
}

async function handleSaveUserRoleStatus(event) {
  event.preventDefault();
  const userId = document.getElementById("edit-user-id-input").value;
  const role = document.getElementById("edit-user-role-select").value;
  const accountStatus = document.getElementById("edit-user-status-select").value;
  const btn = document.getElementById("edit-user-save-btn");
  btn.disabled = true;
  try {
    await API.auth.updateRoleAndStatus(userId, { role, accountStatus });
    App.showToast("User role and status updated successfully", "success");
    closeModal("edit-user-modal");
    loadSecurityTab();
  } catch (err) {
    App.showToast(err.message, "error");
  } finally {
    btn.disabled = false;
  }
}

async function toggleUserLock(userId) {
  try {
    await API.auth.toggleLock(userId);
    App.showToast("User account status updated", "success");
    loadSecurityTab();
  } catch (err) {
    App.showToast(err.message, "error");
  }
}

async function deleteUserAccount(userId, username) {
  if (userId === 1 || username === 'admin') {
    alert("Primary administrator account cannot be deleted.");
    return;
  }
  if (!confirm(`Are you sure you want to permanently delete user account "${username}" (#${userId})?`)) {
    return;
  }
  try {
    await API.auth.deleteUser(userId);
    App.showToast(`User account "${username}" deleted successfully`, "success");
    loadSecurityTab();
  } catch (err) {
    App.showToast(err.message, "error");
  }
}

async function toggleSuspiciousFlag(id) {
  try {
    await API.auth.toggleSuspiciousFlag(id);
    App.showToast("Suspicious flag updated", "success");
    loadSecurityTab();
  } catch (err) {
    App.showToast(err.message, "error");
  }
}

async function deleteSecurityLogEntry(activityId) {
  if (!confirm(`Delete audit log entry #${activityId}?`)) return;
  try {
    await API.auth.deleteSecurityLog(activityId);
    App.showToast("Security log entry deleted", "success");
    loadSecurityTab();
  } catch (err) {
    App.showToast(err.message, "error");
  }
}

async function clearOldSecurityLogsPrompt() {
  const choice = prompt("Enter days of history to retain (e.g., 30 to remove logs older than 30 days, or 0 to clear all logs):", "30");
  if (choice === null) return;
  const days = parseInt(choice, 10);
  if (isNaN(days) || days < 0) {
    alert("Please enter a valid non-negative number of days.");
    return;
  }
  try {
    await API.auth.clearOldSecurityLogs(days);
    App.showToast("Audit logs cleared successfully", "success");
    loadSecurityTab();
  } catch (err) {
    App.showToast(err.message, "error");
  }
}

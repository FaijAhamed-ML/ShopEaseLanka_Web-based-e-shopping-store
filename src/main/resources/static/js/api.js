
  // ===================================================================
  // MODULE 1: USER MANAGEMENT & SECURITY
  // ===================================================================
  auth: {
    login(usernameOrObj, maybePassword) {
      let username, password;
      if (typeof usernameOrObj === "object" && usernameOrObj !== null) {
        username = usernameOrObj.username || usernameOrObj.usernameOrEmail;
        password = usernameOrObj.password;
      } else {
        username = usernameOrObj;
        password = maybePassword;
      }
      return API.request("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
      });
    },
    register(payload) {
      return API.request("/auth/register", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    resetPassword(usernameOrEmail, newPassword) {
      return API.request("/auth/reset-password", {
        method: "POST",
        body: JSON.stringify({ usernameOrEmail, newPassword })
      });
    },
    getUser(userId) {
      return API.request(`/users/profile/${userId}`);
    },
    updateProfile(userId, payload) {
      return API.request(`/users/profile/${userId}`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    },
    getAddresses(customerId) {
      return API.request(`/users/addresses/${customerId}`);
    },
    addAddress(customerId, payload) {
      return API.request(`/users/addresses/${customerId}`, {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    deleteAddress(addressId) {
      return API.request(`/users/addresses/${addressId}`, {
        method: "DELETE"
      });
    },
    // Admin & Account Controls
    getAllUsers() {
      return API.request("/users");
    },
    updateRoleAndStatus(userId, payload) {
      return API.request(`/users/${userId}/role-status`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    },
    toggleLock(userId) {
      return API.request(`/users/${userId}/toggle-lock`, { method: "PUT" });
    },
    deleteUser(userId) {
      return API.request(`/users/${userId}`, { method: "DELETE" });
    },
    getSecurityLogs() {
      return API.request("/users/security/logs");
    },
    getUserSecurityLogs(userId) {
      return API.request(`/users/${userId}/security/logs`);
    },
    getSuspiciousLogs() {
      return API.request("/users/security/suspicious");
    },
    toggleSuspiciousFlag(activityId) {
      return API.request(`/users/security/logs/${activityId}/toggle-flag`, { method: "PUT" });
    },
    deleteSecurityLog(activityId) {
      return API.request(`/users/security/logs/${activityId}`, { method: "DELETE" });
    },
    clearOldSecurityLogs(days = 30) {
      return API.request(`/users/security/logs/clear-old?days=${days}`, { method: "DELETE" });
    }
  },
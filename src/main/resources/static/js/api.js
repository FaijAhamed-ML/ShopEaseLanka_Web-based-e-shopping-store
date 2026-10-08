/**
 * ShopEase Lanka - Unified REST API Client
 * Clean asynchronous Fetch wrapper for all 6 functional modules
 */

const API = {
  baseUrl: "/api",

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const defaultHeaders = {
      "Content-Type": "application/json",
      "Accept": "application/json"
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers
      }
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json();
      if (!response.ok || !data.success) {
        throw new Error(data.message || `Request failed with status ${response.status}`);
      }
      return data.data;
    } catch (error) {
      console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, error);
      throw error;
    }
  },

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

  // ===================================================================
  // MODULE 2: PRODUCT CATALOG MANAGEMENT
  // ===================================================================
  catalog: {
    getCategories() {
      return API.request("/categories/tree");
    },
    createCategory(payload) {
      return API.request("/categories", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    getProducts(params = {}) {
      const query = new URLSearchParams();
      if (params.keyword) query.append("keyword", params.keyword);
      if (params.categoryId) query.append("categoryId", params.categoryId);
      if (params.minPrice !== undefined && params.minPrice !== null && params.minPrice !== "") query.append("minPrice", params.minPrice);
      if (params.maxPrice !== undefined && params.maxPrice !== null && params.maxPrice !== "") query.append("maxPrice", params.maxPrice);
      if (params.inStock !== undefined && params.inStock !== null && params.inStock !== "") query.append("inStock", params.inStock);
      return API.request(`/products?${query.toString()}`);
    },
    getProductById(id) {
      return API.request(`/products/${id}`);
    },
    createProduct(payload) {
      return API.request("/products", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    updateProduct(id, payload) {
      return API.request(`/products/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    },
    deleteProduct(id) {
      return API.request(`/products/${id}`, { method: "DELETE" });
    }
  },

  // ===================================================================
  // MODULE 3: LIVE INVENTORY & BATCH CONTROL
  // ===================================================================
  inventory: {
    getAll() {
      return API.request("/inventory");
    },
    getByProductId(productId) {
      return API.request(`/inventory/product/${productId}`);
    },
    getAlerts() {
      return API.request("/inventory/alerts");
    },
    getMovements() {
      return API.request("/inventory/movements");
    },
    addBatch(payload) {
      return API.request("/inventory/batch", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    adjustStock(payload) {
      return API.request("/inventory/adjust", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    updateThreshold(productId, threshold) {
      return API.request(`/inventory/threshold/${productId}`, {
        method: "PUT",
        body: JSON.stringify({ threshold })
      });
    },
    getSummary() {
      return API.request("/inventory/summary");
    },
    deleteInventory(inventoryId) {
      return API.request(`/inventory/${inventoryId}`, {
        method: "DELETE"
      });
    },
    deleteByProductId(productId) {
      return API.request(`/inventory/product/${productId}`, {
        method: "DELETE"
      });
    },
    deleteMovement(movementId) {
      return API.request(`/inventory/movements/${movementId}`, {
        method: "DELETE"
      });
    },
    clearOldMovements(days = 30) {
      return API.request(`/inventory/movements/clear-old?days=${days}`, {
        method: "DELETE"
      });
    }
  },

  // ===================================================================
  // MODULE 4: ORDER PROCESSING & CHECKOUT
  // ===================================================================
  orders: {
    placeOrder(payload) {
      return API.request("/orders", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    getOrderById(id) {
      return API.request(`/orders/${id}`);
    },
    getCustomerOrders(customerId) {
      return API.request(`/orders/customer/${customerId}`);
    },
    getAllOrders() {
      return API.request("/orders");
    },
    cancelOrder(orderId, customerId) {
      const query = customerId ? `?customerId=${customerId}` : "";
      return API.request(`/orders/${orderId}/cancel${query}`, {
        method: "POST"
      });
    },
    deleteCustomerOrder(orderId, customerId) {
      const query = customerId ? `?customerId=${customerId}` : "";
      return API.request(`/orders/${orderId}${query}`, {
        method: "DELETE"
      });
    },
    updateStatus(orderId, status) {
      return API.request(`/orders/${orderId}/status`, {
        method: "PUT",
        body: JSON.stringify({ status })
      });
    },
    updatePaymentStatus(orderId, paymentStatus) {
      return API.request(`/orders/${orderId}/payment-status`, {
        method: "PUT",
        body: JSON.stringify({ paymentStatus })
      });
    },
    updateOrderAndPayment(orderId, payload) {
      return API.request(`/orders/${orderId}/manage`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    }
  },

  // ===================================================================
  // MODULE 5: DELIVERY & TRACKING MANAGEMENT
  // ===================================================================
  deliveries: {
    getAll() {
      return API.request("/deliveries");
    },
    track(trackingNumber) {
      return API.request(`/deliveries/track/${trackingNumber}`);
    },
    getByOrderId(orderId) {
      return API.request(`/deliveries/order/${orderId}`);
    },
    assignRider(deliveryId, riderId) {
      return API.request(`/deliveries/${deliveryId}/assign`, {
        method: "PUT",
        body: JSON.stringify({ deliveryPersonId: riderId })
      });
    },
    updateStatus(deliveryId, payload) {
      return API.request(`/deliveries/${deliveryId}/status`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    },
    recordAttempt(deliveryId, payload) {
      return API.request(`/deliveries/${deliveryId}/attempt`, {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    getByCustomerId(customerId) {
      return API.request(`/deliveries/customer/${customerId}`);
    },
    deleteDelivery(deliveryId) {
      return API.request(`/deliveries/${deliveryId}`, {
        method: "DELETE"
      });
    }
  },

  // ===================================================================
  // MODULE 6: REVIEW & MODERATION MANAGEMENT
  // ===================================================================
  reviews: {
    getProductReviews(productId) {
      return API.request(`/reviews/product/${productId}`);
    },
    checkEligibility(customerId, productId) {
      return API.request(`/reviews/eligibility?customerId=${customerId}&productId=${productId}`);
    },
    submitReview(payload) {
      return API.request("/reviews", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    },
    getPendingReviews() {
      return API.request("/reviews/moderation/pending");
    },
    getAllReviews() {
      return API.request("/reviews/moderation/all");
    },
    moderate(reviewId, status) {
      return API.request(`/reviews/moderation/${reviewId}`, {
        method: "PUT",
        body: JSON.stringify({ status })
      });
    },
    getMyReview(productId, customerId) {
      return API.request(`/reviews/product/${productId}/my-review?customerId=${customerId}`);
    },
    updateReview(reviewId, payload) {
      return API.request(`/reviews/${reviewId}`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
    },
    deleteReview(reviewId) {
      return API.request(`/reviews/${reviewId}`, {
        method: "DELETE"
      });
    }
  },

  // Notifications
  notifications: {
    getByUser(userId) {
      return API.request(`/notifications/user/${userId}`);
    },
    markRead(id) {
      return API.request(`/notifications/${id}/read`, { method: "PUT" });
    }
  },

  // Helpers
  formatLKR(amount) {
    if (amount == null) return "Rs. 0.00";
    return "Rs. " + Number(amount).toLocaleString("en-LK", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }
};

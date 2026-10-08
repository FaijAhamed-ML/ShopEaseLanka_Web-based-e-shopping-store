package com.shopease.usermanagement.controller;

import com.shopease.common.ApiResponse;
import com.shopease.usermanagement.dto.AuthDto.*;
import com.shopease.usermanagement.entity.Customer;
import com.shopease.usermanagement.entity.CustomerAddress;
import com.shopease.usermanagement.entity.LoginActivity;
import com.shopease.usermanagement.entity.User;
import com.shopease.usermanagement.service.SecurityAuditService;
import com.shopease.usermanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SecurityAuditService securityAuditService;

    public UserController(UserService userService, SecurityAuditService securityAuditService) {
        this.userService = userService;
        this.securityAuditService = securityAuditService;
    }

    // Customer profile endpoints
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<Customer>> getProfile(@PathVariable Integer userId) {
        try {
            Customer customer = userService.getCustomerProfile(userId);
            return ResponseEntity.ok(ApiResponse.ok(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Profile not found: " + e.getMessage()));
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<Customer>> updateProfile(@PathVariable Integer userId, @RequestBody UpdateProfileRequest request) {
        try {
            Customer updated = userService.updateCustomerProfile(userId, request);
            return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }

    @GetMapping("/addresses/{customerId}")
    public ResponseEntity<ApiResponse<List<CustomerAddress>>> getAddresses(@PathVariable Integer customerId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCustomerAddresses(customerId)));
    }

    @PostMapping("/addresses/{customerId}")
    public ResponseEntity<ApiResponse<CustomerAddress>> addAddress(@PathVariable Integer customerId, @Valid @RequestBody AddressRequest request) {
        try {
            CustomerAddress address = userService.addAddress(customerId, request);
            return ResponseEntity.ok(ApiResponse.ok("Address added successfully", address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add address: " + e.getMessage()));
        }
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Integer addressId) {
        try {
            userService.deleteAddress(addressId);
            return ResponseEntity.ok(ApiResponse.ok("Address deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete address: " + e.getMessage()));
        }
    }

    // Admin endpoints
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @PutMapping("/{userId}/role-status")
    public ResponseEntity<ApiResponse<User>> updateRoleAndStatus(@PathVariable Integer userId, @RequestBody UpdateRoleRequest request) {
        try {
            User updated = userService.updateUserRoleAndStatus(userId, request);
            return ResponseEntity.ok(ApiResponse.ok("User updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{userId}/toggle-lock")
    public ResponseEntity<ApiResponse<User>> toggleLock(@PathVariable Integer userId) {
        try {
            User updated = userService.toggleUserLock(userId);
            return ResponseEntity.ok(ApiResponse.ok("User status updated: " + updated.getAccountStatus(), updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.ok("User account deleted successfully", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    // Security monitoring endpoints
    @GetMapping("/security/logs")
    public ResponseEntity<ApiResponse<List<LoginActivity>>> getSecurityLogs() {
        return ResponseEntity.ok(ApiResponse.ok(securityAuditService.getAllLogs()));
    }

    @GetMapping("/{userId}/security/logs")
    public ResponseEntity<ApiResponse<List<LoginActivity>>> getCustomerSecurityLogs(@PathVariable Integer userId) {
        return ResponseEntity.ok(ApiResponse.ok(securityAuditService.getUserLogs(userId)));
    }

    @GetMapping("/security/suspicious")
    public ResponseEntity<ApiResponse<List<LoginActivity>>> getSuspiciousLogs() {
        return ResponseEntity.ok(ApiResponse.ok(securityAuditService.getSuspiciousLogs()));
    }

    @PutMapping("/security/logs/{activityId}/toggle-flag")
    public ResponseEntity<ApiResponse<LoginActivity>> toggleSuspiciousFlag(@PathVariable Integer activityId) {
        try {
            LoginActivity updated = securityAuditService.toggleSuspiciousFlag(activityId);
            return ResponseEntity.ok(ApiResponse.ok("Suspicious flag updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/security/logs/{activityId}")
    public ResponseEntity<ApiResponse<Void>> deleteSecurityLog(@PathVariable Integer activityId) {
        try {
            securityAuditService.deleteLog(activityId);
            return ResponseEntity.ok(ApiResponse.ok("Log entry deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete log: " + e.getMessage()));
        }
    }

    @DeleteMapping("/security/logs/clear-old")
    public ResponseEntity<ApiResponse<Long>> clearOldSecurityLogs(@RequestParam(defaultValue = "30") int days) {
        try {
            long deletedCount = securityAuditService.clearOldLogs(days);
            String msg = days <= 0 ? "All security logs cleared (" + deletedCount + " entries removed)"
                    : "Cleared " + deletedCount + " security logs older than " + days + " days";
            return ResponseEntity.ok(ApiResponse.ok(msg, deletedCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to clear logs: " + e.getMessage()));
        }
    }
}

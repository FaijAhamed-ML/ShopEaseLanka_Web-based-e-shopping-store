package com.shopease.usermanagement.dto;

import com.shopease.usermanagement.entity.AccountStatus;
import com.shopease.usermanagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public static class LoginRequest {
        @NotBlank(message = "Username or email is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Full name is required")
        private String fullName;

        private String phoneNumber;
        private String deliveryAddress;

        public RegisterRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    }

    public static class AuthResponse {
        private Integer userId;
        private String username;
        private String email;
        private Role role;
        private String fullName;
        private String token;

        public AuthResponse() {}

        public AuthResponse(Integer userId, String username, String email, Role role, String fullName, String token) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.fullName = fullName;
            this.token = token;
        }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class UpdateProfileRequest {
        private String username;
        private String email;
        private String fullName;
        private String phoneNumber;
        private String defaultDeliveryAddress;
        private String cardholderName;
        private String cardNumber;
        private String cardExpiry;
        private String cardCvv;

        public UpdateProfileRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getDefaultDeliveryAddress() { return defaultDeliveryAddress; }
        public void setDefaultDeliveryAddress(String defaultDeliveryAddress) { this.defaultDeliveryAddress = defaultDeliveryAddress; }

        public String getCardholderName() { return cardholderName; }
        public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

        public String getCardExpiry() { return cardExpiry; }
        public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }

        public String getCardCvv() { return cardCvv; }
        public void setCardCvv(String cardCvv) { this.cardCvv = cardCvv; }
    }

    public static class AddressRequest {
        @NotBlank
        private String addressLine;
        @NotBlank
        private String city;
        @NotBlank
        private String postalCode;
        private Boolean isPrimary = false;

        public AddressRequest() {}

        public String getAddressLine() { return addressLine; }
        public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    }

    public static class UpdateRoleRequest {
        private Role role;
        private AccountStatus accountStatus;

        public UpdateRoleRequest() {}

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }

        public AccountStatus getAccountStatus() { return accountStatus; }
        public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    }

    public static class PasswordResetRequest {
        @NotBlank(message = "Username or email is required")
        private String usernameOrEmail;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;

        public PasswordResetRequest() {}

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}

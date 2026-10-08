package com.shopease.usermanagement.service;

import com.shopease.usermanagement.dto.AuthDto.*;
import com.shopease.usermanagement.entity.*;
import com.shopease.usermanagement.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final SecurityAuditService securityAuditService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       CustomerAddressRepository customerAddressRepository,
                       SecurityAuditService securityAuditService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.securityAuditService = securityAuditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.CUSTOMER,
                request.getPassword()
        );
        user = userRepository.saveAndFlush(user);

        Customer customer = new Customer(
                user,
                request.getFullName(),
                request.getPhoneNumber() != null ? request.getPhoneNumber() : "",
                request.getDeliveryAddress() != null ? request.getDeliveryAddress() : ""
        );
        customer.setCustomerId(user.getUserId());
        customer = customerRepository.saveAndFlush(customer);

        if (request.getDeliveryAddress() != null && !request.getDeliveryAddress().isBlank()) {
            CustomerAddress address = new CustomerAddress(customer, request.getDeliveryAddress(), "Colombo", "00100", true);
            customerAddressRepository.save(address);
        }

        String token = UUID.randomUUID().toString();
        return new AuthResponse(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole(), customer.getFullName(), token);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElse(null);

        if (user == null) {
            securityAuditService.recordLoginAttempt(null, ipAddress, false);
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            securityAuditService.recordLoginAttempt(user, ipAddress, false);
            throw new IllegalStateException("Your account has been suspended by administration.");
        }

        boolean matches = false;
        try {
            matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        } catch (Exception ignored) {}

        // Match against plain_password column as well and auto-sync hash
        if (!matches && user.getPlainPassword() != null && user.getPlainPassword().equals(request.getPassword())) {
            matches = true;
            try {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            } catch (Exception ignored) {}
        }

        securityAuditService.recordLoginAttempt(user, ipAddress, matches);

        if (!matches) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String fullName = user.getUsername();
        Customer customer = customerRepository.findById(user.getUserId()).orElse(null);
        if (customer != null && customer.getFullName() != null) {
            fullName = customer.getFullName();
        }

        String token = UUID.randomUUID().toString();
        return new AuthResponse(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole(), fullName, token);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public Customer getCustomerProfile(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseGet(() -> {
                    User u = getUserById(customerId);
                    Customer profile = new Customer(u, u.getUsername(), "", "");
                    return customerRepository.save(profile);
                });
        userRepository.findById(customerId).ifPresent(u -> {
            customer.setUsername(u.getUsername());
            customer.setEmail(u.getEmail());
        });
        return customer;
    }

    @Transactional
    public Customer updateCustomerProfile(Integer customerId, UpdateProfileRequest request) {
        Customer customer = getCustomerProfile(customerId);
        User user = getUserById(customerId);

        // Update username if provided and changed
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equalsIgnoreCase(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new IllegalArgumentException("Username '" + newUsername + "' is already taken");
                }
                user.setUsername(newUsername);
            }
        }

        // Update email if provided and changed
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException("Email '" + newEmail + "' is already registered");
                }
                user.setEmail(newEmail);
            }
        }

        userRepository.save(user);

        if (request.getFullName() != null) customer.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null) customer.setPhoneNumber(request.getPhoneNumber().trim());
        if (request.getDefaultDeliveryAddress() != null) customer.setDefaultDeliveryAddress(request.getDefaultDeliveryAddress().trim());

        // Update customer saved card details
        if (request.getCardholderName() != null) customer.setCardholderName(request.getCardholderName().trim());
        if (request.getCardNumber() != null) customer.setCardNumber(request.getCardNumber().trim());
        if (request.getCardExpiry() != null) customer.setCardExpiry(request.getCardExpiry().trim());
        if (request.getCardCvv() != null) customer.setCardCvv(request.getCardCvv().trim());

        Customer saved = customerRepository.save(customer);
        saved.setUsername(user.getUsername());
        saved.setEmail(user.getEmail());
        return saved;
    }

    public List<CustomerAddress> getCustomerAddresses(Integer customerId) {
        return customerAddressRepository.findByCustomer_CustomerId(customerId);
    }

    @Transactional
    public CustomerAddress addAddress(Integer customerId, AddressRequest request) {
        Customer customer = getCustomerProfile(customerId);
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            List<CustomerAddress> addresses = customerAddressRepository.findByCustomer_CustomerId(customerId);
            for (CustomerAddress addr : addresses) {
                addr.setIsPrimary(false);
                customerAddressRepository.save(addr);
            }
        }
        CustomerAddress address = new CustomerAddress(customer, request.getAddressLine(), request.getCity(), request.getPostalCode(), request.getIsPrimary());
        return customerAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Integer addressId) {
        if (!customerAddressRepository.existsById(addressId)) {
            throw new RuntimeException("Address not found: " + addressId);
        }
        customerAddressRepository.deleteById(addressId);
    }

    @Transactional
    public User updateUserRoleAndStatus(Integer userId, UpdateRoleRequest request) {
        User user = getUserById(userId);
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getAccountStatus() != null) {
            user.setAccountStatus(request.getAccountStatus());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserLock(Integer userId) {
        User user = getUserById(userId);
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        } else {
            user.setAccountStatus(AccountStatus.SUSPENDED);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new IllegalArgumentException("No account found with username or email: " + request.getUsernameOrEmail()));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPlainPassword(request.getNewPassword());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        User user = getUserById(userId);
        // Prevent deleting the primary admin account (id 1 or admin username)
        if (user.getUserId() == 1 || "admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalStateException("Primary system administrator account cannot be deleted.");
        }

        // Clean up customer addresses and customer profile first
        try {
            List<CustomerAddress> addresses = customerAddressRepository.findByCustomer_CustomerId(userId);
            if (!addresses.isEmpty()) {
                customerAddressRepository.deleteAll(addresses);
            }
            if (customerRepository.existsById(userId)) {
                customerRepository.deleteById(userId);
            }
        } catch (Exception ignored) {}

        userRepository.delete(user);
    }
}

package com.shopease.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "customers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {

    @Id
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "default_delivery_address", columnDefinition = "TEXT")
    private String defaultDeliveryAddress;

    @Column(name = "cardholder_name", length = 150)
    private String cardholderName;

    @Column(name = "card_number", length = 30)
    private String cardNumber;

    @Column(name = "card_expiry", length = 10)
    private String cardExpiry;

    @Column(name = "card_cvv", length = 10)
    private String cardCvv;

    @Transient
    private String username;

    @Transient
    private String email;

    public Customer() {}

    public Customer(Integer customerId, String fullName, String phoneNumber, String defaultDeliveryAddress) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.defaultDeliveryAddress = defaultDeliveryAddress;
    }

    public Customer(User user, String fullName, String phoneNumber, String defaultDeliveryAddress) {
        this.customerId = (user != null) ? user.getUserId() : null;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.defaultDeliveryAddress = defaultDeliveryAddress;
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public User getUser() { return null; }
    public void setUser(User user) {
        if (user != null && user.getUserId() != null) {
            this.customerId = user.getUserId();
        }
    }

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

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

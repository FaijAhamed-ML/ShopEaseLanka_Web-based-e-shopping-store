package com.shopease.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "customer_addresses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    public CustomerAddress() {}

    public CustomerAddress(Customer customer, String addressLine, String city, String postalCode, Boolean isPrimary) {
        this.customer = customer;
        this.addressLine = addressLine;
        this.city = city;
        this.postalCode = postalCode;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    public Integer getAddressId() { return addressId; }
    public void setAddressId(Integer addressId) { this.addressId = addressId; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
}

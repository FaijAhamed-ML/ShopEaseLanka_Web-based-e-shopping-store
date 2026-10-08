USE `shopease_db`;

-- MODULE 1: USER MANAGEMENT & SECURITY MONITORING

-- Users Table
CREATE TABLE `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(60) NOT NULL UNIQUE,
    `email` VARCHAR(120) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `plain_password` VARCHAR(255) NULL,
    `role` ENUM(
        'CUSTOMER',
        'ADMIN',
        'CATALOG_MANAGER',
        'INVENTORY_OFFICER',
        'SALES_MANAGER',
        'DELIVERY_COORDINATOR',
        'CUSTOMER_EXPERIENCE_OFFICER',
        'DELIVERY_PERSON'
    ) NOT NULL DEFAULT 'CUSTOMER',
    `account_status` ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Customers Table (Profile)
CREATE TABLE `customers` (
    `customer_id` INT PRIMARY KEY,
    `full_name` VARCHAR(150) NOT NULL,
    `phone_number` VARCHAR(30) NOT NULL,
    `default_delivery_address` TEXT NULL,
    `cardholder_name` VARCHAR(150) NULL,
    `card_number` VARCHAR(30) NULL,
    `card_expiry` VARCHAR(10) NULL,
    `card_cvv` VARCHAR(10) NULL,
    CONSTRAINT `fk_customers_user` FOREIGN KEY (`customer_id`) 
        REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Customer Addresses Table (Multi-address support)
CREATE TABLE `customer_addresses` (
    `address_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` INT NOT NULL,
    `address_line` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `postal_code` VARCHAR(20) NOT NULL,
    `is_primary` BOOLEAN DEFAULT FALSE,
    CONSTRAINT `fk_addresses_customer` FOREIGN KEY (`customer_id`) 
        REFERENCES `customers` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Login Activity Table (Security audit & suspicious attempt tracking)
CREATE TABLE `login_activity` (
    `activity_id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `ip_address` VARCHAR(50) NOT NULL,
    `login_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('SUCCESS', 'FAILURE') NOT NULL,
    `suspicious_flag` BOOLEAN DEFAULT FALSE,
    CONSTRAINT `fk_login_activity_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes
CREATE INDEX `idx_login_activity_user` ON `login_activity` (`user_id`, `login_time`);

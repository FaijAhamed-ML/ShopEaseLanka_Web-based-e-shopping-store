CREATE DATABASE IF NOT EXISTS `shopease_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `shopease_db`;

-- Disable foreign key checks for clean re-creation if needed
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `reviews`;
DROP TABLE IF EXISTS `delivery_attempts`;
DROP TABLE IF EXISTS `deliveries`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `stock_movements`;
DROP TABLE IF EXISTS `inventory`;
DROP TABLE IF EXISTS `product_images`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `login_activity`;
DROP TABLE IF EXISTS `customer_addresses`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;



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




-- MODULE 2: PRODUCT MANAGEMENT

-- Categories Table (Self-referencing tree for subcategories)
CREATE TABLE `categories` (
    `category_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `parent_category_id` INT NULL,
    CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_category_id`) 
        REFERENCES `categories` (`category_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Products Table (LKR pricing, discounts, soft-delete capability)
CREATE TABLE `products` (
    `product_id` INT AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT NOT NULL,
    `name` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10, 2) NOT NULL,
    `discount_percentage` DECIMAL(5, 2) DEFAULT 0.00,
    `main_image_url` VARCHAR(500),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) 
        REFERENCES `categories` (`category_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Product Images Table (Multiple secondary gallery images)
CREATE TABLE `product_images` (
    `image_id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL,
    `image_url` VARCHAR(500) NOT NULL,
    CONSTRAINT `fk_images_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;





-- MODULE 3: LIVE INVENTORY & BATCH CONTROL

-- Inventory Table (1-to-1 with products, threshold tracking)
CREATE TABLE `inventory` (
    `inventory_id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL UNIQUE,
    `stock_quantity` INT NOT NULL DEFAULT 0,
    `low_stock_threshold` INT NOT NULL DEFAULT 10,
    `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stock Movements Table (Audit trail for batches & deductions)
CREATE TABLE `stock_movements` (
    `movement_id` INT AUTO_INCREMENT PRIMARY KEY,
    `inventory_id` INT NOT NULL,
    `batch_number` VARCHAR(50) NOT NULL,
    `movement_type` ENUM('ADD_BATCH', 'ORDER_DEDUCTION', 'MANUAL_ADJUSTMENT') NOT NULL,
    `quantity` INT NOT NULL,
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `performed_by` INT NOT NULL,
    CONSTRAINT `fk_movements_inventory` FOREIGN KEY (`inventory_id`) 
        REFERENCES `inventory` (`inventory_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_movements_user` FOREIGN KEY (`performed_by`) 
        REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




-- MODULE 4: ORDER PROCESSING & CHECKOUT

-- Orders Table
CREATE TABLE `orders` (
    `order_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` INT NOT NULL,
    `total_amount` DECIMAL(10, 2) NOT NULL,
    `order_status` ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    `shipping_address` TEXT NOT NULL,
    `deleted_by_customer` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_orders_customer` FOREIGN KEY (`customer_id`) 
        REFERENCES `customers` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Order Items Table
CREATE TABLE `order_items` (
    `order_item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `product_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(10, 2) NOT NULL,
    `discount` DECIMAL(5, 2) DEFAULT 0.00,
    CONSTRAINT `fk_items_order` FOREIGN KEY (`order_id`) 
        REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_items_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Payments Table
CREATE TABLE `payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `payment_method` ENUM('CARD', 'CASH_ON_DELIVERY') NOT NULL,
    `payment_status` ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    `payment_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) 
        REFERENCES `orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;





-- MODULE 5: DELIVERY MANAGEMENT

--Deliveries Table (Auto-generated per confirmed order)
CREATE TABLE `deliveries` (
    `delivery_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL UNIQUE,
    `delivery_person_id` INT NULL,
    `shipment_status` ENUM('PROCESSING', 'DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED') NOT NULL DEFAULT 'PROCESSING',
    `tracking_number` VARCHAR(50) NOT NULL UNIQUE,
    `notes` TEXT,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_deliveries_order` FOREIGN KEY (`order_id`) 
        REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_deliveries_rider` FOREIGN KEY (`delivery_person_id`) 
        REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Delivery Attempts Table (Rider attempt history)
CREATE TABLE `delivery_attempts` (
    `attempt_id` INT AUTO_INCREMENT PRIMARY KEY,
    `delivery_id` INT NOT NULL,
    `attempt_status` ENUM('SUCCESS', 'FAILED') NOT NULL,
    `failure_reason` VARCHAR(255) NULL,
    `attempt_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_attempts_delivery` FOREIGN KEY (`delivery_id`) 
        REFERENCES `deliveries` (`delivery_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




-- MODULE 6: REVIEW MANAGEMENT

-- Reviews Table (Verified buyer rule enforced, moderation queue)
CREATE TABLE `reviews` (
    `review_id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL,
    `customer_id` INT NOT NULL,
    `rating` INT NOT NULL CHECK (`rating` BETWEEN 1 AND 5),
    `comment` TEXT,
    `image_url` VARCHAR(500) NULL,
    `status` ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_reviews_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products` (`product_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reviews_customer` FOREIGN KEY (`customer_id`) 
        REFERENCES `customers` (`customer_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Notifications Table (Multi-purpose alerts)
CREATE TABLE `notifications` (
    `notification_id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `message_type` ENUM('ORDER', 'DELIVERY', 'STOCK', 'SECURITY') NOT NULL,
    `content` TEXT NOT NULL,
    `status` ENUM('UNREAD', 'READ') NOT NULL DEFAULT 'UNREAD',
    `sent_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexing for high-performance querying
CREATE INDEX `idx_products_category` ON `products` (`category_id`);
CREATE INDEX `idx_inventory_product` ON `inventory` (`product_id`);
CREATE INDEX `idx_orders_customer` ON `orders` (`customer_id`);
CREATE INDEX `idx_deliveries_tracking` ON `deliveries` (`tracking_number`);
CREATE INDEX `idx_reviews_product_status` ON `reviews` (`product_id`, `status`);
CREATE INDEX `idx_login_activity_user` ON `login_activity` (`user_id`, `login_time`);

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

USE `shopease_db`;

-- MODULE 1: USERS & SECURITY SEED DATA

INSERT INTO `users` (`user_id`, `username`, `email`, `password_hash`, `plain_password`, `role`, `account_status`, `created_at`) VALUES
(1, 'admin', 'admin@shopease.lk', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Admin@123', 'ADMIN', 'ACTIVE', NOW()),
(2, 'catalog_mgr', 'catalog@shopease.lk', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Catalog@123', 'CATALOG_MANAGER', 'ACTIVE', NOW()),
(3, 'inv_officer', 'inventory@shopease.lk', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Inventory@123', 'INVENTORY_OFFICER', 'ACTIVE', NOW()),
(4, 'sales_mgr', 'sales@shopease.lk', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Sales@123', 'SALES_MANAGER', 'ACTIVE', NOW()),
(5, 'delivery_coord', 'logistics@shopease.lk', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Delivery@123', 'DELIVERY_COORDINATOR', 'ACTIVE', NOW()),
(6, 'cx_officer', 'experience@shopease.lk', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Customer@123', 'CUSTOMER_EXPERIENCE_OFFICER', 'ACTIVE', NOW()),
(7, 'rider_kamal', 'kamal.rider@shopease.lk', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Rider@123', 'DELIVERY_PERSON', 'ACTIVE', NOW()),
(8, 'customer_kasun', 'kasun.perera@gmail.com', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Kasun@123', 'CUSTOMER', 'ACTIVE', NOW()),
(9, 'customer_nimalka', 'nimalka.fernando@yahoo.com', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Nimalka@123', 'CUSTOMER', 'ACTIVE', NOW()),
(10, 'customer_saman', 'saman.kumara@outlook.com', '$2a$10$6sHpUtgnLdCjrLFgLx7u5u4kot2e9OWMjcf.joVY90NhPoDisNvsu', 'Saman@123', 'CUSTOMER', 'ACTIVE', NOW());

-- Customer & Staff Profiles
INSERT INTO `customers` (`customer_id`, `full_name`, `phone_number`, `default_delivery_address`) VALUES
(1, 'System Administrator', '+94 11 234 5678', 'ShopEase HQ, No. 100, Galle Face, Colombo 03'),
(2, 'Catalog Manager', '+94 11 234 5679', 'ShopEase Operations, Colombo 03'),
(3, 'Inventory Control Officer', '+94 11 234 5680', 'ShopEase Central Warehouse, Kelaniya'),
(4, 'Sales Operations Manager', '+94 11 234 5681', 'ShopEase Commercial Division, Colombo 03'),
(5, 'Logistics Coordinator', '+94 11 234 5682', 'ShopEase Dispatch Hub, Orugodawatta'),
(6, 'Customer Experience Officer', '+94 11 234 5683', 'ShopEase Support Center, Colombo 03'),
(7, 'Kamal Silva (Delivery Rider)', '+94 77 999 8877', 'Colombo Central Delivery Depot'),
(8, 'Kasun Perera', '+94 77 123 4567', 'No. 45/2, Galle Road, Colombo 03, Western Province'),
(9, 'Nimalka Fernando', '+94 71 987 6543', '12 Lake View Court, Kandy, Central Province'),
(10, 'Saman Kumara', '+94 76 555 1212', '88 Light House Street, Fort, Galle, Southern Province');

-- Customer Addresses
INSERT INTO `customer_addresses` (`address_id`, `customer_id`, `address_line`, `city`, `postal_code`, `is_primary`) VALUES
(1, 8, 'No. 45/2, Galle Road, Bambalapitiya', 'Colombo 03', '00300', TRUE),
(2, 8, 'Level 4, World Trade Centre, Echelon Square', 'Colombo 01', '00100', FALSE),
(3, 9, '12 Lake View Court', 'Kandy', '20000', TRUE),
(4, 10, '88 Light House Street, Fort', 'Galle', '80000', TRUE),
(5, 1, 'ShopEase HQ, No. 100, Galle Face', 'Colombo 03', '00300', TRUE);

-- Login Activity Audit Trail
-- Demonstrates: Successful logins, consecutive failures (suspicious), non-existent user attempts (NULL user_id), and outdated logs (>30 days)
INSERT INTO `login_activity` (`activity_id`, `user_id`, `ip_address`, `login_time`, `status`, `suspicious_flag`) VALUES
(1, 1, '192.168.1.10', NOW() - INTERVAL 5 HOUR, 'SUCCESS', FALSE),
(2, 8, '112.134.88.21', NOW() - INTERVAL 4 HOUR, 'SUCCESS', FALSE),
(3, 8, '112.134.88.21', NOW() - INTERVAL 1 HOUR, 'SUCCESS', FALSE),
(4, 10, '45.12.89.102', NOW() - INTERVAL 30 MINUTE, 'FAILURE', FALSE),
(5, 10, '45.12.89.102', NOW() - INTERVAL 25 MINUTE, 'FAILURE', FALSE),
(6, 10, '45.12.89.102', NOW() - INTERVAL 20 MINUTE, 'FAILURE', TRUE),
(7, NULL, '185.220.101.5', NOW() - INTERVAL 15 MINUTE, 'FAILURE', TRUE),
(8, NULL, '185.220.101.5', NOW() - INTERVAL 10 MINUTE, 'FAILURE', TRUE),
(9, 8, '112.134.88.21', NOW() - INTERVAL 35 DAY, 'SUCCESS', FALSE),
(10, 10, '45.12.89.102', NOW() - INTERVAL 45 DAY, 'FAILURE', FALSE),
(11, 1, '192.168.1.10', NOW() - INTERVAL 60 DAY, 'SUCCESS', FALSE);

-- ShopEase Lanka - Seed Data
-- Database: shopease_db


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




-- MODULE 2: PRODUCT CATALOG 
INSERT INTO `categories` (`category_id`, `name`, `parent_category_id`) VALUES
(1, 'Ceylon Tea & Spices', NULL),
(2, 'Traditional Handicrafts & Batik', NULL),
(3, 'Electronics & Smart Living', NULL),
(4, 'Ceylon Pure Black Tea', 1),
(5, 'Artisanal Cinnamon & Spices', 1),
(6, 'Traditional Wooden Masks', 2),
(7, 'Handloom & Batik Apparel', 2),
(8, 'Kitchen & Home Appliances', 3);

-- Products
INSERT INTO `products` (`product_id`, `category_id`, `name`, `description`, `price`, `discount_percentage`, `main_image_url`, `created_at`) VALUES
(1, 4, 'Dilmah Single Origin Nuwara Eliya PEKOE (250g)', 'High grown orthodox Ceylon tea renowned for its delicate fragrance, bright golden color, and sophisticated floral notes.', 2450.00, 10.00, 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&auto=format&fit=crop&q=80', NOW()),
(2, 4, 'Mackwoods BOPF Ceylon Broken Orange Pekoe (500g)', 'Full-bodied, robust breakfast tea from the misty hills of Labookellie estate, packed fresh at origin.', 3200.00, 0.00, 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=600&auto=format&fit=crop&q=80', NOW()),
(3, 5, 'Organic Ceylon True Alba Cinnamon Quills (100g)', 'Finest Grade Alba cinnamon harvested in Mirissa, sweet and fragrant with ultra-low coumarin content.', 1850.00, 5.00, 'https://images.unsplash.com/photo-1509358271058-acd22cc93898?w=600&auto=format&fit=crop&q=80', NOW()),
(4, 6, 'Handcrafted Ambalangoda Mayura Peacock Raksha Mask', 'Carved out of seasoned Kaduru wood and painted in natural pigments, depicting protection and vibrant heritage.', 8500.00, 15.00, 'https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=600&auto=format&fit=crop&q=80', NOW()),
(5, 7, 'Handloom Pure Cotton Sri Lankan Batik Shirt', 'Artisan hand-dyed floral batik motif shirt tailored from 100% breathable organic Sri Lankan cotton.', 6200.00, 0.00, 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&auto=format&fit=crop&q=80', NOW()),
(6, 8, 'Singer 1.8L Sri Lankan Clay-Pot Rice Cooker', 'Energy-efficient automatic rice cooker with non-stick inner bowl and keep-warm function.', 14900.00, 12.00, 'https://images.unsplash.com/photo-1584992236310-6edddc08acff?w=600&auto=format&fit=crop&q=80', NOW()),
(7, 5, 'Ceylon Black Pepper Powder & Whole Cardamom Jar Set', 'Estate-grown Matale black pepper and Kandy green cardamom pods packed in eco-friendly glass jars.', 2100.00, 0.00, 'https://images.unsplash.com/photo-1599940824399-b87987ceb72a?w=600&auto=format&fit=crop&q=80', NOW()),
(8, 7, 'Traditional Barefoot Style Handloom Tote Bag', 'Vibrant handwoven multi-color tote with durable stitching, ideal for island shopping and beach outings.', 3400.00, 8.00, 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=600&auto=format&fit=crop&q=80', NOW());

-- Secondary Product Images
INSERT INTO `product_images` (`image_id`, `product_id`, `image_url`) VALUES
(1, 1, 'https://images.unsplash.com/photo-1597481499750-3e6b22637e12?w=600&auto=format&fit=crop&q=80'),
(2, 1, 'https://images.unsplash.com/photo-1563822249548-9a72b6353cd1?w=600&auto=format&fit=crop&q=80'),
(3, 4, 'https://images.unsplash.com/photo-1582562124811-c09040d0a901?w=600&auto=format&fit=crop&q=80'),
(4, 6, 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=600&auto=format&fit=crop&q=80');




-- MODULE 3: INVENTORY & STOCK MOVEMENTS
INSERT INTO `inventory` (`inventory_id`, `product_id`, `stock_quantity`, `low_stock_threshold`, `last_updated`) VALUES
(1, 1, 65, 10, NOW()),
(2, 2, 40, 10, NOW()),
(3, 3, 50, 15, NOW()),
(4, 4, 5, 10, NOW()), 
(5, 5, 22, 8, NOW()),
(6, 6, 14, 5, NOW()),
(7, 7, 30, 10, NOW()),
(8, 8, 0, 5, NOW());  

-- Stock Movements Audit
INSERT INTO `stock_movements` (`movement_id`, `inventory_id`, `batch_number`, `movement_type`, `quantity`, `timestamp`, `performed_by`) VALUES
(1, 1, 'BATCH-2026-T101', 'ADD_BATCH', 70, NOW() - INTERVAL 10 DAY, 3),
(2, 2, 'BATCH-2026-T102', 'ADD_BATCH', 50, NOW() - INTERVAL 8 DAY, 3),
(3, 3, 'BATCH-2026-S201', 'ADD_BATCH', 60, NOW() - INTERVAL 7 DAY, 3),
(4, 4, 'BATCH-2026-H301', 'ADD_BATCH', 10, NOW() - INTERVAL 6 DAY, 3),
(5, 1, 'ORD-DEC-001', 'ORDER_DEDUCTION', -5, NOW() - INTERVAL 2 DAY, 3),
(6, 4, 'ORD-DEC-002', 'ORDER_DEDUCTION', -5, NOW() - INTERVAL 1 DAY, 3);





-- MODULE 4: ORDERS, ORDER ITEMS 

-- Order 1: Completed & Delivered
INSERT INTO `orders` (`order_id`, `customer_id`, `total_amount`, `order_status`, `shipping_address`, `created_at`) VALUES
(1, 8, 11705.00, 'DELIVERED', 'No. 45/2, Galle Road, Bambalapitiya, Colombo 03', NOW() - INTERVAL 4 DAY),
(2, 9, 3200.00, 'PROCESSING', '12 Lake View Court, Kandy', NOW() - INTERVAL 1 DAY),
(3, 8, 7225.00, 'CONFIRMED', 'Level 4, World Trade Centre, Echelon Square, Colombo 01', NOW() - INTERVAL 5 HOUR);

-- Order Items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `product_id`, `quantity`, `unit_price`, `discount`) VALUES
(1, 1, 1, 2, 2450.00, 10.00),
(2, 1, 4, 1, 8500.00, 15.00),
(3, 2, 2, 1, 3200.00, 0.00),
(4, 3, 4, 1, 8500.00, 15.00);

-- Payments
INSERT INTO `payments` (`payment_id`, `order_id`, `amount`, `payment_method`, `payment_status`, `payment_date`) VALUES
(1, 1, 11705.00, 'CARD', 'COMPLETED', NOW() - INTERVAL 4 DAY),
(2, 2, 3200.00, 'CASH_ON_DELIVERY', 'PENDING', NOW() - INTERVAL 1 DAY),
(3, 3, 7225.00, 'CARD', 'COMPLETED', NOW() - INTERVAL 5 HOUR);




-- MODULE 5: DELIVERIES
INSERT INTO `deliveries` (`delivery_id`, `order_id`, `delivery_person_id`, `shipment_status`, `tracking_number`, `notes`, `updated_at`) VALUES
(1, 1, 7, 'DELIVERED', 'SEL-LK-908123', 'Handed over directly to recipient Kasun Perera at residence.', NOW() - INTERVAL 2 DAY),
(2, 2, 7, 'IN_TRANSIT', 'SEL-LK-554210', 'En route to Kandy regional delivery hub.', NOW() - INTERVAL 3 HOUR),
(3, 3, NULL, 'PROCESSING', 'SEL-LK-102938', 'Awaiting warehouse sorting and rider assignment.', NOW() - INTERVAL 1 HOUR);

-- Delivery Attempts
INSERT INTO `delivery_attempts` (`attempt_id`, `delivery_id`, `attempt_status`, `failure_reason`, `attempt_time`) VALUES
(1, 1, 'SUCCESS', NULL, NOW() - INTERVAL 2 DAY),
(2, 2, 'SUCCESS', 'Reached Kadugannawa checkpoint', NOW() - INTERVAL 4 HOUR);




-- MODULE 6: REVIEWS 
INSERT INTO `reviews` (`review_id`, `product_id`, `customer_id`, `rating`, `comment`, `image_url`, `status`, `created_at`) VALUES
(1, 1, 8, 5, 'Exceptional aroma and rich taste. Authentically Nuwara Eliya grown tea! Fast delivery to Colombo 03.', NULL, 'APPROVED', NOW() - INTERVAL 1 DAY),
(2, 4, 8, 5, 'Masterpiece craftsmanship! The Kaduru wood carving and peacock painting look stunning on our living room wall.', NULL, 'PENDING', NOW() - INTERVAL 2 HOUR),
(3, 2, 9, 4, 'Very strong tea, ideal for morning milk tea. Packaging was intact.', NULL, 'APPROVED', NOW() - INTERVAL 3 DAY);

-- Notifications
INSERT INTO `notifications` (`notification_id`, `user_id`, `message_type`, `content`, `status`, `sent_time`) VALUES
(1, 8, 'ORDER', 'Your ShopEase Lanka order #1 has been confirmed successfully.', 'READ', NOW() - INTERVAL 4 DAY),
(2, 8, 'DELIVERY', 'Package SEL-LK-908123 has been safely delivered.', 'READ', NOW() - INTERVAL 2 DAY),
(3, 3, 'STOCK', 'WARNING: Product "Handcrafted Ambalangoda Mayura Peacock Raksha Mask" stock is 5 (Threshold: 10).', 'UNREAD', NOW() - INTERVAL 1 DAY),
(4, 1, 'SECURITY', 'Security Alert: 3 failed consecutive login attempts detected for user saman.', 'UNREAD', NOW() - INTERVAL 20 MINUTE);

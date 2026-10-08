# ShopEase Lanka - Web-Based E-Shopping Platform

ShopEase Lanka is a full-stack e-shopping web application for Sri Lankan customers and store staff. It brings user accounts, the product catalogue, live inventory, order processing, delivery tracking and customer reviews/notifications into one platform.

> **Module:** SE2030 Software Engineering / IT2140 Database Design and Development - SLIIT
> **Group code:** 2026-Y2-S1-MLB-WEB2G1-02

---

## Features

| Module | What it does |
|---|---|
| **User Management** | Registration, secure login/logout, password reset, profile and delivery addresses, account activation/deactivation, login-attempt logging and suspicious-activity flagging |
| **Product Management** | Categories and sub-categories, product add/update/delete, multiple images, prices and discounts, keyword search and filters |
| **Inventory Management** | Live stock levels, stock batches, quantity adjustments, stock-movement history, low-stock alerts, inventory reports |
| **Order Management** | Cart and checkout, automatic stock verification, order tracking, cancellation, payment status, order reports |
| **Delivery Management** | Delivery records, rider assignment, shipment status timeline, delivery attempts, delivery notifications |
| **Review & Notification Management** | Verified-buyer ratings and reviews, edit own review, moderation (approve/reject/delete), average ratings, in-app notification bell |

Staff roles: Administrator, Catalog Manager, Inventory Officer, Sales Manager, Delivery Coordinator, Delivery Rider, Customer Experience Officer. Each role only sees the admin tabs it is allowed to use.

## Tech stack

- **Backend:** Java 21, Spring Boot 4.1.1 (Web, Data JPA, Bean Validation via Hibernate Validator), Spring Security Crypto (BCrypt)
- **Database:** MySQL 8 (`shopease_db`), Hibernate/JPA
- **Frontend:** HTML5, CSS3, vanilla JavaScript (no framework), served from `src/main/resources/static`
- **Build:** Maven (wrapper included)
- **Currency / region:** LKR, Sri Lanka

## Getting started

### Prerequisites
- JDK 21
- MySQL 8 running on `localhost:3306`
- Git

### 1. Clone
```bash
git clone https://github.com/FaijAhamed-ML/ShopEaseLanka_Web-based-e-shopping-store.git
cd ShopEaseLanka_Web-based-e-shopping-store
```

### 2. Configure the database password
The password is **not** stored in the repository. Set it as an environment variable before running:

```bash
# Linux / macOS / Git Bash
export DB_PASSWORD=your_mysql_password

# Windows PowerShell
$env:DB_PASSWORD="your_mysql_password"
```

Database name, user and URL are in `src/main/resources/application.properties` (`root` / `shopease_db`). `application-example.properties` shows the expected format.

### 3. Run
```bash
./mvnw spring-boot:run          # Windows: .\mvnw spring-boot:run
```
On first start the app creates `shopease_db`, builds the tables from `schema.sql` and loads demo data from `data.sql`.

### 4. Open
| Page | URL |
|---|---|
| Storefront | http://localhost:8080/index.html |
| Product details | http://localhost:8080/product-details.html?id=1 |
| Checkout | http://localhost:8080/checkout.html |
| Order tracking | http://localhost:8080/order-tracking.html |
| Staff login | http://localhost:8080/admin/login |
| Staff dashboard | http://localhost:8080/admin/dashboard |

> Always open the pages through the running server. The admin dashboard and product page load HTML fragments with `fetch`, which does not work from `file://`.

### Demo accounts
Seed users are created by `data.sql` (demo data only - never use these in production). Passwords are listed in that file.

| Username | Role |
|---|---|
| `admin` | Administrator |
| `catalog_mgr` | Catalog Manager |
| `inv_officer` | Inventory Officer |
| `sales_mgr` | Sales Manager |
| `delivery_coord` | Delivery Coordinator |
| `cx_officer` | Customer Experience Officer |
| `rider_kamal` | Delivery Rider |
| `customer_kasun`, `customer_nimalka`, `customer_saman` | Customers |

## Running the tests
```bash
./mvnw test
```
Tests cover the order observers, the payment strategies and the delivery status transitions.

## Project structure

```
shopease-lanka/
├── pom.xml, mvnw, mvnw.cmd
├── database/                       reference SQL extracts per module
├── src/main/java/com/shopease/
│   ├── ShopEaseLankaApplication.java
│   ├── common/                     ApiResponse wrapper
│   ├── config/                     SecurityConfig, WebConfig
│   ├── usermanagement/             controller, dto, entity, repository, service
│   ├── productmanagement/
│   ├── inventorymanagement/
│   ├── ordermanagement/            + observer/ and strategy/ packages
│   ├── deliverymanagement/
│   └── reviewmanagement/           reviews + notifications
├── src/main/resources/
│   ├── application.properties, schema.sql, data.sql
│   └── static/
│       ├── *.html                  index, product-details, checkout, order-tracking, admin-login, admin-dashboard
│       ├── fragments/              review-section/modal and admin/tab-*.html (loaded at runtime)
│       ├── css/                    all.css (manifest) + base, product, order, delivery, review ... files
│       └── js/                     api-*.js, app-*.js, page scripts, admin/tab-*.js
└── src/test/java/com/shopease/     unit tests
```

Backend packages follow a layered design: **Controller -> Service -> Repository -> Entity**, with DTOs for requests. `ordermanagement` also uses the **Observer** and **Strategy** patterns.

### Frontend layout
The frontend is split so every member owns separate HTML, CSS and JS files:

- `css/all.css` imports every stylesheet in cascade order (shared base files plus one or two files per module).
- `js/api-core.js` defines the shared `API` client; each `api-<module>.js` adds that module's endpoints.
- `js/app-core.js` defines the shared `App` controller; `app-user.js`, `app-order.js` and `app-notification.js` add features to it.
- The admin dashboard shell (`admin-dashboard.html`) loads one `fragments/admin/tab-<module>.html` and one `js/admin/tab-<module>.js` per module.

## REST API overview

All responses use the `ApiResponse` wrapper (`success`, `message`, `data`).

| Base path | Module |
|---|---|
| `/api/auth` | login, register, password reset |
| `/api/users` | profiles, addresses, accounts, login activity |
| `/api/products`, `/api/categories` | catalogue |
| `/api/inventory` | stock, batches, adjustments, movements, alerts |
| `/api/orders` | place, track, cancel, update status |
| `/api/deliveries` | delivery records, riders, status, attempts |
| `/api/reviews` | eligibility, submit, edit, moderate, summaries |
| `/api/notifications` | list and mark-as-read |

## Team and branches

| # | Member | Student ID | Module | Branch |
|---|---|---|---|---|
| 1 | Sathsarani H.M.H.K. | IT25101557 | User Management | `UserManagement` |
| 2 | Thamoddaya W.M.R. | IT25101611 | Product Management | `ProductManagement` |
| 3 | Wickramasundara D.G.U.B. | IT25101588 | Inventory Management | `InventoryManagement` |
| 4 | Dharshika T. | IT25101565 | Order Management | `OrderManagement` |
| 5 | Lakshith R. | IT25101492 | Delivery Management | `DeliveryManagement` |
| 6 | Ahamed M.L.F. | IT25101505 | Review & Notification Management | `ReviewManagement` |

### Git workflow
1. `main` holds the final, tested code; `develop` is where branches are merged first.
2. Create your branch from `develop`: `git checkout develop && git pull && git checkout -b feature/<module>`.
3. Commit only the files you own, in small steps (backend, HTML, CSS, JS, database).
4. Push and open a pull request into `develop`.
5. Modules depend on each other (for example Inventory, Order and Delivery call the notification service), so run `./mvnw compile` after all branches are merged, not on a single branch.

## Known limitations
- Login uses an application-side session in the browser; the REST endpoints do not enforce roles on the server yet.
- `data.sql` contains demo passwords in a `plain_password` column, used by the login fallback in `UserService`. Remove it before any real deployment.
- The unread notification badge shows the total number of notifications.
- Only a single currency (LKR) and country (Sri Lanka) are supported.

## License
Academic project for SLIIT. Not licensed for commercial use.

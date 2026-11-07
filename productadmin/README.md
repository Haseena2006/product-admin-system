# Product Admin System

A Spring Boot 3 web application and REST API to manage a product catalogue.
Admins create, edit and delete products; normal users can only browse, search and filter.

> **Status note:** the code was written step by step, but at the time this README was written the
> automated tests (`mvn clean test`) and the Postman collection had **not been executed** by the author
> of this package. Run them once on your machine (instructions below) and only then treat them as passing.

## Features

- Registration, login, logout (session based), BCrypt password hashing, CSRF protection
- Roles: **ADMIN** (full CRUD) and **USER** (read only). New registrations are always USER
- Product CRUD with search by name, filter by category, sort by name/price/rating/stock, pagination
- Server-rendered UI (Thymeleaf + CSS + JS): login, register, dashboard, list, add/edit, details, delete confirmation
- REST API under `/api/products` returning JSON; unauthorized requests get **JSON 401/403, never the login page**
- Global exception handling with one consistent JSON error shape
- PostgreSQL with unique constraints, not-null constraints and indexes
- Demo data (2 accounts + 16 products) created automatically on first start

## Tech stack

Java 17, Spring Boot 3.3.5 (Web, Data JPA, Security, Validation), Thymeleaf, PostgreSQL, Maven,
JUnit 5, Mockito, MockMvc, H2 (tests only), Postman.

## Project structure

```
src/main/java/com/example/productadmin
├── config       DataSeeder (demo data)
├── controller   web/ (HTML pages)   api/ (JSON REST)
├── dto          request/response objects (entities are never exposed)
├── entity       User, Role, Product
├── exception    custom exceptions + @RestControllerAdvice / @ControllerAdvice
├── repository   Spring Data JPA repositories
├── security     SecurityConfig, UserDetailsService, JSON 401/403 handlers
├── service      business rules (ProductService, RegistrationService)
└── validation   @StrongPassword
database/        schema.sql, data.sql
postman/         ProductAdmin.postman_collection.json
```

Flow: `Controller -> Service -> Repository -> PostgreSQL`. Controllers only handle HTTP; rules live in services.

## Setup

### 1. Database
In pgAdmin (Query Tool) or `psql`:
```sql
CREATE DATABASE product_admin;
```
Tables are created by Hibernate on first start. (`database/schema.sql` has the same tables if you prefer to create them by hand.)

### 2. Environment variables (nothing secret is stored in the code)

| Variable | Required | Default | Meaning |
|---|---|---|---|
| `DB_PASSWORD` | **yes** | - | PostgreSQL password |
| `DB_USERNAME` | no | `postgres` | PostgreSQL user |
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/product_admin` | JDBC URL |
| `SEED_ADMIN_PASSWORD` | no | `Admin@123` | password of the demo admin |
| `SEED_USER_PASSWORD` | no | `User@123` | password of the demo user |
| `CORS_ALLOWED_ORIGINS` | no | empty | comma separated origins allowed to call `/api` from another site |
| `PORT` | no | `8080` | server port |

IntelliJ: Run configuration -> Modify options -> Environment variables -> `DB_PASSWORD=your_password`.
Terminal: `export DB_PASSWORD=your_password` (Windows PowerShell: `$env:DB_PASSWORD="your_password"`).
See `.env.example`. Never commit a real `.env` file (it is in `.gitignore`).

### 3. Run
```bash
mvn spring-boot:run
```
Open http://localhost:8080 and log in:

| Role | Username | Password |
|---|---|---|
| ADMIN | `admin` | `Admin@123` |
| USER | `user` | `User@123` |

These are demo credentials. Change them with `SEED_*` variables before any real deployment.

## Testing

```bash
mvn clean test
```
Tests use an in-memory H2 database, so **no PostgreSQL or environment variables are needed**.
Test classes: `ProductServiceTest`, `RegistrationServiceTest`, `CustomUserDetailsServiceTest`,
`StrongPasswordValidatorTest` (Mockito/JUnit), `ProductRepositoryTest` (`@DataJpaTest`),
`ProductApiControllerTest`, `ApiBasicAuthTest`, `AuthApiControllerTest`, `ProductWebControllerTest` (MockMvc: ADMIN, USER,
unauthorized, forbidden, validation failures) and a context-load test.

## REST API

The API is **stateless** and secured with **HTTP Basic Authentication**: send `Authorization: Basic ...`
(username:password) on every request. No session, no cookie and no CSRF token are used for `/api/**`.
`GET` = ADMIN or USER. `POST/PUT/DELETE` = ADMIN only.
The website (Thymeleaf pages) keeps form login, a session cookie and CSRF protection.

| Method | URL | Role | Success | Notes |
|---|---|---|---|---|
| POST | `/api/auth/register` | anyone | 201 | body: username, email, password, confirmPassword |
| GET | `/api/auth/me` | any authenticated user | 200 | username + roles (handy to test Basic Auth) |
| GET | `/api/products` | ADMIN, USER | 200 | query: `q`, `category`, `sort` (name, price, rating, stock, createdAt), `dir` (asc/desc), `page` (from 0), `size` |
| GET | `/api/products/categories` | ADMIN, USER | 200 | list of category names |
| GET | `/api/products/{id}` | ADMIN, USER | 200 / 404 | |
| POST | `/api/products` | ADMIN | 201 + `Location` | |
| PUT | `/api/products/{id}` | ADMIN | 200 / 404 | |
| DELETE | `/api/products/{id}` | ADMIN | 204 / 404 | |

Product body:
```json
{ "name": "Wireless Mouse", "description": "optional", "category": "Electronics",
  "price": 799.00, "stock": 10, "rating": 4.4 }
```
Rules: name required (unique, case-insensitive), category required, price > 0 (max 2 decimals),
stock >= 0, rating 0 to 5.

Error shape (all errors):
```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "Validation failed",
  "path": "/api/products", "fieldErrors": { "price": "Price must be greater than zero" } }
```

| Status | When |
|---|---|
| 400 | validation failed / malformed JSON / bad parameter |
| 401 | missing or wrong Basic Auth credentials (JSON + `WWW-Authenticate: Basic` header) |
| 403 | USER calling an ADMIN endpoint |
| 404 | product not found |
| 409 | duplicate product name or username/email |
| 500 | unexpected / database error (details are logged, not returned) |

## Postman

Import `postman/ProductAdmin.postman_collection.json` and run the collection top to bottom
(Collection Runner) against the app with the demo data.

How authentication works in Postman (no cookies, no CSRF):
1. Every folder of the collection has **Authorization -> Basic Auth** set with the collection variables
   `adminName/adminPassword` or `userName/userPassword`. Change them in the collection variables if needed.
2. Postman sends `Authorization: Basic base64(username:password)` on every request. Nothing is stored on the server,
   so there is no `JSESSIONID` and no `X-CSRF-TOKEN` header.
3. Folder **1** shows the failure case: no credentials / wrong password -> `401` JSON with `WWW-Authenticate: Basic`.
4. Folder **2** (USER) can read but gets `403` JSON for POST/DELETE. Folder **3** (ADMIN) does full CRUD.
5. The collection asserts `Content-Type: application/json` on every 401/403, so an accidental HTML login page would fail the test.

## Security notes

- Passwords are stored only as BCrypt hashes; registration never accepts a role from the client.
- Website: form login + session cookie (HttpOnly, SameSite=Lax) + CSRF protection.
- API (`/api/**`): stateless HTTP Basic, no session, CSRF disabled because no cookie is ever used for authentication.
- `/api/**` has its own security filter chain, so API clients get JSON errors instead of redirects.
- CORS is closed by default; only origins listed in `CORS_ALLOWED_ORIGINS` may call `/api` from another site.
- Server error messages are not exposed to clients.
- `DDL_AUTO=update` is convenient for learning; for production create the schema with `database/schema.sql`
  and set `DDL_AUTO=none`. (`validate` mode was not tested.)

## Screenshots

Add your own after running the app (suggested: login, dashboard, product list, edit form):

```
docs/screenshots/login.png
docs/screenshots/dashboard.png
docs/screenshots/product-list.png
```

## Suggested Git commits

```
chore: initialize Spring Boot 3.3.5 project with PostgreSQL configuration
feat: add User, Role and Product entities with constraints and indexes
feat: add repositories and ProductService with search, filter, sort, pagination
feat: add registration, BCrypt hashing and role-based security
feat: add REST API with DTOs, validation and global exception handling
feat: add Thymeleaf pages (login, register, dashboard, product CRUD)
test: add JUnit, Mockito and MockMvc tests
docs: add README, Postman collection and SQL scripts
```

## Notes for reviewers

- Update logic lives in `ProductService.update(...)` (transactional); controllers only translate HTTP.
- Requests/responses use DTOs (`ProductRequest`, `ProductResponse`); JPA entities are never bound to request bodies (no mass assignment).
- `users.created_at` is set by `@PrePersist`. Products are indexed on `category` and `price`; `name` is indexed through its UNIQUE constraint, so a second index on it would be redundant.
- `database/schema.sql` = DDL, `database/data.sql` = seed data (BCrypt hashes).
- The product details page (`/products/{id}`) shows all fields plus created/updated timestamps; the list has a **View** button on every row.
- Database is PostgreSQL. Run with `./mvnw spring-boot:run` (or `mvnw.cmd` on Windows).

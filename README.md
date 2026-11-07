# Product Admin System

A role-based Product Management application built using **Spring Boot 3, Spring Security, Thymeleaf, and PostgreSQL**. It provides a web interface and REST API for managing a product catalogue with secure authentication and authorization.

Admins can create, view, update, and delete products, while normal users have read-only access to browse, search, and filter products.

## Project Status

- Automated tests: **94 passed, 0 failures, 0 errors**
- Build status: **BUILD SUCCESS**
- REST API and Postman collection: Collection execution pending verification.

## Key Features

### Authentication and Authorization
- User registration, login, and logout.
- BCrypt password hashing.
- Role-based access control with ADMIN and USER roles.
- New registrations are assigned the USER role by default.
- Session-based authentication and CSRF protection for the web application.
- Stateless HTTP Basic authentication for REST API endpoints.

### Product Management
- Create, view, update, and delete products.
- Search products by name.
- Filter products by category.
- Sort by name, price, rating, and stock.
- Pagination with page-size limits.
- Product details and delete confirmation pages.

### REST API
- RESTful endpoints under `/api`.
- JSON request and response handling.
- DTO-based data transfer.
- Input validation and consistent error responses.
- Separate security handling for API authentication and authorization.

### Database
- PostgreSQL integration using Spring Data JPA.
- Unique constraints, not-null constraints, and indexes.
- Automatic demo data initialization.
- Two demo accounts and 16 sample products.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language / target |
| Spring Boot 3.3.5 | Application framework |
| Spring Web | REST API and web controllers |
| Spring Data JPA | Database operations |
| Spring Security | Authentication and authorization |
| Thymeleaf | Server-side UI |
| HTML, CSS, JavaScript | Frontend |
| PostgreSQL | Database |
| Maven | Build and dependency management |
| JUnit 5, Mockito | Unit testing |
| MockMvc | Controller testing |
| H2 | In-memory test database |
| Postman | API testing |

**Test environment:** The successful test run was performed using JDK 23 with Maven.

## Project Architecture

The application follows a layered architecture:

`Controller → Service → Repository → PostgreSQL`

- **Controller:** Handles HTTP requests and responses.
- **Service:** Contains business logic and transactional operations.
- **Repository:** Handles database access using Spring Data JPA.
- **Entity:** Represents database entities.
- **DTO:** Transfers request and response data without exposing JPA entities.

### Project Structure

```text
product-admin-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/productadmin/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   │   ├── web/
│   │   │   │   └── api/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── validation/
│   │   └── resources/
│   └── test/
├── database/
│   ├── schema.sql
│   └── data.sql
├── docs/
├── postman/
│   └── ProductAdmin.postman_collection.json
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- JDK 17 or compatible JDK.
- Maven (or the included Maven Wrapper).
- PostgreSQL.
- IntelliJ IDEA or another Java IDE.

### 1. Clone the Repository

```bash
git clone https://github.com/Haseena2006/product-admin-system.git
cd product-admin-system
```

### 2. Create the Database

In PostgreSQL or pgAdmin, execute:

```sql
CREATE DATABASE product_admin;
```

Tables are created by Hibernate when the application starts. Alternatively, refer to `database/schema.sql`.

### 3. Configure Environment Variables

Configure the following environment variables:

| Variable | Required | Default |
|---|---|---|
| DB_URL | No | jdbc:postgresql://localhost:5432/product_admin |
| DB_USERNAME | No | postgres |
| DB_PASSWORD | Yes | None |
| SEED_ADMIN_PASSWORD | No | Admin@12345 |
| SEED_USER_PASSWORD | No | User@12345 |
| CORS_ALLOWED_ORIGINS | No | Empty |
| PORT | No | 8080 |

Example for Windows PowerShell:

```powershell
$env:DB_PASSWORD="your_database_password"
```

See `.env.example` for the configuration template.

**Security:** Never commit real database passwords, credentials, or `.env` files to GitHub.

### 4. Run the Application

Using the Maven Wrapper on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Or:

```bash
./mvnw spring-boot:run
```

Open:

```text
http://localhost:8080
```

### Demo Accounts

| Role | Username | Password |
|---|---|---|
| ADMIN | admin | Admin@12345 |
| USER | user | User@12345 |

These are demo credentials. Configure your own passwords using the `SEED_*_PASSWORD` environment variables before deployment.

## REST API Documentation

The REST API is available under `/api`.

### Authentication

The API uses stateless HTTP Basic Authentication. Send the `Authorization: Basic` header with each authenticated request.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new user |
| GET | `/api/auth/me` | Authenticated | Get current user details |
| GET | `/api/products` | ADMIN, USER | List and search products |
| GET | `/api/products/categories` | ADMIN, USER | Get product categories |
| GET | `/api/products/{id}` | ADMIN, USER | Get product details |
| POST | `/api/products` | ADMIN | Create a product |
| PUT | `/api/products/{id}` | ADMIN | Update a product |
| DELETE | `/api/products/{id}` | ADMIN | Delete a product |

### Product Request Example

```json
{
  "name": "Wireless Mouse",
  "description": "Wireless optical mouse",
  "category": "Electronics",
  "price": 799.00,
  "stock": 10,
  "rating": 4.4
}
```

### Product Validation Rules

- Product name is required and unique (case-insensitive).
- Category is required.
- Price must be greater than zero and have at most two decimal places.
- Stock must be zero or greater.
- Rating must be between 0 and 5.

### HTTP Status Codes

| Status | Description |
|---|---|
| 200 | Request successful |
| 201 | Resource created |
| 204 | Resource deleted |
| 400 | Validation or malformed request error |
| 401 | Authentication required or invalid credentials |
| 403 | Insufficient permissions |
| 404 | Product not found |
| 409 | Duplicate product name or username/email |
| 500 | Unexpected server error |

## Security Implementation

- Passwords are stored using BCrypt hashing.
- Registration does not allow clients to assign their own roles.
- Web application uses session-based form authentication and CSRF protection.
- REST API uses stateless HTTP Basic authentication.
- API responses return JSON authentication and authorization errors instead of HTML login redirects.
- CORS is disabled by default unless allowed origins are configured.
- Internal server error details are not exposed to clients.

## Testing

Run the automated test suite using:

```powershell
.\mvnw.cmd clean test
```

### Latest Test Results

| Metric | Result |
|---|---:|
| Total tests | 94 |
| Passed | 94 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |

**Build result: BUILD SUCCESS**

Test coverage includes:

- Product service operations and validation.
- Registration service.
- User details service.
- Strong password validation.
- Product repository operations.
- REST API controllers.
- Authentication and authorization.
- Web controller access restrictions.
- Application context loading.

The tests use JUnit 5, Mockito, MockMvc, and H2.

## Postman API Testing

The Postman collection is available at:

```text
postman/ProductAdmin.postman_collection.json
```

Import the collection into Postman and configure the demo account variables.

The collection covers:

- Missing or invalid credentials.
- USER read-only access.
- ADMIN product CRUD operations.
- JSON responses for unauthorized and forbidden requests.

**Note:** The collection is included in the repository, but its full execution has not yet been verified.

## Screenshots

Add application screenshots to `docs/screenshots/`:

- Login page
- Registration page
- Admin dashboard
- Product list
- Product details
- Product creation and editing pages

Example paths:

```text
docs/screenshots/login.png
docs/screenshots/dashboard.png
docs/screenshots/product-list.png
docs/screenshots/product-details.png
```

## Future Improvements

- Add more integration tests.
- Verify the complete Postman collection.
- Add deployment configuration.
- Improve application monitoring and logging.
- Add API documentation using OpenAPI/Swagger.

## Developer

**P. Haseena Fathim**

Final-year B.E. Computer Science and Engineering student.

Interested in Java Full Stack Development and Software Engineering.

GitHub: https://github.com/Haseena2006


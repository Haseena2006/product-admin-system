# Product Admin System

A full-stack Product Administration System built using Java 17, Spring Boot 3.3.5, Spring Data JPA, Spring Security, Thymeleaf, and PostgreSQL.

The application provides secure user authentication, role-based product management, RESTful APIs, product search, filtering, sorting, pagination, and input validation.

## Features

### Authentication and Security
- User registration and login.
- Password encryption using BCrypt.
- Role-based access control with ADMIN and USER roles.
- ADMIN can create, update, and delete products.
- USER can view products but cannot modify them.
- Session-based authentication for the web application.
- HTTP Basic Authentication for REST APIs.
- CSRF protection for browser-based requests.

### Product Management
- Create, view, update, and delete products.
- Search products by name.
- Filter products by category.
- Sort products using supported fields.
- Pagination with page-size limits.
- Product validation and centralized exception handling.
- DTO-based request and response handling.

### User Interface
- Responsive product management interface using Thymeleaf.
- Dynamic category filtering.
- Debounced product search.
- Stock status indicators.
- Product details and management actions.

### REST API
- RESTful endpoints returning JSON responses.
- Request validation.
- DTO-based request and response handling.
- Consistent error responses.
- Authentication and authorization.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language / target |
| Spring Boot 3.3.5 | Backend framework |
| Spring Web | REST API development |
| Spring Data JPA | Database operations |
| Spring Security | Authentication and authorization |
| Thymeleaf | Server-side UI |
| PostgreSQL | Relational database |
| Maven | Build and dependency management |
| JUnit 5 | Unit testing |
| Mockito | Mock-based testing |
| MockMvc | Web layer testing |
| H2 | Test database |
| Postman | API testing |

## Project Structure

```text
product-admin-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/productadmin/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── validation/
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/productadmin/
├── database/
├── docs/
├── postman/
├── .env.example
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Prerequisites

Install the following before running the project:

- JDK 17 or compatible configured JDK.
- PostgreSQL.
- IntelliJ IDEA or another Java IDE.
- Postman for API testing.

## Installation and Setup

### 1. Clone the repository

```bash
git clone https://github.com/Haseena2006/product-admin-system.git
cd product-admin-system
```

### 2. Configure PostgreSQL

Create a database:

```sql
CREATE DATABASE product_admin;
```

### 3. Configure environment variables

The application supports environment variables for database and seed account configuration.

Configure these variables in your terminal or IDE Run Configuration:

```properties
DB_URL=jdbc:postgresql://localhost:5432/product_admin
DB_USERNAME=postgres
DB_PASSWORD=your_database_password

SEED_ADMIN_PASSWORD=Admin@12345
SEED_USER_PASSWORD=User@12345

CORS_ALLOWED_ORIGINS=
```

The application properties provide local development defaults for the database URL, username, and empty password. Override them with your actual database credentials.

Do not commit real database passwords or secrets to GitHub.

### 4. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

The application runs at:

http://localhost:8080

## Demo Accounts

| Role | Username | Password |
|---|---|---|
| ADMIN | admin | Admin@12345 |
| USER | user | User@12345 |

These are development/demo credentials. Change them before deploying the application.

## REST API Endpoints

Base URL:

```text
http://localhost:8080
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| GET | `/api/auth/me` | Get authenticated user details |

### Products

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/products` | ADMIN / USER |
| GET | `/api/products/categories` | ADMIN / USER |
| GET | `/api/products/{id}` | ADMIN / USER |
| POST | `/api/products` | ADMIN |
| PUT | `/api/products/{id}` | ADMIN |
| DELETE | `/api/products/{id}` | ADMIN |

### Sample Product JSON

```json
{
  "name": "Wireless Mouse",
  "description": "Wireless optical mouse",
  "category": "Electronics",
  "price": 799.00,
  "stock": 25,
  "rating": 4
}
```

## API Authentication

The REST API uses HTTP Basic Authentication.

In Postman, configure:

- Authorization Type: Basic Auth
- Username: `admin`
- Password: `Admin@12345` or the configured admin password.

For USER account testing, use the configured USER credentials.

The browser-based web application uses session authentication.

## Security

- Passwords are stored using BCrypt password hashing.
- ADMIN and USER roles have different permissions.
- ADMIN users can perform product CRUD operations.
- USER users have read-only product access.
- REST API requests use stateless HTTP Basic Authentication.
- Browser-based requests use session authentication and CSRF protection.
- DTOs separate API request/response data from database entities.
- Input validation protects product data.

## Testing

Run the automated test suite using:

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux / macOS:

```bash
./mvnw clean test
```

The automated test suite covers:

- Product service operations and validation.
- Registration service.
- User details service.
- Strong password validation.
- Product repository operations.
- REST API controllers.
- Authentication and authorization.
- Web controller access restrictions.
- Application context loading.

Testing technologies include JUnit 5, Mockito, MockMvc, and H2.

## Postman API Testing

The Postman collection is available at:

```text
postman/ProductAdmin.postman_collection.json
```

Import the collection into Postman and configure the required environment variables and authentication credentials.

### Latest Postman Test Results

| Metric | Result |
|---|---:|
| API scenarios tested | 24 |
| Expected status codes matched | 24 |
| Failed scenarios | 0 |

The tested scenarios cover:

- User registration and password validation.
- Missing and incorrect credentials.
- USER read-only access.
- ADMIN product CRUD operations.
- Product search, category filtering, and sorting.
- Invalid product data and duplicate products.
- Unauthorized and forbidden requests.
- Product not found responses.

## Database

The application uses PostgreSQL for persistent storage and Spring Data JPA for database access.

The `database/` directory contains database-related SQL scripts and resources. Hibernate handles application schema management, and the application uses its Java data seeding component for initial data.

## Future Improvements

- Add advanced reporting and analytics.
- Improve product inventory tracking.
- Add audit logging.
- Improve pagination and performance for large datasets.
- Add deployment and production configuration.

## Author

**P. Haseena Fathim**

B.E. Computer Science and Engineering

Interested in Java Full Stack Development and Software Engineering.

GitHub: https://github.com/Haseena2006


# Product Admin System

A web-based Product Admin System developed using Spring Boot, Spring Data JPA, MySQL, Spring Security, and Thymeleaf. The application provides product management, search, filtering, sorting, pagination, and REST API operations.

## Features

- **Admin Authentication:** Secure login and logout functionality.
- **Product Management:** Create, view, update, and delete products.
- **REST API:** Supports GET, POST, PUT, and DELETE operations.
- **Search:** Search products by keyword.
- **Category Filtering:** Filter products by category.
- **Sorting:** Sort products by name, price, and other supported fields.
- **Pagination:** Display products across multiple pages.
- **Validation:** Validate input data before processing.
- **Exception Handling:** Centralized exception handling for application errors.
- **Testing:** Unit tests using JUnit and Mockito.
- **API Testing:** API endpoints tested using Postman.

## Technology Stack

- **Backend:** Java, Spring Boot, Spring MVC
- **Database:** MySQL
- **ORM:** Spring Data JPA, Hibernate
- **Security:** Spring Security, BCrypt password hashing
- **Frontend:** Thymeleaf, HTML, CSS, JavaScript
- **Build Tool:** Maven
- **Testing:** JUnit, Mockito
- **API Testing:** Postman
- **Version Control:** Git and GitHub

## REST API Endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | Retrieve all products |
| GET | `/api/products/{id}` | Retrieve a product by ID |
| POST | `/api/products` | Create a product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

Additional supported query parameters can be used for product search, category filtering, sorting, and pagination.

## API Documentation

Published Postman Documentation:

https://documenter.getpostman.com/view/57870781/2sBYHSxfuj

## Prerequisites

Make sure the following are installed:

- Java JDK
- MySQL Server and MySQL Workbench
- Maven
- Git

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Haseena2006/product-admin-system.git
cd product-admin-system
```

### 2. Create the Database

Open MySQL Workbench and execute:

```sql
CREATE DATABASE product_admin;
```

### 3. Configure Database Connection

Update the database URL, username, and password in `src/main/resources/application.properties` according to your local MySQL configuration.

Do not commit actual database passwords or other secrets to GitHub.

### 4. Run the Application

From the project root directory, execute:

```bash
mvn spring-boot:run
```

Alternatively, open the project in IntelliJ IDEA and run the main Spring Boot application class.

### 5. Access the Application

Open your browser and visit:

`http://localhost:8080`

Use the configured admin credentials to access the admin features.

## Testing

- Run automated tests using:

```bash
mvn test
```

- Use the published Postman documentation to review the API requests and test the endpoints.
- Ensure the application and MySQL database are running before testing the APIs locally.

## Author

**Haseena Fathim**

GitHub: https://github.com/Haseena2006

---

This project was developed to demonstrate backend development, database integration, authentication, REST API design, and product management using Spring Boot.

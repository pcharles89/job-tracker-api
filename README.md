# Job Tracker API

A RESTful Spring Boot application for tracking job applications throughout the job search process.

This project demonstrates modern backend development using Spring Boot, Spring Security with JWT authentication, REST APIs, Spring Data JPA, MySQL, Flyway database migrations, Docker, automated testing, and containerized deployment.

## Features

- Create job applications
- View all applications with pagination
- Get application by ID
- Update applications
- Partially update applications
- Delete applications
- Search applications by company, location, and status using Spring Data JPA specifications
- Request validation
- Global exception handling
- Integration, controller, and service tests
- JWT Authentication (Register/Login)
- BCrypt Password Hashing
- User authorization (users can only access their own applications)
- Docker support
- Containerized deployment
- Swagger/OpenAPI documentation


## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- H2 Database (testing)
- Flyway
- MapStruct
- Lombok
- JUnit 5
- Mockito
- MockMvc
- Swagger / OpenAPI
- Spring Security
- JWT
- Docker
- Docker Compose

## Architecture

```text
HTTP Request
      ↓
Spring Security Filter Chain
      ↓
JWT Authentication Filter
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
MySQL
```
## Project Structure

```text
src
└── main
    ├── java
    │   └── com.paul.jobtrackerapi
    │       ├── config
    │       ├── controllers
    │       ├── dtos
    │       ├── entities
    │       ├── exceptions
    │       ├── mappers
    │       ├── repositories
    │       ├── security
    │       ├── services
    │       ├── specifications
    │       └── JobTrackerApiApplication.java
    └── resources
        ├── db
        │   └── migration
        └── application.yml
```

### Layer Responsibilities

- **Controller** - Handles HTTP requests and responses
- **Service** - Contains business logic
- **Repository** - Provides database access through Spring Data JPA
- **Database** - Stores job application information

## API Endpoints

| Method | Endpoint | Description |
|----------|----------|----------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Authenticate a user and return a JWT |

### Job Applications

| Method | Endpoint | Description |
|----------|----------|----------|
| POST | `/applications` | Create a job application |
| GET | `/applications` | Get all applications for the authenticated user |
| GET | `/applications/{id}` | Get an application by ID |
| PUT | `/applications/{id}` | Fully update an application |
| PATCH | `/applications/{id}` | Partially update an application |
| DELETE | `/applications/{id}` | Delete an application |
| GET | `/applications/search` | Search applications by company, location, and status |


## Authentication

This API uses JWT-based authentication.

Users must register or log in to receive a JWT token.

```http
POST /auth/register
POST /auth/login
```

Protected endpoints require the JWT to be included in the `Authorization` header.

```http
Authorization: Bearer <your-jwt-token>
```

Each user can only access their own job applications.


## Swagger UI

Interactive API documentation is available through Swagger/OpenAPI.

Access Swagger at:

```text
http://localhost:8080/swagger-ui.html
```

### API Overview

![Swagger Overview](images/swagger-ui1.png)

### Endpoint Details

![Swagger Endpoint Details](images/swagger-ui2.png)

## Search Examples

Search by company:

```http
GET /applications/search?companyName=Amazon
```

Search by location:

```http
GET /applications/search?location=New York
```

Search by status:

```http
GET /applications/search?status=APPLIED
```

## Example Create Request

```json
{
  "companyName": "Amazon",
  "jobTitle": "Backend Developer",
  "location": "New York"
}
```

## Example Response

```json
{
  "id": 1,
  "companyName": "Amazon",
  "jobTitle": "Backend Developer",
  "location": "New York",
  "status": "APPLIED"
}
```
## Validation Example

If invalid data is submitted, the API returns a structured error response.

```json
{
  "message": "Validation failed",
  "status": 400,
  "errors": {
    "companyName": "Company name is required"
  }
}
```

## Testing

The project contains 51 automated tests covering:

- Service tests
- Controller tests
- Integration tests

Test coverage includes:

- CRUD operations
- Authentication
- Authorization
- Validation
- Exception handling
- Search functionality
- Pagination
- Sorting

Integration tests run against an H2 in-memory database using a dedicated test profile.

## Running the Application

Clone the repository:

```bash
git clone <your-repository-url>
```

Navigate to the project directory:

```bash
cd job-tracker-api
```

Build and start the application:

```bash
docker compose up --build
```

This command starts both the Spring Boot application and a MySQL database.

Once the containers are running, the API is available at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```
## Future Improvements

- Refresh token support for JWT authentication
- Role-based authorization (Admin/User roles)
- Email notifications for application status updates
- Resume and cover letter file uploads
- Job application analytics dashboard
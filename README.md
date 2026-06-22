# Job Tracker API

A RESTful Spring Boot application for tracking job applications throughout the job search process.

This project was built to practice Spring Boot development concepts including REST APIs, validation, exception handling, JPA, Specifications, pagination, and automated testing.

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

## Architecture

```text
HTTP Request
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

### Layer Responsibilities

- **Controller** - Handles HTTP requests and responses
- **Service** - Contains business logic
- **Repository** - Provides database access through Spring Data JPA
- **Database** - Stores job application information

## API Endpoints

| Method | Endpoint | Description |
|----------|----------|----------|
| POST | `/applications` | Create application |
| GET | `/applications` | Get all applications |
| GET | `/applications/{id}` | Get application by ID |
| PUT | `/applications/{id}` | Update application |
| PATCH | `/applications/{id}` | Partially update application |
| DELETE | `/applications/{id}` | Delete application |
| GET | `/applications/search` | Search applications |

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

The project includes:

- Service Tests
- Controller Tests
- Integration Tests

Test coverage includes:

- CRUD operations
- Validation scenarios
- Exception handling
- Search functionality
- Pagination

Integration tests run against an H2 in-memory database using a dedicated test profile.

## Running the Application

### Clone Repository

```bash
git clone <your-repository-url>
```

### Run MySQL

Create a database named:

```sql
CREATE DATABASE jobtracker;
```

### Start Application

```bash
./mvnw spring-boot:run
```

## Future Improvements

- JWT Authentication
- User Accounts
- Docker Support
- Job Application Analytics
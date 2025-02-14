# LoginController Documentation

## Overview

The `LoginController` class is a REST controller responsible for handling user authentication in a Spring Boot application. It provides a login endpoint that validates user credentials and returns an authentication token upon successful login.

## Class Structure

### LoginController

- **Annotations:**
  - `@RestController`
  - `@EnableAutoConfiguration`

- **Fields:**
  - `secret` (String): Application secret retrieved from configuration

- **Methods:**
  - `login(LoginRequest input)`: Handles user login

### LoginRequest

- **Fields:**
  - `username` (String)
  - `password` (String)

### LoginResponse

- **Fields:**
  - `token` (String)

- **Constructor:**
  - `LoginResponse(String msg)`

### Unauthorized

- **Annotation:**
  - `@ResponseStatus(HttpStatus.UNAUTHORIZED)`

- **Constructor:**
  - `Unauthorized(String exception)`

## Endpoints

| Endpoint | Method | Consumes | Produces | Description |
|----------|--------|----------|----------|-------------|
| `/login` | POST   | application/json | application/json | Authenticates user and returns a token |

## Authentication Flow

1. Receives login request with username and password
2. Fetches user data based on the provided username
3. Hashes the provided password using MD5
4. Compares the hashed password with the stored hashed password
5. If passwords match, generates and returns a token
6. If passwords don't match, throws an `Unauthorized` exception

## Security Considerations

- Uses cross-origin resource sharing (CORS) with `@CrossOrigin(origins = "*")`
- Passwords are hashed using MD5 before comparison
- Authentication failures result in an `Unauthorized` exception

## Dependencies

- Spring Boot
- Spring Web
- PostgreSQL (implied by `Postgres.md5()` method call)

## Insights

- The controller uses MD5 for password hashing, which is considered weak for cryptographic purposes
- The application secret is injected using `@Value` annotation, suggesting the use of external configuration
- The login endpoint allows requests from any origin due to the CORS configuration
- The `User` class is not provided but is used to fetch user data and generate tokens
- Error handling is implemented using a custom `Unauthorized` exception

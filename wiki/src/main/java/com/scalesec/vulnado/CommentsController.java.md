# CommentsController

## Overview
The `CommentsController` is a Spring Boot REST controller that manages comments in a web application. It provides endpoints for fetching, creating, and deleting comments.

## Dependencies
- Spring Boot
- Spring Web

## Configuration
- Uses `@EnableAutoConfiguration` for automatic configuration
- Utilizes `@Value("${app.secret}")` to inject a secret value from application properties

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /comments | Fetch all comments |
| POST | /comments | Create a new comment |
| DELETE | /comments/{id} | Delete a specific comment |

### GET /comments
- Requires `x-auth-token` header for authentication
- Returns a list of all comments
- Authentication is performed using `User.assertAuth(secret, token)`

### POST /comments
- Requires `x-auth-token` header for authentication
- Accepts JSON payload with `username` and `body`
- Creates a new comment

### DELETE /comments/{id}
- Requires `x-auth-token` header for authentication
- Deletes the comment with the specified `id`

## Cross-Origin Resource Sharing (CORS)
All endpoints allow CORS from any origin (`@CrossOrigin(origins = "*")`)

## Data Structures

### CommentRequest
A serializable class representing the request body for creating a comment:
- `username`: String
- `body`: String

## Error Handling

### BadRequest
- Extends `RuntimeException`
- Maps to HTTP status 400 (Bad Request)

### ServerError
- Extends `RuntimeException`
- Maps to HTTP status 500 (Internal Server Error)

## Insights
- The controller uses token-based authentication for GET and DELETE operations, but not for POST operations, which could be a security concern.
- CORS is configured to allow requests from any origin, which may pose security risks in production environments.
- The `Comment` class is not provided in this file but is used for fetching, creating, and deleting comments.
- Error handling is implemented using custom exception classes with specific HTTP status codes.

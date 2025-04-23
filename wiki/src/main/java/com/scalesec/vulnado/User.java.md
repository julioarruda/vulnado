# User.java: User Authentication and Database Access Manager

## Overview
User.java manages user authentication, JWT token generation/validation, and database operations for retrieving user information from a PostgreSQL database.

## Process Flow
```mermaid
flowchart TD
    Start("User Authentication Flow") --> FetchUser["fetch(username)"]
    FetchUser --> QueryDB["Execute SQL query to get user"]
    QueryDB --> UserExists{"User Found?"}
    UserExists -->|"Yes"| CreateUser["Create User object"]
    UserExists -->|"No"| ReturnNull["Return null"]
    CreateUser --> ReturnUser["Return User object"]
    
    TokenGen["token(secret)"] --> CreateKey["Create HMAC key from secret"]
    CreateKey --> BuildJWT["Build JWT with username"]
    BuildJWT --> ReturnToken["Return JWT token"]
    
    AuthCheck["assertAuth(secret, token)"] --> ParseKey["Create key from secret"]
    ParseKey --> ValidateToken["Parse and validate JWT"]
    ValidateToken --> TokenValid{"Token Valid?"}
    TokenValid -->|"Yes"| AuthSuccess["Authentication Success"]
    TokenValid -->|"No"| ThrowUnauthorized["Throw Unauthorized Exception"]
```

## Insights
- The class handles three main responsibilities: user data storage, JWT token generation, and token validation
- User authentication is performed using JWT (JSON Web Token) with HMAC-SHA signing
- The `fetch` method retrieves user information from a PostgreSQL database
- User passwords are stored in hashed format as indicated by the `hashedPassword` field

## Dependencies
```mermaid
flowchart LR
    User.java --- |"Uses"| io_jsonwebtoken
    User.java --- |"Reads"| Postgres
    User.java --- |"Throws"| Unauthorized
    User.java --- |"Uses"| java_sql
```

- `io.jsonwebtoken` : Used for JWT token generation and validation
- `Postgres` : Used to establish database connections via the `Postgres.connection()` method
- `Unauthorized` : Custom exception class thrown when authentication fails
- `java.sql` : Used for database operations (Connection, Statement, ResultSet)

## Vulnerabilities
1. **SQL Injection**: The `fetch` method constructs SQL queries by directly concatenating user input (`un` parameter) without sanitization or prepared statements, making it vulnerable to SQL injection attacks.

2. **Weak Key Management**: The JWT secret key is passed as a string parameter and converted to bytes without proper key derivation or secure storage mechanisms.

3. **Insecure Error Handling**: Exception stack traces are printed to standard output, potentially exposing sensitive information about the application structure and database.

4. **Malformed SQL Query**: The query includes an incomplete "SELECT TOP" statement at the end, which could cause SQL syntax errors or unexpected behavior.

5. **No Connection Pooling**: Database connections are opened and closed for each request without proper connection pooling, which could lead to resource exhaustion under high load.

6. **No Password Verification**: While passwords are stored in hashed format, there's no method to verify passwords during authentication.

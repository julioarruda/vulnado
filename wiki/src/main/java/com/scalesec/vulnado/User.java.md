# User.java: User Authentication and JWT Management

## Overview

This class represents a user entity with authentication capabilities, including JWT token generation and validation. It also provides a method to fetch user data from a PostgreSQL database.

## Process Flow

```mermaid
flowchart TD
    A("User Instance Created") --> B["token(secret) called"]
    B --> C["Generate SecretKey from secret"]
    C --> D["Build JWT with username as subject"]
    D --> E["Return JWT token"]

    F["assertAuth(secret, token) called"] --> G["Generate SecretKey from secret"]
    G --> H["Parse and validate JWT token"]
    H --> I{"Exception?"}
    I -- "Yes" --> J["Print stack trace and throw Unauthorized"]
    I -- "No" --> K("Validation Successful")

    L["fetch(un) called"] --> M["Get DB connection from Postgres"]
    M --> N["Create SQL query with username"]
    N --> O["Execute query"]
    O --> P{"User found?"}
    P -- "Yes" --> Q["(Incomplete) Extract user data"]
    P -- "No" --> R("Return null")
```

## Insights

- The class combines user data representation with authentication logic.
- JWT tokens are generated using the username as the subject and a provided secret.
- Token validation throws a custom `Unauthorized` exception on failure.
- The `fetch` method is incomplete and vulnerable to SQL injection.
- The class depends on an external `Postgres` class for database connections and a custom `Unauthorized` exception.

## Vulnerabilities

- **SQL Injection**: The `fetch` method concatenates user input directly into the SQL query, making it vulnerable to SQL injection attacks.
- **Incomplete Implementation**: The `fetch` method is incomplete and does not properly extract user data from the `ResultSet`.
- **Exception Handling**: Catching generic `Exception` can obscure the root cause of errors and is not a best practice.
- **Hardcoded Algorithm**: The JWT generation uses a default algorithm without explicit specification, which may lead to future compatibility issues.

## Dependencies

```mermaid
flowchart LR
    User --- |"Uses"| Postgres
    User --- |"Uses"| Unauthorized
    User --- |"Imports"| io_jsonwebtoken_Jwts
    User --- |"Imports"| io_jsonwebtoken_JwtParser
    User --- |"Imports"| io_jsonwebtoken_SignatureAlgorithm
    User --- |"Imports"| io_jsonwebtoken_security_Keys
    User --- |"Imports"| javax_crypto_SecretKey
```

- `Postgres` : Provides the `connection()` method for obtaining a database connection. Nature: Uses.
- `Unauthorized` : Custom exception thrown when authentication fails. Nature: Uses.
- `io.jsonwebtoken.Jwts`, `io.jsonwebtoken.JwtParser`, `io.jsonwebtoken.SignatureAlgorithm`, `io.jsonwebtoken.security.Keys`, `javax.crypto.SecretKey` : Used for JWT creation and validation. Nature: Imports.

## Data Manipulation (SQL)

| Attribute      | Type   | Description                |
|----------------|--------|----------------------------|
| id             | String | User identifier            |
| username       | String | Username                   |
| hashedPassword | String | Hashed user password       |

- `users`: SELECT operation to fetch user data by username. The query is vulnerable to SQL injection due to direct string concatenation.

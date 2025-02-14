# User Class Documentation

## Overview

The `User` class represents a user entity in the system, providing functionality for user authentication, token generation, and database operations.

## Class Structure

```markdown
| Field           | Type   | Description                     |
|-----------------|--------|---------------------------------|
| id              | String | Unique identifier for the user  |
| username        | String | User's username                 |
| hashedPassword  | String | User's hashed password          |
```

## Constructor

```markdown
User(String id, String username, String hashedPassword)
```

Creates a new User instance with the specified id, username, and hashed password.

## Methods

### token

```markdown
public String token(String secret)
```

Generates a JSON Web Token (JWT) for the user.

**Parameters:**
- `secret`: A string used as the secret key for token generation

**Returns:**
- A compact JWT string

### assertAuth

```markdown
public static void assertAuth(String secret, String token)
```

Verifies the authenticity of a given token.

**Parameters:**
- `secret`: The secret key used for token verification
- `token`: The JWT to be verified

**Throws:**
- `Unauthorized`: If the token is invalid or verification fails

### fetch

```markdown
public static User fetch(String un)
```

Retrieves a user from the database based on the provided username.

**Parameters:**
- `un`: The username to search for

**Returns:**
- A `User` object if found, or `null` if not found

## Database Interaction

The `fetch` method interacts with a PostgreSQL database to retrieve user information. It uses a direct SQL query to fetch user details based on the provided username.

## Security Considerations

1. The class uses JWT for token generation and verification.
2. Passwords are stored in hashed form.
3. The SQL query in the `fetch` method is vulnerable to SQL injection attacks.

## Dependencies

- `java.sql`: For database operations
- `io.jsonwebtoken`: For JWT operations
- `javax.crypto`: For cryptographic operations

## Insights

1. The class combines authentication, token management, and data access responsibilities, which may violate the Single Responsibility Principle.
2. Error handling in the `fetch` method could be improved to provide more specific exceptions.
3. The use of a static `fetch` method suggests that the class is being used as both an entity and a data access object.
4. The direct concatenation of user input in SQL queries poses a significant security risk.
5. The token generation method uses HMAC-SHA for signing, which is generally considered secure for JWT operations.

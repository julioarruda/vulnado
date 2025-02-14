# Comment Class Documentation

## Overview

The `Comment` class represents a comment entity in a Java application, providing functionality for creating, fetching, and deleting comments from a PostgreSQL database.

## Class Structure

```markdown
public class Comment
├── Properties
│   ├── id: String
│   ├── username: String
│   ├── body: String
│   └── created_on: Timestamp
├── Constructor
└── Methods
    ├── create(String username, String body): Comment
    ├── fetch_all(): List<Comment>
    ├── delete(String id): Boolean
    └── commit(): Boolean
```

## Properties

| Property    | Type      | Description                           |
|-------------|-----------|---------------------------------------|
| id          | String    | Unique identifier for the comment     |
| username    | String    | Username of the comment author        |
| body        | String    | Content of the comment                |
| created_on  | Timestamp | Timestamp when the comment was created|

## Methods

### create(String username, String body)

Creates a new comment with the given username and body.

- **Parameters:**
  - username: String
  - body: String
- **Returns:** Comment
- **Throws:**
  - BadRequest: If unable to save the comment
  - ServerError: For any other exceptions

### fetch_all()

Retrieves all comments from the database.

- **Returns:** List<Comment>

### delete(String id)

Deletes a comment with the specified id from the database.

- **Parameters:**
  - id: String
- **Returns:** Boolean

### commit()

Inserts the current comment instance into the database.

- **Returns:** Boolean
- **Throws:** SQLException

## Insights

1. The class uses UUID for generating unique comment IDs.
2. Database operations are performed using JDBC with prepared statements for improved security.
3. Error handling is implemented, but some methods may silently fail (e.g., delete method).
4. The fetch_all method doesn't use prepared statements, which could be a potential security risk.
5. The class relies on a separate Postgres class for database connections.

# Postgres Database Utility

## Overview

This Java class, `Postgres`, provides utility methods for managing a PostgreSQL database connection and performing initial setup operations. It includes functionality for creating database tables, inserting seed data, and handling user authentication.

## Key Components

1. **Database Connection**
   - Method: `connection()`
   - Establishes a connection to the PostgreSQL database using environment variables for configuration.

2. **Database Setup**
   - Method: `setup()`
   - Creates tables for users and comments if they don't exist.
   - Clears existing data and inserts seed data for users and comments.

3. **MD5 Hashing**
   - Method: `md5(String input)`
   - Generates an MD5 hash for the given input string.

4. **User Insertion**
   - Method: `insertUser(String username, String password)`
   - Inserts a new user into the database with a hashed password.

5. **Comment Insertion**
   - Method: `insertComment(String username, String body)`
   - Inserts a new comment into the database.

## Database Schema

### Users Table

| Column     | Type         | Constraints           |
|------------|--------------|------------------------|
| user_id    | VARCHAR(36)  | PRIMARY KEY           |
| username   | VARCHAR(50)  | UNIQUE, NOT NULL      |
| password   | VARCHAR(50)  | NOT NULL              |
| created_on | TIMESTAMP    | NOT NULL              |
| last_login | TIMESTAMP    |                       |

### Comments Table

| Column     | Type         | Constraints           |
|------------|--------------|------------------------|
| id         | VARCHAR(36)  | PRIMARY KEY           |
| username   | VARCHAR(36)  |                       |
| body       | VARCHAR(500) |                       |
| created_on | TIMESTAMP    | NOT NULL              |

## Usage

1. Ensure environment variables (PGHOST, PGDATABASE, PGUSER, PGPASSWORD) are set for database connection.
2. Call `Postgres.setup()` to initialize the database schema and insert seed data.
3. Use `Postgres.connection()` to obtain a database connection for custom operations.

## Insights

- The class uses environment variables for database configuration, enhancing security and flexibility.
- MD5 hashing is used for password storage, which is not considered secure for modern applications. A more robust hashing algorithm should be implemented.
- The `setup()` method recreates the database schema and inserts seed data on each execution, which may not be suitable for production environments.
- Error handling is present but could be improved with more specific exception handling and logging.
- The use of prepared statements in `insertUser()` and `insertComment()` helps prevent SQL injection attacks.

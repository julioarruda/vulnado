# Postgres.java

## Program Description

This Java program establishes a connection to a PostgreSQL database and sets up the database schema. It includes functionalities for creating tables, inserting seed data, and hashing passwords using MD5.

## Methods

| Method Name | Description |
|---|---|
| `connection()` | Establishes a connection to the PostgreSQL database using environment variables (`PGHOST`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`) for connection parameters. |
| `setup()` | Sets up the database schema by creating tables for users and comments. It also inserts seed data into these tables. The `users` table stores user information including a unique user ID, username, password, creation timestamp, and last login timestamp. The `comments` table stores comment data including a unique comment ID, username of the commenter, comment body, and creation timestamp. |
| `md5(String input)` |  Calculates the MD5 hash value of a given string. This method is used for hashing passwords before storing them in the database. |
| `insertUser(String username, String password)` | Inserts a new user into the `users` table with the provided username and hashed password. A unique user ID is generated using `UUID.randomUUID()`. |
| `insertComment(String username, String body)` | Inserts a new comment into the `comments` table with the provided username and comment body. A unique comment ID is generated using `UUID.randomUUID()`. |

## Insights

- The program relies on environment variables for database connection parameters, promoting portability and security by avoiding hardcoded credentials.
- Passwords are hashed using MD5 before storage, enhancing security. However, it's important to note that MD5 is considered a weak hashing algorithm and more robust alternatives like bcrypt or Argon2 are recommended for password hashing in production environments.
- The program uses prepared statements for database operations, which helps prevent SQL injection vulnerabilities.
- The use of UUIDs for user and comment IDs ensures uniqueness and can be beneficial in distributed systems. 


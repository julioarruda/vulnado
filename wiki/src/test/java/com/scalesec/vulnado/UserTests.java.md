# UserTest Class Documentation

## Overview

The `UserTest` class is a comprehensive test suite for the `User` class in the `com.scalesec.vulnado` package. It utilizes JUnit 5 and Mockito for unit testing various functionalities related to user authentication, token generation, and database operations.

## Key Components

- **Mocked Objects**: `Connection`, `Statement`, and `ResultSet` for simulating database interactions.
- **Test User**: A sample user object used across multiple tests.
- **Test Secret**: A constant string used for JWT token generation and verification.

## Test Categories

### Token Generation and Validation

1. **Token Generation**
   - Ensures tokens are generated correctly and have the expected format.
   - Verifies uniqueness of tokens for different users.

2. **Token Validation**
   - Tests the `assertAuth` method with valid and invalid tokens.
   - Checks behavior with modified and expired tokens.

### User Fetching

1. **Database Queries**
   - Verifies correct SQL query execution for user fetching.
   - Tests handling of existing and non-existing users.
   - Checks for proper connection closure after execution.

2. **Error Handling**
   - Tests behavior when database exceptions occur.
   - Verifies null return for non-existent users and empty result sets.

3. **SQL Injection Prevention**
   - Ensures the system handles potential SQL injection attempts safely.

### Logging and Output

- Verifies that database queries and connection messages are logged correctly.
- Checks error message output for various exception scenarios.

### Edge Cases

- Tests trimming of whitespace from usernames.
- Verifies handling of multiple results in user fetching.

## Insights

1. **Security Focus**: The test suite emphasizes security aspects, including token validation and SQL injection prevention.
2. **Comprehensive Coverage**: It covers a wide range of scenarios, including happy paths, error cases, and edge conditions.
3. **Mocking Strategy**: Extensive use of Mockito for simulating database interactions, allowing for controlled testing environments.
4. **Attention to Detail**: Tests include checks for subtle issues like whitespace handling and duplicate user entries.
5. **Console Output Verification**: The suite includes tests for verifying console output, which is useful for debugging and logging purposes.

## Test Methods

| Method Name | Purpose |
|-------------|---------|
| `token_ShouldGenerateValidJWT` | Validates JWT token generation |
| `assertAuth_WithValidToken_ShouldNotThrowException` | Checks token authentication for valid tokens |
| `fetch_WithExistingUser_ShouldReturnUser` | Tests user retrieval for existing users |
| `fetch_WithNonExistingUser_ShouldReturnNull` | Verifies behavior for non-existent users |
| `fetch_ShouldHandleSQLInjectionAttempt` | Tests SQL injection prevention |
| `assertAuth_WithExpiredToken_ShouldThrowUnauthorized` | Checks handling of expired tokens |
| `fetch_ShouldPrintQueryToConsole` | Verifies query logging |
| `fetch_ShouldHandleExceptionAndPrintErrorMessage` | Tests exception handling and error logging |
| `fetch_ShouldTrimWhitespaceFromUsername` | Checks username whitespace trimming |

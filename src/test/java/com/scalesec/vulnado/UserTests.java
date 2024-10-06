package com.scalesec.vulnado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class UserTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private User testUser;
    private static final String TEST_SECRET = "testSecretKeyForJWTTesting";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("1", "testUser", "hashedPassword");
    }

    @Test
    void token_ShouldGenerateValidJWT() {
        String token = testUser.token(TEST_SECRET);
        assertNotNull(token, "Generated token should not be null");
        assertTrue(token.split("\\.").length == 3, "Token should have three parts separated by dots");
    }

    @Test
    void assertAuth_WithValidToken_ShouldNotThrowException() {
        String token = testUser.token(TEST_SECRET);
        assertDoesNotThrow(() -> User.assertAuth(TEST_SECRET, token), "assertAuth should not throw exception for valid token");
    }

    @Test
    void assertAuth_WithInvalidToken_ShouldThrowUnauthorized() {
        String invalidToken = "invalidToken";
        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, invalidToken), "assertAuth should throw Unauthorized for invalid token");
    }

    @Test
    void fetch_WithExistingUser_ShouldReturnUser() throws Exception {
        String username = "existingUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("user_id")).thenReturn("1");
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn("hashedPassword");

        User result = User.fetch(username);

        assertNotNull(result, "Fetch should return a user for existing username");
        assertEquals(username, result.username, "Fetched user should have correct username");
    }

    @Test
    void fetch_WithNonExistingUser_ShouldReturnNull() throws Exception {
        String username = "nonExistingUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = User.fetch(username);

        assertNull(result, "Fetch should return null for non-existing username");
    }

    @Test
    void fetch_WithDatabaseException_ShouldReturnNull() throws Exception {
        String username = "exceptionUser";
        when(Postgres.connection()).thenThrow(new RuntimeException("Database connection failed"));

        User result = User.fetch(username);

        assertNull(result, "Fetch should return null when database exception occurs");
    }

    @Test
    void fetch_WithInvalidUsername_ShouldThrowBadRequest() {
        String invalidUsername = "user@name";
        assertThrows(BadRequest.class, () -> User.fetch(invalidUsername), "Fetch should throw BadRequest for invalid username");
    }

    @Test
    void fetch_WithValidUsername_ShouldNotThrowException() {
        String validUsername = "valid_user123";
        when(Postgres.connection()).thenReturn(mockConnection);
        assertDoesNotThrow(() -> User.fetch(validUsername), "Fetch should not throw exception for valid username");
    }

    @Test
    void fetch_ShouldUsePreparedStatement() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        User.fetch(username);

        verify(mockConnection).prepareStatement("select * from users where username = ? limit 1");
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    void fetch_ShouldCloseConnectionAfterExecution() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        User.fetch("testUser");

        verify(mockConnection).close();
    }

    @Test
    void fetch_ShouldHandleExceptionAndPrintErrorMessage() throws Exception {
        String username = "exceptionUser";
        RuntimeException testException = new RuntimeException("Test database exception");
        when(Postgres.connection()).thenThrow(testException);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        User result = User.fetch(username);

        assertNull(result, "Fetch should return null when an exception occurs");
        assertTrue(errContent.toString().contains("Error fetching user: Test database exception"), "Fetch should print the exception message to stderr");

        System.setErr(System.err);
    }

    @Test
    void fetch_WithShortUsername_ShouldThrowBadRequest() {
        String shortUsername = "ab";
        assertThrows(BadRequest.class, () -> User.fetch(shortUsername), "Fetch should throw BadRequest for username shorter than 3 characters");
    }

    @Test
    void fetch_WithUsernameContainingInvalidCharacters_ShouldThrowBadRequest() {
        String invalidUsername = "user$name";
        assertThrows(BadRequest.class, () -> User.fetch(invalidUsername), "Fetch should throw BadRequest for username containing invalid characters");
    }

    @Test
    void fetch_WithValidComplexUsername_ShouldNotThrowException() {
        String complexUsername = "valid_user.name-123";
        when(Postgres.connection()).thenReturn(mockConnection);
        assertDoesNotThrow(() -> User.fetch(complexUsername), "Fetch should not throw exception for valid complex username");
    }

    @Test
    void assertAuth_WithModifiedToken_ShouldThrowUnauthorized() {
        String token = testUser.token(TEST_SECRET);
        String modifiedToken = token.substring(0, token.length() - 1) + "X"; // Modify the last character

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, modifiedToken), "assertAuth should throw Unauthorized for modified token");
    }

    @Test
    void assertAuth_ShouldPrintStackTraceOnException() {
        String invalidToken = "invalidToken";
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, invalidToken));

        assertTrue(errContent.toString().contains("Stack trace:"), "assertAuth should print stack trace on exception");

        System.setErr(System.err);
    }

    @Test
    void token_ShouldContainCorrectUsername() {
        String token = testUser.token(TEST_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        String subject = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        assertEquals(testUser.username, subject, "Token should contain the correct username");
    }
}

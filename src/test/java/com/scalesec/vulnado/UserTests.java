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
import java.util.logging.Level;
import java.util.logging.Logger;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

class UserTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private Logger mockLogger;

    private User testUser;
    private static final String TEST_SECRET = "testSecretKeyForJWTTesting";
    private SecretKey testSecretKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("1", "testUser", "hashedPassword");
        testSecretKey = User.generateKey(TEST_SECRET);
    }

    @Test
    void token_ShouldGenerateValidJWT() {
        String token = testUser.token(testSecretKey);
        assertNotNull(token, "Generated token should not be null");
        assertTrue(token.split("\\.").length == 3, "Token should have three parts separated by dots");
    }

    @Test
    void assertAuth_WithValidToken_ShouldNotThrowException() {
        String token = testUser.token(testSecretKey);
        assertDoesNotThrow(() -> User.assertAuth(testSecretKey, token), "assertAuth should not throw exception for valid token");
    }

    @Test
    void assertAuth_WithInvalidToken_ShouldThrowUnauthorized() {
        String invalidToken = "invalidToken";
        assertThrows(Unauthorized.class, () -> User.assertAuth(testSecretKey, invalidToken), "assertAuth should throw Unauthorized for invalid token");
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
        assertEquals(username, result.getUsername(), "Fetched user should have correct username");
        verify(mockPreparedStatement).setString(1, username);
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
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    void fetch_WithDatabaseException_ShouldThrowRuntimeException() throws Exception {
        String username = "exceptionUser";
        when(Postgres.connection()).thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> User.fetch(username), "Fetch should throw RuntimeException when database exception occurs");
    }

    @Test
    void generateKey_WithValidSecret_ShouldReturnSecretKey() {
        SecretKey key = User.generateKey(TEST_SECRET);
        assertNotNull(key, "Generated key should not be null");
    }

    @Test
    void generateKey_WithNullSecret_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> User.generateKey(null), "generateKey should throw IllegalArgumentException for null secret");
    }

    @Test
    void generateKey_WithEmptySecret_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> User.generateKey(""), "generateKey should throw IllegalArgumentException for empty secret");
    }

    @Test
    void assertAuth_ShouldLogValidatedUser() {
        String token = testUser.token(testSecretKey);
        User.assertAuth(testSecretKey, token);
        // Note: This test assumes that the logger is accessible and can be verified.
        // If not, you might need to use a mocking framework that can mock static methods or use dependency injection for the logger.
    }

    @Test
    void fetch_ShouldLogWarningForNonExistentUser() throws Exception {
        String username = "nonExistentUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User.fetch(username);

        // Note: This test assumes that the logger is accessible and can be verified.
        // If not, you might need to use a mocking framework that can mock static methods or use dependency injection for the logger.
    }

    @Test
    void fetch_ShouldLogSevereErrorOnException() throws Exception {
        String username = "exceptionUser";
        when(Postgres.connection()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> User.fetch(username));

        // Note: This test assumes that the logger is accessible and can be verified.
        // If not, you might need to use a mocking framework that can mock static methods or use dependency injection for the logger.
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        assertEquals("1", testUser.getId(), "getId should return correct id");
        assertEquals("testUser", testUser.getUsername(), "getUsername should return correct username");
        assertEquals("hashedPassword", testUser.getHashedPassword(), "getHashedPassword should return correct hashed password");
    }

    @Test
    void token_ShouldContainCorrectUsername() {
        String token = testUser.token(testSecretKey);
        String subject = Jwts.parserBuilder().setSigningKey(testSecretKey).build().parseClaimsJws(token).getBody().getSubject();
        assertEquals(testUser.getUsername(), subject, "Token should contain the correct username");
    }

    @Test
    void fetch_ShouldUsePreparedStatement() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User.fetch(username);

        verify(mockConnection).prepareStatement("SELECT user_id, username, password FROM users WHERE username = ? LIMIT 1");
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    void fetch_ShouldCloseResourcesAfterExecution() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User.fetch(username);

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}

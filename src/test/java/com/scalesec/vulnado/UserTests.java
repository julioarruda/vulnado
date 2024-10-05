package com.scalesec.vulnado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

class UserTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
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
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
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
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
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
    void token_ShouldGenerateUniqueTokensForDifferentUsers() {
        User user1 = new User("1", "user1", "password1");
        User user2 = new User("2", "user2", "password2");

        String token1 = user1.token(TEST_SECRET);
        String token2 = user2.token(TEST_SECRET);

        assertNotEquals(token1, token2, "Tokens for different users should be unique");
    }

    @Test
    void assertAuth_WithModifiedToken_ShouldThrowUnauthorized() {
        String token = testUser.token(TEST_SECRET);
        String modifiedToken = token.substring(0, token.length() - 1) + "X"; // Modify the last character

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, modifiedToken), "assertAuth should throw Unauthorized for modified token");
    }

    @Test
    void fetch_ShouldExecuteCorrectSQLQuery() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        User.fetch(username);

        verify(mockStatement).executeQuery("select * from users where username = '" + username + "' limit 1");
    }

    @Test
    void fetch_ShouldCloseConnectionAfterExecution() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        User.fetch("testUser");

        verify(mockConnection).close();
    }
}

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
    void getId_ShouldReturnCorrectId() {
        assertEquals("1", testUser.getId(), "getId should return the correct id");
    }

    @Test
    void getUsername_ShouldReturnCorrectUsername() {
        assertEquals("testUser", testUser.getUsername(), "getUsername should return the correct username");
    }

    @Test
    void getHashedPassword_ShouldReturnCorrectHashedPassword() {
        assertEquals("hashedPassword", testUser.getHashedPassword(), "getHashedPassword should return the correct hashed password");
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
        assertNotNull(key, "generateKey should return a non-null SecretKey for valid secret");
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
        // Replace the static logger with a mock
        Logger originalLogger = TestUtils.setStaticField(User.class, "logger", mockLogger);
        
        String token = testUser.token(testSecretKey);
        User.assertAuth(testSecretKey, token);

        verify(mockLogger).info(contains("Token validated for user: testUser"));

        // Restore the original logger
        TestUtils.setStaticField(User.class, "logger", originalLogger);
    }

    @Test
    void fetch_ShouldLogWarningForNonExistentUser() throws Exception {
        // Replace the static logger with a mock
        Logger originalLogger = TestUtils.setStaticField(User.class, "logger", mockLogger);

        String username = "nonExistentUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User.fetch(username);

        verify(mockLogger).warning("User not found: " + username);

        // Restore the original logger
        TestUtils.setStaticField(User.class, "logger", originalLogger);
    }

    @Test
    void fetch_ShouldLogSevereErrorOnException() throws Exception {
        // Replace the static logger with a mock
        Logger originalLogger = TestUtils.setStaticField(User.class, "logger", mockLogger);

        String username = "exceptionUser";
        RuntimeException testException = new RuntimeException("Test database exception");
        when(Postgres.connection()).thenThrow(testException);

        assertThrows(RuntimeException.class, () -> User.fetch(username));

        verify(mockLogger).log(eq(Level.SEVERE), eq("Error fetching user from database"), eq(testException));

        // Restore the original logger
        TestUtils.setStaticField(User.class, "logger", originalLogger);
    }
}

// Utility class for setting private static fields in tests
class TestUtils {
    public static <T> T setStaticField(Class<?> targetClass, String fieldName, T newValue) {
        try {
            java.lang.reflect.Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            T oldValue = (T) field.get(null);
            field.set(null, newValue);
            return oldValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

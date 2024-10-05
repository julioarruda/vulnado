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
import java.sql.SQLException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final String TEST_SECRET = "testSecretKeyForJWTTestingMustBe32Chars";

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
    void token_WithShortSecret_ShouldThrowIllegalArgumentException() {
        String shortSecret = "short";
        assertThrows(IllegalArgumentException.class, () -> testUser.token(shortSecret),
                "Token generation should throw IllegalArgumentException for short secret");
    }

    @Test
    void assertAuth_WithValidToken_ShouldNotThrowException() {
        String token = testUser.token(TEST_SECRET);
        assertDoesNotThrow(() -> User.assertAuth(TEST_SECRET, token),
                "assertAuth should not throw exception for valid token");
    }

    @Test
    void assertAuth_WithInvalidToken_ShouldThrowUnauthorized() {
        String invalidToken = "invalidToken";
        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, invalidToken),
                "assertAuth should throw Unauthorized for invalid token");
    }

    @Test
    void assertAuth_WithExpiredToken_ShouldThrowUnauthorized() {
        String expiredToken = Jwts.builder()
                .setSubject(testUser.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, expiredToken),
                "assertAuth should throw Unauthorized for expired token");
    }

    @Test
    void assertAuth_WithShortSecret_ShouldThrowIllegalArgumentException() {
        String shortSecret = "short";
        String token = testUser.token(TEST_SECRET);
        assertThrows(IllegalArgumentException.class, () -> User.assertAuth(shortSecret, token),
                "assertAuth should throw IllegalArgumentException for short secret");
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
    void fetch_WithDatabaseException_ShouldReturnNullAndLogError() throws Exception {
        String username = "exceptionUser";
        SQLException sqlException = new SQLException("Database error");
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenThrow(sqlException);

        // Replace the logger with a mock
        Logger originalLogger = User.class.getDeclaredField("LOGGER").get(null);
        User.class.getDeclaredField("LOGGER").set(null, mockLogger);

        User result = User.fetch(username);

        assertNull(result, "Fetch should return null when database exception occurs");
        verify(mockLogger).log(Level.SEVERE, "Database error while fetching user", sqlException);

        // Restore the original logger
        User.class.getDeclaredField("LOGGER").set(null, originalLogger);
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        assertEquals("1", testUser.getId(), "getId should return correct id");
        assertEquals("testUser", testUser.getUsername(), "getUsername should return correct username");
        assertEquals("hashedPassword", testUser.getHashedPassword(), "getHashedPassword should return correct hashed password");
    }

    @Test
    void token_ShouldGenerateTokenWithCorrectClaims() {
        String token = testUser.token(TEST_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        var claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        assertEquals(testUser.getUsername(), claims.getSubject(), "Token should contain correct username as subject");
        assertNotNull(claims.getExpiration(), "Token should have an expiration date");
        assertTrue(claims.getExpiration().after(new Date()), "Token expiration should be in the future");
    }

    @Test
    void fetch_ShouldUseCorrectSQLQuery() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        User.fetch("testUser");

        verify(mockConnection).prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1");
    }

    @Test
    void fetch_ShouldCloseResourcesAfterExecution() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        User.fetch("testUser");

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}

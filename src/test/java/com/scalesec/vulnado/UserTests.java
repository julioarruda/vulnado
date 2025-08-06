package com.scalesec.vulnado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("userid")).thenReturn("1");
            when(mockResultSet.getString("username")).thenReturn(username);
            when(mockResultSet.getString("password")).thenReturn("hashedPassword");

            User result = User.fetch(username);

            assertNotNull(result, "Fetch should return a user for existing username");
            assertEquals(username, result.username, "Fetched user should have correct username");
            assertEquals("1", result.id, "Fetched user should have correct id");
            assertEquals("hashedPassword", result.hashedPassword, "Fetched user should have correct password");
        }
    }

    @Test
    void fetch_WithNonExistingUser_ShouldReturnNull() throws Exception {
        String username = "nonExistingUser";
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            User result = User.fetch(username);

            assertNull(result, "Fetch should return null for non-existing username");
        }
    }

    @Test
    void fetch_WithDatabaseException_ShouldReturnNull() throws Exception {
        String username = "exceptionUser";
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenThrow(new RuntimeException("Database connection failed"));

            User result = User.fetch(username);

            assertNull(result, "Fetch should return null when database exception occurs");
        }
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
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            User.fetch(username);

            verify(mockConnection).prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1");
            verify(mockPreparedStatement).setString(1, username);
        }
    }

    @Test
    void fetch_ShouldHandleSQLException() throws Exception {
        String username = "exceptionUser";
        SQLException sqlException = new SQLException("Database error");
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenThrow(sqlException);

            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));

            User result = User.fetch(username);

            assertNull(result, "Fetch should return null when SQLException occurs");
            assertTrue(errContent.toString().contains("Database error"), "Should print SQLException message to stderr");

            System.setErr(System.err);
        }
    }

    @Test
    void assertAuth_WithExpiredToken_ShouldThrowUnauthorized() {
        // This test assumes that the token implementation includes an expiration time
        String expiredToken = Jwts.builder()
                .setSubject(testUser.username)
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000)) // Set expiration to 1 second ago
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, expiredToken), "assertAuth should throw Unauthorized for expired token");
    }

    @Test
    void assertAuth_ShouldPrintStackTraceOnException() {
        String invalidToken = "invalidToken";
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, invalidToken));

        assertTrue(errContent.toString().length() > 0, "assertAuth should print stack trace on exception");

        System.setErr(System.err);
    }

    @Test
    void token_ShouldContainCorrectUsername() {
        String token = testUser.token(TEST_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        String subject = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        assertEquals(testUser.username, subject, "Token should contain the correct username");
    }

    @Test
    void fetch_ShouldHandleMultipleResultsAndReturnFirstOne() throws Exception {
        String username = "duplicateUser";
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("userid")).thenReturn("1");
            when(mockResultSet.getString("username")).thenReturn(username);
            when(mockResultSet.getString("password")).thenReturn("password1");

            User result = User.fetch(username);

            assertNotNull(result, "Fetch should return a user when results exist");
            assertEquals("1", result.id, "Fetch should return the user with correct id");
        }
    }

    @Test
    void fetch_ShouldSetParameterCorrectly() throws Exception {
        String username = "parameterUser";
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            User.fetch(username);

            verify(mockPreparedStatement).setString(1, username);
        }
    }

    @Test
    void constructor_ShouldSetAllFields() {
        String id = "123";
        String username = "testUser";
        String hashedPassword = "hashedPass";

        User user = new User(id, username, hashedPassword);

        assertEquals(id, user.id, "Constructor should set id correctly");
        assertEquals(username, user.username, "Constructor should set username correctly");
        assertEquals(hashedPassword, user.hashedPassword, "Constructor should set hashedPassword correctly");
    }

    @Test
    void token_WithDifferentSecrets_ShouldGenerateDifferentTokens() {
        String secret1 = "secret1";
        String secret2 = "secret2";

        String token1 = testUser.token(secret1);
        String token2 = testUser.token(secret2);

        assertNotEquals(token1, token2, "Tokens generated with different secrets should be different");
    }

    @Test
    void assertAuth_WithNullToken_ShouldThrowUnauthorized() {
        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, null), "assertAuth should throw Unauthorized for null token");
    }

    @Test
    void assertAuth_WithEmptyToken_ShouldThrowUnauthorized() {
        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, ""), "assertAuth should throw Unauthorized for empty token");
    }

    @Test
    void fetch_WithNullUsername_ShouldHandleGracefully() throws Exception {
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            User result = User.fetch(null);

            assertNull(result, "Fetch should return null for null username");
            verify(mockPreparedStatement).setString(1, null);
        }
    }

    @Test
    void fetch_WithEmptyUsername_ShouldHandleGracefully() throws Exception {
        String emptyUsername = "";
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            User result = User.fetch(emptyUsername);

            assertNull(result, "Fetch should return null for empty username");
            verify(mockPreparedStatement).setString(1, emptyUsername);
        }
    }

    @Test
    void fetch_ShouldHandlePreparedStatementException() throws Exception {
        String username = "exceptionUser";
        SQLException sqlException = new SQLException("PreparedStatement error");
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenThrow(sqlException);

            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));

            User result = User.fetch(username);

            assertNull(result, "Fetch should return null when PreparedStatement creation fails");
            assertTrue(errContent.toString().contains("PreparedStatement error"), "Should print PreparedStatement exception message to stderr");

            System.setErr(System.err);
        }
    }

    @Test
    void fetch_ShouldPrintSQLExceptionClassName() throws Exception {
        String username = "exceptionUser";
        SQLException sqlException = new SQLException("Test SQL exception");
        
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenThrow(sqlException);

            ByteArrayOutputStream errContent = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errContent));

            User result = User.fetch(username);

            assertNull(result, "Fetch should return null when SQLException occurs");
            assertTrue(errContent.toString().contains("SQLException"), "Should print SQLException class name to stderr");

            System.setErr(System.err);
        }
    }

    @Test
    void token_WithNullSecret_ShouldThrowException() {
        assertThrows(Exception.class, () -> testUser.token(null), "Token generation should throw exception for null secret");
    }

    @Test
    void token_WithEmptySecret_ShouldThrowException() {
        assertThrows(Exception.class, () -> testUser.token(""), "Token generation should throw exception for empty secret");
    }

    @Test
    void assertAuth_WithNullSecret_ShouldThrowUnauthorized() {
        String token = testUser.token(TEST_SECRET);
        assertThrows(Unauthorized.class, () -> User.assertAuth(null, token), "assertAuth should throw Unauthorized for null secret");
    }

    @Test
    void assertAuth_WithEmptySecret_ShouldThrowUnauthorized() {

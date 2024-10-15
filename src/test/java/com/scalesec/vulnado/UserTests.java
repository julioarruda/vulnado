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
    void fetch_ShouldUsePreparedStatement() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        User.fetch(username);

        verify(mockConnection).prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1");
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
    void assertAuth_WithModifiedToken_ShouldThrowUnauthorized() {
        String token = testUser.token(TEST_SECRET);
        String modifiedToken = token.substring(0, token.length() - 1) + "X"; // Modify the last character

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, modifiedToken), "assertAuth should throw Unauthorized for modified token");
    }

    @Test
    void assertAuth_WithExpiredToken_ShouldThrowUnauthorized() {
        String expiredToken = Jwts.builder()
                .setSubject(testUser.username)
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000)) // Set expiration to 1 second ago
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();

        assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, expiredToken), "assertAuth should throw Unauthorized for expired token");
    }

    @Test
    void fetch_ShouldPrintDatabaseOpenMessage() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        User.fetch("testUser");

        assertTrue(outContent.toString().contains("Opened database successfully"), "Fetch should print 'Opened database successfully' message");

        System.setOut(System.out);
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
        assertTrue(errContent.toString().contains("Test database exception"), "Fetch should print the exception message to stderr");

        System.setErr(System.err);
    }

    @Test
    void assertAuth_ShouldThrowUnauthorizedWithCustomMessage() {
        String invalidToken = "invalidToken";
        Unauthorized exception = assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, invalidToken));
        assertEquals("Token inválido", exception.getMessage(), "assertAuth should throw Unauthorized with custom message for invalid token");
    }

    @Test
    void assertAuth_ShouldThrowUnauthorizedWithGenericErrorMessage() {
        String malformedToken = "malformed.token.structure";
        Unauthorized exception = assertThrows(Unauthorized.class, () -> User.assertAuth(TEST_SECRET, malformedToken));
        assertTrue(exception.getMessage().startsWith("Erro na autenticação:"), "assertAuth should throw Unauthorized with generic error message for malformed token");
    }

    @Test
    void fetch_ShouldHandleMultipleResultsAndReturnFirstOne() throws Exception {
        String username = "duplicateUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // Simulate only one result due to LIMIT 1
        when(mockResultSet.getString("user_id")).thenReturn("1");
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn("password1");

        User result = User.fetch(username);

        assertNotNull(result, "Fetch should return a user when a result exists");
        assertEquals("1", result.id, "Fetch should return the correct user ID");
        assertEquals(username, result.username, "Fetch should return the correct username");
        assertEquals("password1", result.hashedPassword, "Fetch should return the correct hashed password");
    }

    @Test
    void fetch_ShouldNotTrimWhitespaceFromUsername() throws Exception {
        String username = "  whitespaceUser  ";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("user_id")).thenReturn("1");
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn("password");

        User result = User.fetch(username);

        assertNotNull(result, "Fetch should return a user for username with whitespace");
        assertEquals(username, result.username, "Fetched user should have untrimmed username");
        verify(mockPreparedStatement).setString(1, username);
    }
}

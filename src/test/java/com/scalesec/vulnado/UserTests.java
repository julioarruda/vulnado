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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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

    @Test
    void fetch_ShouldHandleSQLInjectionAttempt() throws Exception {
        String maliciousUsername = "user' OR '1'='1";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = User.fetch(maliciousUsername);

        assertNull(result, "Fetch should return null for SQL injection attempt");
        verify(mockStatement).executeQuery("select * from users where username = '" + maliciousUsername + "' limit 1");
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
    void fetch_ShouldPrintQueryToConsole() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        User.fetch(username);

        String expectedQuery = "select * from users where username = '" + username + "' limit 1";
        assertTrue(outContent.toString().contains(expectedQuery), "Fetch should print the executed query to console");

        System.setOut(System.out);
    }

    @Test
    void fetch_ShouldPrintDatabaseOpenMessage() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

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
    void fetch_ShouldReturnNullWhenResultSetIsEmpty() throws Exception {
        String username = "nonExistentUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = User.fetch(username);

        assertNull(result, "Fetch should return null when the ResultSet is empty");
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

    @Test
    void fetch_ShouldHandleMultipleResultsAndReturnFirstOne() throws Exception {
        String username = "duplicateUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("user_id")).thenReturn("1", "2");
        when(mockResultSet.getString("username")).thenReturn(username, username);
        when(mockResultSet.getString("password")).thenReturn("password1", "password2");

        User result = User.fetch(username);

        assertNotNull(result, "Fetch should return a user when multiple results exist");
        assertEquals("1", result.id, "Fetch should return the first user when multiple results exist");
    }

    @Test
    void fetch_ShouldTrimWhitespaceFromUsername() throws Exception {
        String username = "  whitespaceUser  ";
        String trimmedUsername = "whitespaceUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("user_id")).thenReturn("1");
        when(mockResultSet.getString("username")).thenReturn(trimmedUsername);
        when(mockResultSet.getString("password")).thenReturn("password");

        User result = User.fetch(username);

        assertNotNull(result, "Fetch should return a user for username with whitespace");
        assertEquals(trimmedUsername, result.username, "Fetched user should have trimmed username");
        verify(mockStatement).executeQuery(contains("'" + trimmedUsername + "'"));
    }
    
    @Test
    void token_WithEmptySecret_ShouldStillGenerateToken() {
        String emptySecret = "";
        String token = testUser.token(emptySecret);
        
        assertNotNull(token, "Token should be generated even with empty secret");
        assertTrue(token.split("\\.").length == 3, "Token should have three parts separated by dots");
    }
    
    @Test
    void token_WithNullUsername_ShouldGenerateTokenWithNullSubject() {
        User userWithNullUsername = new User("1", null, "password");
        String token = userWithNullUsername.token(TEST_SECRET);
        
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        String subject = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        
        assertNull(subject, "Token should have null subject when username is null");
    }
    
    @Test
    void token_WithLongSecret_ShouldGenerateValidToken() {
        String longSecret = "ThisIsAVeryLongSecretKeyThatExceedsTheNormalLengthOfSecretsToTestHowTheSystemHandlesLongSecrets";
        String token = testUser.token(longSecret);
        
        assertNotNull(token, "Token should be generated with long secret");
        assertTrue(token.split("\\.").length == 3, "Token should have three parts separated by dots");
    }
    
    @Test
    void token_WithSpecialCharactersInSecret_ShouldGenerateValidToken() {
        String specialSecret = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String token = testUser.token(specialSecret);
        
        assertNotNull(token, "Token should be generated with special characters in secret");
        assertTrue(token.split("\\.").length == 3, "Token should have three parts separated by dots");
    }
    
    @Test
    void token_WithSpecialCharactersInUsername_ShouldGenerateValidToken() {
        User userWithSpecialChars = new User("1", "user!@#$%^&*()", "password");
        String token = userWithSpecialChars.token(TEST_SECRET);
        
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        String subject = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        
        assertEquals("user!@#$%^&*()", subject, "Token should preserve special characters in username");
    }
    
    @Test
    void fetch_WithNullUsername_ShouldHandleNullPointerException() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        
        User result = User.fetch(null);
        
        assertNull(result, "Fetch should return null when username is null");
    }
    
    @Test
    void fetch_ShouldHandleStatementCreationFailure() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenThrow(new RuntimeException("Statement creation failed"));
        
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        
        User result = User.fetch("testUser");
        
        assertNull(result, "Fetch should return null when statement creation fails");
        assertTrue(errContent.toString().contains("Statement creation failed"), 
                "Error message should contain the exception message");
        
        System.setErr(System.err);
    }
    
    @Test
    void fetch_ShouldHandleResultSetExecutionFailure() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenThrow(new RuntimeException("ResultSet execution failed"));
        
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        
        User result = User.fetch("testUser");
        
        assertNull(result, "Fetch should return null when ResultSet execution fails");
        assertTrue(errContent.toString().contains("ResultSet execution failed"), 
                "Error message should contain the exception message");
        
        System.setErr(System.err);
    }
    
    @Test
    void fetch_ShouldHandleConnectionCloseFailure() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        doThrow(new RuntimeException("Connection close failed")).when(mockConnection).close();
        
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        
        User result = User.fetch("testUser");
        
        assertNotNull(result, "Fetch should still return user when connection close fails");
        assertTrue(errContent.toString().contains("Connection close failed"), 
                "Error message should contain the exception message");
        
        System.setErr(System.err);
    }
    
    @Test
    void fetch_ShouldHandleEmptyUsername() throws Exception {
        String emptyUsername = "";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        User result = User.fetch(emptyUsername);
        
        assertNull(result, "Fetch should return null for empty username");
        verify(mockStatement).executeQuery("select * from users where username = '' limit 1");
    }
    
    @Test
    void constructor_ShouldCreateUserWithCorrectValues() {
        String id = "123";
        String username = "testUsername";
        String hashedPassword = "hashedTestPassword";
        
        User user = new User(id, username, hashedPassword);
        
        assertEquals(id, user.id, "User should have the correct id");
        assertEquals(username, user.username, "User should have the correct username");
        assertEquals(hashedPassword, user.hashedPassword, "User should have the correct hashedPassword");
    }
}

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("1", "testUser", "hashedPassword");
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
    void fetch_WithDatabaseException_ShouldReturnNull() throws Exception {
        String username = "exceptionUser";
        when(Postgres.connection()).thenThrow(new RuntimeException("Database connection failed"));

        User result = User.fetch(username);

        assertNull(result, "Fetch should return null when database exception occurs");
    }

    @Test
    void fetch_ShouldExecuteCorrectSQLQuery() throws Exception {
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
        verify(mockPreparedStatement).close();
        verify(mockResultSet).close();
    }

    @Test
    void fetch_ShouldHandleSQLInjectionAttempt() throws Exception {
        String maliciousUsername = "user' OR '1'='1";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = User.fetch(maliciousUsername);

        assertNull(result, "Fetch should return null for SQL injection attempt");
        verify(mockPreparedStatement).setString(1, maliciousUsername);
    }

    @Test
    void fetch_ShouldPrintQueryToConsole() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        User.fetch(username);

        assertTrue(outContent.toString().contains("Opened database successfully"), "Fetch should print 'Opened database successfully' message");
        assertTrue(outContent.toString().contains(mockPreparedStatement.toString()), "Fetch should print the prepared statement to console");

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
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = User.fetch(username);

        assertNull(result, "Fetch should return null when the ResultSet is empty");
    }

    @Test
    void fetch_ShouldHandleMultipleResultsAndReturnFirstOne() throws Exception {
        String username = "duplicateUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("user_id")).thenReturn("1");
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn("password1");

        User result = User.fetch(username);

        assertNotNull(result, "Fetch should return a user when multiple results exist");
        assertEquals("1", result.user_id, "Fetch should return the first user when multiple results exist");
    }

    @Test
    void fetch_ShouldHandleNullUsername() throws Exception {
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        User result = User.fetch(null);

        assertNull(result, "Fetch should return null for null username");
        verify(mockPreparedStatement).setString(1, null);
    }

    @Test
    void fetch_ShouldHandleEmptyUsername() throws Exception {
        String emptyUsername = "";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = User.fetch(emptyUsername);

        assertNull(result, "Fetch should return null for empty username");
        verify(mockPreparedStatement).setString(1, emptyUsername);
    }

    @Test
    void fetch_ShouldHandleLongUsername() throws Exception {
        String longUsername = "a".repeat(1000);
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = User.fetch(longUsername);

        assertNull(result, "Fetch should return null for very long username");
        verify(mockPreparedStatement).setString(1, longUsername);
    }

    @Test
    void fetch_ShouldHandleSpecialCharactersInUsername() throws Exception {
        String specialUsername = "user@example.com";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("user_id")).thenReturn("1");
        when(mockResultSet.getString("username")).thenReturn(specialUsername);
        when(mockResultSet.getString("password")).thenReturn("password");

        User result = User.fetch(specialUsername);

        assertNotNull(result, "Fetch should return a user for username with special characters");
        assertEquals(specialUsername, result.username, "Fetched user should have correct username with special characters");
        verify(mockPreparedStatement).setString(1, specialUsername);
    }

    @Test
    void fetch_ShouldHandleExceptionDuringResultSetClose() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        doThrow(new RuntimeException("ResultSet close error")).when(mockResultSet).close();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        User.fetch(username);

        assertTrue(errContent.toString().contains("ResultSet close error"), "Fetch should print exception message when ResultSet fails to close");

        System.setErr(System.err);
    }

    @Test
    void fetch_ShouldHandleExceptionDuringPreparedStatementClose() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        doThrow(new RuntimeException("PreparedStatement close error")).when(mockPreparedStatement).close();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        User.fetch(username);

        assertTrue(errContent.toString().contains("PreparedStatement close error"), "Fetch should print exception message when PreparedStatement fails to close");

        System.setErr(System.err);
    }

    @Test
    void fetch_ShouldHandleExceptionDuringConnectionClose() throws Exception {
        String username = "testUser";
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        doThrow(new RuntimeException("Connection close error")).when(mockConnection).close();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        User.fetch(username);

        assertTrue(errContent.toString().contains("Connection close error"), "Fetch should print exception message when Connection fails to close");

        System.setErr(System.err);
    }
}

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @BeforeEach
    void setUp() {
        System.setProperty("PGHOST", "localhost");
        System.setProperty("PGDATABASE", "testdb");
        System.setProperty("PGUSER", "testuser");
        System.setProperty("PGPASSWORD", "testpass");
    }

    @Test
    void connection_ShouldReturnConnection_WhenEnvironmentVariablesAreSet() {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            Connection result = Postgres.connection();

            assertNotNull(result, "Connection should not be null");
            mockedDriverManager.verify(() -> DriverManager.getConnection(
                    "jdbc:postgresql://localhost/testdb", "testuser", "testpass"
            ));
        }
    }

    @Test
    void connection_ShouldThrowException_WhenEnvironmentVariablesAreMissing() {
        System.clearProperty("PGHOST");

        assertThrows(IllegalStateException.class, Postgres::connection,
                "Should throw IllegalStateException when environment variables are missing");
    }

    @Test
    void setup_ShouldCreateTablesAndInsertData() throws Exception {
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Postgres.setup();

            verify(mockStatement, times(2)).executeUpdate(contains("CREATE TABLE IF NOT EXISTS"));
            verify(mockStatement, times(2)).executeUpdate(contains("DELETE FROM"));
            verify(mockPreparedStatement, times(5)).executeUpdate(); // 5 users
            verify(mockPreparedStatement, times(2)).executeUpdate(); // 2 comments
        }
    }

    @Test
    void hashPassword_ShouldReturnHashedPassword() {
        String password = "testPassword";
        String hashedPassword = Postgres.hashPassword(password);

        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(password, hashedPassword, "Hashed password should be different from original");
        assertTrue(Postgres.checkPassword(password, hashedPassword), "Password check should pass");
    }

    @Test
    void checkPassword_ShouldReturnTrue_WhenPasswordMatches() {
        String password = "testPassword";
        String hashedPassword = Postgres.hashPassword(password);

        assertTrue(Postgres.checkPassword(password, hashedPassword),
                "Password check should return true for matching password");
    }

    @Test
    void checkPassword_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
        String password = "testPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = Postgres.hashPassword(password);

        assertFalse(Postgres.checkPassword(wrongPassword, hashedPassword),
                "Password check should return false for non-matching password");
    }

    @Test
    void insertUser_ShouldInsertUserSuccessfully() throws Exception {
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Postgres.insertUser("testUser", "testPassword");

            verify(mockPreparedStatement).setString(eq(1), any(String.class)); // UUID
            verify(mockPreparedStatement).setString(2, "testUser");
            verify(mockPreparedStatement).setString(eq(3), anyString()); // Hashed password
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void insertComment_ShouldInsertCommentSuccessfully() throws Exception {
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Postgres.insertComment("testUser", "Test comment");

            verify(mockPreparedStatement).setString(eq(1), any(String.class)); // UUID
            verify(mockPreparedStatement).setString(2, "testUser");
            verify(mockPreparedStatement).setString(3, "Test comment");
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void insertComment_ShouldThrowException_WhenCommentExceedsMaxLength() {
        String longComment = "a".repeat(501);

        assertThrows(IllegalArgumentException.class,
                () -> Postgres.insertComment("testUser", longComment),
                "Should throw IllegalArgumentException when comment exceeds 500 characters");
    }
}

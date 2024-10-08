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
import static org.mockito.ArgumentMatchers.anyString;
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
    void setUp() throws Exception {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    void connection_ShouldThrowIllegalStateException_WhenEnvironmentVariablesAreMissing() {
        try (MockedStatic<System> mockedSystem = mockStatic(System.class)) {
            mockedSystem.when(() -> System.getenv("PGHOST")).thenReturn(null);
            mockedSystem.when(() -> System.getenv("PGDATABASE")).thenReturn(null);
            mockedSystem.when(() -> System.getenv("PGUSER")).thenReturn(null);
            mockedSystem.when(() -> System.getenv("PGPASSWORD")).thenReturn(null);

            assertThrows(IllegalStateException.class, Postgres::connection,
                    "Should throw IllegalStateException when environment variables are missing");
        }
    }

    @Test
    void connection_ShouldReturnConnection_WhenEnvironmentVariablesAreSet() {
        try (MockedStatic<System> mockedSystem = mockStatic(System.class);
             MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedSystem.when(() -> System.getenv("PGHOST")).thenReturn("localhost");
            mockedSystem.when(() -> System.getenv("PGDATABASE")).thenReturn("testdb");
            mockedSystem.when(() -> System.getenv("PGUSER")).thenReturn("user");
            mockedSystem.when(() -> System.getenv("PGPASSWORD")).thenReturn("password");

            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(mockConnection);

            assertNotNull(Postgres.connection(), "Should return a non-null connection");
        }
    }

    @Test
    void setup_ShouldCreateTablesAndInsertData() throws Exception {
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            mockedPostgres.when(() -> Postgres.insertUser(anyString(), anyString())).thenCallRealMethod();
            mockedPostgres.when(() -> Postgres.insertComment(anyString(), anyString())).thenCallRealMethod();

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
        assertNotEquals(password, hashedPassword, "Hashed password should not be equal to original password");
    }

    @Test
    void checkPassword_ShouldReturnTrue_WhenPasswordMatches() {
        String password = "testPassword";
        String hashedPassword = Postgres.hashPassword(password);

        assertTrue(Postgres.checkPassword(password, hashedPassword),
                "checkPassword should return true for matching password");
    }

    @Test
    void checkPassword_ShouldReturnFalse_WhenPasswordDoesNotMatch() {
        String password = "testPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = Postgres.hashPassword(password);

        assertFalse(Postgres.checkPassword(wrongPassword, hashedPassword),
                "checkPassword should return false for non-matching password");
    }

    @Test
    void insertUser_ShouldInsertUserSuccessfully() throws Exception {
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class);
             MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            mockedPostgres.when(() -> Postgres.hashPassword(anyString())).thenCallRealMethod();
            mockedPostgres.when(() -> Postgres.insertUser(anyString(), anyString())).thenCallRealMethod();

            UUID mockUUID = mock(UUID.class);
            when(mockUUID.toString()).thenReturn("mock-uuid");
            mockedUUID.when(UUID::randomUUID).thenReturn(mockUUID);

            Postgres.insertUser("testUser", "testPassword");

            verify(mockPreparedStatement).setString(1, "mock-uuid");
            verify(mockPreparedStatement).setString(2, "testUser");
            verify(mockPreparedStatement).setString(3, anyString()); // Hashed password
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void insertComment_ShouldInsertCommentSuccessfully() throws Exception {
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class);
             MockedStatic<UUID> mockedUUID = mockStatic(UUID.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
            mockedPostgres.when(() -> Postgres.insertComment(anyString(), anyString())).thenCallRealMethod();

            UUID mockUUID = mock(UUID.class);
            when(mockUUID.toString()).thenReturn("mock-uuid");
            mockedUUID.when(UUID::randomUUID).thenReturn(mockUUID);

            Postgres.insertComment("testUser", "Test comment");

            verify(mockPreparedStatement).setString(1, "mock-uuid");
            verify(mockPreparedStatement).setString(2, "testUser");
            verify(mockPreparedStatement).setString(3, "Test comment");
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void insertComment_ShouldThrowIllegalArgumentException_WhenCommentExceedsMaxLength() {
        String longComment = "a".repeat(501);

        assertThrows(IllegalArgumentException.class,
                () -> Postgres.insertComment("testUser", longComment),
                "Should throw IllegalArgumentException when comment exceeds 500 characters");
    }
}

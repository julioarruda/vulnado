import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.util.List;
import java.util.UUID;

class CommentTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Mock Postgres.connection() to return our mockConnection
        try (MockedStatic<Postgres> mockedPostgres = mockStatic(Postgres.class)) {
            mockedPostgres.when(Postgres::connection).thenReturn(mockConnection);
        }
    }

    @Test
    void create_ShouldCreateNewComment_WhenValidInputProvided() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        Comment comment = Comment.create("testUser", "Test comment body");

        // Assert
        assertNotNull(comment, "Created comment should not be null");
        assertEquals("testUser", comment.getUsername(), "Username should match the input");
        assertEquals("Test comment body", comment.getBody(), "Comment body should match the input");
        assertNotNull(comment.getId(), "Comment ID should be generated");
        assertNotNull(comment.getCreatedOn(), "Created timestamp should be set");

        verify(mockPreparedStatement).setString(1, comment.getId());
        verify(mockPreparedStatement).setString(2, "testUser");
        verify(mockPreparedStatement).setString(3, "Test comment body");
        verify(mockPreparedStatement).setTimestamp(eq(4), any(Timestamp.class));
    }

    @Test
    void create_ShouldThrowBadRequest_WhenCommentCannotBeSaved() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act & Assert
        assertThrows(BadRequest.class, () -> Comment.create("testUser", "Test comment body"),
                "Should throw BadRequest when comment cannot be saved");
    }

    @Test
    void create_ShouldThrowServerError_WhenExceptionOccurs() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act & Assert
        assertThrows(ServerError.class, () -> Comment.create("testUser", "Test comment body"),
                "Should throw ServerError when an exception occurs");
    }

    @Test
    void fetchAll_ShouldReturnListOfComments_WhenCommentsExist() throws Exception {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("id")).thenReturn(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(mockResultSet.getString("username")).thenReturn("user1", "user2");
        when(mockResultSet.getString("body")).thenReturn("comment1", "comment2");
        when(mockResultSet.getTimestamp("created_on")).thenReturn(new Timestamp(System.currentTimeMillis()));

        // Act
        List<Comment> comments = Comment.fetchAll();

        // Assert
        assertNotNull(comments, "Fetched comments list should not be null");
        assertEquals(2, comments.size(), "Should return 2 comments");
        verify(mockConnection).close();
    }

    @Test
    void fetchAll_ShouldReturnEmptyList_WhenNoCommentsExist() throws Exception {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Comment> comments = Comment.fetchAll();

        // Assert
        assertNotNull(comments, "Fetched comments list should not be null");
        assertTrue(comments.isEmpty(), "Comments list should be empty when no comments exist");
        verify(mockConnection).close();
    }

    @Test
    void delete_ShouldReturnTrue_WhenCommentIsDeleted() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertTrue(result, "Delete should return true when comment is successfully deleted");
        verify(mockPreparedStatement).setString(1, any(String.class));
    }

    @Test
    void delete_ShouldReturnFalse_WhenCommentIsNotDeleted() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertFalse(result, "Delete should return false when comment is not deleted");
    }

    @Test
    void delete_ShouldReturnFalse_WhenExceptionOccurs() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertFalse(result, "Delete should return false when an exception occurs");
    }
}

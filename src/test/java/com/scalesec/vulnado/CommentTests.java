package com.scalesec.vulnado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(Postgres.connection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    }

    @Test
    void create_ShouldReturnNewComment_WhenCommitSucceeds() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        Comment comment = Comment.create("testUser", "Test comment");

        // Assert
        assertNotNull(comment, "Created comment should not be null");
        assertEquals("testUser", comment.getUsername(), "Username should match");
        assertEquals("Test comment", comment.getBody(), "Comment body should match");
        assertNotNull(comment.getId(), "Comment ID should be generated");
        assertNotNull(comment.getCreatedOn(), "Creation timestamp should be set");
    }

    @Test
    void create_ShouldThrowBadRequest_WhenCommitFails() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act & Assert
        assertThrows(BadRequest.class, () -> Comment.create("testUser", "Test comment"),
                "Should throw BadRequest when commit fails");
    }

    @Test
    void fetchAll_ShouldReturnListOfComments() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("id")).thenReturn(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(mockResultSet.getString("username")).thenReturn("user1", "user2");
        when(mockResultSet.getString("body")).thenReturn("comment1", "comment2");
        when(mockResultSet.getTimestamp("created_on")).thenReturn(new Timestamp(System.currentTimeMillis()));

        // Act
        List<Comment> comments = Comment.fetchAll();

        // Assert
        assertEquals(2, comments.size(), "Should return 2 comments");
        verify(mockStatement).executeQuery("select * from comments;");
    }

    @Test
    void delete_ShouldReturnTrue_WhenCommentDeleted() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertTrue(result, "Delete should return true when comment is deleted");
        verify(mockPreparedStatement).setString(1, anyString());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void delete_ShouldReturnFalse_WhenCommentNotFound() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertFalse(result, "Delete should return false when comment is not found");
    }

    @Test
    void delete_ShouldReturnFalse_WhenExceptionOccurs() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertFalse(result, "Delete should return false when an exception occurs");
    }

    // Additional tests for edge cases and error handling

    @Test
    void create_ShouldThrowServerError_WhenExceptionOccurs() throws SQLException {
        // Arrange
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        assertThrows(ServerError.class, () -> Comment.create("testUser", "Test comment"),
                "Should throw ServerError when an exception occurs during creation");
    }

    @Test
    void fetchAll_ShouldReturnEmptyList_WhenNoCommentsExist() throws SQLException {
        // Arrange
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Comment> comments = Comment.fetchAll();

        // Assert
        assertTrue(comments.isEmpty(), "Should return an empty list when no comments exist");
    }

    @Test
    void fetchAll_ShouldHandleSQLException() throws SQLException {
        // Arrange
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        List<Comment> comments = Comment.fetchAll();

        // Assert
        assertTrue(comments.isEmpty(), "Should return an empty list when an exception occurs");
    }
}

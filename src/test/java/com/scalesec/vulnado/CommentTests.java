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
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Mock Postgres.connection() to return our mockConnection
        try (var mockedStatic = mockStatic(Postgres.class)) {
            mockedStatic.when(Postgres::connection).thenReturn(mockConnection);
        }
    }

    @Test
    void create_ShouldReturnNewComment_WhenCommitSucceeds() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        Comment comment = Comment.create("testUser", "Test comment");

        // Assert
        assertNotNull(comment, "Created comment should not be null");
        assertEquals("testUser", comment.getUsername(), "Username should match");
        assertEquals("Test comment", comment.getBody(), "Comment body should match");
        assertNotNull(comment.getId(), "Comment ID should be generated");
        assertNotNull(comment.getCreatedOn(), "Created timestamp should be set");
    }

    @Test
    void create_ShouldThrowBadRequest_WhenCommitFails() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act & Assert
        assertThrows(BadRequest.class, () -> Comment.create("testUser", "Test comment"),
                "Should throw BadRequest when commit fails");
    }

    @Test
    void fetchAll_ShouldReturnListOfComments() throws Exception {
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
        assertEquals(2, comments.size(), "Should return 2 comments");
        verify(mockConnection).close();
    }

    @Test
    void delete_ShouldReturnTrue_WhenDeletionSucceeds() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertTrue(result, "Delete should return true when successful");
    }

    @Test
    void delete_ShouldReturnFalse_WhenDeletionFails() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = Comment.delete(UUID.randomUUID().toString());

        // Assert
        assertFalse(result, "Delete should return false when unsuccessful");
    }

    @Test
    void commit_ShouldReturnTrue_WhenInsertSucceeds() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        Comment comment = new Comment(UUID.randomUUID().toString(), "testUser", "Test comment", new Timestamp(System.currentTimeMillis()));

        // Act
        boolean result = comment.commit();

        // Assert
        assertTrue(result, "Commit should return true when insert succeeds");
    }

    @Test
    void commit_ShouldReturnFalse_WhenInsertFails() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        Comment comment = new Comment(UUID.randomUUID().toString(), "testUser", "Test comment", new Timestamp(System.currentTimeMillis()));

        // Act
        boolean result = comment.commit();

        // Assert
        assertFalse(result, "Commit should return false when insert fails");
    }

    @Test
    void commit_ShouldReturnFalse_WhenSQLExceptionOccurs() throws Exception {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQL Exception"));
        Comment comment = new Comment(UUID.randomUUID().toString(), "testUser", "Test comment", new Timestamp(System.currentTimeMillis()));

        // Act
        boolean result = comment.commit();

        // Assert
        assertFalse(result, "Commit should return false when SQLException occurs");
    }
}

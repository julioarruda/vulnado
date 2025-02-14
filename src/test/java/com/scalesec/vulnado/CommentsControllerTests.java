package com.scalesec.vulnado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentsControllerTest {

    @InjectMocks
    private CommentsController commentsController;

    @Mock
    private User userMock;

    @Mock
    private Comment commentMock;

    private final String SECRET = "testSecret";
    private final String TOKEN = "validToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(commentsController, "secret", SECRET);
    }

    @Test
    void comments_ValidToken_ReturnsCommentList() {
        // Arrange
        List<Comment> expectedComments = new ArrayList<>();
        expectedComments.add(new Comment());
        when(Comment.fetch_all()).thenReturn(expectedComments);

        // Act
        List<Comment> result = commentsController.comments(TOKEN);

        // Assert
        assertEquals(expectedComments, result, "The returned comment list should match the expected list");
        verify(userMock).assertAuth(SECRET, TOKEN);
    }

    @Test
    void createComment_ValidInput_ReturnsCreatedComment() {
        // Arrange
        CommentRequest input = new CommentRequest();
        input.setUsername("testUser");
        input.setBody("Test comment body");
        Comment expectedComment = new Comment();
        when(Comment.create(input.getUsername(), input.getBody())).thenReturn(expectedComment);

        // Act
        Comment result = commentsController.createComment(TOKEN, input);

        // Assert
        assertEquals(expectedComment, result, "The returned comment should match the created comment");
    }

    @Test
    void deleteComment_ExistingId_ReturnsTrue() {
        // Arrange
        String commentId = "123";
        when(Comment.delete(commentId)).thenReturn(true);

        // Act
        Boolean result = commentsController.deleteComment(TOKEN, commentId);

        // Assert
        assertTrue(result, "The comment should be successfully deleted");
    }

    @Test
    void deleteComment_NonExistingId_ReturnsFalse() {
        // Arrange
        String commentId = "456";
        when(Comment.delete(commentId)).thenReturn(false);

        // Act
        Boolean result = commentsController.deleteComment(TOKEN, commentId);

        // Assert
        assertFalse(result, "The deletion should fail for a non-existing comment");
    }

    @Test
    void commentRequest_GettersAndSetters_WorkCorrectly() {
        // Arrange
        CommentRequest request = new CommentRequest();
        String username = "testUser";
        String body = "Test comment body";

        // Act
        request.setUsername(username);
        request.setBody(body);

        // Assert
        assertEquals(username, request.getUsername(), "Username getter should return the set value");
        assertEquals(body, request.getBody(), "Body getter should return the set value");
    }

    @Test
    void badRequest_Constructor_SetsMessage() {
        // Arrange
        String errorMessage = "Bad Request Error";

        // Act
        BadRequest badRequest = new BadRequest(errorMessage);

        // Assert
        assertEquals(errorMessage, badRequest.getMessage(), "BadRequest should contain the provided error message");
    }

    @Test
    void serverError_Constructor_SetsMessage() {
        // Arrange
        String errorMessage = "Internal Server Error";

        // Act
        ServerError serverError = new ServerError(errorMessage);

        // Assert
        assertEquals(errorMessage, serverError.getMessage(), "ServerError should contain the provided error message");
    }
}

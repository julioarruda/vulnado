import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
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

    private final String SECRET = "test_secret";
    private final String TOKEN = "valid_token";
    private final String ALLOWED_ORIGIN = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(commentsController, "secret", SECRET);
    }

    @Test
    void Comments_WithValidToken_ShouldReturnCommentsList() {
        // Arrange
        List<Comment> expectedComments = Arrays.asList(new Comment(), new Comment());
        doNothing().when(userMock).assertAuth(SECRET, TOKEN);
        when(Comment.fetch_all()).thenReturn(expectedComments);

        // Act
        List<Comment> result = commentsController.comments(TOKEN);

        // Assert
        assertEquals(expectedComments, result, "The returned comments list should match the expected list");
        verify(userMock).assertAuth(SECRET, TOKEN);
        verify(commentMock).fetch_all();
    }

    @Test
    void CreateComment_WithValidInput_ShouldReturnCreatedComment() {
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
        verify(commentMock).create(input.getUsername(), input.getBody());
    }

    @Test
    void DeleteComment_WithValidId_ShouldReturnTrue() {
        // Arrange
        String commentId = "123";
        when(Comment.delete(commentId)).thenReturn(true);

        // Act
        Boolean result = commentsController.deleteComment(TOKEN, commentId);

        // Assert
        assertTrue(result, "The comment deletion should return true");
        verify(commentMock).delete(commentId);
    }

    @Test
    void Comments_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid_token";
        doThrow(new RuntimeException("Invalid token")).when(userMock).assertAuth(SECRET, invalidToken);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> commentsController.comments(invalidToken),
                "An exception should be thrown for an invalid token");
    }

    @Test
    void CreateComment_WithNullInput_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> commentsController.createComment(TOKEN, null),
                "An exception should be thrown for null input");
    }

    @Test
    void DeleteComment_WithNonExistentId_ShouldReturnFalse() {
        // Arrange
        String nonExistentId = "999";
        when(Comment.delete(nonExistentId)).thenReturn(false);

        // Act
        Boolean result = commentsController.deleteComment(TOKEN, nonExistentId);

        // Assert
        assertFalse(result, "Deleting a non-existent comment should return false");
        verify(commentMock).delete(nonExistentId);
    }
}

class CommentRequestTest {

    @Test
    void GettersAndSetters_ShouldWorkCorrectly() {
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
}

class BadRequestTest {

    @Test
    void Constructor_ShouldSetExceptionMessage() {
        // Arrange
        String exceptionMessage = "Bad Request Error";

        // Act
        BadRequest badRequest = new BadRequest(exceptionMessage);

        // Assert
        assertEquals(exceptionMessage, badRequest.getMessage(), "The exception message should be set correctly");
    }
}

class ServerErrorTest {

    @Test
    void Constructor_ShouldSetExceptionMessage() {
        // Arrange
        String exceptionMessage = "Internal Server Error";

        // Act
        ServerError serverError = new ServerError(exceptionMessage);

        // Assert
        assertEquals(exceptionMessage, serverError.getMessage(), "The exception message should be set correctly");
    }
}

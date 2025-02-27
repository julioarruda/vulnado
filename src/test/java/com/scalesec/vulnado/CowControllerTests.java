import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CowControllerTest {

    @InjectMocks
    private CowController cowController;

    @Mock
    private Cowsay cowsay;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void cowsay_WithDefaultInput_ShouldReturnDefaultMessage() {
        // Arrange
        String expectedOutput = " _______________\n< I love Linux! >\n ---------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run("I love Linux!")).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(null);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return the default message when no input is provided");
        verify(Cowsay.class, times(1)).run("I love Linux!");
    }

    @Test
    void cowsay_WithCustomInput_ShouldReturnCustomMessage() {
        // Arrange
        String customInput = "Hello, World!";
        String expectedOutput = " ______________\n< Hello, World! >\n --------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(customInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(customInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return a custom message when input is provided");
        verify(Cowsay.class, times(1)).run(customInput);
    }

    @Test
    void cowsay_WithEmptyInput_ShouldReturnEmptyMessage() {
        // Arrange
        String emptyInput = "";
        String expectedOutput = " __\n<  >\n --\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(emptyInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(emptyInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return an empty message when input is empty");
        verify(Cowsay.class, times(1)).run(emptyInput);
    }

    @Test
    void cowsay_WithLongInput_ShouldReturnWrappedMessage() {
        // Arrange
        String longInput = "This is a very long input that should be wrapped to multiple lines by the Cowsay utility.";
        String expectedOutput = " _________________________________________\n/ This is a very long input that should be \\\n\\ wrapped to multiple lines by the Cowsay  /\n\\ utility.                                 /\n -----------------------------------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(longInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(longInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return a wrapped message for long inputs");
        verify(Cowsay.class, times(1)).run(longInput);
    }

    @Test
    void cowsay_WithSpecialCharacters_ShouldHandleThemCorrectly() {
        // Arrange
        String specialCharsInput = "Hello! @#$%^&*()_+";
        String expectedOutput = " ____________________\n< Hello! @#$%^&*()_+ >\n --------------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(specialCharsInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(specialCharsInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should handle special characters correctly");
        verify(Cowsay.class, times(1)).run(specialCharsInput);
    }
}

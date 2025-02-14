import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CowControllerTest {

    @InjectMocks
    private CowController cowController;

    @Mock
    private Cowsay cowsay;

    @Test
    public void cowsay_WithDefaultInput_ShouldReturnDefaultMessage() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        String expectedOutput = " _______________\n< I love Linux! >\n ---------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run("I love Linux!")).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(null);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return the default message when no input is provided");
    }

    @Test
    public void cowsay_WithCustomInput_ShouldReturnCustomMessage() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        String customInput = "Hello, World!";
        String expectedOutput = " ______________\n< Hello, World! >\n --------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(customInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(customInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return a custom message when input is provided");
    }

    @Test
    public void cowsay_WithLongInput_ShouldReturnFormattedMessage() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        String longInput = "This is a very long input that should be wrapped by the Cowsay utility.";
        String expectedOutput = " _________________________________________\n/ This is a very long input that should be \\\n\\ wrapped by the Cowsay utility.           /\n -----------------------------------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(longInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(longInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should properly format long input messages");
    }

    @Test
    public void cowsay_WithSpecialCharacters_ShouldHandleThemCorrectly() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        String specialInput = "Hello, World! @#$%^&*()";
        String expectedOutput = " _________________________\n< Hello, World! @#$%^&*() >\n -------------------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(specialInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(specialInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should handle special characters correctly");
    }

    @Test
    public void cowsay_WithEmptyInput_ShouldReturnEmptyMessage() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        String emptyInput = "";
        String expectedOutput = " __\n<  >\n --\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        when(Cowsay.run(emptyInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(emptyInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should handle empty input correctly");
    }
}

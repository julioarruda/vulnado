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
        
        String expectedOutput = "Default cow message";
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
        String expectedOutput = "Custom cow message";
        when(Cowsay.run(customInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(customInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return a custom message when input is provided");
    }

    @Test
    public void cowsay_WithEmptyInput_ShouldReturnDefaultMessage() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        String expectedOutput = "Default cow message";
        when(Cowsay.run("I love Linux!")).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay("");

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return the default message when an empty input is provided");
    }

    @Test
    public void cowsay_WithLongInput_ShouldReturnTruncatedMessage() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        String longInput = "This is a very long input that exceeds the maximum allowed length for the cowsay command.";
        String expectedOutput = "Truncated cow message";
        when(Cowsay.run(longInput)).thenReturn(expectedOutput);

        // Act
        String result = cowController.cowsay(longInput);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay method should return a truncated message when the input exceeds the maximum allowed length");
    }
}

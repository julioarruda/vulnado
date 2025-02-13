import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class CowsayTest {

    @Mock
    private ProcessBuilder mockProcessBuilder;

    @Mock
    private Process mockProcess;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void run_WithValidInput_ShouldReturnCowsayOutput() throws Exception {
        // Arrange
        String input = "Hello, World!";
        String expectedOutput = " ______________ \n< Hello, World! >\n -------------- \n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        
        InputStream inputStream = new ByteArrayInputStream(expectedOutput.getBytes());
        
        when(mockProcessBuilder.command("bash", "-c", "/usr/games/cowsay", input)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(inputStream);

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay output should match the expected output");
        verify(mockProcessBuilder).command("bash", "-c", "/usr/games/cowsay", input);
        verify(mockProcessBuilder).start();
        verify(mockProcess).getInputStream();
    }

    @Test
    public void run_WithEmptyInput_ShouldReturnEmptyCowsayOutput() throws Exception {
        // Arrange
        String input = "";
        String expectedOutput = " __ \n<  >\n -- \n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        
        InputStream inputStream = new ByteArrayInputStream(expectedOutput.getBytes());
        
        when(mockProcessBuilder.command("bash", "-c", "/usr/games/cowsay", input)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(inputStream);

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay output for empty input should match the expected output");
        verify(mockProcessBuilder).command("bash", "-c", "/usr/games/cowsay", input);
        verify(mockProcessBuilder).start();
        verify(mockProcess).getInputStream();
    }

    @Test
    public void run_WithLongInput_ShouldWrapText() throws Exception {
        // Arrange
        String input = "This is a very long input that should be wrapped by cowsay";
        String expectedOutput = " _________________________________________ \n/ This is a very long input that should be \\\n\\ wrapped by cowsay                        /\n ----------------------------------------- \n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        
        InputStream inputStream = new ByteArrayInputStream(expectedOutput.getBytes());
        
        when(mockProcessBuilder.command("bash", "-c", "/usr/games/cowsay", input)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(inputStream);

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay output for long input should be wrapped and match the expected output");
        verify(mockProcessBuilder).command("bash", "-c", "/usr/games/cowsay", input);
        verify(mockProcessBuilder).start();
        verify(mockProcess).getInputStream();
    }

    @Test
    public void run_WithSpecialCharacters_ShouldHandleThemCorrectly() throws Exception {
        // Arrange
        String input = "Hello, World! @#$%^&*()";
        String expectedOutput = " _________________________ \n< Hello, World! @#$%^&*() >\n ------------------------- \n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";
        
        InputStream inputStream = new ByteArrayInputStream(expectedOutput.getBytes());
        
        when(mockProcessBuilder.command("bash", "-c", "/usr/games/cowsay", input)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(inputStream);

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The cowsay output should handle special characters correctly");
        verify(mockProcessBuilder).command("bash", "-c", "/usr/games/cowsay", input);
        verify(mockProcessBuilder).start();
        verify(mockProcess).getInputStream();
    }

    @Test
    public void run_WhenProcessThrowsException_ShouldReturnEmptyString() throws Exception {
        // Arrange
        String input = "Test input";
        when(mockProcessBuilder.command("bash", "-c", "/usr/games/cowsay", input)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenThrow(new RuntimeException("Process error"));

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals("", result, "The method should return an empty string when an exception occurs");
        verify(mockProcessBuilder).command("bash", "-c", "/usr/games/cowsay", input);
        verify(mockProcessBuilder).start();
    }
}

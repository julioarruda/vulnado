package com.scalesec.vulnado;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class CowsayTest {

    @Mock
    private ProcessBuilder mockProcessBuilder;

    @Mock
    private Process mockProcess;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void run_ShouldExecuteCowsayCommand() throws Exception {
        // Arrange
        String input = "Hello, Cow!";
        String expectedOutput = " ____________\n< Hello, Cow! >\n ------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";

        List<String> expectedCommand = Arrays.asList("bash", "-c", "/usr/games/cowsay", input);

        when(mockProcessBuilder.command(expectedCommand)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The output should match the expected cowsay output");
        verify(mockLogger).info("/usr/games/cowsay '" + input + "'");
    }

    @Test
    void run_ShouldHandleExceptionGracefully() throws Exception {
        // Arrange
        String input = "Error Test";
        String errorMessage = "Command execution failed";

        when(mockProcessBuilder.command(any())).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenThrow(new RuntimeException(errorMessage));

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertTrue(result.isEmpty(), "The result should be empty when an exception occurs");
        verify(mockLogger).severe("Error executing cowsay command: " + errorMessage);
    }

    @Test
    void run_ShouldHandleEmptyInput() throws Exception {
        // Arrange
        String input = "";
        String expectedOutput = " __\n<  >\n --\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";

        List<String> expectedCommand = Arrays.asList("bash", "-c", "/usr/games/cowsay", input);

        when(mockProcessBuilder.command(expectedCommand)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The output should match the expected cowsay output for empty input");
        verify(mockLogger).info("/usr/games/cowsay ''");
    }

    @Test
    void run_ShouldHandleSpecialCharacters() throws Exception {
        // Arrange
        String input = "Hello, Cow! $pecial Ch@r@cters: !@#$%^&*()";
        String expectedOutput = " _________________________________________\n< Hello, Cow! $pecial Ch@r@cters: !@#$%^&*() >\n -----------------------------------------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";

        List<String> expectedCommand = Arrays.asList("bash", "-c", "/usr/games/cowsay", input);

        when(mockProcessBuilder.command(expectedCommand)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The output should match the expected cowsay output with special characters");
        verify(mockLogger).info("/usr/games/cowsay '" + input + "'");
    }

    @Test
    void run_ShouldHandleMultilineInput() throws Exception {
        // Arrange
        String input = "Hello,\nCow!\nMultiple\nLines";
        String expectedOutput = " _________\n< Hello,   >\n< Cow!     >\n< Multiple >\n< Lines    >\n ---------\n        \\   ^__^\n         \\  (oo)\\_______\n            (__)\\       )\\/\\\n                ||----w |\n                ||     ||\n";

        List<String> expectedCommand = Arrays.asList("bash", "-c", "/usr/games/cowsay", input);

        when(mockProcessBuilder.command(expectedCommand)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream(expectedOutput.getBytes()));

        // Act
        String result = Cowsay.run(input);

        // Assert
        assertEquals(expectedOutput, result, "The output should match the expected cowsay output for multiline input");
        verify(mockLogger).info("/usr/games/cowsay '" + input + "'");
    }
}

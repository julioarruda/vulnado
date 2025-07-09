package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {
  public static String run(String input) {
    // Sanitize input to prevent command injection
    if (input == null) {
      input = "I love Linux!";
    }
    
    // Remove dangerous characters that could be used for command injection
    String sanitizedInput = input.replaceAll("[;&|`$()\\\\<>\"']", "");
    // Limit input length to prevent buffer overflow attacks
    if (sanitizedInput.length() > 100) {
      sanitizedInput = sanitizedInput.substring(0, 100);
    }
    
    ProcessBuilder processBuilder = new ProcessBuilder();
    System.out.println("Sanitized input: " + sanitizedInput);
    // Use ProcessBuilder with separate arguments instead of shell command
    processBuilder.command("/usr/games/cowsay", sanitizedInput);

    StringBuilder output = new StringBuilder();

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
      
      // Wait for process to complete and check exit code
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        return "Error executing cowsay command";
      }
    } catch (Exception e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
    return output.toString();
  }
}

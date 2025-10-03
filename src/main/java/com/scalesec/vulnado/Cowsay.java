package com.scalesec.vulnado;

import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {
  private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());
  public static String run(String input) {
  private Cowsay() {}
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input + "'";
    LOGGER.info(cmd);
    // Use a whitelist of allowed inputs or sanitize the input to prevent command injection
    String sanitizedInput = input.replaceAll("[^a-zA-Z0-9\s]", "");

    String sanitizedCmd = "/usr/games/cowsay '" + sanitizedInput + "'";
    StringBuilder output = new StringBuilder();
    processBuilder.command("bash", "-c", sanitizedCmd);

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      LOGGER.severe("Error executing cowsay command: " + e.getMessage());
    }
    return output.toString();
  }
}

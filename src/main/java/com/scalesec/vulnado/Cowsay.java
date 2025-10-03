package com.scalesec.vulnado;

import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {
  private Cowsay() {}
  private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());
  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String[] command = {"bash", "-c", "/usr/games/cowsay '" + input.replaceAll("[;&|<>]", "") + "'"};  // Sanitize input
    LOGGER.info(String.join(" ", command));
    processBuilder.command(command);

    StringBuilder output = new StringBuilder();

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

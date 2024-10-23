package com.scalesec.vulnado;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {
  private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());
  public static String run(String input) {
  private Cowsay() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    // Private constructor to prevent instantiation
    String cmd = "/usr/games/cowsay '" + input + "'";
  }
    System.out.println(cmd);
    String sanitizedInput = input.replace("'", "'\\''"); // Sanitize input
    String cmd = "/usr/games/cowsay '" + sanitizedInput + "'";
    processBuilder.command("bash", "-c", cmd);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.severe(e.getMessage());
    return output.toString();
  }
}

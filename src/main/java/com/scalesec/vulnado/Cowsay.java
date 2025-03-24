package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cowsay {

  private static final Logger logger = Logger.getLogger(Cowsay.class.getName());

  // Private constructor to prevent instantiation
  private Cowsay() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input + "'";
    logger.log(Level.INFO, "Executing command: {0}", cmd);
    processBuilder.command("bash", "-c", cmd);

    StringBuilder output = new StringBuilder();

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while running the cowsay command", e);
    }
    return output.toString();
  }
}
package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cowsay {
  // Private constructor to prevent instantiation
  private Cowsay() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  private static final Logger logger = LoggerFactory.getLogger(Cowsay.class);

  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input + "'";
    logger.info("Executing command: {}", cmd);
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
      logger.error("An error occurred while executing the command", e);
    }
    return output.toString();
  }
}
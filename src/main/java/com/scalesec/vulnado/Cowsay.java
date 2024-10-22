package com.scalesec.vulnado;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Cowsay {
  private static final Logger logger = Logger.getLogger(Cowsay.class.getName());
  public static String run(String input) {
  private Cowsay() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    // Hide the implicit public constructor
    String cmd = "/usr/games/cowsay '" + input + "'";
  }
    System.out.println(cmd);
  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String sanitizedInput = input.replaceAll("[^a-zA-Z0-9]", ""); // Simple sanitization
    StringBuilder output = new StringBuilder();
    String cmd = "/usr/games/cowsay '" + sanitizedInput + "'";
    logger.info(cmd); // Use logger instead of System.out.println
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      logger.severe(e.getMessage()); // Use logger instead of e.printStackTrace()
    }
    return output.toString();
  }
}

package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import java.util.logging.Level;
public final class Cowsay {
  private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());
  public static String run(String input) {

    ProcessBuilder processBuilder = new ProcessBuilder();
  private Cowsay() {
    String cmd = "/usr/games/cowsay '" + input + "'";
    // Private constructor to hide the implicit public one
    LOGGER.info(cmd);
  }
    processBuilder.command("bash", "-c", "/usr/games/cowsay", input);


    StringBuilder output = new StringBuilder();

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "An error occurred", e);
    }
    return output.toString();
  }
}

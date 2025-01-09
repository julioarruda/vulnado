package com.scalesec.vulnado;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.List;
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
    List<String> command = new ArrayList<>();
    command.add("bash");


    command.add("-c");
    StringBuilder output = new StringBuilder();
    command.add(cmd);

    processBuilder.command(command);
    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error executing cowsay command", e);
    }
    return output.toString();
  }
}

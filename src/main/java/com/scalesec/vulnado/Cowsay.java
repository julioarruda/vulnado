import java.util.logging.Logger;
package com.scalesec.vulnado;

import java.util.Arrays;
import java.io.BufferedReader;
import java.util.List;
import java.io.InputStreamReader;

    private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());
public class Cowsay {
    private Cowsay() {}
  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input + "'";
    LOGGER.info(cmd);
    List<String> command = Arrays.asList("bash", "-c", "/usr/games/cowsay", input);
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

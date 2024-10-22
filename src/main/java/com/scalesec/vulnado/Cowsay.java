package com.scalesec.vulnado;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

  private Cowsay() { throw new IllegalStateException("Utility class"); }
  private static final Logger logger = Logger.getLogger(Cowsay.class.getName());
public class Cowsay {
  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input.replace("'", "'\\''") + "'";
    logger.info(cmd);
    processBuilder.command("bash", "-c", cmd);

    StringBuilder output = new StringBuilder();

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return output.toString();
  }
}

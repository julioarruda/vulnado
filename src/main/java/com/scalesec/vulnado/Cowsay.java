package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cowsay {
  
  // Logger para a classe
  private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());

  // Construtor privado para evitar instanciação
  private Cowsay() {
    // Evita instanciação
  }

  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input + "'";
    LOGGER.log(Level.INFO, "Executing command: {0}", cmd);
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
      LOGGER.log(Level.SEVERE, "Error executing command", e);
    }
    return output.toString();
  }
}
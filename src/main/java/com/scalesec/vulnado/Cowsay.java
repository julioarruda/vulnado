package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

  public class Cowsay {
    private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());

    private Cowsay() {
        // Private constructor to hide implicit public one
    }

    public static String run(String input) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String cmd = "/usr/games/cowsay " + input;
        LOGGER.info("Executing command: " + cmd);
        processBuilder.command("/usr/games/cowsay", input);

        StringBuilder output = new StringBuilder();

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
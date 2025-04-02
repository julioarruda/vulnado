# Cowsay.java

## Overview
This Java program executes the Linux command `cowsay` with user-provided input, captures the output, and returns it as a string.

## Data Structures
N/A - This code snippet defines a program, not just a data structure.

## Functions

| Function Signature | Description |
|---|---|
| `public static String run(String input)` | Executes the `cowsay` command with the given input and returns the output. |

## Insights
- The program relies on the availability of the `cowsay` command in the system's PATH. If the command is not found, it will result in a runtime error.
- The program uses a `ProcessBuilder` to create and execute the shell command.
- The output from the `cowsay` command is captured line by line and stored in a `StringBuilder`.
- Any exception during the execution of the command is caught, printed to the console, and an empty string is returned. 


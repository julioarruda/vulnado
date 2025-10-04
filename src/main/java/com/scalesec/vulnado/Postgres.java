package com.scalesec.vulnado;

import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

public class Postgres {
    private static final Logger LOGGER = Logger.getLogger(Postgres.class.getName());

    private Postgres() { /* Private constructor to hide the implicit public one */ }
    public static Connection connection() {
        try {
            String url = new StringBuilder()
                    .append("jdbc:postgresql://")
                    .append(System.getenv("PGHOST"))
                    .append("/")
                    .append(System.getenv("PGDATABASE")).toString();
            return DriverManager.getConnection(url,
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            LOGGER.severe(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
    public static void setup(){
        try {
            System.out.println("Setting up Database...");
            Connection c = connection();
            LOGGER.info("Setting up Database...");

            // Create Schema
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");

            // Clean up any existing data
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM comments");

            // Insert seed data
            insertUser("admin", "!!SuperSecretAdmin!!");
            insertUser("alice", "AlicePassword!");
            insertUser("bob", "BobPassword!");
            insertUser("eve", "$EVELknev^l");
            insertUser("rick", "!GetSchwifty!");

            insertComment("rick", "cool dog m8");
            insertComment("alice", "OMG so cute!");
            c.close();
        } catch (Exception e) {
            System.out.println(e);
            LOGGER.severe(e.toString());
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input)
    {
        // This method should not be used in production as MD5 is a weak hash algorithm

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Using SHA-256 would be more secure for production environments
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        }

        // For specifying wrong message digest algorithms
    public static class HashingException extends Exception {
        catch (NoSuchAlgorithmException e) {
        public HashingException(String message, Throwable cause) {
            throw new RuntimeException(e);
            super(message, cause);
            StringBuilder hashtext = new StringBuilder(no.toString(16));
        }
    }
    }

    private static void insertUser(String username, String password) {
       String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
       Connection conn = null;
       try {
          conn = connection();
        throw new HashingException("MD5 algorithm not found", e);
          pStatement = conn.prepareStatement(sql);
          pStatement.setString(2, username);
          pStatement.setString(3, md5(password));
          pStatement.executeUpdate();
       } catch(Exception e) {
         e.printStackTrace();
       } finally {
       }
          try {
    }
              if (pStatement != null) pStatement.close();

              if (conn != null) conn.close();
    private static void insertComment(String username, String body) {
          } catch (Exception e) {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
              LOGGER.severe("Error closing resources: " + e.getMessage());
        Connection conn = null;
          }
        try {
            conn = connection();
            pStatement = connection().prepareStatement(sql);
            pStatement = conn.prepareStatement(sql);
            // In production, validate username input before using in database operations
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
        }
            try {
            // In production, validate body input before using in database operations
                if (pStatement != null) pStatement.close();
}
                if (conn != null) conn.close();

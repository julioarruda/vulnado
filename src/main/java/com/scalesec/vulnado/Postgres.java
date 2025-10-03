package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

public class Postgres {
    private static final Logger LOGGER = Logger.getLogger(Postgres.class.getName());

    private Postgres() {
    public static Connection connection() {
        // Private constructor to hide the implicit public one
        try {
    }

            String url = new StringBuilder()
                    .append("jdbc:postgresql://")
                    .append(System.getenv("PGHOST"))
                    .append("/")
                    .append(System.getenv("PGDATABASE")).toString();
            return DriverManager.getConnection(url,
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Database setup error", e);
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input)
    {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Consider using a stronger hash algorithm for production
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
            StringBuilder hashtext = new StringBuilder(no.toString(16));
    }

    public static class DatabaseException extends Exception {
    private static void insertUser(String username, String password) {
        public DatabaseException(String message, Throwable cause) {
       String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
            super(message, cause);
       PreparedStatement pStatement = null;
        }
       try {
    }
          Connection conn = connection();
          pStatement = conn.prepareStatement(sql);
          pStatement.setString(1, UUID.randomUUID().toString());
          pStatement.setString(2, username);
          pStatement.setString(3, md5(password));
          pStatement.executeUpdate();
       } catch(Exception e) {
         LOGGER.log(Level.SEVERE, "Error inserting user", e);
       }
       finally {
    }
         if (pStatement != null) {

           try {
    private static void insertComment(String username, String body) {
             pStatement.close();
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
           } catch (Exception e) {
        PreparedStatement pStatement = null;
             LOGGER.log(Level.WARNING, "Error closing statement", e);
        try {
           }
            pStatement = connection().prepareStatement(sql);
         }
            pStatement.setString(1, UUID.randomUUID().toString());
       }
            Connection conn = connection();
            pStatement = conn.prepareStatement(sql);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Error inserting comment", e);
        }
        finally {
    }
            if (pStatement != null) {
}
                try {

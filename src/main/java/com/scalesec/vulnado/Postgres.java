package com.scalesec.vulnado;
package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Statement;
import java.util.UUID;
import java.util.UUID;
import java.util.logging.Logger;

import java.util.logging.Level;
public class Postgres {

public class Postgres {
    public static Connection connection() {
    private static final Logger LOGGER = Logger.getLogger(Postgres.class.getName());
        try {

            Class.forName("org.postgresql.Driver");
    // Private constructor to hide the implicit public one
            String url = new StringBuilder()
    private Postgres() {
                    .append("jdbc:postgresql://")
        // Private constructor to hide the implicit public one
                    .append(System.getenv("PGHOST"))
    }
                    .append("/")

                    .append(System.getenv("PGDATABASE")).toString();
    public static Connection connection() {
            return DriverManager.getConnection(url,
        try {
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
            String url = new StringBuilder()
        } catch (Exception e) {
                    .append("jdbc:postgresql://")
            e.printStackTrace();
                    .append(System.getenv("PGHOST"))
            System.err.println(e.getClass().getName()+": "+e.getMessage());
                    .append("/")
            System.exit(1);
                    .append(System.getenv("PGDATABASE")).toString();
        }
            return DriverManager.getConnection(url,
        return null;
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
    }
        } catch (Exception e) {
    public static void setup(){
            LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
        try {
            System.exit(1);
            System.out.println("Setting up Database...");
        }
            Connection c = connection();
        return null;
            Statement stmt = c.createStatement();
    }

    public static void setup(){
            // Create Schema
        try {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
            LOGGER.info("Setting up Database...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");
            Connection c = connection();

            Statement stmt = c.createStatement();
            // Clean up any existing data

            stmt.executeUpdate("DELETE FROM users");
            // Create Schema
            stmt.executeUpdate("DELETE FROM comments");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");
            // Insert seed data

            insertUser("admin", "!!SuperSecretAdmin!!");
            // Clean up any existing data
            insertUser("alice", "AlicePassword!");
            stmt.executeUpdate("DELETE FROM users");
            insertUser("bob", "BobPassword!");
            stmt.executeUpdate("DELETE FROM comments");
            insertUser("eve", "$EVELknev^l");

            insertUser("rick", "!GetSchwifty!");
            // Insert seed data

            insertUser("admin", "!!SuperSecretAdmin!!");
            insertComment("rick", "cool dog m8");
            insertUser("alice", "AlicePassword!");
            insertComment("alice", "OMG so cute!");
            insertUser("bob", "BobPassword!");
            c.close();
            insertUser("eve", "$EVELknev^l");
        } catch (Exception e) {
            insertUser("rick", "!GetSchwifty!");
            System.out.println(e);

            System.exit(1);
            insertComment("rick", "cool dog m8");
        }
            insertComment("alice", "OMG so cute!");
    }
            c.close();

        } catch (Exception e) {
    // Java program to calculate MD5 hash value
            LOGGER.log(Level.SEVERE, "Error setting up database", e);
    public static String md5(String input)
            System.exit(1);
    {
        }
        try {
    }


            // Static getInstance method is called with hashing MD5
    // Java program to calculate SHA-256 hash value
            MessageDigest md = MessageDigest.getInstance("MD5");
    public static String md5(String input)

    {
            // digest() method is called to calculate message digest
        try {
            //  of an input digest() return array of byte

            byte[] messageDigest = md.digest(input.getBytes());
            // Using SHA-256 instead of MD5 for better security

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Convert byte array into signum representation

            BigInteger no = new BigInteger(1, messageDigest);
            // digest() method is called to calculate message digest

            //  of an input digest() return array of byte
            // Convert message digest into hex value
            byte[] messageDigest = md.digest(input.getBytes());
            String hashtext = no.toString(16);

            while (hashtext.length() < 32) {
            // Convert byte array into signum representation
                hashtext = "0" + hashtext;
            BigInteger no = new BigInteger(1, messageDigest);
            }

            return hashtext;
            // Convert message digest into hex value
        }
            StringBuilder hashtext = new StringBuilder(no.toString(16));

            while (hashtext.length() < 32) {
        // For specifying wrong message digest algorithms
                hashtext.insert(0, "0");
        catch (NoSuchAlgorithmException e) {
            }
            throw new RuntimeException(e);
            return hashtext.toString();
        }
        }
    }


        // For specifying wrong message digest algorithms
    private static void insertUser(String username, String password) {
        catch (NoSuchAlgorithmException e) {
       String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
            throw new HashingException("Error hashing password", e);
       PreparedStatement pStatement = null;
        }
       try {
    }
          pStatement = connection().prepareStatement(sql);

          pStatement.setString(1, UUID.randomUUID().toString());
    private static void insertUser(String username, String password) {
          pStatement.setString(2, username);
       String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
          pStatement.setString(3, md5(password));
       try (Connection conn = connection();
          pStatement.executeUpdate();
            PreparedStatement pStatement = conn.prepareStatement(sql)) {
       } catch(Exception e) {
          pStatement.setString(1, UUID.randomUUID().toString());
         e.printStackTrace();
          pStatement.setString(2, username);
       }
          pStatement.setString(3, md5(password));
    }
          pStatement.executeUpdate();

       } catch(Exception e) {
    private static void insertComment(String username, String body) {
         LOGGER.log(Level.SEVERE, "Error inserting user", e);
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
       }
        PreparedStatement pStatement = null;
    }
        try {

            pStatement = connection().prepareStatement(sql);
    private static void insertComment(String username, String body) {
            pStatement.setString(1, UUID.randomUUID().toString());
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
            pStatement.setString(2, username);
        try (Connection conn = connection();
            pStatement.setString(3, body);
             PreparedStatement pStatement = conn.prepareStatement(sql)) {
            pStatement.executeUpdate();
            pStatement.setString(1, UUID.randomUUID().toString());
        } catch(Exception e) {
            pStatement.setString(2, username);
            e.printStackTrace();
            pStatement.setString(3, body);
        }
            pStatement.executeUpdate();
    }
        } catch(Exception e) {
}
            LOGGER.log(Level.SEVERE, "Error inserting comment", e);

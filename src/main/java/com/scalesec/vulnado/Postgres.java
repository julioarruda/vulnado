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
    private static final Logger LOGGER = Logger.getLogger(Postgres.class.getName());
    public static Connection connection() {

        try {
    // Private constructor to hide the implicit public one
            Class.forName("org.postgresql.Driver");
    private Postgres() {
            String url = new StringBuilder()
        // Private constructor to hide the implicit public one
                    .append("jdbc:postgresql://")
    }
                    .append(System.getenv("PGHOST"))

                    .append("/")
    public static Connection connection() {
                    .append(System.getenv("PGDATABASE")).toString();
        try {
            return DriverManager.getConnection(url,
            String url = new StringBuilder()
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
                    .append("jdbc:postgresql://")
        } catch (Exception e) {
                    .append(System.getenv("PGHOST"))
            e.printStackTrace();
                    .append("/")
            System.err.println(e.getClass().getName()+": "+e.getMessage());
                    .append(System.getenv("PGDATABASE")).toString();
            System.exit(1);
            return DriverManager.getConnection(url,
        }
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        return null;
        } catch (Exception e) {
    }
            LOGGER.log(Level.SEVERE, "Database connection error", e);
    public static void setup(){
            System.exit(1);
        try {
        }
            System.out.println("Setting up Database...");
        return null;
            Connection c = connection();
    }
            Statement stmt = c.createStatement();
    public static void setup(){

        try {
            // Create Schema
            LOGGER.info("Setting up Database...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
            Connection c = connection();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");
            Statement stmt = c.createStatement();


            // Clean up any existing data
            // Create Schema
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (50) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
            stmt.executeUpdate("DELETE FROM comments");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments(id VARCHAR (36) PRIMARY KEY, username VARCHAR (36), body VARCHAR (500), created_on TIMESTAMP NOT NULL)");


            // Insert seed data
            // Clean up any existing data
            insertUser("admin", "!!SuperSecretAdmin!!");
            stmt.executeUpdate("DELETE FROM users");
            insertUser("alice", "AlicePassword!");
            stmt.executeUpdate("DELETE FROM comments");
            insertUser("bob", "BobPassword!");

            insertUser("eve", "$EVELknev^l");
            // Insert seed data
            insertUser("rick", "!GetSchwifty!");
            insertUser("admin", "!!SuperSecretAdmin!!");

            insertUser("alice", "AlicePassword!");
            insertComment("rick", "cool dog m8");
            insertUser("bob", "BobPassword!");
            insertComment("alice", "OMG so cute!");
            insertUser("eve", "$EVELknev^l");
            c.close();
            insertUser("rick", "!GetSchwifty!");
        } catch (Exception e) {

            System.out.println(e);
            insertComment("rick", "cool dog m8");
            System.exit(1);
            insertComment("alice", "OMG so cute!");
        }
            c.close();
    }
        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, "Database setup error", e);
    // Java program to calculate MD5 hash value
            System.exit(1);
    public static String md5(String input)
        }
    {
    }
        try {


    // Java program to calculate SHA-256 hash value
            // Static getInstance method is called with hashing MD5
    public static String md5(String input)
            MessageDigest md = MessageDigest.getInstance("MD5");
    {

        try {
            // digest() method is called to calculate message digest

            //  of an input digest() return array of byte
            // Static getInstance method is called with hashing SHA-256
            byte[] messageDigest = md.digest(input.getBytes());
            MessageDigest md = MessageDigest.getInstance("SHA-256");


            // Convert byte array into signum representation
            // digest() method is called to calculate message digest
            BigInteger no = new BigInteger(1, messageDigest);
            //  of an input digest() return array of byte

            byte[] messageDigest = md.digest(input.getBytes());
            // Convert message digest into hex value

            String hashtext = no.toString(16);
            // Convert byte array into signum representation
            while (hashtext.length() < 32) {
            BigInteger no = new BigInteger(1, messageDigest);
                hashtext = "0" + hashtext;

            }
            // Convert message digest into hex value
            return hashtext;
            StringBuilder hashtext = new StringBuilder(no.toString(16));
        }
            while (hashtext.length() < 32) {

                hashtext.insert(0, "0");
        // For specifying wrong message digest algorithms
            }
        catch (NoSuchAlgorithmException e) {
            return hashtext.toString();
            throw new RuntimeException(e);
        }
        }

    }
        // For specifying wrong message digest algorithms

        catch (NoSuchAlgorithmException e) {
    private static void insertUser(String username, String password) {
            throw new HashingException("Error hashing password", e);
       String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
        }
       PreparedStatement pStatement = null;
    }
       try {

          pStatement = connection().prepareStatement(sql);
    private static void insertUser(String username, String password) {
          pStatement.setString(1, UUID.randomUUID().toString());
       String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
          pStatement.setString(2, username);
       try (Connection conn = connection();
          pStatement.setString(3, md5(password));
            PreparedStatement pStatement = conn.prepareStatement(sql)) {
          pStatement.executeUpdate();
          pStatement.setString(1, UUID.randomUUID().toString());
       } catch(Exception e) {
          pStatement.setString(2, username);
         e.printStackTrace();
          pStatement.setString(3, md5(password));
       }
          pStatement.executeUpdate();
    }
       } catch(Exception e) {

         LOGGER.log(Level.SEVERE, "Error inserting user", e);
    private static void insertComment(String username, String body) {
       }
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
    }
        PreparedStatement pStatement = null;

        try {
    private static void insertComment(String username, String body) {
            pStatement = connection().prepareStatement(sql);
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
            pStatement.setString(1, UUID.randomUUID().toString());
        try (Connection conn = connection();
            pStatement.setString(2, username);
             PreparedStatement pStatement = conn.prepareStatement(sql)) {
            pStatement.setString(3, body);
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.executeUpdate();
            pStatement.setString(2, username);
        } catch(Exception e) {
            pStatement.setString(3, body);
            e.printStackTrace();
            pStatement.executeUpdate();
        }
        } catch(Exception e) {
    }
            LOGGER.log(Level.SEVERE, "Error inserting comment", e);
}
        }

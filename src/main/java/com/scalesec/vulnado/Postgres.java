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

    private Postgres() {}
    public static Connection connection() {
        try {
            // Class.forName is not needed since JDBC 4.0
            String url = new StringBuilder()
                    .append("jdbc:postgresql://")
                    .append(System.getenv("PGHOST"))
                    .append("/")
                    .append(System.getenv("PGDATABASE")).toString();
            return DriverManager.getConnection(url,
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            LOGGER.severe(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
    public static void setup(){
        try {
            LOGGER.info("Setting up Database...");
            Connection c = connection();
            Statement stmt = c.createStatement();

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
            LOGGER.log(Level.SEVERE, "Database setup error", e);
            System.exit(1);
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input)
    {
        // This method should not be used in production code

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
       PreparedStatement pStatement = null;
       try {
            throw new HashingException("Error generating hash", e);
          pStatement.setString(1, UUID.randomUUID().toString());
          pStatement.setString(2, username);
          pStatement.setString(3, md5(password));
          pStatement.executeUpdate();
       } catch(Exception e) {
         e.printStackTrace();
       }
       Connection conn = null;
       try {

          conn = connection();
    private static void insertComment(String username, String body) {
          pStatement = conn.prepareStatement(sql);
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
          pStatement.setString(1, UUID.randomUUID().toString());
          pStatement.setString(2, username);
          pStatement.setString(3, md5(password));
          pStatement.executeUpdate();
       } catch(Exception e) {
         LOGGER.log(Level.SEVERE, "Error inserting user", e);
       } finally {
           try {
        } catch(Exception e) {
               if (pStatement != null) pStatement.close();
            e.printStackTrace();
               if (conn != null) conn.close();
        }
           } catch (Exception e) {
    }
               LOGGER.log(Level.SEVERE, "Error closing resources", e);
}
           }

import java.util.logging.Logger;
package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

    private static final Logger logger = Logger.getLogger(Postgres.class.getName());
public class Postgres {


    private Postgres() {}
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
            logger.severe(e.getClass().getName() + ": " + e.getMessage());
            logger.severe(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
        return null;
    }
    public static void setup(){
        try {
            logger.info("Setting up Database...");
            Connection c = connection();
            logger.info("Database setup completed.");

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
            logger.severe("Error during database setup: " + e.getMessage());
            logger.severe("Error during database setup: " + e.getMessage());
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input)
    {
        try {
            // Warning: MD5 is a weak hash algorithm and should not be used for sensitive data

            logger.warning("MD5 is being used, which is a weak hash algorithm.");
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

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
        StringBuilder hashtext = new StringBuilder();
        hashtext.append(no.toString(16));
        while (hashtext.length() < 32) {
            hashtext.insert(0, "0");
        }
        return hashtext.toString();
       try {
        throw new IllegalArgumentException("MD5 algorithm not found", e);
          pStatement.setString(1, UUID.randomUUID().toString());
          pStatement.setString(2, username);
          pStatement.setString(3, md5(password));
          pStatement.executeUpdate();
       } catch(Exception e) {
         e.printStackTrace();
       }
    }

    private static void insertComment(String username, String body) {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
        PreparedStatement pStatement = null;
        try {
            pStatement = connection().prepareStatement(sql);
            pStatement.setString(1, UUID.randomUUID().toString());
            logger.fine("Inserting comment for user: " + username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
            logger.fine("Comment inserted successfully.");
}

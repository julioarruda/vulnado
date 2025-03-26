package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Postgres {

    private static final Logger logger = Logger.getLogger(Postgres.class.getName());

    // Private constructor to prevent instantiation
    private Postgres() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Connection connection() throws DatabaseConnectionException {
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("jdbc:postgresql://")
                      .append(System.getenv("PGHOST"))
                      .append("/")
                      .append(System.getenv("PGDATABASE"));
            String url = urlBuilder.toString();
            return DriverManager.getConnection(url,
                    System.getenv("PGUSER"), System.getenv("PGPASSWORD"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
            throw new DatabaseConnectionException("Failed to establish database connection", e);
        }
    }
    
    public static void setup() {
        try {
            logger.info("Setting up Database...");
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
        } catch (DatabaseConnectionException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to setup database", e);
            System.exit(1);
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input) throws HashCalculationException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Failed to compute MD5 hash", e);
            throw new HashCalculationException("Failed to compute MD5 hash", e);
        }
    }

    private static void insertUser(String username, String password) {
       String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
       PreparedStatement pStatement = null;
       try {
          pStatement = connection().prepareStatement(sql);
          pStatement.setString(1, UUID.randomUUID().toString());
          pStatement.setString(2, username);
          pStatement.setString(3, md5(password));
          pStatement.executeUpdate();
          logger.info("User inserted: " + username);
       } catch (DatabaseConnectionException | HashCalculationException e) {
         logger.log(Level.SEVERE, "Failed to insert user: " + username, e);
       } catch (SQLException e) {
         logger.log(Level.SEVERE, "Failed to execute user insertion for: " + username, e);
       } finally {
         if (pStatement != null) {
            try {
                pStatement.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to close PreparedStatement", e);
            }
         }
       }
    }

    private static void insertComment(String username, String body) {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
        PreparedStatement pStatement = null;
        try {
            pStatement = connection().prepareStatement(sql);
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
            logger.info("Comment inserted by: " + username);
        } catch (DatabaseConnectionException e) {
            logger.log(Level.SEVERE, "Failed to establish connection for comment insertion by: " + username, e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute comment insertion by: " + username, e);
        } finally {
            if (pStatement != null) {
                try {
                    pStatement.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Failed to close PreparedStatement", e);
                }
            }
        }
    }
}

// Custom exceptions
class DatabaseConnectionException extends Exception {
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

class HashCalculationException extends Exception {
    public HashCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
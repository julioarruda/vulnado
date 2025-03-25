package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Postgres {

    private static final Logger logger = LoggerFactory.getLogger(Postgres.class);

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
        } catch (SQLException e) {
            logger.error("Connection error: ", e);
            throw new DatabaseConnectionException("Failed to establish database connection", e);
        }
    }
    
    public static void setup() throws DatabaseSetupException {
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
        } catch (SQLException | DatabaseConnectionException e) {
            logger.error("Setup error: ", e);
            throw new DatabaseSetupException("Failed during database setup", e);
        }
    }

    // Java program to calculate MD5 hash value
    public static String md5(String input) throws HashingException {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            StringBuilder hashtextBuilder = new StringBuilder(no.toString(16));
            while (hashtextBuilder.length() < 32) {
                hashtextBuilder.insert(0, "0");
            }
            return hashtextBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 algorithm error: ", e);
            throw new HashingException("Failed to calculate MD5 hash", e);
        }
    }

    private static void insertUser(String username, String password) throws DatabaseInsertionException {
        String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
        PreparedStatement pStatement = null;
        try {
            pStatement = connection().prepareStatement(sql);
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, md5(password));
            pStatement.executeUpdate();
        } catch (SQLException | DatabaseConnectionException | HashingException e) {
            logger.error("Error inserting user: ", e);
            throw new DatabaseInsertionException("Failed to insert user into database", e);
        }
    }

    private static void insertComment(String username, String body) throws DatabaseInsertionException {
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
        PreparedStatement pStatement = null;
        try {
            pStatement = connection().prepareStatement(sql);
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch (SQLException | DatabaseConnectionException e) {
            logger.error("Error inserting comment: ", e);
            throw new DatabaseInsertionException("Failed to insert comment into database", e);
        }
    }
}

// Custom exception classes
class DatabaseConnectionException extends Exception {
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

class DatabaseSetupException extends Exception {
    public DatabaseSetupException(String message, Throwable cause) {
        super(message, cause);
    }
}

class HashingException extends Exception {
    public HashingException(String message, Throwable cause) {
        super(message, cause);
    }
}

class DatabaseInsertionException extends Exception {
    public DatabaseInsertionException(String message, Throwable cause) {
        super(message, cause);
    }
}
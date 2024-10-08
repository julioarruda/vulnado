package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;

public class Postgres {

    private static final Logger LOGGER = Logger.getLogger(Postgres.class.getName());

    public static Connection connection() {
        String host = System.getenv("PGHOST");
        String database = System.getenv("PGDATABASE");
        String user = System.getenv("PGUSER");
        String password = System.getenv("PGPASSWORD");

        if (host == null || database == null || user == null || password == null) {
            throw new IllegalStateException("Missing database environment variables");
        }

        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://" + host + "/" + database;
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            LOGGER.severe("Database connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void setup() {
        try {
            LOGGER.info("Setting up Database...");
            Connection c = connection();
            Statement stmt = c.createStatement();

            // Create Schema
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR (36) PRIMARY KEY, username VARCHAR (50) UNIQUE NOT NULL, password VARCHAR (255) NOT NULL, created_on TIMESTAMP NOT NULL, last_login TIMESTAMP)");
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
            LOGGER.severe("Error setting up database: " + e.getMessage());
            System.exit(1);
        }
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    private static void insertUser(String username, String password) {
        String sql = "INSERT INTO users (user_id, username, password, created_on) VALUES (?, ?, ?, current_timestamp)";
        PreparedStatement pStatement = null;
        try {
            String hashedPassword = hashPassword(password);
            pStatement = connection().prepareStatement(sql);
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, hashedPassword);
            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.severe("Error inserting user: " + e.getMessage());
        } finally {
            password = null; // Limpar a variável de senha
        }
    }

    private static void insertComment(String username, String body) {
        if (body.length() > 500) {
            throw new IllegalArgumentException("Comment body exceeds maximum length of 500 characters.");
        }
        String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?, ?, ?, current_timestamp)";
        PreparedStatement pStatement = null;
        try {
            pStatement = connection().prepareStatement(sql);
            pStatement.setString(1, UUID.randomUUID().toString());
            pStatement.setString(2, username);
            pStatement.setString(3, body);
            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.severe("Error inserting comment: " + e.getMessage());
        }
    }
}

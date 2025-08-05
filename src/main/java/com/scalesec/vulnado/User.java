package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.logging.Logger;

  public class User {
    private static final Logger LOGGER = Logger.getLogger(User.class.getName());
    private String id;
    private String username;
    private String hashedPassword;

    public User(String id, String username, String hashedPassword) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String token(String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder().setSubject(this.username).signWith(key).compact();
    }

    public static void assertAuth(String secret, String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token);
        } catch (Exception e) {
            LOGGER.severe("Authentication failed: " + e.getMessage());
            throw new Unauthorized(e.getMessage());
        }
    }

    public static User fetch(String un) {
        User user = null;
        String query = "select * from users where username = ? limit 1";
        try (Connection cxn = Postgres.connection();
             PreparedStatement stmt = cxn.prepareStatement(query)) {
            LOGGER.info("Opened database successfully");

            stmt.setString(1, un);
            LOGGER.info("Executing query: " + query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("userid");
                String username = rs.getString("username");
                String password = rs.getString("password");
                user = new User(userId, username, password);
            }
        } catch (Exception e) {
            LOGGER.severe("Database error: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return user;
    }

    // Getter methods
    public String getId() {
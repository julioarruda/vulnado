package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
  private static final Logger logger = Logger.getLogger(User.class.getName());
  private String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  public String token(SecretKey key) {
    return Jwts.builder()
               .setSubject(this.username)
               .signWith(key)
               .compact();
  }

  public static void assertAuth(SecretKey key, String token) {
    try {
      JwtParser parser = Jwts.parserBuilder()
                             .setSigningKey(key)
                             .build();
      Claims claims = parser.parseClaimsJws(token).getBody();
      logger.info("Token validated for user: " + claims.getSubject());
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Invalid token", e);
      throw new Unauthorized("Invalid token");
    }
  }

  public static User fetch(String username) {
    String query = "SELECT user_id, username, password FROM users WHERE username = ? LIMIT 1";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      
      preparedStatement.setString(1, username);
      try (ResultSet rs = preparedStatement.executeQuery()) {
        if (rs.next()) {
          String userId = rs.getString("user_id");
          String userName = rs.getString("username");
          String password = rs.getString("password");
          return new User(userId, userName, password);
        } else {
          logger.warning("User not found: " + username);
          return null;
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error fetching user from database", e);
      throw new RuntimeException("Error fetching user", e);
    }
  }

  public static SecretKey generateKey(String secret) {
    if (secret == null || secret.isEmpty()) {
      throw new IllegalArgumentException("Secret cannot be null or empty");
    }
    return Keys.hmacShaKeyFor(secret.getBytes());
  }
}

package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.logging.Logger;
import java.util.logging.Level;

public class User {
  private static final Logger logger = Logger.getLogger(User.class.getName());
  private final String id;
  private String username;
  private String hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder().setSubject(this.username).signWith(key).compact();
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);
    } catch(Exception e) {
      logger.log(Level.SEVERE, "Authentication failed", e);
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    PreparedStatement stmt = null;
    User user = null;
    Connection cxn = null;
    try {
      cxn = Postgres.connection();
      logger.info("Opened database successfully");

      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      stmt = cxn.prepareStatement(query);
      stmt.setString(1, un);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(userId, username, password);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
    } finally {
      try {
        if (stmt != null) stmt.close();
        if (cxn != null) cxn.close();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Failed to close resources", e);
      }
    }
    return user;
  }
}
package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.logging.Level;
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

  public String getUsername() {
    return username;
  }

  protected String getHashedPassword() {
    return hashedPassword;
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
      LOGGER.log(Level.SEVERE, "Unauthorized access attempt", e);
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    PreparedStatement stmt = null;
    Connection cxn = null;
    User user = null;
    try {
      cxn = Postgres.connection();
      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      stmt = cxn.prepareStatement(query);
      stmt.setString(1, un);
      LOGGER.info("Executing query for user fetch");

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(userId, username, password);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
    } finally {
      closeResources(stmt, cxn);
    }
    return user;
  }

  private static void closeResources(PreparedStatement stmt, Connection cxn) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Failed to close PreparedStatement", e);
      }
    }
    if (cxn != null) {
      try {
        cxn.close();
      } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Failed to close Connection", e);
      }
    }
  }
}
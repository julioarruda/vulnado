package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.logging.Logger;
import java.util.logging.Level;

public class User {
  private static final Logger LOGGER = Logger.getLogger(User.class.getName());

  private final String id;
  private final String username;
  private final String hashedPassword;

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
      LOGGER.log(Level.WARNING, "Authorization failed", e);
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    PreparedStatement stmt = null;
    Connection cxn = null;
    User user = null;

    try {
      cxn = Postgres.connection();
      LOGGER.log(Level.INFO, "Opened database successfully");

      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      stmt = cxn.prepareStatement(query);
      stmt.setString(1, un);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
    } finally {
      try {
        if (stmt != null) stmt.close();
        if (cxn != null) cxn.close();
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error closing resources", e);
      }
    }
    return user;
  }
}
package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.logging.Logger;

public class User {
  private static final Logger logger = Logger.getLogger(User.class.getName());

  private String id;
  private String username;
  private String hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  // Getter for id
  public String getId() {
    return id;
  }

  // Getter for username
  public String getUsername() {
    return username;
  }

  // Removed Getter for hashedPassword to avoid exposing password hashes

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
      logger.severe("Authentication failed: " + e.getMessage());
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    PreparedStatement pstmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      logger.info("Opened database successfully");

      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      pstmt = cxn.prepareStatement(query);
      pstmt.setString(1, un);
      logger.info("Executing query with parameterized username");
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      logger.severe("Error fetching user: " + e.getClass().getName() + ": " + e.getMessage());
    } finally {
      return user;
    }
  }
}
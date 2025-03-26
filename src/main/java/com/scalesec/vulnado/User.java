package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
  private static final Logger logger = LoggerFactory.getLogger(User.class);
  
  private final String id;
  private String username;
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

  private void setUsername(String username) {
    this.username = username;
  }

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    String jws = Jwts.builder().setSubject(this.username).signWith(key).compact();
    return jws;
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);
    } catch(Exception e) {
      logger.error("Authorization error: {}", e.getMessage(), e);
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    Connection cxn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    User user = null;
    try {
      cxn = Postgres.connection();
      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      pstmt = cxn.prepareStatement(query);
      pstmt.setString(1, un);
      logger.info("Opened database successfully");
      rs = pstmt.executeQuery();
      if (rs.next()) {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(userId, username, password);
      }
    } catch (SQLException e) {
      logger.error("Database error: {}", e.getMessage(), e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (pstmt != null) {
          pstmt.close();
        }
        if (cxn != null) {
          cxn.close();
        }
      } catch (SQLException e) {
        logger.error("Error closing database resources: {}", e.getMessage(), e);
      }
    }
    return user;
  }
}
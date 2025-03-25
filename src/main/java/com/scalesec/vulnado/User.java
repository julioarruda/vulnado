package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
  private static final Logger logger = Logger.getLogger(User.class.getName());

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
      logger.log(Level.SEVERE, "Unauthorized access attempt.", e);
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    User user = null;
    try (Connection cxn = Postgres.connection()) {
      logger.info("Opened database successfully");

      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      try (PreparedStatement pstmt = cxn.prepareStatement(query)) {
        pstmt.setString(1, un);

        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            String user_id = rs.getString("user_id");
            String username = rs.getString("username");
            String password = rs.getString("password");
            user = new User(user_id, username, password);
          }
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while fetching user.", e);
    }
    return user;
  }
}
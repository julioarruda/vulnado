package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class User {
  private static final Logger LOGGER = Logger.getLogger(User.class.getName());
  private static final long TOKEN_EXPIRATION_TIME = 3600000; // 1 hour in milliseconds

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

  public String token(String secret) {
    if (secret.length() < 32) {
      throw new IllegalArgumentException("Secret key must be at least 32 characters long");
    }
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder()
      .setSubject(this.username)
      .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
      .signWith(key)
      .compact();
  }

  public static void assertAuth(String secret, String token) {
    if (secret.length() < 32) {
      throw new IllegalArgumentException("Secret key must be at least 32 characters long");
    }
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);
    } catch (io.jsonwebtoken.security.SecurityException | io.jsonwebtoken.MalformedJwtException e) {
      throw new Unauthorized("Invalid JWT token", e);
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      throw new Unauthorized("Expired JWT token", e);
    } catch (Exception e) {
      throw new Unauthorized("Unauthorized", e);
    }
  }

  public static User fetch(String username) {
    String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
    try (Connection cxn = Postgres.connection();
         PreparedStatement pstmt = cxn.prepareStatement(query)) {
      pstmt.setString(1, username);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String userId = rs.getString("user_id");
          String userUsername = rs.getString("username");
          String userPassword = rs.getString("password");
          return new User(userId, userUsername, userPassword);
        }
      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Database error while fetching user", e);
    }
    return null;
  }
}

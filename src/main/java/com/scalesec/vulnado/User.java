package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    String jws = Jwts.builder().setSubject(this.username).signWith(key).compact();
    return jws;
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parser()
        .setSigningKey(key)
        .parseClaimsJws(token);
    } catch(Exception e) {
      // Log securely without exposing stack trace
      System.err.println("Authentication failed: " + e.getMessage());
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    User user = null;
    String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
    try (Connection cxn = Postgres.connection();
         PreparedStatement pstmt = cxn.prepareStatement(query)) {
      
      pstmt.setString(1, un);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String user_id = rs.getString("user_id");
          String username = rs.getString("username");
          String password = rs.getString("password");
          user = new User(user_id, username, password);
        }
      }
    } catch (Exception e) {
      // Log securely without exposing stack trace
      System.err.println("Error fetching user: " + e.getMessage());
    }
    return user;
  }
}

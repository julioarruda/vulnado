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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  // Geração segura de token JWT
  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
      .setSubject(this.username)
      .signWith(key, SignatureAlgorithm.HS256)
      .compact();
  }

  // Validação segura de token JWT
  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
      Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);
    } catch (Exception e) {
      throw new Unauthorized("Invalid token: " + e.getMessage());
    }
  }

  // Consulta segura com PreparedStatement (evita SQL Injection)
  public static User fetch(String usernameInput) {
    User user = null;
    String query = "SELECT * FROM users WHERE username = ? LIMIT 1";

    try (Connection cxn = Postgres.connection();
         PreparedStatement pstmt = cxn.prepareStatement(query)) {

      pstmt.setString(1, usernameInput);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String user_id = rs.getString("user_id");
          String username = rs.getString("username");
          String password = rs.getString("password");
          user = new User(user_id, username, password);
        }
      }

    } catch (SQLException e) {
      System.err.println("Database error: " + e.getMessage());
    }

    return user;
  }
}

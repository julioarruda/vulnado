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

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    String jws = Jwts.builder()
      .setSubject(this.username)
      .signWith(key, SignatureAlgorithm.HS256)
      .compact();
    return jws;
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
      parser.parseClaimsJws(token);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String username) {
    User user = null;
    try (Connection cxn = Postgres.connection()) {
      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      try (PreparedStatement stmt = cxn.prepareStatement(query)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          String user_id = rs.getString("user_id");
          String dbUsername = rs.getString("username");
          String password = rs.getString("password");
          user = new User(user_id, dbUsername, password);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    return user;
  }
}

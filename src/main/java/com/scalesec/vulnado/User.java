import java.util.logging.Logger;
package com.scalesec.vulnado;
import java.util.logging.Level;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

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

  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder().setSubject(this.username).signWith(key).compact();
    return jws;
  }

  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parser()
        .setSigningKey(key)
        .parseClaimsJws(token);
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error parsing JWT", e);
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    Statement stmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      stmt = cxn.createStatement();
      LOGGER.info("Opened database successfully");
      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      String query = "select * from users where username = '" + un + "' limit 1";
      LOGGER.info("Executing query: " + query);
      try (PreparedStatement pstmt = cxn.prepareStatement(query)) {
        pstmt.setString(1, un);
      if (rs.next()) {
        ResultSet rs = pstmt.executeQuery();
        String user_id = rs.getString("user_id");
        if (rs.next()) {
          String userId = rs.getString("user_id");
          String username = rs.getString("username");
          String password = rs.getString("password");
          user = new User(userId, username, password);
        }
      }
      LOGGER.log(Level.SEVERE, "Error fetching user", e);
    } finally {
      return user;
    }
  }
}

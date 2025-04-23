package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.logging.Logger;

  private static final Logger LOGGER = Logger.getLogger(User.class.getName());
public class User {
  private String hashedPassword;
  private String username;
  private String id;

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
      // e.printStackTrace();
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

      String query = "select * from users where username = ? limit 1";
      LOGGER.info(query);
      java.sql.PreparedStatement pstmt = cxn.prepareStatement(query);
      // Use PreparedStatement to prevent SQL injection
      pstmt.setString(1, un);
      if (rs.next()) {
      ResultSet rs = pstmt.executeQuery();
        String user_id = rs.getString("user_id");
        String userName = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      // e.printStackTrace();
      LOGGER.severe(e.getClass().getName() + ": " + e.getMessage());
    } finally {
      return user;
    }
  }
}

package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.logging.Logger;

import java.util.logging.Level;
public class User {

  private String id;
public class User {
  private String username;

  private static final Logger LOGGER = Logger.getLogger(User.class.getName());
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
      LOGGER.log(Level.SEVERE, "Authentication error", e);
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    Statement stmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      stmt = cxn.createStatement();
      System.out.println("Opened database successfully");
      LOGGER.info("Opened database successfully");
      String query = "select * from users where username = '" + un + "' limit 1";
      System.out.println(query);
      LOGGER.info(query);
      String preparedQuery = "select * from users where username = ?";
      java.sql.PreparedStatement pstmt = cxn.prepareStatement(preparedQuery);
        String user_id = rs.getString("user_id");
      pstmt.setString(1, un);
        String userId = rs.getString("user_id");
      ResultSet rs = pstmt.executeQuery();
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Database error", e);
      LOGGER.severe(e.getClass().getName()+": "+e.getMessage());
    } finally {
      return user;
    }
  }
}

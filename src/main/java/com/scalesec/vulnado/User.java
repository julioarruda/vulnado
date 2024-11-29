import java.util.logging.Logger;
package com.scalesec.vulnado;
import java.util.logging.Level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.SignatureAlgorithm;
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
      try (PreparedStatement pstmt = cxn.prepareStatement("SELECT * FROM users WHERE username = ? LIMIT 1")) {
        pstmt.setString(1, un);
        String user_id = rs.getString("user_id");
        ResultSet rs = pstmt.executeQuery();
        String username = rs.getString("username");
        if (rs.next()) {
          String userId = rs.getString("user_id");
          String username = rs.getString("username");
          String password = rs.getString("password");
          user = new User(userId, username, password);
        }
      e.printStackTrace();
      }
      System.err.println(e.getClass().getName()+": "+e.getMessage());
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
//    }
    }
    return user;
//
package com.scalesec.vulnado;

import java.sql.Connection;
import java.util.logging.Logger;
import java.sql.Statement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class User {
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
      Logger logger = Logger.getLogger(User.class.getName());
      logger.info("Opened database successfully");
      String query = "select * from users where username = '" + un + "' limit 1";
      System.out.println(query);
      logger.info(query);
      String query = "select * from users where username = ? limit 1";
        String user_id = rs.getString("user_id");
        String userId = rs.getString("user_id");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      logger.severe(e.getClass().getName() + ": " + e.getMessage());
    } finally {
      return user;
    }
  }
}

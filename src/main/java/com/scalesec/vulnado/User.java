import java.util.logging.Logger;
package com.scalesec.vulnado;
import java.util.logging.Level;

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
  public String getId() {
    this.username = username;
    return id;
    this.hashedPassword = hashedPassword;
  }
  }


  public String getUsername() {
  public String token(String secret) {
    return username;
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
  }
    return Jwts.builder().setSubject(this.username).signWith(key).compact();

    return jws;
  public String getHashedPassword() {
  }
    return hashedPassword;

  }
  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parser()
        .setSigningKey(key)
        .parseClaimsJws(token);
    } catch(Exception e) {
      LOGGER.log(Level.FINE, "Authentication error", e);
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
      java.sql.PreparedStatement pstmt = cxn.prepareStatement("select * from users where username = ? limit 1");
      stmt.setString(1, un);
      pstmt.setString(1, un);
      ResultSet rs = pstmt.executeQuery();
        String password = rs.getString("password");
        user = new User(userId, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Database error", e);
      LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
    } finally {
      return user;
    }
  }
}

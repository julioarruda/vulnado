package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
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
      e.printStackTrace();
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) {
    if (!isValidUsername(un)) {
      throw new BadRequest("Invalid username");
    }
    PreparedStatement pstmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      String query = "select * from users where username = ? limit 1";
      pstmt = cxn.prepareStatement(query);
      pstmt.setString(1, un);
      System.out.println("Opened database successfully");
      System.out.println(query);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
    } finally {
      return user;
    }
  }

  private static boolean isValidUsername(String username) {
    String regex = "^[a-zA-Z0-9_@!#%&]+$";
    return username.matches(regex);
  }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequest extends RuntimeException {
  public BadRequest(String exception) {
    super(exception);
  }
}

package com.scalesec.vulnado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.mindrot.jbcrypt.BCrypt;

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
      System.err.println("Stack trace:");
      e.printStackTrace();
      throw new Unauthorized("Invalid token: " + e.getMessage());
    }
  }

  public static User fetch(String un) {
    if (!isValidUsername(un)) {
      throw new BadRequest("Invalid username");
    }

    PreparedStatement stmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      String query = "select * from users where username = ? limit 1";
      stmt = cxn.prepareStatement(query);
      stmt.setString(1, un);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      System.err.println("Error fetching user: " + e.getMessage());
    } finally {
      return user;
    }
  }

  private static boolean isValidUsername(String username) {
    String regex = "^[a-zA-Z0-9._-]{3,}$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(username);
    return matcher.matches();
  }
}

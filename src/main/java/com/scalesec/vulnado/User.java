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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.mindrot.jbcrypt.BCrypt;

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  // Gerar um token JWT seguro
  public String token(String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    String jws = Jwts.builder()
        .setSubject(this.username)
        .signWith(key, SignatureAlgorithm.HS256)  // Definindo o algoritmo explicitamente
        .compact();
    return jws;
  }

  // Validação segura do token JWT
  public static void assertAuth(String secret, String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token);  // Valida e verifica o token JWT
    } catch (Exception e) {
      System.err.println("Falha na autenticação do token: " + e.getMessage());
      throw new Unauthorized("Token inválido ou expirado");
    }
  }

  // Fetch seguro do usuário, prevenindo injeção de SQL
  public static User fetch(String username) {
    PreparedStatement stmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      System.out.println("Conexão com o banco de dados aberta com sucesso");

      String query = "SELECT user_id, username, password FROM users WHERE username = ? LIMIT 1";
      stmt = cxn.prepareStatement(query);
      stmt.setString(1, username);  // Usando PreparedStatement para prevenir injeção de SQL
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String fetchedUsername = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, fetchedUsername, password);
      }
      cxn.close();
    } catch (SQLException e) {
      System.err.println("Erro ao buscar o usuário: " + e.getMessage());
    } finally {
      return user;
    }
  }

  // Verifica a senha utilizando hashing seguro
  public boolean verifyPassword(String plainPassword) {
    return BCrypt.checkpw(plainPassword, this.hashedPassword);
  }

  // Gera um hash seguro para a senha
  public static String hashPassword(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));  // Gera um hash com um salt seguro
  }
}

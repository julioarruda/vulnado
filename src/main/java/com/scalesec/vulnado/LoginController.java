package com.scalesec.vulnado;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class LoginController {
  @Value("${app.secret}")
  private String secret;

  @CrossOrigin(origins = "http://localhost:8080") // Restrict CORS to specific origin
  @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
  LoginResponse login(@RequestBody LoginRequest input) {
    User user = User.fetch(input.getUsername());
    if (Postgres.md5(input.getPassword()).equals(user.hashedPassword)) {
      return new LoginResponse(user.token(secret));
    } else {
      throw new Unauthorized("Access Denied");
    }
  }
}

class LoginRequest implements Serializable {
  private String username;
  private String password;
  
}
  public String getUsername() {

    return username;
class LoginResponse implements Serializable {
  }
  private String token;
  
  public LoginResponse(String msg) { this.token = msg; }
  public String getToken() {
  public void setUsername(String username) {
}
    return token;
    this.username = username;

  }
@ResponseStatus(HttpStatus.UNAUTHORIZED)
  
class Unauthorized extends RuntimeException {
  public void setToken(String token) {
  public String getPassword() {
  public Unauthorized(String exception) {
    this.token = token;
    return password;
    super(exception);
  }
  }
  
}
  public void setPassword(String password) {

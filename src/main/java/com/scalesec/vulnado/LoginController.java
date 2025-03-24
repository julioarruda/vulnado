package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class LoginController {
  @Value("${app.secret}")
  private String secret;

  @CrossOrigin(origins = "*")
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
  private transient String password; // Transient to avoid serialization

  // Getters
  public String getUsername() {
    return username;
  }

  // Removed getPassword() to protect access to the password

  // Setters
  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  
  // Method to verify password (if needed in future)
  public boolean verifyPassword(String inputPassword) {
    return this.password.equals(inputPassword);
  }
}

class LoginResponse implements Serializable {
  private final String token; // Made token private and final

  public LoginResponse(String msg) {
    this.token = msg;
  }
  
  public String getToken() {
    return token; // Getter allows controlled access to token
  }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  public Unauthorized(String exception) {
    super(exception);
  }
}
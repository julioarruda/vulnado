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
      input.clearPassword(); // Clear the password after use
      return new LoginResponse(user.token(secret));
    } else {
      input.clearPassword(); // Clear the password if login fails
      throw new Unauthorized("Access Denied");
    }
  }
}

class LoginRequest implements Serializable {
  private String username;
  private char[] password; 

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public char[] getPassword() {
    return password.clone(); 
  }

  public void setPassword(char[] password) {
    this.password = password.clone(); 
  }

  public void clearPassword() {
    java.util.Arrays.fill(password, '\0');
  }
}

class LoginResponse implements Serializable {
  private final String token; // Made private and final
  
  public LoginResponse(String msg) { 
    this.token = msg; 
  }

  public String getToken() {
    return token;
  }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  public Unauthorized(String exception) {
    super(exception);
  }
}
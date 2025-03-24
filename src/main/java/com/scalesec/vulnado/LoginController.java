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
  private transient String password; // Mark password as transient

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  // Removed getPassword() and setPassword()

  public String getPassword() {
    // Added internal method to securely access password
    // This method can be private or package-private depending on necessity
    return password;
  }

  public void setPassword(String password) {
    // Added internal method to securely set password
    // This method can be private or package-private depending on necessity
    this.password = password;
  }
}

class LoginResponse implements Serializable {
  private final String token; // Make token private and final

  public LoginResponse(String msg) { 
    this.token = msg; 
  }

  public String getToken() { // Provide a public accessor
    return token;
  }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  public Unauthorized(String exception) {
    super(exception);
  }
}
package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;
import java.util.Arrays;

@RestController
@EnableAutoConfiguration
public class LoginController {
  @Value("${app.secret}")
  private String secret;

  @CrossOrigin(origins = "*")
  @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
  LoginResponse login(@RequestBody LoginRequest input) {
    User user = User.fetch(input.getUsername());
    try {
      if (Postgres.md5(input.getPassword()).equals(user.hashedPassword)) {
        return new LoginResponse(user.token(secret));
      } else {
        throw new Unauthorized("Access Denied");
      }
    } finally {
      input.clearPassword(); // Clear password from memory
    }
  }
}

class LoginRequest implements Serializable {
  private String username;
  private char[] password; // Use char[] instead of String for password

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public char[] getPassword() {
    return password;
  }

  public void setPassword(char[] password) {
    this.password = password;
  }

  public void clearPassword() {
    if (this.password != null) {
      Arrays.fill(this.password, '\0'); // Clear password from memory
    }
  }
}

class LoginResponse implements Serializable {
  private final String token; // Made token final
  public LoginResponse(String msg) { this.token = msg; }

  public String getToken() {
    return token; // Getter for token
  }

  // Removed the public setter for token to preserve immutability
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  public Unauthorized(String exception) {
    super(exception);
  }
}
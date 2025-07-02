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

  @CrossOrigin(origins = "${allowed.origins:*}")
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
  public String getUsername() { return username; }
  private String username;
  public void setUsername(String username) { this.username = username; }
  private String password;
}
  public String getPassword() { return password; }

  public void setPassword(String password) { this.password = password; }
class LoginResponse implements Serializable {
  private String token;
  public LoginResponse(String msg) { this.token = msg; }
  public String getToken() { return token; }
}
  public void setToken(String token) { this.token = token; }

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class Unauthorized extends RuntimeException {
  public Unauthorized(String exception) {
    super(exception);
  }
}

package com.scalesec.vulnado;

import org.springframework.boot.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class RegistrationController {
  @Value("${app.secret}")
  private String secret;

  @CrossOrigin(origins = "*")
  @RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
  RegistrationResponse register(@RequestBody RegistrationRequest input) {
    // Check if username already exists
    User existingUser = User.fetch(input.username);
    if (existingUser != null) {
      throw new BadRequest("Username already exists");
    }
    
    // Create new user
    boolean userCreated = Postgres.createUser(input.username, input.password);
    if (userCreated) {
      User newUser = User.fetch(input.username);
      return new RegistrationResponse("User created successfully", newUser.token(secret));
    } else {
      throw new ServerError("Failed to create user");
    }
  }
}

class RegistrationRequest implements Serializable {
  public String username;
  public String password;
}

class RegistrationResponse implements Serializable {
  public String message;
  public String token;
  public RegistrationResponse(String message, String token) { 
    this.message = message; 
    this.token = token;
  }
}
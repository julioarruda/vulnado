package com.scalesec.vulnado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import java.util.List;
import java.io.Serializable;

@RestController
@EnableAutoConfiguration
public class CommentsController {
    @Value("app.secret")
    private String secret;

    @CrossOrigin(origins = "*")
    @GetMapping(value = "/comments", produces = "application/json")
    public List<Comment> comments(@RequestHeader(value = "x-auth-token") String token) {
    User.assertAuth(secret, token);
        return Comment.fetchall();
  }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/comments", produces = "application/json", consumes = "application/json")
    public Comment createComment(@RequestHeader(value = "x-auth-token") String token, @RequestBody CommentRequest input) {
        return Comment.create(input.getUsername(), input.getBody());
  }

    @CrossOrigin(origins = "*")
    @DeleteMapping(value = "/comments/{id}", produces = "application/json")
    public Boolean deleteComment(@RequestHeader(value = "x-auth-token") String token, @PathVariable("id") String id) {
    return Comment.delete(id);
  }
}

class CommentRequest implements Serializable {
    private String username;
    private String body;
    public String getUsername() {
}
        return username;

    }
@ResponseStatus(HttpStatus.BAD_REQUEST)
    public void setUsername(String username) {
class BadRequest extends RuntimeException {
        this.username = username;
  public BadRequest(String exception) {
    }
    super(exception);
    public String getBody() {
  }
        return body;
}
    }

    public void setBody(String body) {
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        this.body = body;
class ServerError extends RuntimeException {
    }
  public ServerError(String exception) {
    super(exception);
  }
}

package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Comment {
  private static final Logger LOGGER = Logger.getLogger(Comment.class.getName());
  private final String id;
  private final String username;
  private final Timestamp createdOn; // Should remain private final
  private String body;

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Timestamp getCreatedOn() {
    return createdOn;
  }

  public static Comment create(String username, String body) {
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    try {
      if (comment.commitComment()) {
        return comment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (Exception e) {
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> fetchAll() {
    List<Comment> comments = new ArrayList<>();
    try (Connection connection = Postgres.connection();
         Statement statement = connection.createStatement()) {

      String query = "SELECT * FROM comments;";
      ResultSet resultSet = statement.executeQuery(query);
      while (resultSet.next()) {
        String id = resultSet.getString("id");
        String username = resultSet.getString("username");
        String body = resultSet.getString("body");
        Timestamp createdOn = resultSet.getTimestamp("created_on");
        Comment comment = new Comment(id, username, body, createdOn);
        comments.add(comment);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
    }
    return comments;
  }

  public static boolean delete(String id) {
    String sql = "DELETE FROM comments WHERE id = ?";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, id);
      return 1 == preparedStatement.executeUpdate();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    return false;
  }

  private boolean commitComment() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, this.id);
      preparedStatement.setString(2, this.username);
      preparedStatement.setString(3, this.body);
      preparedStatement.setTimestamp(4, this.createdOn);
      return 1 == preparedStatement.executeUpdate();
    }
  }
}
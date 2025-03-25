package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Comment {
  private static final Logger logger = Logger.getLogger(Comment.class.getName());
  private final String id; 
  private final String body;
  private final String username;
  private static final Timestamp CREATED_ON = new Timestamp(new Date().getTime());

  public Comment(String id, String username, String body) {
    this.id = id;
    this.username = username;
    this.body = body;
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

  public static Timestamp getCreatedOn() {
    return new Timestamp(CREATED_ON.getTime());
  }

  public static Comment create(String username, String body) {
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body);
    try {
      if (comment.commit()) {
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
         Statement stmt = connection.createStatement()) {

      String query = "select * from comments;";
      ResultSet resultSet = stmt.executeQuery(query);
      while (resultSet.next()) {
        String id = resultSet.getString("id");
        String username = resultSet.getString("username");
        String body = resultSet.getString("body");
        Comment comment = new Comment(id, username, body);
        comments.add(comment);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error fetching comments", e);
    }
    return comments;
  }

  public static boolean delete(String id) {
    String sql = "DELETE FROM comments where id = ?";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, id);
      return 1 == preparedStatement.executeUpdate();
    } catch(Exception e) {
      logger.log(Level.SEVERE, "Error deleting comment with id: " + id, e);
    } 
    return false;
  }

  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, this.id);
      preparedStatement.setString(2, this.username);
      preparedStatement.setString(3, this.body);
      preparedStatement.setTimestamp(4, getCreatedOn());
      return 1 == preparedStatement.executeUpdate();
    }
  }
}
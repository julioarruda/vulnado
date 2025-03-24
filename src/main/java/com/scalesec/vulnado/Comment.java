package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Comment {
  private final String id;
  private final String username;
  private final String body;
  private final Timestamp createdOn;

  private static final String COMMENT_TABLE = "comments";
  private static final Logger logger = Logger.getLogger(Comment.class.getName());

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }

  public static Comment create(String username, String body) {
    long currentTime = new Date().getTime();
    Timestamp createdTimestamp = new Timestamp(currentTime);
    Comment newComment = new Comment(UUID.randomUUID().toString(), username, body, createdTimestamp);
    try {
      if (newComment.commit()) {
        return newComment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (Exception e) {
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> getAllComments() {
    List<Comment> comments = new ArrayList<Comment>();
    try (Connection connection = Postgres.connection();
         Statement statement = connection.createStatement()) {

      String query = "SELECT * FROM " + COMMENT_TABLE + ";";
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
      logger.log(Level.SEVERE, "Error retrieving comments", e);
    }
    return comments;
  }

  public static boolean delete(String id) {
    String sql = "DELETE FROM " + COMMENT_TABLE + " WHERE id = ?";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, id);
      return 1 == preparedStatement.executeUpdate();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error deleting comment with id: " + id, e);
    }
    return false;
  }

  private boolean commit() throws SQLException {
    String sql = "INSERT INTO " + COMMENT_TABLE + " (id, username, body, created_on) VALUES (?,?,?,?)";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, this.id);
      preparedStatement.setString(2, this.username);
      preparedStatement.setString(3, this.body);
      preparedStatement.setTimestamp(4, this.createdOn);
      return 1 == preparedStatement.executeUpdate();
    }
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

  public Timestamp getCreatedOn() {
    return createdOn;
  }
}
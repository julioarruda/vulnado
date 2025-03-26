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

  public String id;
  public String username;
  public String body;
  private final Timestamp createdOn;

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }

  public Timestamp getCreatedOn() {
    return createdOn;
  }

  public static Comment createComment(String username, String body){
    long currentTime = new Date().getTime();
    Timestamp currentTimestamp = new Timestamp(currentTime);
    Comment newComment = new Comment(UUID.randomUUID().toString(), username, body, currentTimestamp);
    try {
      if (newComment.commitComment()) {
        return newComment;
      } else {
        throw new BadRequest("Unable to save comment");
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error creating comment", e);
      throw new ServerError(e.getMessage());
    }
  }

  public static List<Comment> fetchAllComments() {
    Statement statement = null;
    List<Comment> comments = new ArrayList<>();
    try (Connection connection = Postgres.connection()) {
      statement = connection.createStatement();

      String query = "SELECT * FROM comments;";
      ResultSet resultSet = statement.executeQuery(query);
      while (resultSet.next()) {
        String commentId = resultSet.getString("id");
        String commentUsername = resultSet.getString("username");
        String commentBody = resultSet.getString("body");
        Timestamp commentCreatedOn = resultSet.getTimestamp("created_on");
        Comment comment = new Comment(commentId, commentUsername, commentBody, commentCreatedOn);
        comments.add(comment);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error fetching comments", e);
    }
    return comments;
  }

  public static boolean deleteComment(String id) {
    String sql = "DELETE FROM comments WHERE id = ?";
    try (Connection connection = Postgres.connection();
         PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, id);
      return 1 == preparedStatement.executeUpdate();
    } catch(Exception e) {
      logger.log(Level.SEVERE, "Error deleting comment", e);
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
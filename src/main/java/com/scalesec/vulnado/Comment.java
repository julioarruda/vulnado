package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Comment {
  private static final Logger logger = LoggerFactory.getLogger(Comment.class);

  private final String id;
  private final String username;
  private String body;
  private final Timestamp createdOn;

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = new Timestamp(createdOn.getTime());
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
    return new Timestamp(createdOn.getTime());
  }

  public static Comment create(String username, String body) {
    long currentTime = new Date().getTime();
    Timestamp timestamp = new Timestamp(currentTime);
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
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
    Statement stmt = null;
    List<Comment> comments = new ArrayList<>();
    try {
      Connection connection = Postgres.connection();
      stmt = connection.createStatement();

      String query = "select * from comments;";
      ResultSet resultSet = stmt.executeQuery(query);
      while (resultSet.next()) {
        String id = resultSet.getString("id");
        String username = resultSet.getString("username");
        String body = resultSet.getString("body");
        Timestamp createdOn = resultSet.getTimestamp("created_on");
        Comment comment = new Comment(id, username, body, createdOn);
        comments.add(comment);
      }
      connection.close();
    } catch (Exception e) {
      logger.error("An error occurred while fetching comments: {}", e.getMessage(), e);
    }
    return comments;
  }

  public static boolean delete(String id) {
    try {
      String sql = "DELETE FROM comments where id = ?";
      Connection connection = Postgres.connection();
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, id);
      return 1 == preparedStatement.executeUpdate();
    } catch(Exception e) {
      logger.error("An error occurred while deleting comment with id {}: {}", id, e.getMessage(), e);
    } 
    return false;
  }

  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    Connection connection = Postgres.connection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setString(1, this.id);
    preparedStatement.setString(2, this.username);
    preparedStatement.setString(3, this.body);
    preparedStatement.setTimestamp(4, this.createdOn);
    return 1 == preparedStatement.executeUpdate();
  }
}
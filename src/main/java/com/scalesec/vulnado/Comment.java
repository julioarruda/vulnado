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
  private static final Logger logger = Logger.getLogger(Comment.class.getName());

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }

  public static Comment create(String username, String body) {
    Timestamp timestamp = new Timestamp(new Date().getTime());
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
    List<Comment> comments = new ArrayList<Comment>();
    try (Connection cxn = Postgres.connection();
         Statement stmt = cxn.createStatement()) {
      String query = "select * from comments;";
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String body = rs.getString("body");
        Timestamp createdOn = rs.getTimestamp("created_on");
        Comment comment = new Comment(id, username, body, createdOn);
        comments.add(comment);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
    }
    return comments;
  }

  public static boolean delete(String id) {
    String sql = "DELETE FROM comments where id = ?";
    try (Connection con = Postgres.connection();
         PreparedStatement preparedStatement = con.prepareStatement(sql)) {
      preparedStatement.setString(1, id);
      return 1 == preparedStatement.executeUpdate();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error deleting comment with ID: " + id, e);
    }
    return false;
  }

  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    try (Connection con = Postgres.connection();
         PreparedStatement preparedStatement = con.prepareStatement(sql)) {
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
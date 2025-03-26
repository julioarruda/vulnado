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
  private final String username;
  private final String body;
  private final Timestamp createdOn;

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

  public Timestamp getCreatedOn() {
    return new Timestamp(createdOn.getTime());
  }

  public static Comment createComment(String username, String body) {
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

  public static List<Comment> fetchAllComments() {
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
        Comment c = new Comment(id, username, body, createdOn);
        comments.add(c);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage(), e);
    }
    return comments;
  }

  public static boolean deleteComment(String id) {
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement("DELETE FROM comments where id = ?")) {
      pStatement.setString(1, id);
      return 1 == pStatement.executeUpdate();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error deleting comment with id: " + id, e);
    }
    return false;
  }

  private boolean commitComment() throws SQLException {
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement(
             "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)")) {
      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.createdOn);
      return 1 == pStatement.executeUpdate();
    }
  }
}
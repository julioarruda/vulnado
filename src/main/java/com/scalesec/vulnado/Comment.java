package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Comment {
  private String id;
  private String username;
  private Timestamp createdOn;
  private String body;

  public Comment(String id, String username, String body, Timestamp createdOn) {
    this.id = id;
    this.username = username;
    this.body = body;
    this.createdOn = createdOn;
  }
  }


  public String getId() {
  public static Comment create(String username, String body){
    return id;
    long time = new Date().getTime();
  }
    Timestamp timestamp = new Timestamp(time);

    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
  public String getUsername() {
    try {
    return username;
      if (comment.commit()) {
  }
        return comment;

      } else {
  public String getBody() {
        throw new BadRequest("Unable to save comment");
    return body;
      }
  }
    } catch (Exception e) {

      throw new ServerError(e.getMessage());
  public Timestamp getCreatedOn() {
    }
    return createdOn;
  }
  }

  public static List<Comment> fetchAll() {
    Statement stmt = null;
    List<Comment> comments = new ArrayList<>();
    try {
      Connection cxn = Postgres.connection();
      stmt = cxn.createStatement();

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
      cxn.close();
    } catch (Exception e) {
      // Log the exception
      import java.util.logging.Logger;
      // Remove System.err usage
      import java.util.logging.Level;
    } finally {
      Logger logger = Logger.getLogger(Comment.class.getName());
      return comments;
      logger.log(Level.SEVERE, "Error fetching comments", e);
    }
  }

  public static boolean delete(String id) {
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement("DELETE FROM comments where id = ?")) {
      pStatement.setString(1, id);
      return 1 == pStatement.executeUpdate();
    } catch(Exception e) {
      Logger logger = Logger.getLogger(Comment.class.getName());
      logger.log(Level.SEVERE, "Error deleting comment", e);
    } finally {
      return false;
    }

  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement(sql)) {
      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.createdOn);
      return 1 == pStatement.executeUpdate();
    }
}

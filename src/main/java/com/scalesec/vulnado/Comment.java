import java.util.logging.Logger;
package com.scalesec.vulnado;
import java.util.logging.Level;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Comment {
  private static final Logger LOGGER = Logger.getLogger(Comment.class.getName());
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

  public static Comment create(String username, String body){
    long time = new Date().getTime();
    Timestamp timestamp = new Timestamp(time);
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
      Connection cxn = Postgres.connection();
      stmt = cxn.createStatement();

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
      cxn.close();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error fetching comments", e);
    } finally {
      return comments;
    }
  }

  public static boolean delete(String id) {
    try {
      String sql = "DELETE FROM comments where id = ?";
      try (Connection con = Postgres.connection();
           PreparedStatement pStatement = con.prepareStatement(sql)) {
      PreparedStatement pStatement = con.prepareStatement(sql);
        pStatement.setString(1, id);
        return 1 == pStatement.executeUpdate();
      }
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error deleting comment", e);
      return false;
  }

  private Boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    try (Connection con = Postgres.connection();
         PreparedStatement pStatement = con.prepareStatement(sql)) {
    PreparedStatement pStatement = con.prepareStatement(sql);
      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.createdOn);
      return 1 == pStatement.executeUpdate();
    }
  }
}

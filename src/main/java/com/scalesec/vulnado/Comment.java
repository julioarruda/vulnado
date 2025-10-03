package com.scalesec.vulnado;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.UUID;

public class Comment {
  private String id;
  private String username;
  private Timestamp createdOn;
  private String body;

  private static final Logger LOGGER = Logger.getLogger(Comment.class.getName());
    this.id = id;
  public Comment(String id, String username, String body, Timestamp createdOn) {
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
        Comment comment = new Comment(id, username, body, createdOn);
        comments.add(comment);
      }
      cxn.close();
    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
      LOGGER.severe(e.getClass().getName() + ": " + e.getMessage());
    } finally {
      return comments;
    }
  }

  public static boolean delete(String id) {
    Connection con = null;
    PreparedStatement pStatement = null;
      String sql = "DELETE FROM comments where id = ?";
    try {
      String sql = "DELETE FROM comments where id = ?";
      con = Postgres.connection();
      pStatement = con.prepareStatement(sql);
      pStatement.setString(1, id);
      return 1 == pStatement.executeUpdate();
    } catch(Exception e) {
      LOGGER.severe(e.getMessage());
      return false;
    } finally {
      try {
    }
        if (pStatement != null) pStatement.close();

        if (con != null) con.close();
  private boolean commit() throws SQLException {
      } catch (SQLException e) {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
        LOGGER.severe("Error closing resources: " + e.getMessage());
    Connection con = null;
    PreparedStatement pStatement = null;
      }
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    try {
    con = Postgres.connection();
    pStatement = con.prepareStatement(sql);
    pStatement.setString(3, this.body);
    pStatement.setTimestamp(4, this.createdOn);
    return 1 == pStatement.executeUpdate();
    } finally {
  }
      if (pStatement != null) pStatement.close();
}
  public String getId() {
      if (con != null) con.close();

package com.scalesec.vulnado;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

  private static final Logger LOGGER = Logger.getLogger(Comment.class.getName());
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
  public String getId() {

    return id;
  public static Comment create(String username, String body){
  }
    long time = new Date().getTime();

    Timestamp timestamp = new Timestamp(time);
  public String getUsername() {
    Comment comment = new Comment(UUID.randomUUID().toString(), username, body, timestamp);
    return username;
    try {
  }
      if (comment.commit()) {

        return comment;
  public String getBody() {
      } else {
    return body;
        throw new BadRequest("Unable to save comment");
  }
      }

    } catch (Exception e) {
  public Timestamp getCreatedOn() {
      throw new ServerError(e.getMessage());
    return createdOn;
    }
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
      LOGGER.log(Level.SEVERE, "Error fetching comments", e);
      LOGGER.log(Level.SEVERE, e.getClass().getName() + ": " + e.getMessage());
    } finally {
      return comments;
    }
  }

  public static boolean delete(String id) {
    try {
      String sql = "DELETE FROM comments where id = ?";
      Connection con = Postgres.connection();
      try (PreparedStatement pStatement = con.prepareStatement(sql)) {
      PreparedStatement pStatement = con.prepareStatement(sql);
        pStatement.setString(1, id);
        return 1 == pStatement.executeUpdate();
      } finally {
        con.close();
      }
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Error deleting comment", e);
      return false;
  private boolean commit() throws SQLException {
    String sql = "INSERT INTO comments (id, username, body, created_on) VALUES (?,?,?,?)";
    Connection con = Postgres.connection();
    try (PreparedStatement pStatement = con.prepareStatement(sql)) {
    pStatement.setString(1, this.id);
      pStatement.setString(1, this.id);
      pStatement.setString(2, this.username);
      pStatement.setString(3, this.body);
      pStatement.setTimestamp(4, this.createdOn);
      return 1 == pStatement.executeUpdate();
    } finally {
  }
      con.close();

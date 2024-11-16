import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class User {
  // Assuming the User class has these fields and a constructor
  private String user_id;
  private String username;
  private String password;

  public User(String user_id, String username, String password) {
    this.user_id = user_id;
    this.username = username;
    this.password = password;
  }

  public static User fetch(String un) {
    User user = null;
    Connection cxn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      cxn = Postgres.connection();
      String query = "SELECT * FROM users WHERE username = ? LIMIT 1";
      pstmt = cxn.prepareStatement(query);
      pstmt.setString(1, un);
      System.out.println("Opened database successfully");
      System.out.println(pstmt);

      rs = pstmt.executeQuery();
      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    } finally {
      try {
        if (rs != null) rs.close();
        if (pstmt != null) pstmt.close();
        if (cxn != null) cxn.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return user;
  }
}

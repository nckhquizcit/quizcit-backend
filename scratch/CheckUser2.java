import java.sql.*;

public class CheckUser2 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://34.87.73.15:5432/postgres?prepareThreshold=0";
        try (Connection conn = DriverManager.getConnection(url, "postgres", "quizcit");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, username, email, status FROM users WHERE email ILIKE '%nghhuan%' OR username ILIKE '%nghhuan%'")) {
            if (!rs.next()) {
                System.out.println("User not found!");
            } else {
                do {
                    System.out.println("ID: " + rs.getString("user_id"));
                    System.out.println("Username: " + rs.getString("username"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("Status: " + rs.getString("status"));
                    System.out.println("-------------------------");
                } while (rs.next());
            }
        }
    }
}

import java.sql.*;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://34.87.73.15:5432/postgres?prepareThreshold=0";
        try (Connection conn = DriverManager.getConnection(url, "postgres", "quizcit");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM email_outbox ORDER BY created_at DESC LIMIT 5")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getString("outbox_id"));
                System.out.println("Recipient: " + rs.getString("recipient"));
                System.out.println("Template: " + rs.getString("template"));
                System.out.println("Created At: " + rs.getTimestamp("created_at"));
                System.out.println("Sent At: " + rs.getTimestamp("sent_at"));
                System.out.println("Attempt Count: " + rs.getInt("attempt_count"));
                System.out.println("Last Error: " + rs.getString("last_error"));
                System.out.println("-------------------------");
            }
        }
    }
}

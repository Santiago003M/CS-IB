import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{

    private static final String URL = "jdbc:mysql://localhost:3306/COMPUTER_IA?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";  // your MySQL username
    private static final String PASSWORD = "rootroot"; // your REAL MySQL password

    public static Connection getConnection()
    {
        try
        {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e)
        {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
}

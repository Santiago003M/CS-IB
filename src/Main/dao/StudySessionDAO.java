package Main.dao;

import Main.util.DatabaseConnection;  // Add this
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudySessionDAO
{

    public int getTaskCountByStatus(String status)
    {
        String sql = "SELECT COUNT(*) FROM task WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getTotalHoursThisWeek()
    {
        String sql = "SELECT COALESCE(SUM(duration), 0) as total_sec " +
                "FROM study_session " +
                "WHERE start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql))
        {
            if (rs.next())
            {
                return rs.getInt("total_sec") / 3600.0; // Convert to hours
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public List<Object[]> getRecentSessions(int limit)
    {
        List<Object[]> sessions = new ArrayList<>();
        String sql = "SELECT s.start_time, s.duration, t.task_name, s.status_change " +
                "FROM study_session s LEFT JOIN task t ON s.task_id = t.task_id " +
                "ORDER BY s.start_time DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                Object[] row = new Object[4];
                row[0] = rs.getTimestamp("start_time");
                int seconds = rs.getInt("duration");
                row[1] = String.format("%d min", seconds / 60);
                row[2] = rs.getString("task_name") != null ? rs.getString("task_name") : "General Study";
                row[3] = rs.getBoolean("status_change") ? "Completed" : "In Progress";
                sessions.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }
}
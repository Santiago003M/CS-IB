package Main.dao;

import Main.model.Task;           // Add this
import Main.util.DatabaseConnection;  // Add this
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO
{
    public List<Object[]> getAllTasks()
    {
        List<Object[]> taskList = new ArrayList<>();

        String query = """
            SELECT 
                task_name,
                subject,
                deadline,
                status,
                total_study_time
            FROM task
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery())
        {

            while (rs.next())
            {
                Object[] row =
                        {
                        rs.getString("task_name"),
                        rs.getString("subject"),
                        rs.getTimestamp("deadline"),
                        rs.getString("status"),
                        rs.getInt("total_study_time")
                };
                taskList.add(row);
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return taskList;
    }

    public List<Task> getAllTasksWithTime()
    {

        List<Task> tasks = new ArrayList<>();

        String query =
                "SELECT t.task_id, t.task_name, t.subject, t.deadline, t.status, " +
                        "COALESCE(SUM(s.duration), 0) AS total_seconds " +
                        "FROM task t " +
                        "LEFT JOIN study_session s ON t.task_id = s.task_id " +
                        "GROUP BY t.task_id";

        try (Connection conn = DatabaseConnection.getConnection();  // unified name
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery())
        {

            while (rs.next())
            {
                Task task = new Task(
                        rs.getInt("task_id"),
                        rs.getString("task_name"),
                        rs.getString("subject"),
                        rs.getTimestamp("deadline"),
                        rs.getString("status"),
                        rs.getLong("total_seconds")
                );
                tasks.add(task);
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return tasks;
    }
    public void updateTask(Task task)
    {
        String sql = "UPDATE task SET task_name = ?, subject = ?, deadline = ?, status = ? WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {

            stmt.setString(1, task.getTaskName());
            stmt.setString(2, task.getSubject());
            stmt.setTimestamp(3, task.getDeadline());
            stmt.setString(4, task.getStatus());
            stmt.setInt(5, task.getTaskId());
            stmt.executeUpdate();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void deleteTask(int taskId)
    {
        // First delete related study sessions (foreign key constraint)
        String deleteSessions = "DELETE FROM study_session WHERE task_id = ?";
        String deleteTask = "DELETE FROM task WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection())
        {
            // Delete sessions first
            try (PreparedStatement stmt = conn.prepareStatement(deleteSessions))
            {
                stmt.setInt(1, taskId);
                stmt.executeUpdate();
            }
            // Delete task
            try (PreparedStatement stmt = conn.prepareStatement(deleteTask))
            {
                stmt.setInt(1, taskId);
                stmt.executeUpdate();
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Task getTaskById(int taskId)
    {
        String sql = "SELECT * FROM task WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                return new Task(
                        rs.getInt("task_id"),
                        rs.getString("task_name"),
                        rs.getString("subject"),
                        rs.getTimestamp("deadline"),
                        rs.getString("status"),
                        0 // Don't need time for editing
                );
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List<Task> getUpcomingDeadlines(int daysAhead)
    {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE status != 'Completed' " +
                "AND deadline BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? DAY) " +
                "ORDER BY deadline ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, daysAhead);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                tasks.add(new Task(
                        rs.getInt("task_id"),
                        rs.getString("task_name"),
                        rs.getString("subject"),
                        rs.getTimestamp("deadline"),
                        rs.getString("status"),
                        0
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tasks;
    }

    public List<Task> getTasksDueWithin24Hours()
    {
        return getUpcomingDeadlines(1); // Special case for reminders
    }

}




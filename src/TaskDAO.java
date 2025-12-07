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
}




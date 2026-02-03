package Main.view;

import Main.util.DatabaseConnection;
import javax.swing.*;
import java.sql.*;

public class AddTaskSubDashboard extends AbstractTaskForm
{

    public AddTaskSubDashboard()
    {
        this(null);
    }

    public AddTaskSubDashboard(Runnable onCloseCallback)
    {
        super("Add Task", onCloseCallback);
        statusBox.setSelectedItem("Not Started");
        setVisible(true);
    }

    @Override
    protected String getSaveButtonText()
    {
        return "Save Task";
    }

    @Override
    protected void handleSave()
    {
        if (!validateInput()) return;

        String taskName = nameField.getText().trim();
        String subject = subjectField.getText().trim();
        String status = (String) statusBox.getSelectedItem();
        Timestamp deadline = getSelectedTimestamp();

        // Save to database in background
        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                String sql = "INSERT INTO task (user_id, task_name, subject, deadline, status, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, NOW())";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql))
                {

                    stmt.setInt(1, 1); // Default user
                    stmt.setString(2, taskName);
                    stmt.setString(3, subject);
                    stmt.setTimestamp(4, deadline);
                    stmt.setString(5, status);
                    stmt.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done()
            {
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(AddTaskSubDashboard.this,
                            "Task saved successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e)
                {
                    JOptionPane.showMessageDialog(AddTaskSubDashboard.this,
                            "Error saving task: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
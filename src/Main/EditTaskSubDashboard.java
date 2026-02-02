package Main;

import Main.dao.TaskDAO;
import Main.model.Task;
import javax.swing.*;
import java.sql.*;
import java.util.Date;

public class EditTaskSubDashboard extends AbstractTaskForm
{
    private final int taskId;
    private final TaskDAO taskDAO;

    public EditTaskSubDashboard(int taskId, Runnable onCloseCallback)
    {
        super("Edit Task", onCloseCallback);
        this.taskId = taskId;
        this.taskDAO = new TaskDAO();
        loadTaskData();
        setVisible(true);
    }

    private void loadTaskData()
    {
        Task task = taskDAO.getTaskById(taskId);
        if (task != null)
        {
            nameField.setText(task.getTaskName());
            subjectField.setText(task.getSubject());
            statusBox.setSelectedItem(task.getStatus());
            calendarPicker.setDate(new Date(task.getDeadline().getTime()));
        } else
        {
            JOptionPane.showMessageDialog(this,
                    "Task not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    @Override
    protected String getSaveButtonText()
    {
        return "Save Changes";
    }

    @Override
    protected void handleSave()
    {
        if (!validateInput()) return;

        Task existingTask = taskDAO.getTaskById(taskId);
        if (existingTask == null) return;

        Task updatedTask = new Task(
                taskId,
                nameField.getText().trim(),
                subjectField.getText().trim(),
                getSelectedTimestamp(),
                (String) statusBox.getSelectedItem(),
                existingTask.getTotalSeconds()
        );

        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                taskDAO.updateTask(updatedTask);
                return null;
            }

            @Override
            protected void done()
            {
                try
                {
                    get();
                    JOptionPane.showMessageDialog(EditTaskSubDashboard.this,
                            "Task updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e)
                {
                    JOptionPane.showMessageDialog(EditTaskSubDashboard.this,
                            "Error updating task: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
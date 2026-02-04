package Main.view;

import Main.MainDashboard;
import Main.dao.TaskDAO;
import Main.model.Task;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskManagerDashboard extends JFrame
{
    private JPanel TaskManagerPanel_TMD;
    private JButton AddTask_TMD;
    private JButton EditTask_TMD;
    private JButton DeleteTask_TMD;
    private JButton LinkTaskToTimer_TMD;
    private JTable TaskTable_TMD;
    private JLabel StatusKey_TMD;
    private JButton ExitTaskManager_TMD;

    private void loadTaskTable()
    {
        TaskDAO dao = new TaskDAO();
        java.util.List<Task> tasks = dao.getAllTasksWithTime();

        String[] columns = {"ID", "Name", "Subject", "Deadline", "Status", "Time Spent"};
        Object[][] data = new Object[tasks.size()][6];

        for (int i = 0; i < tasks.size(); i++)
        {
            Task t = tasks.get(i);
            long totalSec = t.getTotalSeconds();
            long hours = totalSec / 3600;
            long minutes = (totalSec % 3600) / 60;
            String timeFormatted = hours + "h " + minutes + "m";

            data[i][0] = t.getTaskId();  // ID column (needed for Edit/Delete)
            data[i][1] = t.getTaskName();
            data[i][2] = t.getSubject();
            data[i][3] = t.getDeadline();
            data[i][4] = t.getStatus();
            data[i][5] = timeFormatted;
        }

        TaskTable_TMD.setModel(new javax.swing.table.DefaultTableModel(data, columns));

        TaskTable_TMD.getColumnModel().getColumn(0).setMinWidth(0);
        TaskTable_TMD.getColumnModel().getColumn(0).setMaxWidth(0);
    }


    public TaskManagerDashboard()
    {
        setTitle("Task Manager");
        setContentPane(TaskManagerPanel_TMD);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadTaskTable();

        // ADD TASK
        AddTask_TMD.addActionListener(e ->
        {
            new AddTaskSubDashboard(() -> loadTaskTable());
        });

        // EDIT TASK - NEW
        EditTask_TMD.addActionListener(e ->
        {
            int selectedRow = TaskTable_TMD.getSelectedRow();
            if (selectedRow == -1)
            {
                JOptionPane.showMessageDialog(this,
                        "Please select a task to edit!",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get task ID from first column
            int taskId = Integer.parseInt(TaskTable_TMD.getValueAt(selectedRow, 0).toString());
            new EditTaskSubDashboard(taskId, () -> loadTaskTable());
        });

        DeleteTask_TMD.addActionListener(e ->
        {
            int selectedRow = TaskTable_TMD.getSelectedRow();
            if (selectedRow == -1)
            {
                JOptionPane.showMessageDialog(this,
                        "Please select a task to delete!",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int taskId = Integer.parseInt(TaskTable_TMD.getValueAt(selectedRow, 0).toString());
            String taskName = TaskTable_TMD.getValueAt(selectedRow, 1).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete task: " + taskName + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION)
            {
                TaskDAO dao = new TaskDAO();
                dao.deleteTask(taskId);
                loadTaskTable();
                JOptionPane.showMessageDialog(this, "Task deleted!");
            }
        });

        ExitTaskManager_TMD.addActionListener(e ->
        {
            new MainDashboard();
            dispose();
        });

        setVisible(true);

        LinkTaskToTimer_TMD.addActionListener(e ->
        {
            int selectedRow = TaskTable_TMD.getSelectedRow();
            if (selectedRow == -1)
            {
                JOptionPane.showMessageDialog(this,
                        "Please select a task to link!",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get task ID and name from selected row
            int taskId = Integer.parseInt(TaskTable_TMD.getValueAt(selectedRow, 0).toString());
            String taskName = TaskTable_TMD.getValueAt(selectedRow, 1).toString();

            // Confirm with user
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Link task '" + taskName + "' to timer?\n\n" +
                            "This will return you to the main dashboard with this task pre-selected.",
                    "Link Task",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION)
            {
                // Return to MainDashboard with selected task
                MainDashboard main = new MainDashboard();
                main.setSelectedTask(taskId, taskName);
                dispose();
            }
        });

    }
}
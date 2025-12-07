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

        // Column names for your JTable
        String[] columns = {"Name", "Subject", "Deadline", "Status", "Time Spent"};

        // Convert List<Task> → Object[][]
        Object[][] data = new Object[tasks.size()][5];

        for (int i = 0; i < tasks.size(); i++)
        {
            Task t = tasks.get(i);

            // Convert seconds → hours + minutes
            long totalSec = t.getTotalSeconds();
            long hours = totalSec / 3600;
            long minutes = (totalSec % 3600) / 60;

            String timeFormatted = hours + "h " + minutes + "m";

            data[i][0] = t.getTaskName();
            data[i][1] = t.getSubject();
            data[i][2] = t.getDeadline();
            data[i][3] = t.getStatus();
            data[i][4] = timeFormatted;
        }

        // Set JTable model
        TaskTable_TMD.setModel(new javax.swing.table.DefaultTableModel(
                data,
                columns
        ));
    }

    public TaskManagerDashboard()
    {
        setTitle("Task Manager");
        setContentPane(TaskManagerPanel_TMD);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // OPEN AddTaskSubDashboard when clicking Add Task
        AddTask_TMD.addActionListener(e -> new AddTaskSubDashboard().setVisible(true));

        // Load tasks into JTable
        loadTaskTable();

        setVisible(true);

        ExitTaskManager_TMD.addActionListener(e ->
        {
            new MainDashboard();  // open main dashboard
            dispose();            // close task manager window
        });
    }
}
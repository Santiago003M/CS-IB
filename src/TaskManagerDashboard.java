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

    public TaskManagerDashboard()
    {
        setTitle("Task Manager");
        setContentPane(TaskManagerPanel_TMD);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // OPEN AddTaskSubDashboard when clicking Add Task
        AddTask_TMD.addActionListener(e -> new AddTaskSubDashboard().setVisible(true));

        setVisible(true);

        ExitTaskManager_TMD.addActionListener(e ->
        {
            new MainDashboard();  // open main dashboard
            dispose();            // close task manager window
        });
    }
}
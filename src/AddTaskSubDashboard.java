import javax.swing.*;
import java.awt.*;

public class AddTaskSubDashboard extends JFrame
{
    private JTextField NameOfTask_ATSD;
    private JTextField SubjectOfTask_ATSD;
    private JComboBox StatusOfTask_ATSD;
    private JLabel AddTaskSubDashboard_ATSD;
    private JPanel AddTaskSDPanel_ATSD;
    private JLabel TaskFields_ATSD;

    public AddTaskSubDashboard()
    {
        setContentPane(AddTaskSDPanel_ATSD);
        setTitle("Add Task");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
    }
}
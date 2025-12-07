import com.toedter.calendar.JCalendar;
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
    private JPanel CalendarPanel_ATSD;
    private JCalendar calendarPicker;

    public AddTaskSubDashboard()
    {
        setContentPane(AddTaskSDPanel_ATSD);
        setTitle("Add Task");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);

        // ===== CREATE CALENDAR AND SET SIZE =====
        calendarPicker = new JCalendar();
        calendarPicker.setPreferredSize(new Dimension(100, 130));  // <<-- adjust size here

        // ===== ADD TO PANEL =====
        CalendarPanel_ATSD.setLayout(new BorderLayout());
        CalendarPanel_ATSD.add(calendarPicker, BorderLayout.CENTER);

        setVisible(true);
    }
}

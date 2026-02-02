package Main;

import com.toedter.calendar.JCalendar;
import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Date;
// No need to import Task and TaskDAO since they're in the same package (Main)

public class EditTaskSubDashboard extends JFrame
{

    private JTextField nameField;
    private JTextField subjectField;
    private JComboBox<String> statusBox;
    private JCalendar calendarPicker;
    private int taskId;
    private Runnable onCloseCallback;

    public EditTaskSubDashboard(int taskId, Runnable onCloseCallback)
    {
        this.taskId = taskId;
        this.onCloseCallback = onCloseCallback;

        setTitle("Edit Task");
        setSize(500, 600); // Taller window!
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel for text fields
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        nameField = new JTextField(20);
        subjectField = new JTextField(20);
        statusBox = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed"});

        formPanel.add(new JLabel("Task Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Subject:"));
        formPanel.add(subjectField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusBox);

        // Center - Calendar with plenty of space!
        calendarPicker = new JCalendar();
        calendarPicker.setPreferredSize(new Dimension(400, 300)); // Much bigger

        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.add(new JLabel("Deadline:", JLabel.CENTER), BorderLayout.NORTH);
        calendarPanel.add(calendarPicker, BorderLayout.CENTER);

        // Bottom - Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveTask());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        // Assemble
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadTaskData();

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosed(java.awt.event.WindowEvent e)
            {
                if (onCloseCallback != null) onCloseCallback.run();
            }
        });

        setVisible(true);
    }


    private void loadTaskData()
    {
        TaskDAO dao = new TaskDAO();
        Task task = dao.getTaskById(taskId);

        if (task != null)
        {
            nameField.setText(task.getTaskName());
            subjectField.setText(task.getSubject());
            statusBox.setSelectedItem(task.getStatus());
            calendarPicker.setDate(new Date(task.getDeadline().getTime()));
        }
    }

    private void saveTask()
    {
        String name = nameField.getText().trim();
        String subject = subjectField.getText().trim();

        if (name.isEmpty() || subject.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!");
            return;
        }

        TaskDAO dao = new TaskDAO();
        Task existingTask = dao.getTaskById(taskId);

        Task updatedTask = new Task(
                taskId,
                name,
                subject,
                new Timestamp(calendarPicker.getDate().getTime()),
                (String) statusBox.getSelectedItem(),
                existingTask.getTotalSeconds()
        );

        dao.updateTask(updatedTask);
        JOptionPane.showMessageDialog(this, "Task updated successfully!");
        dispose();
    }
}
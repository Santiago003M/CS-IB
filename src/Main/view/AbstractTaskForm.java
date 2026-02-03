package Main.view;

import com.toedter.calendar.JCalendar;
import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Date;

public abstract class AbstractTaskForm extends JFrame
{
    protected JTextField nameField;
    protected JTextField subjectField;
    protected JComboBox<String> statusBox;
    protected JCalendar calendarPicker;
    protected Runnable onCloseCallback;

    public AbstractTaskForm(String title, Runnable onCloseCallback)
    {
        this.onCloseCallback = onCloseCallback;
        setTitle(title);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initializeComponents();
        layoutComponents();
        addEventListeners();
    }

    private void initializeComponents()
    {
        nameField = new JTextField(20);
        subjectField = new JTextField(20);
        statusBox = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed"});

        calendarPicker = new JCalendar();
        calendarPicker.setPreferredSize(new Dimension(400, 300));

        // Prevent selecting past dates
        calendarPicker.setMinSelectableDate(new Date());
    }

    private void layoutComponents()
    {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("Task Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Subject:"));
        formPanel.add(subjectField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusBox);

        // Calendar panel
        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.add(new JLabel("Deadline:", JLabel.CENTER), BorderLayout.NORTH);
        calendarPanel.add(calendarPicker, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton(getSaveButtonText());
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addEventListeners()
    {
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosed(java.awt.event.WindowEvent e)
            {
                if (onCloseCallback != null) onCloseCallback.run();
            }
        });
    }

    protected boolean validateInput()
    {
        String name = nameField.getText().trim();
        String subject = subjectField.getText().trim();

        if (name.isEmpty() || subject.isEmpty())
        {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if date is in the past
        Date selectedDate = calendarPicker.getDate();
        if (selectedDate.before(new Date()))
        {
            JOptionPane.showMessageDialog(this,
                    "Deadline cannot be in the past!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    protected Timestamp getSelectedTimestamp()
    {
        return new Timestamp(calendarPicker.getDate().getTime());
    }

    protected abstract String getSaveButtonText();
    protected abstract void handleSave();
}
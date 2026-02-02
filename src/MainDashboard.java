package Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.sql.*;


public class MainDashboard extends JFrame
{
    // Form-bound fields (must match MainDashboard.form exactly):
    private JPanel MainDashboard;
    private JPanel MainDashboard_2;
    private JPanel MainDashboard_3;
    private JPanel MainTimer_MD;
    private JButton StartTimer_MD;
    private JButton PauseTimer_MD;
    private JButton ResumeTimer_MD;
    private JButton ResetTimer_MD;
    private JTable UpcomingDeadlinesTable_MD;
    private JLabel UpcomingDeadlines_MD;
    private JButton GoToTaskManager_MD;
    private JButton GoToProdLog_MD;
    private JButton Exit_MD;
    private JButton Settings;
    private JComboBox TaskSelector_MD;

    // Non-form fields (runtime only):
    private JLabel timeLabel;
    private Timer timer;
    private int elapsedTime = 0;
    private int seconds = 0, minutes = 0, hours = 0;
    private boolean running = false;
    private long lastActivityTime = System.currentTimeMillis();
    private Timer inactivityTimer;
    private static final int INACTIVITY_LIMIT = 5 * 60 * 1000; // 5 minutes in milliseconds

    private int currentSessionId = -1;
    private int currentTaskId = -1;

    public MainDashboard()
    {
        setContentPane(MainDashboard);
        setTitle("MainDashboard");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        loadTasksIntoDropdown();
        loadUpcomingDeadlines();      // ADD THIS
        checkAndShowReminders();      // ADD THIS
        setupInactivityMonitor();

        // Setup timer display inside MainTimer_MD
        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Verdana", Font.PLAIN, 30));
        if (MainTimer_MD != null)
        {
            MainTimer_MD.setLayout(new BorderLayout());
            MainTimer_MD.add(timeLabel, BorderLayout.CENTER);
        }

        // Load tasks
        if (TaskSelector_MD != null)
        {
            loadTasksIntoDropdown();
            setupInactivityMonitor();
        }

        // Timer
        timer = new Timer(1000, e ->
        {
            elapsedTime += 1000;
            hours = (elapsedTime / 3600000);
            minutes = (elapsedTime / 60000) % 60;
            seconds = (elapsedTime / 1000) % 60;
            timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        });

        // Listeners
        StartTimer_MD.addActionListener(e -> startTimer());
        PauseTimer_MD.addActionListener(e -> pauseTimer());
        ResumeTimer_MD.addActionListener(e ->
        {
            if (!running) { running = true; timer.start(); }
        });
        ResetTimer_MD.addActionListener(e -> resetTimer());
        Exit_MD.addActionListener(e -> System.exit(0));

        GoToTaskManager_MD.addActionListener(e ->
        {
            new TaskManagerDashboard();
            dispose();
        });

        GoToProdLog_MD.addActionListener(e ->
        {
            new ProductivityLogDashboard();
            dispose();
        });

        Settings.addActionListener(e ->
        {
            new Settings();
            dispose();
        });

        setVisible(true);
    }

    private void loadTasksIntoDropdown()
    {
        TaskSelector_MD.removeAllItems();
        TaskSelector_MD.addItem("General Study");

        TaskDAO dao = new TaskDAO();
        for (Task task : dao.getAllTasksWithTime())
        {
            TaskSelector_MD.addItem(task.getTaskId() + ": " + task.getTaskName());
        }

        TaskSelector_MD.addActionListener(e ->
        {
            String selected = (String) TaskSelector_MD.getSelectedItem();
            if (selected != null && !selected.equals("General Study"))
            {
                currentTaskId = Integer.parseInt(selected.split(":")[0]);
            } else
            {
                currentTaskId = -1;
            }
        });
    }

    private void startTimer()
    {
        if (running) return;
        running = true;
        timer.start();

        String sql = "INSERT INTO study_session (user_id, task_id, start_time) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setInt(1, 1);
            if (currentTaskId > 0)
            {
                stmt.setInt(2, currentTaskId);
                updateTaskStatus(currentTaskId, "In Progress");
            } else
            {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) currentSessionId = rs.getInt(1);
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    private void pauseTimer()
    {
        if (!running) return;
        running = false;
        timer.stop();
        updateSessionDuration();
    }

    private void resetTimer()
    {
        timer.stop();
        running = false;
        endSession(false);
        elapsedTime = 0;
        timeLabel.setText("00:00:00");
    }

    private void updateSessionDuration()
    {
        if (currentSessionId == -1) return;
        String sql = "UPDATE study_session SET duration = ? WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, elapsedTime / 1000);
            stmt.setInt(2, currentSessionId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void endSession(boolean complete)
    {
        if (currentSessionId == -1) return;
        String sql = "UPDATE study_session SET end_time = NOW(), duration = ? WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, elapsedTime / 1000);
            stmt.setInt(2, currentSessionId);
            stmt.executeUpdate();
            if (complete && currentTaskId > 0) updateTaskStatus(currentTaskId, "Completed");
            currentSessionId = -1;
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateTaskStatus(int taskId, String status)
    {
        String sql = "UPDATE task SET status = ? WHERE task_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setString(1, status);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupInactivityMonitor()
    {
        // Check every 10 seconds
        inactivityTimer = new Timer(10000, e ->
        {
            if (!running) return; // Only check if timer is running

            long timeSinceLastActivity = System.currentTimeMillis() - lastActivityTime;

            if (timeSinceLastActivity > INACTIVITY_LIMIT)
            {
                // Auto-pause
                timer.stop();
                running = false;
                updateSessionDuration(); // Save current time

                // Ask user
                int response = JOptionPane.showConfirmDialog(this,
                        "No activity detected for 5 minutes.\nAre you still studying?\n\n" +
                                "Click YES to continue, NO to stop timer.",
                        "Inactivity Detected",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (response == JOptionPane.YES_OPTION)
                {
                    // Resume
                    running = true;
                    timer.start();
                    lastActivityTime = System.currentTimeMillis(); // Reset timer
                } else
                {
                    // Stop completely and reset
                    endSession(false);
                    elapsedTime = 0;
                    timeLabel.setText("00:00:00");
                }
            }
        });
        inactivityTimer.start();

        // Track ALL mouse and keyboard activity in the entire app
        Toolkit.getDefaultToolkit().addAWTEventListener(event ->
        {
            lastActivityTime = System.currentTimeMillis();
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    private void loadUpcomingDeadlines()
    {
        String[] columns = {"Task", "Subject", "Deadline", "Time Remaining"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        TaskDAO dao = new TaskDAO();
        List<Task> upcoming = dao.getUpcomingDeadlines(30); // Show next 30 days

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (Task task : upcoming)
        {
            java.time.LocalDateTime deadlineTime = task.getDeadline().toLocalDateTime();
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, deadlineTime);
            long hoursRemaining = java.time.temporal.ChronoUnit.HOURS.between(now, deadlineTime) % 24;

            String timeRemaining;
            java.time.Duration duration = java.time.Duration.between(now, deadlineTime);

            if (daysRemaining < 0)
            {
                timeRemaining = "OVERDUE!";
            } else if (daysRemaining == 0)
            {
                if (hoursRemaining <= 0)
                {
                    timeRemaining = "DUE NOW!";
                } else
                {
                    timeRemaining = hoursRemaining + " hours left!";
                }
            } else if (daysRemaining == 1)
            {
                timeRemaining = "1 day, " + hoursRemaining + "h left";
            } else
            {
                timeRemaining = daysRemaining + " days left";
            }

            // Color coding logic for urgency
            Object[] row =
            {
                    task.getTaskName(),
                    task.getSubject(),
                    task.getDeadline().toLocalDateTime().format(
                            java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                    ),
                    timeRemaining
            };
            model.addRow(row);
        }

        UpcomingDeadlinesTable_MD.setModel(model);

        // Set custom renderer to color-code urgency
        UpcomingDeadlinesTable_MD.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer()
                {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);

                        String val = (String) value;
                        if (val.contains("OVERDUE") || val.contains("DUE NOW"))
                        {
                            c.setForeground(Color.RED);
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else if (val.contains("hours left"))
                        {
                            c.setForeground(new Color(220, 53, 69)); // Red-ish
                        } else if (val.contains("1 day"))
                        {
                            c.setForeground(new Color(255, 193, 7)); // Yellow/Orange
                        } else
                        {
                            c.setForeground(new Color(34, 139, 34)); // Green
                        }
                        return c;
                    }
                }
        );

        // Adjust column widths
        UpcomingDeadlinesTable_MD.getColumnModel().getColumn(0).setPreferredWidth(150);
        UpcomingDeadlinesTable_MD.getColumnModel().getColumn(3).setPreferredWidth(120);
    }

    private void checkAndShowReminders()
    {
        // Check if reminders are enabled in settings (you'll need to load this from DB or file)
        // For now, we'll check but you should integrate with your Settings class
        boolean remindersEnabled = true; // Load from settings storage

        if (!remindersEnabled) return;

        TaskDAO dao = new TaskDAO();
        List<Task> urgentTasks = dao.getTasksDueWithin24Hours();

        if (!urgentTasks.isEmpty())
        {
            StringBuilder message = new StringBuilder();
            message.append("⚠️ REMINDER: You have tasks due soon!\n\n");

            for (Task task : urgentTasks)
            {
                java.time.Duration remaining = java.time.Duration.between(
                        java.time.LocalDateTime.now(),
                        task.getDeadline().toLocalDateTime()
                );
                long hours = remaining.toHours();

                if (hours < 0)
                {
                    message.append("• ").append(task.getTaskName())
                            .append(" - OVERDUE!\n");
                } else if (hours == 0)
                {
                    message.append("• ").append(task.getTaskName())
                            .append(" - Due within the hour!\n");
                } else
                {
                    message.append("• ").append(task.getTaskName())
                            .append(" - ").append(hours).append(" hours remaining\n");
                }
            }

            message.append("\nStay focused and complete these tasks!");

            JOptionPane.showMessageDialog(
                    this,
                    message.toString(),
                    "Daily Reminder - Upcoming Deadlines",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }


    public static void main(String[] args) { new MainDashboard(); }
}
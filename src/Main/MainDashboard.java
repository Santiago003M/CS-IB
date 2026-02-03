package Main;

import Main.dao.TaskDAO;
import Main.model.Task;
import Main.util.DatabaseConnection;
import Main.service.InactivityMonitor;
import Main.service.NotificationService;
import Main.service.SettingsRepository;
import Main.view.ProductivityLogDashboard;
import Main.view.Settings;
import Main.view.TaskManagerDashboard;
import Main.viewmodel.TaskViewModel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.List;
import Main.dao.StudySessionDAO;

public class MainDashboard extends JFrame
{
    // Form-bound fields (must match MainDashboard.form exactly):
    private JPanel MainDashboard;
    private JPanel MainTimer_MD;
    private JButton StartTimer_MD;
    private JButton PauseTimer_MD;
    private JButton ResumeTimer_MD;
    private JButton ResetTimer_MD;
    private JTable UpcomingDeadlinesTable_MD;
    private JButton GoToTaskManager_MD;
    private JButton GoToProdLog_MD;
    private JButton Exit_MD;
    private JButton Settings;
    private JComboBox TaskSelector_MD;

    // NEW: Service fields
    private final InactivityMonitor inactivityMonitor;
    private final NotificationService notificationService;
    private final SettingsRepository settingsRepo;
    private final TaskDAO taskDAO;

    // Non-form fields (runtime only):
    private JLabel timeLabel;
    private JButton displayStudyStreak;
    private Timer displayTimer;
    private int elapsedTime = 0;
    private boolean running = false;
    private static final int INACTIVITY_MINUTES = 5;
    private int currentSessionId = -1;
    private int currentTaskId = -1;

    public MainDashboard()
    {
        // Keep existing initialization
        setContentPane(MainDashboard);
        setTitle("Study Activity Manager");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        // NEW: Initialize services
        this.taskDAO = new TaskDAO();
        this.settingsRepo = new SettingsRepository();
        this.inactivityMonitor = new InactivityMonitor(INACTIVITY_MINUTES);
        this.notificationService = new NotificationService(taskDAO, settingsRepo, this);

        // Keep existing UI setup
        loadTasksIntoDropdown();
        loadUpcomingDeadlines();
        setupInactivityMonitor();
        setupTimerDisplay();

        // NEW: Start notification service if enabled
        if (settingsRepo.areRemindersEnabled())
        {
            notificationService.startMonitoring();
            checkAndShowReminders();
        }

        // Keep existing listeners
        attachListeners();

        setVisible(true);
    }

    private void attachListeners()
    {
        StartTimer_MD.addActionListener(e -> startTimer());
        PauseTimer_MD.addActionListener(e -> pauseTimer());
        ResumeTimer_MD.addActionListener(e -> resumeTimer());
        ResetTimer_MD.addActionListener(e -> resetTimer());
        Exit_MD.addActionListener(e ->
        {
            notificationService.stop();
            inactivityMonitor.stop();
            System.exit(0);
        });

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

        Settings.addActionListener(e -> {
            new Settings(this);
        });

        displayStudyStreak.addActionListener(e -> displayStudyStreak());
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

    private void setupTimerDisplay()
    {
        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Verdana", Font.PLAIN, 30));
        if (MainTimer_MD != null)
        {
            MainTimer_MD.setLayout(new BorderLayout());
            MainTimer_MD.add(timeLabel, BorderLayout.CENTER);
        }

        displayTimer = new Timer(1000, e ->
        {
            elapsedTime += 1000;
            updateTimeLabel();
        });
    }

    private void updateTimeLabel()
    {
        int hours = (elapsedTime / 3600000);
        int minutes = (elapsedTime / 60000) % 60;
        int seconds = (elapsedTime / 1000) % 60;
        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void startTimer()
    {
        if (running) return;

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception
            {
                String sql = "INSERT INTO study_session (user_id, task_id, start_time) VALUES (?, ?, NOW())";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
                {
                    stmt.setInt(1, 1);
                    if (currentTaskId > 0)
                    {
                        stmt.setInt(2, currentTaskId);
                    } else
                    {
                        stmt.setNull(2, Types.INTEGER);
                    }

                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next())
                    {
                        return rs.getInt(1);
                    }
                }
                return -1;
            }

            @Override
            protected void done()
            {
                try
                {
                    currentSessionId = get();
                    if (currentSessionId != -1)
                    {
                        running = true;
                        displayTimer.start();
                        if (currentTaskId > 0)
                        {
                            updateTaskStatus(currentTaskId, "In Progress");
                        }
                    }
                } catch (Exception e)
                {
                    JOptionPane.showMessageDialog(MainDashboard.this,
                            "Failed to start session: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void pauseTimer()
    {
        if (!running) return;
        running = false;
        displayTimer.stop();
        updateSessionDuration();
    }

    private void resumeTimer()
    {
        if (running) return;
        running = true;
        displayTimer.start();
    }

    private void resetTimer()
    {
        displayTimer.stop();
        running = false;
        endSession(false);
        resetTimerDisplay();
    }

    private void resetTimerDisplay()
    {
        elapsedTime = 0;
        updateTimeLabel();
    }

    private void updateSessionDuration()
    {
        if (currentSessionId == -1) return;

        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                String sql = "UPDATE study_session SET duration = ? WHERE session_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql))
                {
                    stmt.setInt(1, elapsedTime / 1000);
                    stmt.setInt(2, currentSessionId);
                    stmt.executeUpdate();
                }
                return null;
            }
        }.execute();
    }

    private void endSession(boolean complete)
    {
        if (currentSessionId == -1) return;

        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                String sql = "UPDATE study_session SET end_time = NOW(), duration = ? WHERE session_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql))
                {
                    stmt.setInt(1, elapsedTime / 1000);
                    stmt.setInt(2, currentSessionId);
                    stmt.executeUpdate();

                    if (complete && currentTaskId > 0)
                    {
                        updateTaskStatus(currentTaskId, "Completed");
                    }
                }
                return null;
            }

            @Override
            protected void done()
            {
                currentSessionId = -1;
            }
        }.execute();
    }

    private void updateTaskStatus(int taskId, String status)
    {
        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                String sql = "UPDATE task SET status = ? WHERE task_id = ?";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql))
                {
                    stmt.setString(1, status);
                    stmt.setInt(2, taskId);
                    stmt.executeUpdate();
                }
                return null;
            }
        }.execute();
    }

    private void setupInactivityMonitor()
    {
        inactivityMonitor.addListener(new InactivityMonitor.InactivityListener()
        {
            @Override
            public void onInactivityDetected()
            {
                if (running)
                {
                    handleInactivityDetected();
                }
            }

            @Override
            public void onActivityResumed()
            {
                // Not used - we handle resume via dialog
            }
        });
        inactivityMonitor.start();
    }

    private void handleInactivityDetected()
    {
        SwingUtilities.invokeLater(() ->
        {
            // Auto-pause timer
            pauseTimer();

            int response = JOptionPane.showConfirmDialog(this,
                    "No activity detected for " + INACTIVITY_MINUTES + " minutes.\n" +
                            "Are you still studying?\n\n" +
                            "Click YES to continue, NO to stop timer.",
                    "Inactivity Detected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (response == JOptionPane.YES_OPTION)
            {
                resumeTimer();
                inactivityMonitor.resetTimer();
            } else
            {
                endSession(false);
                resetTimerDisplay();
            }
        });
    }

    private void loadUpcomingDeadlines()
    {
        String[] columns = {"Task", "Subject", "Deadline", "Time Remaining"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        TaskDAO dao = new TaskDAO();
        List<Task> upcoming = dao.getUpcomingDeadlines(30);

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
                            c.setForeground(new Color(220, 53, 69));
                        } else if (val.contains("1 day"))
                        {
                            c.setForeground(new Color(255, 193, 7));
                        } else
                        {
                            c.setForeground(new Color(34, 139, 34));
                        }
                        return c;
                    }
                }
        );

        UpcomingDeadlinesTable_MD.getColumnModel().getColumn(0).setPreferredWidth(150);
        UpcomingDeadlinesTable_MD.getColumnModel().getColumn(3).setPreferredWidth(120);
    }

    private void checkAndShowReminders()
    {
        if (!settingsRepo.areRemindersEnabled()) return;

        new SwingWorker<List<Task>, Void>()
        {
            @Override
            protected List<Task> doInBackground()
            {
                return taskDAO.getTasksDueWithin24Hours();
            }

            @Override
            protected void done()
            {
                try
                {
                    List<Task> urgentTasks = get();
                    if (urgentTasks.isEmpty()) return;

                    StringBuilder message = new StringBuilder();
                    message.append("⚠️ REMINDER: You have tasks due soon!\n\n");

                    for (Task task : urgentTasks)
                    {
                        TaskViewModel vm = new TaskViewModel(task);
                        message.append("• ").append(vm.getTaskName())
                                .append(" - ").append(vm.getTimeRemaining()).append("\n");
                    }

                    JOptionPane.showMessageDialog(MainDashboard.this,
                            message.toString(),
                            "Daily Reminder - Upcoming Deadlines",
                            JOptionPane.WARNING_MESSAGE);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public static void main(String[] args) { new MainDashboard(); }

    /**
     * Called when returning from TaskManager with a selected task
     */
    public void setSelectedTask(int taskId, String taskName)
    {
        this.currentTaskId = taskId;

        // Find and select the task in the dropdown
        for (int i = 0; i < TaskSelector_MD.getItemCount(); i++)
        {
            String item = (String) TaskSelector_MD.getItemAt(i);
            if (item.startsWith(taskId + ":"))
            {
                TaskSelector_MD.setSelectedIndex(i);
                break;
            }
        }

        // Show confirmation to user
        JOptionPane.showMessageDialog(this,
                "Task '" + taskName + "' is now linked to the timer!\n\n" +
                        "Click 'Start Timer' to begin studying.",
                "Task Linked Successfully",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void displayStudyStreak()
    {
        new SwingWorker<Integer, Void>()
        {
            @Override
            protected Integer doInBackground()
            {
                StudySessionDAO dao = new StudySessionDAO();
                return dao.getStudyStreak();
            }

            @Override
            protected void done()
            {
                try
                {
                    int streak = get();
                    String message;
                    if (streak > 0)
                    {
                        message = String.format("🔥 Study Streak: %d day%s!", streak, streak > 1 ? "s" : "");
                    } else
                    {
                        message = "Start studying today to build your streak!";
                    }

                    // You can display this in a label or popup
                    JOptionPane.showMessageDialog(MainDashboard.this,
                            message,
                            "Study Streak",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

}
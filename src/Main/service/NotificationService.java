package Main.service;

import Main.dao.TaskDAO;
import Main.model.Task;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService
{
    private final ScheduledExecutorService scheduler;
    private final TaskDAO taskDAO;
    private final SettingsRepository settings;
    private final Component parentComponent;

    public NotificationService(TaskDAO taskDAO, SettingsRepository settings, Component parent)
    {
        this.taskDAO = taskDAO;
        this.settings = settings;
        this.parentComponent = parent;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r ->
        {
            Thread t = new Thread(r, "Notification-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public void startMonitoring()
    {
        if (!settings.areRemindersEnabled()) return;

        // Check immediately, then every hour
        scheduler.scheduleAtFixedRate(this::checkDeadlines, 0, 1, TimeUnit.HOURS);
    }

    public void stop()
    {
        scheduler.shutdown();
    }

    private void checkDeadlines()
    {
        if (!settings.areRemindersEnabled()) return;

        int hoursAhead = settings.getReminderHoursAhead();
        List<Task> upcoming = taskDAO.getUpcomingDeadlines(hoursAhead / 24 + 1);

        LocalDateTime now = LocalDateTime.now();
        List<Task> dueSoon = upcoming.stream()
                .filter(task ->
                {
                    Duration diff = Duration.between(now, task.getDeadline().toLocalDateTime());
                    long hours = diff.toHours();
                    return hours >= 0 && hours <= hoursAhead;
                })
                .toList();

        if (!dueSoon.isEmpty())
        {
            SwingUtilities.invokeLater(() -> showReminderDialog(dueSoon));
        }
    }

    private void showReminderDialog(List<Task> tasks)
    {
        StringBuilder message = new StringBuilder();
        message.append("⚠️ REMINDER: You have ").append(tasks.size())
                .append(" task(s) due within ")
                .append(settings.getReminderHoursAhead()).append(" hours!\n\n");

        for (Task task : tasks)
        {
            Duration remaining = Duration.between(
                    LocalDateTime.now(),
                    task.getDeadline().toLocalDateTime()
            );
            long hours = remaining.toHours();

            if (hours < 0)
            {
                message.append("• ").append(task.getTaskName()).append(" - OVERDUE!\n");
            } else if (hours == 0)
            {
                message.append("• ").append(task.getTaskName()).append(" - Due within the hour!\n");
            } else
            {
                message.append("• ").append(task.getTaskName())
                        .append(" - ").append(hours).append(" hours remaining\n");
            }
        }

        JOptionPane.showMessageDialog(
                parentComponent,
                message.toString(),
                "Deadline Reminder",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
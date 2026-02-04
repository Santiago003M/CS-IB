package Main.viewmodel;

import Main.model.Task;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TaskViewModel
{
    private final Task task;
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    public TaskViewModel(Task task)
    {
        this.task = task;
    }

    public String getTaskName() { return task.getTaskName(); }
    public String getSubject() { return task.getSubject(); }
    public String getStatus() { return task.getStatus(); }
    public int getTaskId() { return task.getTaskId(); }

    public String getFormattedDeadline()
    {
        return task.getDeadline().toLocalDateTime().format(formatter);
    }

    // calculate "3 days left left" string
    public String getTimeRemaining()
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = task.getDeadline().toLocalDateTime();

        long daysRemaining = ChronoUnit.DAYS.between(now, deadline);
        long hoursRemaining = ChronoUnit.HOURS.between(now, deadline) % 24;

        if (deadline.isBefore(now))
        {
            return "OVERDUE!";
        } else if (daysRemaining == 0 && hoursRemaining <= 0)
        {
            return "DUE NOW!";
        } else if (daysRemaining == 0)
        {
            return hoursRemaining + " hours left!";
        } else if (daysRemaining == 1)
        {
            return "1 day, " + hoursRemaining + "h left";
        } else
        {
            return daysRemaining + " days left";
        }
    }

    // This decides the color (red/yellow/green)
    public Color getUrgencyColor()
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = task.getDeadline().toLocalDateTime();

        long hoursRemaining = ChronoUnit.HOURS.between(now, deadline);

        if (hoursRemaining < 0)
        {
            return Color.RED;
        } else if (hoursRemaining <= 24)
        {
            return new Color(220, 53, 69);
        } else if (hoursRemaining <= 48)
        {
            return new Color(255, 193, 7);
        } else
        {
            return new Color(34, 139, 34);
        }
    }

    public boolean isUrgent()
    {
        LocalDateTime now = LocalDateTime.now();
        return task.getDeadline().toLocalDateTime().isBefore(now.plusHours(24));
    }

    public String getFormattedTimeSpent()
    {
        long totalSec = task.getTotalSeconds();
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}
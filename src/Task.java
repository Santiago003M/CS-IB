public class Task
{
    private int taskId;
    private String taskName;
    private String subject;
    private java.sql.Timestamp deadline;
    private String status;
    private long totalSeconds;  // total study duration in seconds

    public Task(int taskId, String taskName, String subject,
                java.sql.Timestamp deadline, String status,
                long totalSeconds)
    {
        this.taskId = taskId;
        this.taskName = taskName;
        this.subject = subject;
        this.deadline = deadline;
        this.status = status;
        this.totalSeconds = totalSeconds;
    }

    // ====== GETTERS ======
    public int getTaskId() { return taskId; }
    public String getTaskName() { return taskName; }
    public String getSubject() { return subject; }
    public java.sql.Timestamp getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public long getTotalSeconds() { return totalSeconds; }

    // Convenience methods
    public double getTotalHours()
    {
        return totalSeconds / 3600.0;
    }

    public double getTotalMinutes()
    {
        return totalSeconds / 60.0;
    }
}

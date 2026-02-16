package Main.service;

import Main.dao.TaskDAO;
import Main.model.Task;

import java.sql.SQLException;
import java.util.List;

public class TaskService
{
    private final TaskDAO taskDAO;

    public TaskService(TaskDAO taskDAO)
    {
        this.taskDAO = taskDAO;
    }

    public List<Task> getAllTasks() throws ServiceException
    {
        try
        {
            return taskDAO.getAllTasksWithTime();
        } catch (Exception e)
        {
            throw new ServiceException("Failed to load tasks", e);
        }
    }

    public void deleteTask(int taskId) throws ServiceException
    {
        try
        {
            taskDAO.deleteTask(taskId);
        } catch (Exception e)
        {
            throw new ServiceException("Failed to delete task", e);
        }
    }

    private void validateTask(Task task) throws ServiceException
    {
        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty())
        {
            throw new ServiceException("Task name cannot be empty");
        }
        if (task.getDeadline() == null)
        {
            throw new ServiceException("Deadline cannot be null");
        }
        if (task.getDeadline().toLocalDateTime().isBefore(java.time.LocalDateTime.now()))
        {
            throw new ServiceException("Deadline cannot be in the past");
        }
    }

    public static class ServiceException extends Exception
    {
        public ServiceException(String message) { super(message); }
        public ServiceException(String message, Throwable cause) { super(message, cause); }
    }
}
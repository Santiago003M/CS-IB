package Main.service;

import java.io.*;
import java.util.Properties;

public class SettingsRepository
{
    private static final String CONFIG_FILE = "app_settings.properties";
    private Properties properties;
    private boolean remindersEnabled = true;
    private int reminderHoursAhead = 24;

    public SettingsRepository()
    {
        loadSettings();
    }

    private void loadSettings()
    {
        properties = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.exists())
        {
            try (FileInputStream fis = new FileInputStream(file))
            {
                properties.load(fis);
                remindersEnabled = Boolean.parseBoolean(
                        properties.getProperty("reminders.enabled", "true")
                );
                reminderHoursAhead = Integer.parseInt(
                        properties.getProperty("reminders.hours", "24")
                );
            } catch (IOException e)
            {
                System.err.println("Could not load settings: " + e.getMessage());
            }
        }
    }

    public void saveSettings()
    {
        properties.setProperty("reminders.enabled", String.valueOf(remindersEnabled));
        properties.setProperty("reminders.hours", String.valueOf(reminderHoursAhead));

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE))
        {
            properties.store(fos, "Study App Settings");
        } catch (IOException e)
        {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    public boolean areRemindersEnabled() { return remindersEnabled; }
    public void setRemindersEnabled(boolean enabled) {
        this.remindersEnabled = enabled;
        saveSettings();
    }

    public int getReminderHoursAhead() { return reminderHoursAhead; }
    public void setReminderHoursAhead(int hours) {
        this.reminderHoursAhead = hours;
        saveSettings();
    }
}
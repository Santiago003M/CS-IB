package Main;

import Main.service.SettingsRepository;
import javax.swing.*;
import java.awt.*;

public class Settings extends JDialog
{

    // ALL fields that Settings.form expects (add any missing ones):
    private JPanel SettingsDashboard;
    private JLabel SettingsTitle;
    private JLabel NotificationSettings_S;
    private JLabel DailyReminderTime_S;
    private JCheckBox EnableDisableReminders_S;
    private JCheckBox EnablePopUps;  // <-- ADD THIS
    private JLabel DataStorageOptions_S;
    private JButton SelectDatabase_S;
    private JButton ExportImport_S;
    private JButton SaveSettings_S;
    private JButton ResetSettings_S;

    private SettingsRepository settingsRepo;

    public Settings(Frame owner)
    {
        super(owner, "Settings", true);
        setContentPane(SettingsDashboard);
        setSize(600, 600);
        setLocationRelativeTo(owner);

        settingsRepo = new SettingsRepository();

        // Load saved settings into checkboxes
        if (EnableDisableReminders_S != null)
        {
            EnableDisableReminders_S.setSelected(settingsRepo.areRemindersEnabled());
        }

        if (SaveSettings_S != null)
        {
            SaveSettings_S.addActionListener(e -> saveSettings());
        }

        setVisible(true);
    }

    private void saveSettings()
    {
        // Save both checkboxes
        if (EnableDisableReminders_S != null)
        {
            settingsRepo.setRemindersEnabled(EnableDisableReminders_S.isSelected());
        }

        // Optional: Save EnablePopUps too if you want
        if (EnablePopUps != null)
        {
            // You can add this to SettingsRepository later
            System.out.println("Popups enabled: " + EnablePopUps.isSelected());
        }

        JOptionPane.showMessageDialog(this, "Settings saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
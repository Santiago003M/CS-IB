package Main.view;

import Main.service.SettingsRepository;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Main.dao.StudySessionDAO;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

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

        ExportImport_S.addActionListener(e -> exportProductivityReport());

        setVisible(true);

        ExportImport_S.addActionListener(e -> exportProductivityReport());
        {

        }
    }

    private void exportProductivityReport()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Productivity Report");
        chooser.setSelectedFile(new File("productivity_report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = chooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.endsWith(".csv"))
            {
                path += ".csv";
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(path)))
            {
                // Use SEMICOLONS instead of commas (better for Excel)
                writer.println("Date;Time;Duration (minutes);Task;Status");

                StudySessionDAO dao = new StudySessionDAO();
                List<Object[]> sessions = dao.getRecentSessions(1000);

                for (Object[] row : sessions)
                {
                    Timestamp date = (Timestamp) row[0];
                    String duration = (String) row[1];
                    String taskName = (String) row[2];
                    String status = (String) row[3];

                    int minutes = 0;
                    try
                    {
                        minutes = Integer.parseInt(duration.replace(" min", ""));
                    } catch (Exception ignored) {}

                    String safeTaskName = taskName != null ? taskName : "General Study";

                    // Use semicolons as separators
                    writer.printf("%s;%s;%d;%s;%s%n",
                            date.toLocalDateTime().toLocalDate(),
                            date.toLocalDateTime().toLocalTime(),
                            minutes,
                            safeTaskName,
                            status
                    );
                }

                JOptionPane.showMessageDialog(this,
                        "Report exported successfully!",
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex)
            {
                JOptionPane.showMessageDialog(this,
                        "Export failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveSettings()
    {
        // Save both checkboxes
        if (EnableDisableReminders_S != null)
        {
            settingsRepo.setRemindersEnabled(EnableDisableReminders_S.isSelected());
        }

        if (EnablePopUps != null)
        {
            System.out.println("Popups enabled: " + EnablePopUps.isSelected());
        }

        JOptionPane.showMessageDialog(this, "Settings saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
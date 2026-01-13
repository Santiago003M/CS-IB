import org.jfree.util.StackableRuntimeException;

import javax.swing.*;

public class Settings extends JFrame
{
    private JCheckBox EnableDisableReminders_S;
    private JCheckBox EnablePopUps;
    private JButton SelectDatabase_S;
    private JButton ExportImport_S;
    private JButton SaveSettings_S;
    private JButton ResetSettings_S;
    private JLabel SettingsTitle;
    private JLabel NotificationSettings_S;
    private JLabel DailyReminderTime_S;
    private JLabel DataStorageOptions_S;
    private JPanel SettingsDashboard;

    public Settings()
    {
        SetTitle(SettingsTitle);
        SetContentPane(SettingsDashboard);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }
}

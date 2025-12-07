import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainDashboard extends JFrame
{
    private JPanel MainDashboard;
    private JButton StartTimer_MD;
    private JButton PauseTimer_MD;
    private JButton ResumeTimer_MD;
    private JTable UpcomingDeadlinesTable_MD;
    private JButton GoToTaskManager_MD;
    private JButton GoToProdLog_MD;
    private JPanel MainDashboard_2;
    private JPanel MainDashboard_3;
    private JLabel UpcomingDeadlines_MD;
    private JButton Exit_MD;
    private JPanel MainTimer_MD;
    private JButton ResetTimer_MD;

    // Timer fields
    private JLabel timeLabel;
    private JButton Settings;
    private Timer timer;
    private int elapsedTime = 0;
    private int seconds = 0, minutes = 0, hours = 0;
    private boolean running = false;

    // Constructor
    public MainDashboard()
    {
        setContentPane(MainDashboard);
        setTitle("MainDashboard");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        // Setup timer display inside MainTimer_MD panel
        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Verdana", Font.PLAIN, 30));
        MainTimer_MD.setLayout(new BorderLayout());
        MainTimer_MD.add(timeLabel, BorderLayout.CENTER);

        // Swing Timer (ticks every 1 second)
        timer = new Timer(1000, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                elapsedTime += 1000;
                hours = (elapsedTime / 3600000);
                minutes = (elapsedTime / 60000) % 60;
                seconds = (elapsedTime / 1000) % 60;
                String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                timeLabel.setText(timeFormatted);
            }
        });

        // Exit button
        Exit_MD.addActionListener(e -> System.exit(0));

        // Start Timer button
        StartTimer_MD.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!running)
                {
                    running = true;
                    timer.start();
                }
            }
        });

        // Pause Timer button
        PauseTimer_MD.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (running)
                {
                    running = false;
                    timer.stop();
                }
            }
        });

        // Resume Timer button
        ResumeTimer_MD.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!running)
                {
                    running = true;
                    timer.start();
                }
            }
        });

        ResetTimer_MD.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (timer != null)
                {
                    timer.stop();
                }
                // reset counters
                elapsedTime = 0;
                seconds = 0;
                minutes = 0;
                hours = 0;
                timeLabel.setText("00:00:00");
            }
        });

        // Navigation buttons
        GoToTaskManager_MD.addActionListener(e ->
        {
            new TaskManagerDashboard();
            dispose();
        });

        GoToProdLog_MD.addActionListener(e ->
        {
            ProductivityLogDashboard pl = new ProductivityLogDashboard();
            pl.setVisible(true);
            dispose();
        });

        setVisible(true);

        // Settings button
        Settings.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

            }
        });
    }

    public static void main(String[] args)
    {
        new MainDashboard();
    }
}
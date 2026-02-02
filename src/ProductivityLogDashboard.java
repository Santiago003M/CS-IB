package Main;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;

public class ProductivityLogDashboard extends JFrame
{
    private JTable ProductivityLogTable;
    private JPanel ChartPanelContainer;
    private JPanel mainPanel_PL;
    private JButton ExitProdLog_PL;

    private StudySessionDAO sessionDAO;

    public ProductivityLogDashboard()
    {
        sessionDAO = new StudySessionDAO();

        setContentPane(mainPanel_PL);
        setSize(900, 700); // Made window bigger for chart
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ===== GET REAL DATA FROM DATABASE =====
        int completed = sessionDAO.getTaskCountByStatus("Completed");
        int inProgress = sessionDAO.getTaskCountByStatus("In Progress");
        int notStarted = sessionDAO.getTaskCountByStatus("Not Started");
        double weeklyHours = sessionDAO.getTotalHoursThisWeek();

        // ===== CREATE PIE CHART =====
        DefaultPieDataset pieData = new DefaultPieDataset();
        pieData.setValue("Completed", completed);
        pieData.setValue("In Progress", inProgress);
        pieData.setValue("Not Started", notStarted);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Task Status Distribution",
                pieData,
                true,   // Show legend
                true,   // Show tooltips
                false   // No URLs
        );

        // ===== CONFIGURE PLOT WITH HOVER TOOLTIPS =====
        PiePlot plot = (PiePlot) pieChart.getPlot();

        // COLORS
        plot.setSectionPaint("Completed", new Color(34, 139, 34));     // Forest Green
        plot.setSectionPaint("In Progress", new Color(255, 193, 7));   // Amber
        plot.setSectionPaint("Not Started", new Color(220, 53, 69));   // Red
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        // Add visible labels with exact percentages
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                "{0}\n{1} tasks\n({2})",                    // Format: Name, Count, Percentage
                java.text.NumberFormat.getIntegerInstance(), // Count as integer
                java.text.NumberFormat.getPercentInstance()  // Percentage format
        ));

        // Keep your existing tooltip generator for hover details
        plot.setToolTipGenerator(new org.jfree.chart.labels.StandardPieToolTipGenerator(
                "{0}: {1} tasks ({2})",
                java.text.NumberFormat.getIntegerInstance(),
                java.text.NumberFormat.getPercentInstance()
        ));

        plot.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 220));
        plot.setLabelOutlinePaint(Color.GRAY);
        plot.setSimpleLabels(false);  // Use curved labels for better fit

        // Make the chart look nicer
        plot.setLabelFont(new Font("Arial", Font.BOLD, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200)); // Semi-transparent white
        plot.setLabelOutlinePaint(Color.BLACK);
        plot.setLabelShadowPaint(null);

        // separatte the slices slightly for better visibility
        plot.setExplodePercent("Completed", 0.005);
        plot.setExplodePercent("In Progress", 0.005);
        plot.setExplodePercent("Not Started", 0.005);

        // ===== ADD CHART TO PANEL =====
        ChartPanel piePanel = new ChartPanel(pieChart);
        piePanel.setPreferredSize(new Dimension(500, 400)); // Bigger chart
        piePanel.setMouseWheelEnabled(true); // Allow zoom with mouse wheel

        ChartPanelContainer.setLayout(new BorderLayout());
        ChartPanelContainer.add(piePanel, BorderLayout.CENTER);

        // ===== SHOW WEEKLY SUMMARY =====
        JOptionPane.showMessageDialog(this,
                String.format("📊 This Week's Study Time: %.1f hours\n\n" +
                        "Keep up the good work!", weeklyHours),
                "Weekly Progress",
                JOptionPane.INFORMATION_MESSAGE);

        // ===== LOAD REAL DATA INTO TABLE =====
        loadSessionTable();

        // ===== EXIT BUTTON =====
        ExitProdLog_PL.addActionListener(e ->
        {
            new MainDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadSessionTable()
    {
        String[] columns = {"Date", "Duration", "Task", "Result"};
        java.util.List<Object[]> data = sessionDAO.getRecentSessions(20);

        ProductivityLogTable.setModel(new DefaultTableModel(
                data.toArray(new Object[0][]),
                columns
        ));
    }

}

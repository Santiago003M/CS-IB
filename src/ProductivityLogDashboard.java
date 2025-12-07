import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;

public class ProductivityLogDashboard extends JFrame
{
    private JTable ProductivityLogTable;
    private JPanel ChartPanelContainer;
    private JPanel mainPanel_PL;
    private JButton ExitProdLog_PL;

    public ProductivityLogDashboard()
    {
        setContentPane(mainPanel_PL);
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ===== SAMPLE TASK STATUS DATA =====
        int completedCount = 5;
        int inProgressCount = 3;
        int notStartedCount = 2;

        // ===== CREATE PIE DATASET =====
        DefaultPieDataset pieData = new DefaultPieDataset();
        pieData.setValue("Completed", completedCount);
        pieData.setValue("In Progress", inProgressCount);
        pieData.setValue("Not Started", notStartedCount);

        // ===== CREATE PIE CHART =====
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Task Status Breakdown",
                pieData,
                true,
                true,
                false
        );

        // colors
        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setSectionPaint("Completed", new Color(0, 180, 0));     // green
        plot.setSectionPaint("In Progress", new Color(255, 215, 0)); // yellow-gold
        plot.setSectionPaint("Not Started", new Color(220, 20, 60)); // red

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        // wrap the chart into tje panel
        ChartPanel piePanel = new ChartPanel(pieChart);
        piePanel.setPreferredSize(new Dimension(390, 250));

        ChartPanelContainer.setLayout(new BorderLayout());
        ChartPanelContainer.add(piePanel, BorderLayout.CENTER);

        setVisible(true);

        ExitProdLog_PL.addActionListener(e ->
        {
            new MainDashboard();
            dispose();
        });
    }
}

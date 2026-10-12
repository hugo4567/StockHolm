package com.stockholm.ui;

import com.stockholm.repo.ProductRepository;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final ProductRepository productRepository;
    private final JPanel chartsHolder = new JPanel(new GridLayout(1, 2, 12, 12));

    public DashboardPanel(ProductRepository productRepository) {
        this.productRepository = productRepository;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton refresh = new JButton("Actualiser");
        refresh.addActionListener(e -> refreshCharts());

        add(refresh, BorderLayout.NORTH);
        add(chartsHolder, BorderLayout.CENTER);

        refreshCharts();
    }

    private void refreshCharts() {
        chartsHolder.removeAll();
        chartsHolder.add(buildStatusChart());
        chartsHolder.add(buildFinancialChart());
        revalidate();
        repaint();
    }

    private ChartPanel buildStatusChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        List<ProductRepository.StatusCount> counts = productRepository.countByStatus();
        counts.forEach(count -> dataset.setValue(count.status(), count.total()));
        if (counts.isEmpty()) {
            dataset.setValue("Aucun produit", 1);
        }
        JFreeChart chart = ChartFactory.createPieChart("Produits par statut", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("EN_ATTENTE", new Color(120, 120, 120));
        plot.setSectionPaint("EN_COURS", new Color(160, 160, 160));
        plot.setSectionPaint("REPARE", new Color(190, 190, 190));
        plot.setSectionPaint("REBUT", new Color(90, 90, 90));
        plot.setBackgroundPaint(new Color(45, 45, 45));
        plot.setLabelPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        ChartPanel panel = new ChartPanel(chart);
        installHoverEffect(panel);
        return panel;
    }

    private ChartPanel buildFinancialChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        ProductRepository.FinancialSummary summary = productRepository.summarizeFinancials().get(0);
        dataset.addValue(summary.totalCost().doubleValue(), "Coûts", "Total");
        dataset.addValue(summary.totalRevenue().doubleValue(), "Ventes", "Total");

        JFreeChart chart = ChartFactory.createBarChart("Coûts et ventes", "Type", "Montant", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(45, 45, 45));
        plot.setRangeGridlinePaint(new Color(120, 120, 120));
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(160, 160, 160));
        renderer.setSeriesPaint(1, new Color(100, 100, 100));
        ChartPanel panel = new ChartPanel(chart);
        installHoverEffect(panel);
        return panel;
    }

    private void installHoverEffect(ChartPanel panel) {
        panel.setMouseWheelEnabled(true);
        panel.setDisplayToolTips(true);
        panel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                if (event.getEntity() instanceof PieSectionEntity entity) {
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    panel.setToolTipText(entity.getSectionKey().toString());
                } else if (event.getEntity() instanceof CategoryItemEntity entity) {
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    panel.setToolTipText(entity.getRowKey() + " / " + entity.getColumnKey());
                } else {
                    panel.setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }
}

package com.stockholm.ui;

import com.stockholm.repo.BatteryRepository;
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
import java.math.BigDecimal;
import java.util.List;

public class DashboardPanel extends JPanel {
    private static final String STATUS = "Statuts produits";
    private static final String COSTS = "Coûts et ventes";
    private static final String HEALTHY_CELLS = "Cellules saines";
    private static final String PURE_GAINS = "Gains purs";
    private static final String NET_GAINS = "Gains nets avec coûts";
    private static final String PROFITABLE = "Objets rentables";
    private static final String KPIS = "KPI globaux";

    private final ProductRepository productRepository;
    private final BatteryRepository batteryRepository;
    private final JComboBox<String> chartMenu = new JComboBox<>(
            new String[]{STATUS, COSTS, HEALTHY_CELLS, PURE_GAINS, NET_GAINS, PROFITABLE, KPIS}
    );
    private final JPanel content = new JPanel(new BorderLayout());

    public DashboardPanel(ProductRepository productRepository, BatteryRepository batteryRepository) {
        this.productRepository = productRepository;
        this.batteryRepository = batteryRepository;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menu.add(new JLabel("Vue graphique :"));
        chartMenu.addActionListener(event -> refreshSelectedView());
        menu.add(chartMenu);
        JButton refresh = new JButton("Actualiser");
        refresh.addActionListener(event -> refreshSelectedView());
        menu.add(refresh);
        add(menu, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        refreshSelectedView();
    }

    private void refreshSelectedView() {
        content.removeAll();
        String selected = (String) chartMenu.getSelectedItem();
        JComponent view = switch (selected) {
            case STATUS -> buildStatusChart();
            case COSTS -> buildFinancialChart();
            case HEALTHY_CELLS -> buildHealthyCellsChart();
            case PURE_GAINS -> buildGainChart(false);
            case NET_GAINS -> buildGainChart(true);
            case PROFITABLE -> buildProfitableObjectsChart();
            case KPIS -> buildKpis();
            default -> new JPanel();
        };
        content.add(view, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private ChartPanel buildStatusChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        productRepository.countByStatus().forEach(count -> dataset.setValue(count.status(), count.total()));
        if (dataset.getItemCount() == 0) {
            dataset.setValue("Aucun produit", 1);
        }
        JFreeChart chart = ChartFactory.createPieChart("Produits par statut", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("EN_ATTENTE", new Color(120, 120, 120));
        plot.setSectionPaint("EN_COURS", new Color(160, 160, 160));
        plot.setSectionPaint("REPARE", new Color(190, 190, 190));
        plot.setSectionPaint("REBUT", new Color(90, 90, 90));
        stylePlot(plot);
        return chartPanel(chart);
    }

    private ChartPanel buildFinancialChart() {
        ProductRepository.FinancialSummary summary = productRepository.summarizeFinancials().get(0);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(summary.totalCost().doubleValue(), "Coûts", "Total");
        dataset.addValue(summary.totalRevenue().doubleValue(), "Ventes", "Total");
        return chartPanel(buildBar("Coûts et ventes", dataset));
    }

    private ChartPanel buildHealthyCellsChart() {
        BatteryRepository.BatteryMetrics metrics = batteryRepository.metrics();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(metrics.healthyCells(), "Cellules", "Saines");
        dataset.addValue(Math.max(0, metrics.totalCells() - metrics.healthyCells()), "Cellules", "Autres états");
        return chartPanel(buildBar("Stock de batteries : cellules saines", dataset));
    }

    private ChartPanel buildGainChart(boolean net) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ProductRepository.Profitability item : productRepository.profitability()) {
            BigDecimal value = net ? item.netGain() : item.pureGain();
            dataset.addValue(value.doubleValue(), net ? "Gain net" : "Gain pur", "#" + item.id() + " " + item.name());
        }
        if (dataset.getColumnCount() == 0) {
            dataset.addValue(0, net ? "Gain net" : "Gain pur", "Aucune vente");
        }
        return chartPanel(buildBar(net ? "Gains avec coûts intégrés" : "Gains purs après frais de vente", dataset));
    }

    private ChartPanel buildProfitableObjectsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        productRepository.profitability().stream()
                .filter(item -> item.netGain().signum() > 0)
                .forEach(item -> dataset.addValue(item.netGain().doubleValue(), "Rentabilité", "#" + item.id() + " " + item.name()));
        if (dataset.getColumnCount() == 0) {
            dataset.addValue(0, "Rentabilité", "Aucun objet rentable");
        }
        return chartPanel(buildBar("Tous les objets rentables", dataset));
    }

    private JComponent buildKpis() {
        ProductRepository.GlobalKpis kpis = productRepository.globalKpis();
        BatteryRepository.BatteryMetrics batteries = batteryRepository.metrics();
        JPanel panel = new JPanel(new GridLayout(2, 3, 14, 14));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        addKpi(panel, "Produits suivis", String.valueOf(kpis.totalProducts()));
        addKpi(panel, "Objets rentables", String.valueOf(kpis.profitableProducts()));
        addKpi(panel, "Gains purs", money(kpis.pureGain()));
        addKpi(panel, "Gains nets", money(kpis.netGain()));
        addKpi(panel, "ROI global", kpis.roiPercent() + " %");
        addKpi(panel, "Cellules saines", batteries.healthyCells() + " / " + batteries.totalCells());
        return panel;
    }

    private void addKpi(JPanel parent, String title, String value) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(115, 115, 125)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(220, 220, 225));
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 24f));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        parent.add(card);
    }

    private JFreeChart buildBar(String title, DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(title, "Objet", "Montant / quantité", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        stylePlot(plot);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(165, 165, 175));
        return chart;
    }

    private void stylePlot(org.jfree.chart.plot.Plot plot) {
        plot.setBackgroundPaint(new Color(45, 45, 45));
        if (plot instanceof CategoryPlot categoryPlot) {
            categoryPlot.setRangeGridlinePaint(new Color(120, 120, 130));
        }
    }

    private ChartPanel chartPanel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
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
                    panel.setToolTipText(null);
                }
            }
        });
        return panel;
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP) + " €";
    }
}

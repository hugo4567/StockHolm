package com.stockholm.ui;

import com.stockholm.db.DatabaseManager;
import com.stockholm.repo.PartRepository;
import com.stockholm.repo.BatteryRepository;
import com.stockholm.repo.ProductRepository;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final JLayeredPane layeredPane = new JLayeredPane();
    private final RouletteDrawer rouletteDrawer = new RouletteDrawer();
    private final JButton rouletteToggle = new JButton("🎲 Roulette");
    private final JPanel base = new JPanel(new BorderLayout());

    public MainFrame(DatabaseManager databaseManager) {
        super("StockHolm");
        Theme.install();

        ProductRepository productRepository = new ProductRepository(databaseManager);
        PartRepository partRepository = new PartRepository(databaseManager);
        BatteryRepository batteryRepository = new BatteryRepository(databaseManager);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ajouter un produit", new ProductPanel(productRepository, partRepository));
        tabs.addTab("Mode suivi", new PartPanel(productRepository, partRepository));
        tabs.addTab("Stock batteries", new BatteryPanel(batteryRepository));
        tabs.addTab("Consulter mes graphes", new DashboardPanel(productRepository, batteryRepository));

        base.add(buildHeader(databaseManager), BorderLayout.NORTH);
        base.add(tabs, BorderLayout.CENTER);

        layeredPane.setLayout(null);
        layeredPane.add(base, JLayeredPane.DEFAULT_LAYER);
        rouletteDrawer.setVisible(false);
        layeredPane.add(rouletteDrawer, JLayeredPane.PALETTE_LAYER);

        rouletteToggle.addActionListener(event -> toggleRoulette());
        rouletteToggle.setToolTipText("Ouvrir ou ranger la roulette");
        layeredPane.add(rouletteToggle, JLayeredPane.PALETTE_LAYER);
        add(layeredPane, BorderLayout.CENTER);

        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                layoutFloatingControls();
            }
        });
        SwingUtilities.invokeLater(this::layoutFloatingControls);
    }

    private void toggleRoulette() {
        boolean open = !rouletteDrawer.isVisible();
        rouletteDrawer.setVisible(open);
        rouletteToggle.setText(open ? "✕ Fermer" : "🎲 Roulette");
        layoutFloatingControls();
    }

    private void layoutFloatingControls() {
        int width = layeredPane.getWidth();
        int height = layeredPane.getHeight();
        base.setBounds(0, 0, width, height);
        int buttonWidth = 118;
        int buttonHeight = 34;
        int margin = 16;
        rouletteToggle.setBounds(width - buttonWidth - margin, height - buttonHeight - margin, buttonWidth, buttonHeight);

        int drawerWidth = 330;
        int drawerHeight = 430;
        rouletteDrawer.setBounds(
                width - drawerWidth - margin,
                Math.max(margin, height - drawerHeight - buttonHeight - margin - 8),
                drawerWidth,
                drawerHeight
        );
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private JComponent buildHeader(DatabaseManager databaseManager) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("StockHolm");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel db = new JLabel("Base locale : " + databaseManager.getDatabaseFile());
        db.setForeground(new Color(225, 225, 230));

        panel.add(title, BorderLayout.WEST);
        panel.add(db, BorderLayout.EAST);
        return panel;
    }
}

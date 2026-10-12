package com.stockholm.ui;

import com.stockholm.db.DatabaseManager;
import com.stockholm.repo.PartRepository;
import com.stockholm.repo.ProductRepository;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame(DatabaseManager databaseManager) {
        super("StockHolm");
        Theme.install();

        ProductRepository productRepository = new ProductRepository(databaseManager);
        PartRepository partRepository = new PartRepository(databaseManager);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ajouter un produit", new ProductPanel(productRepository, partRepository));
        tabs.addTab("Mode suivi", new PartPanel(productRepository, partRepository));
        tabs.addTab("Consulter mes graphes", new DashboardPanel(productRepository));

        add(buildHeader(databaseManager), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
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

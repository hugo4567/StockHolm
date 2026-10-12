package com.stockholm;

import com.stockholm.db.DatabaseManager;
import com.stockholm.ui.MainFrame;

import javax.swing.SwingUtilities;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initialize();
        SwingUtilities.invokeLater(() -> new MainFrame(databaseManager).setVisible(true));
    }
}

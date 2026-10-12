package com.stockholm;

import com.stockholm.db.DatabaseManager;
import com.stockholm.ui.MainFrame;
import com.stockholm.ui.SplashWindow;

import javax.swing.SwingUtilities;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SplashWindow splash = new SplashWindow();
            splash.setVisible(true);

            new javax.swing.SwingWorker<DatabaseManager, Void>() {
                @Override
                protected DatabaseManager doInBackground() {
                    DatabaseManager databaseManager = new DatabaseManager();
                    databaseManager.initialize();
                    return databaseManager;
                }

                @Override
                protected void done() {
                    splash.dispose();
                    try {
                        new MainFrame(get()).setVisible(true);
                    } catch (Exception exception) {
                        javax.swing.JOptionPane.showMessageDialog(
                                null,
                                "Impossible de démarrer StockHolm : " + exception.getMessage(),
                                "Erreur de démarrage",
                                javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                        System.exit(1);
                    }
                }
            }.execute();
        });
    }
}

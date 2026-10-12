package com.stockholm;

import com.stockholm.db.DatabaseManager;
import com.stockholm.ui.MainFrame;
import com.stockholm.ui.SplashWindow;

import javax.swing.SwingUtilities;

public final class App {
    private static final long MINIMUM_SPLASH_TIME_MILLIS = 2_000L;

    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SplashWindow splash = new SplashWindow();
            long splashStartNanos = System.nanoTime();
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
                    DatabaseManager databaseManager;
                    try {
                        databaseManager = get();
                    } catch (Exception exception) {
                        showStartupErrorAfterSplash(splash, splashStartNanos, exception);
                        return;
                    }

                    long elapsedMillis = (System.nanoTime() - splashStartNanos) / 1_000_000L;
                    int remainingMillis = (int) Math.max(0L, MINIMUM_SPLASH_TIME_MILLIS - elapsedMillis);
                    javax.swing.Timer timer = new javax.swing.Timer(remainingMillis, event -> {
                        splash.dispose();
                        new MainFrame(databaseManager).setVisible(true);
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }.execute();
        });
    }

    private static void showStartupErrorAfterSplash(
            SplashWindow splash,
            long splashStartNanos,
            Exception exception
    ) {
        long elapsedMillis = (System.nanoTime() - splashStartNanos) / 1_000_000L;
        int remainingMillis = (int) Math.max(0L, MINIMUM_SPLASH_TIME_MILLIS - elapsedMillis);
        javax.swing.Timer timer = new javax.swing.Timer(remainingMillis, event -> {
            splash.dispose();
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Impossible de démarrer StockHolm : " + exception.getMessage(),
                    "Erreur de démarrage",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        });
        timer.setRepeats(false);
        timer.start();
    }
}

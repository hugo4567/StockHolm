package com.stockholm;

import com.stockholm.db.DatabaseManager;
import com.stockholm.ui.FxMainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Objects;

public final class FxApp extends Application {
    private static final long MINIMUM_SPLASH_MILLIS = 2_000L;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        long start = System.currentTimeMillis();
        Stage splash = new Stage();
        splash.setTitle("StockHolm");
        splash.setScene(new Scene(buildSplash(), 720, 390));
        splash.setResizable(false);
        splash.show();

        Thread.startVirtualThread(() -> {
            try {
                DatabaseManager databaseManager = new DatabaseManager();
                databaseManager.initialize();
                long remaining = MINIMUM_SPLASH_MILLIS - (System.currentTimeMillis() - start);
                if (remaining > 0) {
                    Thread.sleep(remaining);
                }
                Platform.runLater(() -> {
                    splash.close();
                    FxMainView view = new FxMainView(databaseManager);
                    Scene scene = new Scene(view, 1440, 900);
                    scene.getStylesheets().add(Objects.requireNonNull(
                            getClass().getResource("/stockholm.css")).toExternalForm());
                    stage.setTitle("StockHolm");
                    stage.setMinWidth(1120);
                    stage.setMinHeight(720);
                    stage.setScene(scene);
                    stage.show();
                });
            } catch (Exception exception) {
                Platform.runLater(() -> {
                    splash.close();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de démarrage");
                    alert.setHeaderText("StockHolm n'a pas pu démarrer");
                    alert.setContentText(exception.getMessage());
                    alert.showAndWait();
                    Platform.exit();
                });
            }
        });
    }

    private StackPane buildSplash() {
        StackPane root = new StackPane();
        root.getStyleClass().add("splash");
        InputStream stream = getClass().getResourceAsStream("/banniere.jpg");
        if (stream != null) {
            ImageView image = new ImageView(new Image(stream));
            image.setPreserveRatio(true);
            image.setFitWidth(680);
            image.setFitHeight(350);
            root.getChildren().add(image);
        } else {
            root.getChildren().add(new Label("StockHolm"));
        }
        return root;
    }
}

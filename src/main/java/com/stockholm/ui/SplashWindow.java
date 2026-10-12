package com.stockholm.ui;

import com.stockholm.config.AppPaths;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SplashWindow extends JWindow {
    public SplashWindow() {
        Theme.install();

        Path banner = AppPaths.resolveBannerFile();
        ImageIcon source;
        if (Files.isRegularFile(banner)) {
            source = new ImageIcon(banner.toString());
        } else {
            java.net.URL resource = SplashWindow.class.getResource("/banniere.jpg");
            if (resource == null) {
                throw new IllegalStateException("Image de démarrage introuvable : " + banner);
            }
            source = new ImageIcon(resource);
        }
        JLabel image = new JLabel(scale(source, 720, 360));
        image.setBorder(BorderFactory.createLineBorder(new Color(130, 130, 140), 1));
        setContentPane(image);
        pack();
        setLocationRelativeTo(null);
    }

    private ImageIcon scale(ImageIcon source, int maxWidth, int maxHeight) {
        int width = source.getIconWidth();
        int height = source.getIconHeight();
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException("Image de démarrage invalide");
        }
        double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int scaledWidth = Math.max(1, (int) Math.round(width * ratio));
        int scaledHeight = Math.max(1, (int) Math.round(height * ratio));
        Image scaled = source.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}

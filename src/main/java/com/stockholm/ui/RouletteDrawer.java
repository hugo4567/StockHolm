package com.stockholm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.ThreadLocalRandom;

public final class RouletteDrawer extends JPanel {
    private static final int SECTOR_COUNT = 12;
    private static final int ANIMATION_MILLIS = 2_800;

    private final JLabel resultLabel = new JLabel("Résultat : —", SwingConstants.CENTER);
    private final RouletteCanvas rouletteCanvas = new RouletteCanvas();
    private final JButton spinButton = new JButton("Faire tourner");
    private Timer animationTimer;
    private long animationStart;
    private double startAngle;
    private double targetAngle;
    private int result;

    public RouletteDrawer() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(145, 145, 155), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(34, 34, 38));

        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 18f));
        resultLabel.setForeground(new Color(250, 250, 250));
        add(resultLabel, BorderLayout.NORTH);
        add(rouletteCanvas, BorderLayout.CENTER);

        spinButton.addActionListener(event -> spin());
        add(spinButton, BorderLayout.SOUTH);
    }

    private void spin() {
        if (animationTimer != null && animationTimer.isRunning()) {
            return;
        }

        result = ThreadLocalRandom.current().nextInt(1, SECTOR_COUNT + 1);
        animationStart = System.nanoTime();
        startAngle = rouletteCanvas.getRotation();
        double selectedCenter = (result - 1) * (Math.PI * 2 / SECTOR_COUNT)
                + (Math.PI / SECTOR_COUNT);
        double pointerAngle = -Math.PI / 2;
        double turns = 5 + ThreadLocalRandom.current().nextInt(3);
        targetAngle = startAngle + turns * Math.PI * 2 + normalize(pointerAngle - selectedCenter);
        resultLabel.setText("Tirage en cours…");
        spinButton.setEnabled(false);

        animationTimer = new Timer(16, event -> {
            double elapsed = (System.nanoTime() - animationStart) / 1_000_000.0;
            double progress = Math.min(1.0, elapsed / ANIMATION_MILLIS);
            double eased = 1 - Math.pow(1 - progress, 3);
            rouletteCanvas.setRotation(startAngle + (targetAngle - startAngle) * eased);
            if (progress >= 1.0) {
                animationTimer.stop();
                spinButton.setEnabled(true);
                resultLabel.setText("Résultat : " + result);
            }
        });
        animationTimer.start();
    }

    private double normalize(double angle) {
        while (angle < -Math.PI) {
            angle += Math.PI * 2;
        }
        while (angle > Math.PI) {
            angle -= Math.PI * 2;
        }
        return angle;
    }

    private static final class RouletteCanvas extends JComponent {
        private double rotation;

        double getRotation() {
            return rotation;
        }

        void setRotation(double rotation) {
            this.rotation = rotation;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diameter = Math.min(getWidth(), getHeight()) - 18;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            double sector = 360.0 / SECTOR_COUNT;

            g.setColor(new Color(20, 20, 22));
            g.fillOval(x - 5, y - 5, diameter + 10, diameter + 10);
            for (int index = 0; index < SECTOR_COUNT; index++) {
                g.setColor(index % 2 == 0 ? new Color(105, 105, 112) : new Color(60, 60, 68));
                g.fillArc(x, y, diameter, diameter, (int) Math.toDegrees(rotation) + index * (int) sector, (int) sector + 1);
            }
            g.setColor(new Color(220, 220, 225));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(x, y, diameter, diameter);

            FontMetrics metrics = g.getFontMetrics();
            for (int index = 0; index < SECTOR_COUNT; index++) {
                double angle = rotation + Math.PI / 2 - (index + 0.5) * Math.PI * 2 / SECTOR_COUNT;
                String label = String.valueOf(index + 1);
                int labelX = (int) (getWidth() / 2.0 + Math.cos(angle) * diameter * 0.36);
                int labelY = (int) (getHeight() / 2.0 - Math.sin(angle) * diameter * 0.36);
                g.setColor(Color.WHITE);
                g.drawString(label, labelX - metrics.stringWidth(label) / 2, labelY + metrics.getAscent() / 2);
            }

            int center = getWidth() / 2;
            int ballRadius = 7;
            double ballAngle = -Math.PI / 2 - rotation * 1.7;
            double ballRadiusFromCenter = diameter * 0.39;
            double ballX = center + Math.cos(ballAngle) * ballRadiusFromCenter;
            double ballY = getHeight() / 2.0 + Math.sin(ballAngle) * ballRadiusFromCenter;
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(ballX - ballRadius, ballY - ballRadius, ballRadius * 2, ballRadius * 2));
            g.setColor(new Color(30, 30, 35));
            g.draw(new Ellipse2D.Double(ballX - ballRadius, ballY - ballRadius, ballRadius * 2, ballRadius * 2));

            Polygon pointer = new Polygon();
            pointer.addPoint(center, y - 4);
            pointer.addPoint(center - 10, y - 22);
            pointer.addPoint(center + 10, y - 22);
            g.setColor(new Color(220, 70, 70));
            g.fill(pointer);
            g.dispose();
        }
    }
}

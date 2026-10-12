package com.stockholm.ui;

import javax.swing.*;
import java.awt.*;

public final class Theme {
    private Theme() {
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible d'installer le thème Swing", exception);
        }

        Color background = new Color(38, 38, 42);
        Color control = new Color(58, 58, 64);
        Color foreground = new Color(245, 245, 245);
        Color secondaryForeground = new Color(220, 220, 225);

        UIManager.put("control", control);
        UIManager.put("info", control);
        UIManager.put("nimbusBase", new Color(55, 55, 62));
        UIManager.put("nimbusBlueGrey", new Color(70, 70, 78));
        UIManager.put("text", foreground);
        UIManager.put("Panel.background", background);
        UIManager.put("Label.foreground", foreground);
        UIManager.put("Button.background", control);
        UIManager.put("Button.foreground", foreground);
        UIManager.put("TextField.background", control);
        UIManager.put("TextField.foreground", foreground);
        UIManager.put("TextArea.background", control);
        UIManager.put("TextArea.foreground", foreground);
        UIManager.put("ComboBox.background", control);
        UIManager.put("ComboBox.foreground", foreground);
        UIManager.put("ComboBox.selectionBackground", new Color(105, 105, 115));
        UIManager.put("ComboBox.selectionForeground", foreground);
        UIManager.put("TabbedPane.background", background);
        UIManager.put("TabbedPane.foreground", foreground);
        UIManager.put("Table.background", new Color(48, 48, 54));
        UIManager.put("Table.foreground", foreground);
        UIManager.put("Table.gridColor", new Color(105, 105, 112));
        UIManager.put("Table.selectionBackground", new Color(100, 100, 112));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", new Color(75, 75, 84));
        UIManager.put("TableHeader.foreground", foreground);
        UIManager.put("ToolTip.background", new Color(235, 235, 235));
        UIManager.put("ToolTip.foreground", new Color(25, 25, 25));
        UIManager.put("nimbusDisabledText", secondaryForeground);
    }
}

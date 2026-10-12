package com.stockholm.ui;

import javax.swing.*;
import java.awt.*;

public final class Theme {
    private Theme() {
    }

    public static void install() {
        UIManager.put("control", new Color(70, 70, 70));
        UIManager.put("info", new Color(90, 90, 90));
        UIManager.put("nimbusBase", new Color(60, 60, 60));
        UIManager.put("nimbusBlueGrey", new Color(80, 80, 80));
        UIManager.put("text", Color.WHITE);
        UIManager.put("Table.background", new Color(54, 54, 54));
        UIManager.put("Table.foreground", Color.WHITE);
        UIManager.put("Table.gridColor", new Color(95, 95, 95));
        UIManager.put("Table.selectionBackground", new Color(110, 110, 110));
        UIManager.put("Panel.background", new Color(45, 45, 45));
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Button.background", new Color(85, 85, 85));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.background", new Color(70, 70, 70));
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextArea.background", new Color(70, 70, 70));
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("ComboBox.background", new Color(70, 70, 70));
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("TabbedPane.background", new Color(45, 45, 45));
    }
}

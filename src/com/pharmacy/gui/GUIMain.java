package com.pharmacy.gui;

import javax.swing.*;

/**
 * GUI entry point — launches the LoginFrame.
 * Use this instead of the old console-based Main.java.
 */
public class GUIMain {

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Dark tooltip styling
        UIManager.put("ToolTip.background", PharmacyTheme.BG_CARD);
        UIManager.put("ToolTip.foreground", PharmacyTheme.TEXT_PRIMARY);
        UIManager.put("ToolTip.font", PharmacyTheme.FONT_SMALL);

        // Dark option pane styling
        UIManager.put("OptionPane.background", PharmacyTheme.BG_PANEL);
        UIManager.put("Panel.background", PharmacyTheme.BG_PANEL);
        UIManager.put("OptionPane.messageForeground", PharmacyTheme.TEXT_PRIMARY);

        SwingUtilities.invokeLater(LoginFrame::new);
    }
}

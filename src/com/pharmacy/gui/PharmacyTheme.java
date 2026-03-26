package com.pharmacy.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Shared theme constants for the entire GUI.
 * Dark mode pharmacy aesthetic with green accents.
 */
public class PharmacyTheme {

    // ═══════ Color Palette ═══════
    public static final Color BG_DARK       = new Color(18, 18, 24);
    public static final Color BG_PANEL      = new Color(26, 26, 36);
    public static final Color BG_CARD       = new Color(34, 34, 48);
    public static final Color BG_INPUT      = new Color(42, 42, 56);
    public static final Color BG_HOVER      = new Color(50, 50, 68);

    public static final Color ACCENT_GREEN  = new Color(0, 200, 120);
    public static final Color ACCENT_BLUE   = new Color(80, 140, 255);
    public static final Color ACCENT_RED    = new Color(255, 80, 80);
    public static final Color ACCENT_YELLOW = new Color(255, 200, 60);
    public static final Color ACCENT_PURPLE = new Color(160, 100, 255);

    public static final Color TEXT_PRIMARY   = new Color(230, 230, 240);
    public static final Color TEXT_SECONDARY = new Color(150, 150, 170);
    public static final Color TEXT_MUTED     = new Color(100, 100, 120);
    public static final Color BORDER         = new Color(60, 60, 80);

    // ═══════ Fonts ═══════
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 13);

    // ═══════ Dimensions ═══════
    public static final int CARD_RADIUS = 12;
    public static final int INPUT_HEIGHT = 40;
    public static final int BUTTON_HEIGHT = 42;
    public static final Insets CARD_PADDING = new Insets(16, 20, 16, 20);

    /**
     * Create a styled button with hover effects.
     */
    public static JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_HEADING);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(200, BUTTON_HEIGHT));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Create a styled text field.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_BODY);
                    g2.drawString(placeholder, getInsets().left + 5, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(ACCENT_GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setPreferredSize(new Dimension(300, INPUT_HEIGHT));
        return field;
    }

    /**
     * Create a styled password field.
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_BODY);
                    g2.drawString(placeholder, getInsets().left + 5, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(ACCENT_GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setPreferredSize(new Dimension(300, INPUT_HEIGHT));
        return field;
    }

    /**
     * Create a rounded panel with dark background.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
                CARD_PADDING.top, CARD_PADDING.left, CARD_PADDING.bottom, CARD_PADDING.right));
        return card;
    }

    /**
     * Create a label with the theme's primary color.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Style a JTable with the dark theme.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_CARD);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT_GREEN.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.getTableHeader().setFont(FONT_HEADING);
        table.getTableHeader().setBackground(BG_PANEL);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GREEN));
    }

    /**
     * Style a JScrollPane with the dark theme.
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(BG_CARD);
    }
}

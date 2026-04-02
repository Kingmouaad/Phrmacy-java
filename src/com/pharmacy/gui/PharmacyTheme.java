package com.pharmacy.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Shared theme constants — refined dark-mode slate palette
 * with professional accent colors and custom icon/badge helpers.
 */
public class PharmacyTheme {

    // ═══════ Slate Color Palette ═══════
    public static final Color BG_DARK       = new Color(15, 23, 42);    // slate-900
    public static final Color BG_PANEL      = new Color(30, 41, 59);    // slate-800
    public static final Color BG_CARD       = new Color(51, 65, 85);    // slate-700
    public static final Color BG_INPUT      = new Color(71, 85, 105);   // slate-600
    public static final Color BG_HOVER      = new Color(100, 116, 139); // slate-500
    public static final Color BG_CARD_ALT   = new Color(45, 58, 78);    // slightly lighter for alt rows

    // ═══════ Accent Colors (muted & professional) ═══════
    public static final Color ACCENT_GREEN  = new Color(52, 211, 153);  // emerald-400
    public static final Color ACCENT_BLUE   = new Color(129, 140, 248); // indigo-400
    public static final Color ACCENT_RED    = new Color(251, 113, 133); // rose-400
    public static final Color ACCENT_YELLOW = new Color(251, 191, 36);  // amber-400
    public static final Color ACCENT_PURPLE = new Color(167, 139, 250); // violet-400

    // ═══════ Text Colors ═══════
    public static final Color TEXT_PRIMARY   = new Color(226, 232, 240); // slate-200
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184); // slate-400
    public static final Color TEXT_MUTED     = new Color(100, 116, 139); // slate-500
    public static final Color BORDER         = new Color(51, 65, 85);    // slate-700

    // ═══════ Fonts ═══════
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_STAT     = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_ICON     = new Font("Segoe UI", Font.BOLD, 28);

    // ═══════ Dimensions ═══════
    public static final int CARD_RADIUS = 16;
    public static final int INPUT_HEIGHT = 40;
    public static final int BUTTON_HEIGHT = 42;
    public static final Insets CARD_PADDING = new Insets(20, 24, 20, 24);

    // ═══════════════════════════════════════════════
    //  Factory Methods
    // ═══════════════════════════════════════════════

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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
     * Create a styled text field with placeholder.
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
     * Create a styled password field with placeholder.
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
     * Create a rounded card panel with subtle border glow.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
                // Subtle border
                g2.setColor(new Color(100, 116, 139, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_RADIUS, CARD_RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
                CARD_PADDING.top, CARD_PADDING.left, CARD_PADDING.bottom, CARD_PADDING.right));
        return card;
    }

    /**
     * Create a themed label.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Create a status badge — pill-shaped colored label.
     */
    public static JLabel createStatusBadge(String text, Color bgColor) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(bgColor);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return badge;
    }

    /**
     * Create a simple icon label using Graphics2D — replaces broken emoji.
     * Types: "cart", "dollar", "pill", "users", "box", "home", "rx"
     */
    public static JLabel createIconLabel(String type, Color color, int size) {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int r = size / 2;
                switch (type.toLowerCase()) {
                    case "cart":
                        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        // Cart body
                        int[] xPts = {cx - r, cx - r + 4, cx + r - 2, cx + r};
                        int[] yPts = {cy - r + 4, cy + r - 6, cy + r - 6, cy - r + 4};
                        g2.drawPolyline(xPts, yPts, 4);
                        // Handle
                        g2.drawLine(cx - r, cy - r + 4, cx - r - 4, cy - r - 2);
                        // Wheels
                        g2.fillOval(cx - r + 6, cy + r - 5, 5, 5);
                        g2.fillOval(cx + r - 8, cy + r - 5, 5, 5);
                        break;
                    case "dollar":
                        g2.setFont(new Font("Segoe UI", Font.BOLD, size));
                        FontMetrics fm = g2.getFontMetrics();
                        String s = "$";
                        g2.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getAscent() / 2 - 2);
                        break;
                    case "pill":
                        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawRoundRect(cx - r / 2, cy - r, r, r * 2, r, r);
                        g2.drawLine(cx - r / 2, cy, cx + r / 2, cy);
                        break;
                    case "users":
                        // Two user silhouettes
                        g2.fillOval(cx - 5, cy - r, 10, 10);
                        g2.fillArc(cx - 10, cy, 20, 14, 0, 180);
                        // Second user behind
                        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 140));
                        g2.fillOval(cx + 5, cy - r - 2, 9, 9);
                        g2.fillArc(cx, cy - 2, 18, 12, 0, 180);
                        break;
                    case "box":
                        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawRect(cx - r, cy - r + 4, size, size - 4);
                        g2.drawLine(cx - r, cy - r + 4, cx, cy - r - 4);
                        g2.drawLine(cx, cy - r - 4, cx + r, cy - r + 4);
                        g2.drawLine(cx, cy - r - 4, cx, cy + 2);
                        break;
                    default: // dot fallback
                        g2.fillOval(cx - r / 2, cy - r / 2, r, r);
                        break;
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(size + 12, size + 12);
            }
        };
        label.setOpaque(false);
        return label;
    }

    /**
     * Style a JTable with the refined dark theme + alternating rows.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_CARD);
        table.setGridColor(new Color(51, 65, 85, 80));
        table.setSelectionBackground(new Color(52, 211, 153, 50));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_CARD_ALT);
                }
                c.setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        });

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADING);
        header.setBackground(BG_PANEL);
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GREEN));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                l.setBackground(BG_PANEL);
                l.setForeground(TEXT_SECONDARY);
                l.setFont(FONT_HEADING);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_GREEN),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                return l;
            }
        });
    }

    /**
     * Style a JScrollPane with the dark theme.
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(BG_CARD);
    }

    /**
     * Get status badge color by status string.
     */
    public static Color getStatusColor(String status) {
        if (status == null) return TEXT_MUTED;
        switch (status.toUpperCase()) {
            case "OK": case "AVAILABLE": return ACCENT_GREEN;
            case "LOW": return ACCENT_YELLOW;
            case "CRITICAL": case "UNAVAILABLE": case "EXPIRED": case "EXPIRING": return ACCENT_RED;
            default: return TEXT_MUTED;
        }
    }
}

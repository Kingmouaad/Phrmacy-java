package com.pharmacy.gui;

import com.pharmacy.db.*;
import com.pharmacy.models.persons.Pharmacist;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Dashboard Home — overview stats + quick actions.
 */
public class DashboardHome extends JPanel {

    private final Pharmacist pharmacist;
    private JLabel salesCountLabel, revenueLabel, productsLabel, customersLabel;

    public DashboardHome(Pharmacist pharmacist) {
        this.pharmacist = pharmacist;
        setBackground(PharmacyTheme.BG_DARK);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // ═══ Header ═══
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel welcome = PharmacyTheme.createLabel(
                "Welcome back, " + pharmacist.getFullName() + " 👋",
                PharmacyTheme.FONT_TITLE, PharmacyTheme.TEXT_PRIMARY);
        header.add(welcome, BorderLayout.WEST);

        JLabel dateLabel = PharmacyTheme.createLabel(
                java.time.LocalDate.now().toString(),
                PharmacyTheme.FONT_BODY, PharmacyTheme.TEXT_MUTED);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(dateLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ═══ Stats Cards ═══
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);

        salesCountLabel = new JLabel("0");
        revenueLabel = new JLabel("$0.00");
        productsLabel = new JLabel("0");
        customersLabel = new JLabel("0");

        statsGrid.add(createStatCard("Today's Sales", salesCountLabel, PharmacyTheme.ACCENT_GREEN, "🛒"));
        statsGrid.add(createStatCard("Revenue", revenueLabel, PharmacyTheme.ACCENT_BLUE, "💰"));
        statsGrid.add(createStatCard("Products", productsLabel, PharmacyTheme.ACCENT_PURPLE, "💊"));
        statsGrid.add(createStatCard("Customers", customersLabel, PharmacyTheme.ACCENT_YELLOW, "👥"));

        add(statsGrid, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent, String emoji) {
        JPanel card = PharmacyTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        emojiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(emojiLabel);
        card.add(Box.createVerticalStrut(10));

        JLabel titleLabel = PharmacyTheme.createLabel(title, PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);

        valueLabel.setFont(PharmacyTheme.FONT_TITLE);
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valueLabel);

        return card;
    }

    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                SaleDAO saleDAO = new SaleDAO();
                ProductDAO productDAO = new ProductDAO();
                CustomerDAO customerDAO = new CustomerDAO();

                salesCountLabel.setText(String.valueOf(saleDAO.getTodaySalesCount()));
                revenueLabel.setText(String.format("$%.2f", saleDAO.getTodaysRevenue()));
                productsLabel.setText(String.valueOf(productDAO.findAll().size()));
                customersLabel.setText(String.valueOf(customerDAO.getCount()));
            } catch (SQLException e) {
                salesCountLabel.setText("ERR");
            }
        });
    }
}

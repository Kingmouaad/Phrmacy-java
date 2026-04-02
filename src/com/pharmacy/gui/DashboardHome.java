package com.pharmacy.gui;

import com.pharmacy.db.CustomerDAO;
import com.pharmacy.db.ProductDAO;
import com.pharmacy.db.SaleDAO;
import com.pharmacy.models.persons.Pharmacist;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Dashboard Home — overview stats + quick actions.
 */
public class DashboardHome extends JPanel {

    private final Pharmacist pharmacist;
    private final SaleDAO saleDAO;
    private final ProductDAO productDAO;
    private final CustomerDAO customerDAO;
    private JLabel salesCountLabel, revenueLabel, productsLabel, customersLabel;

    public DashboardHome(Pharmacist pharmacist) {
        this.pharmacist = pharmacist;
        this.saleDAO = new SaleDAO();
        this.productDAO = new ProductDAO();
        this.customerDAO = new CustomerDAO();
        setBackground(PharmacyTheme.BG_DARK);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // ═══ Header ═══
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel welcome = PharmacyTheme.createLabel(
                "Welcome back, " + pharmacist.getFullName(),
                PharmacyTheme.FONT_TITLE, PharmacyTheme.TEXT_PRIMARY);
        header.add(welcome, BorderLayout.WEST);

        JLabel dateLabel = PharmacyTheme.createLabel(
                java.time.LocalDate.now().toString(),
                PharmacyTheme.FONT_BODY, PharmacyTheme.TEXT_MUTED);
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(dateLabel, BorderLayout.EAST);

        // ═══ Stats Cards — wrapped so they don't stretch ═══
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        statsGrid.setOpaque(false);

        salesCountLabel = new JLabel("0");
        revenueLabel = new JLabel("$0.00");
        productsLabel = new JLabel("0");
        customersLabel = new JLabel("0");

        statsGrid.add(createStatCard("Today's Sales", salesCountLabel, PharmacyTheme.ACCENT_GREEN, "cart"));
        statsGrid.add(createStatCard("Revenue", revenueLabel, PharmacyTheme.ACCENT_BLUE, "dollar"));
        statsGrid.add(createStatCard("Products", productsLabel, PharmacyTheme.ACCENT_PURPLE, "pill"));
        statsGrid.add(createStatCard("Customers", customersLabel, PharmacyTheme.ACCENT_YELLOW, "users"));

        // Wrapper to constrain height and center horizontally
        JPanel statsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        statsWrapper.setOpaque(false);
        statsWrapper.add(statsGrid);

        // Top section: header + stats (not stretching to fill)
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(header);
        topSection.add(Box.createVerticalStrut(24));
        statsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        topSection.add(statsWrapper);

        add(topSection, BorderLayout.NORTH);

        // Empty center so cards don't stretch
        JPanel emptyCenter = new JPanel();
        emptyCenter.setOpaque(false);
        add(emptyCenter, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent, String iconType) {
        JPanel card = PharmacyTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(220, 150));
        card.setMinimumSize(new Dimension(200, 140));
        card.setMaximumSize(new Dimension(240, 160));

        // Icon — custom painted, always renders
        JLabel iconLabel = PharmacyTheme.createIconLabel(iconType, accent, 28);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));

        // Title
        JLabel titleLabel = PharmacyTheme.createLabel(title, PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_MUTED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));

        // Value
        valueLabel.setFont(PharmacyTheme.FONT_STAT);
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel);

        return card;
    }

    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
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

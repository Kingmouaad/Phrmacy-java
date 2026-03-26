package com.pharmacy.gui;

import com.pharmacy.models.persons.Pharmacist;

import javax.swing.*;
import java.awt.*;

/**
 * Main Dashboard — the central hub after login.
 * Sidebar navigation + swappable content panels.
 */
public class MainDashboard extends JFrame {

    private final Pharmacist pharmacist;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Panels
    private DashboardHome homePanel;
    private ProductPanel productPanel;
    private CustomerPanel customerPanel;
    private SalesPanel salesPanel;
    private InventoryPanel inventoryPanel;

    public MainDashboard(Pharmacist pharmacist) {
        this.pharmacist = pharmacist;
        setTitle("PharmaSystem — Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(PharmacyTheme.BG_DARK);

        // ═══ Sidebar ═══
        JPanel sidebar = buildSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);

        // ═══ Content Area ═══
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(PharmacyTheme.BG_DARK);

        homePanel = new DashboardHome(pharmacist);
        productPanel = new ProductPanel();
        customerPanel = new CustomerPanel();
        salesPanel = new SalesPanel(pharmacist);
        inventoryPanel = new InventoryPanel();

        contentPanel.add(homePanel, "HOME");
        contentPanel.add(productPanel, "PRODUCTS");
        contentPanel.add(customerPanel, "CUSTOMERS");
        contentPanel.add(salesPanel, "SALES");
        contentPanel.add(inventoryPanel, "INVENTORY");

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainContainer);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(PharmacyTheme.BG_PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, PharmacyTheme.BORDER));

        // Title in sidebar
        JPanel titleSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        titleSection.setBackground(PharmacyTheme.BG_PANEL);
        titleSection.setMaximumSize(new Dimension(220, 70));
        JLabel logo = new JLabel("💊 PharmaSystem");
        logo.setFont(PharmacyTheme.FONT_SUBTITLE);
        logo.setForeground(PharmacyTheme.ACCENT_GREEN);
        titleSection.add(logo);
        sidebar.add(titleSection);

        sidebar.add(Box.createVerticalStrut(10));

        // Navigation buttons
        addNavButton(sidebar, "🏠  Dashboard",  "HOME");
        addNavButton(sidebar, "💊  Products",    "PRODUCTS");
        addNavButton(sidebar, "👥  Customers",   "CUSTOMERS");
        addNavButton(sidebar, "🛒  Sales",       "SALES");
        addNavButton(sidebar, "📦  Inventory",   "INVENTORY");

        sidebar.add(Box.createVerticalGlue());

        // User info at bottom
        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(PharmacyTheme.BG_PANEL);
        userInfo.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        userInfo.setMaximumSize(new Dimension(220, 80));

        JLabel userName = new JLabel("👤 " + pharmacist.getFullName());
        userName.setFont(PharmacyTheme.FONT_SMALL);
        userName.setForeground(PharmacyTheme.TEXT_PRIMARY);
        userInfo.add(userName);

        JLabel userRole = new JLabel("   " + pharmacist.getAccessLevelName());
        userRole.setFont(PharmacyTheme.FONT_SMALL);
        userRole.setForeground(PharmacyTheme.TEXT_MUTED);
        userInfo.add(userRole);

        JButton logoutBtn = PharmacyTheme.createButton("Logout", PharmacyTheme.ACCENT_RED);
        logoutBtn.setPreferredSize(new Dimension(190, 32));
        logoutBtn.setMaximumSize(new Dimension(190, 32));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        userInfo.add(Box.createVerticalStrut(8));
        userInfo.add(logoutBtn);

        sidebar.add(userInfo);
        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(PharmacyTheme.FONT_BODY);
        btn.setForeground(PharmacyTheme.TEXT_PRIMARY);
        btn.setBackground(PharmacyTheme.BG_PANEL);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(220, 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PharmacyTheme.BG_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(PharmacyTheme.BG_PANEL);
            }
        });

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            // Refresh data when switching panels
            switch (cardName) {
                case "HOME": homePanel.refresh(); break;
                case "PRODUCTS": productPanel.refresh(); break;
                case "CUSTOMERS": customerPanel.refresh(); break;
                case "INVENTORY": inventoryPanel.refresh(); break;
            }
        });

        sidebar.add(btn);
    }
}

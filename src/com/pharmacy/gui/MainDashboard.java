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

    // Track active nav button for highlight
    private JButton activeNavButton = null;

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
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(PharmacyTheme.BG_PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, PharmacyTheme.BORDER));

        // Logo section
        JPanel titleSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        titleSection.setBackground(PharmacyTheme.BG_PANEL);
        titleSection.setMaximumSize(new Dimension(230, 70));

        JLabel plusSign = new JLabel("+");
        plusSign.setFont(new Font("Segoe UI", Font.BOLD, 24));
        plusSign.setForeground(PharmacyTheme.ACCENT_GREEN);
        titleSection.add(plusSign);

        JLabel logoText = new JLabel("PharmaSystem");
        logoText.setFont(PharmacyTheme.FONT_SUBTITLE);
        logoText.setForeground(PharmacyTheme.ACCENT_GREEN);
        titleSection.add(logoText);

        sidebar.add(titleSection);
        sidebar.add(Box.createVerticalStrut(12));

        // Navigation buttons with colored dots
        JButton dashBtn = addNavButton(sidebar, "Dashboard", PharmacyTheme.ACCENT_GREEN, "HOME");
        addNavButton(sidebar, "Products", PharmacyTheme.ACCENT_PURPLE, "PRODUCTS");
        addNavButton(sidebar, "Customers", PharmacyTheme.ACCENT_YELLOW, "CUSTOMERS");
        addNavButton(sidebar, "Sales", PharmacyTheme.ACCENT_BLUE, "SALES");
        addNavButton(sidebar, "Inventory", PharmacyTheme.ACCENT_RED, "INVENTORY");

        // Set Dashboard as initially active
        activeNavButton = dashBtn;
        dashBtn.setBackground(PharmacyTheme.BG_HOVER);

        sidebar.add(Box.createVerticalGlue());

        // User info at bottom
        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(PharmacyTheme.BG_PANEL);
        userInfo.setBorder(BorderFactory.createEmptyBorder(10, 18, 18, 18));
        userInfo.setMaximumSize(new Dimension(230, 90));

        JLabel userName = new JLabel(pharmacist.getFullName());
        userName.setFont(PharmacyTheme.FONT_BODY);
        userName.setForeground(PharmacyTheme.TEXT_PRIMARY);
        userInfo.add(userName);

        JLabel userRole = new JLabel(pharmacist.getAccessLevelName());
        userRole.setFont(PharmacyTheme.FONT_SMALL);
        userRole.setForeground(PharmacyTheme.TEXT_MUTED);
        userInfo.add(userRole);

        JButton logoutBtn = PharmacyTheme.createButton("Logout", PharmacyTheme.ACCENT_RED);
        logoutBtn.setPreferredSize(new Dimension(194, 34));
        logoutBtn.setMaximumSize(new Dimension(194, 34));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        userInfo.add(Box.createVerticalStrut(10));
        userInfo.add(logoutBtn);

        sidebar.add(userInfo);
        return sidebar;
    }

    private JButton addNavButton(JPanel sidebar, String text, Color dotColor, String cardName) {
        // Button with colored dot prefix
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw colored dot
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(20, getHeight() / 2 - 4, 8, 8);

                // Draw active indicator bar on left edge
                if (this == activeNavButton) {
                    g2.setColor(PharmacyTheme.ACCENT_GREEN);
                    g2.fillRoundRect(0, 6, 3, getHeight() - 12, 3, 3);
                }
                g2.dispose();
            }
        };
        btn.setFont(PharmacyTheme.FONT_BODY);
        btn.setForeground(PharmacyTheme.TEXT_PRIMARY);
        btn.setBackground(PharmacyTheme.BG_PANEL);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        // Extra left padding to make room for the dot
        btn.setBorder(BorderFactory.createEmptyBorder(12, 38, 12, 20));
        btn.setMaximumSize(new Dimension(230, 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeNavButton) {
                    btn.setBackground(new Color(45, 58, 78));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeNavButton) {
                    btn.setBackground(PharmacyTheme.BG_PANEL);
                }
            }
        });

        btn.addActionListener(e -> {
            // Update active state
            if (activeNavButton != null) {
                activeNavButton.setBackground(PharmacyTheme.BG_PANEL);
            }
            activeNavButton = btn;
            btn.setBackground(PharmacyTheme.BG_HOVER);

            cardLayout.show(contentPanel, cardName);
            switch (cardName) {
                case "HOME": homePanel.refresh(); break;
                case "PRODUCTS": productPanel.refresh(); break;
                case "CUSTOMERS": customerPanel.refresh(); break;
                case "INVENTORY": inventoryPanel.refresh(); break;
            }
        });

        sidebar.add(btn);
        return btn;
    }
}

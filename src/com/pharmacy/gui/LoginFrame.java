package com.pharmacy.gui;

import com.pharmacy.db.DatabaseConnection;
import com.pharmacy.db.UserDAO;
import com.pharmacy.models.persons.Pharmacist;

import javax.swing.*;
import java.awt.*;

/**
 * Login screen — dark-themed, modern pharmacy login.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Pharmacy Management System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize DB
        DatabaseConnection.getInstance().initializeDatabase();

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PharmacyTheme.BG_DARK,
                        getWidth(), getHeight(), new Color(10, 20, 36));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ═══ Logo — styled "Rx" text ═══
        JLabel icon = new JLabel("Rx", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 56));
        icon.setForeground(PharmacyTheme.ACCENT_GREEN);
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 0, 0, 0);
        mainPanel.add(icon, gbc);

        JLabel title = PharmacyTheme.createLabel("PharmaSystem", PharmacyTheme.FONT_TITLE, PharmacyTheme.ACCENT_GREEN);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 2, 0);
        mainPanel.add(title, gbc);

        JLabel subtitle = PharmacyTheme.createLabel("Management System v2.0",
                PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_MUTED);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(subtitle, gbc);

        // ═══ Username ═══
        JLabel userLabel = PharmacyTheme.createLabel("Username", PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_SECONDARY);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 60, 2, 60);
        mainPanel.add(userLabel, gbc);

        usernameField = PharmacyTheme.createTextField("Enter username");
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 60, 8, 60);
        mainPanel.add(usernameField, gbc);

        // ═══ Password ═══
        JLabel passLabel = PharmacyTheme.createLabel("Password", PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_SECONDARY);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 60, 2, 60);
        mainPanel.add(passLabel, gbc);

        passwordField = PharmacyTheme.createPasswordField("Enter password");
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 60, 16, 60);
        mainPanel.add(passwordField, gbc);

        // ═══ Login Button ═══
        JButton loginBtn = PharmacyTheme.createButton("Login", PharmacyTheme.ACCENT_GREEN);
        loginBtn.addActionListener(e -> attemptLogin());
        gbc.gridy = 7;
        gbc.insets = new Insets(8, 60, 8, 60);
        mainPanel.add(loginBtn, gbc);

        // ═══ Status Label ═══
        statusLabel = PharmacyTheme.createLabel(" ", PharmacyTheme.FONT_SMALL, PharmacyTheme.ACCENT_RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 8;
        gbc.insets = new Insets(8, 60, 8, 60);
        mainPanel.add(statusLabel, gbc);

        // ═══ Footer ═══
        JLabel footer = PharmacyTheme.createLabel("Default: admin / admin123",
                PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 9;
        gbc.insets = new Insets(20, 0, 20, 0);
        mainPanel.add(footer, gbc);

        // Enter key triggers login
        passwordField.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        setContentPane(mainPanel);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            return;
        }

        statusLabel.setForeground(PharmacyTheme.ACCENT_YELLOW);
        statusLabel.setText("Authenticating...");

        // Run DB query on a background thread so the UI stays responsive
        new SwingWorker<Pharmacist, Void>() {
            @Override
            protected Pharmacist doInBackground() throws Exception {
                UserDAO userDAO = new UserDAO();
                return userDAO.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    Pharmacist pharmacist = get();
                    if (pharmacist != null) {
                        statusLabel.setForeground(PharmacyTheme.ACCENT_GREEN);
                        statusLabel.setText("Welcome, " + pharmacist.getFullName() + "!");

                        Timer timer = new Timer(600, ev -> {
                            dispose();
                            new MainDashboard(pharmacist);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        statusLabel.setForeground(PharmacyTheme.ACCENT_RED);
                        statusLabel.setText("Invalid username or password");
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(PharmacyTheme.ACCENT_RED);
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}

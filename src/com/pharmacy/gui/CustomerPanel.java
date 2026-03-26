package com.pharmacy.gui;

import com.pharmacy.db.CustomerDAO;
import com.pharmacy.db.SaleDAO;
import com.pharmacy.models.persons.Customer;
import com.pharmacy.models.transactions.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Customer Management Panel — view, register, and check purchase history.
 */
public class CustomerPanel extends JPanel {

    private CustomerDAO customerDAO;
    private SaleDAO saleDAO;
    private JTable table;
    private DefaultTableModel tableModel;

    public CustomerPanel() {
        this.customerDAO = new CustomerDAO();
        this.saleDAO = new SaleDAO();
        setBackground(PharmacyTheme.BG_DARK);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // Header
        JLabel title = PharmacyTheme.createLabel("👥 Customer Management",
                PharmacyTheme.FONT_TITLE, PharmacyTheme.TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Phone", "Email", "Loyalty Points", "Allergens"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PharmacyTheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        PharmacyTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actions.setOpaque(false);

        JButton addBtn = PharmacyTheme.createButton("+ Register", PharmacyTheme.ACCENT_GREEN);
        addBtn.setPreferredSize(new Dimension(140, 36));
        addBtn.addActionListener(e -> showRegisterDialog());
        actions.add(addBtn);

        JButton historyBtn = PharmacyTheme.createButton("Purchase History", PharmacyTheme.ACCENT_BLUE);
        historyBtn.setPreferredSize(new Dimension(170, 36));
        historyBtn.addActionListener(e -> showPurchaseHistory());
        actions.add(historyBtn);

        JButton refreshBtn = PharmacyTheme.createButton("Refresh", PharmacyTheme.ACCENT_PURPLE);
        refreshBtn.setPreferredSize(new Dimension(120, 36));
        refreshBtn.addActionListener(e -> refresh());
        actions.add(refreshBtn);

        add(actions, BorderLayout.SOUTH);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        try {
            List<Customer> customers = customerDAO.findAll();
            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                    c.getPersonId(), c.getFullName(), c.getPhoneNumber(),
                    c.getEmail(), String.format("%.0f", c.getLoyaltyPoints()),
                    c.getAllergens().isEmpty() ? "None" : c.getAllergensAsString()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showRegisterDialog() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        JTextField idF = new JTextField();
        JTextField nameF = new JTextField();
        JTextField phoneF = new JTextField();
        JTextField emailF = new JTextField();
        JTextField addrF = new JTextField();

        form.add(new JLabel("Customer ID:")); form.add(idF);
        form.add(new JLabel("Full Name:"));   form.add(nameF);
        form.add(new JLabel("Phone:"));       form.add(phoneF);
        form.add(new JLabel("Email:"));       form.add(emailF);
        form.add(new JLabel("Address:"));     form.add(addrF);

        int result = JOptionPane.showConfirmDialog(this, form, "Register Customer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Customer c = new Customer(idF.getText().trim(), nameF.getText().trim(),
                        phoneF.getText().trim(), emailF.getText().trim(), addrF.getText().trim());
                customerDAO.insert(c);
                refresh();
                JOptionPane.showMessageDialog(this, "Customer registered!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPurchaseHistory() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer first.");
            return;
        }
        String customerId = (String) tableModel.getValueAt(row, 0);
        String customerName = (String) tableModel.getValueAt(row, 1);

        try {
            List<Sale> sales = saleDAO.findByCustomer(customerId);
            StringBuilder sb = new StringBuilder();
            sb.append("Purchase History for ").append(customerName).append("\n\n");

            if (sales.isEmpty()) {
                sb.append("No purchases yet.");
            } else {
                for (Sale s : sales) {
                    sb.append("TX: ").append(s.getTransactionId())
                      .append(" | Total: $").append(String.format("%.2f", s.getTotalAmount()))
                      .append(" | Status: ").append(s.getStatus())
                      .append("\n");
                }
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(PharmacyTheme.FONT_MONO);
            textArea.setEditable(false);
            JScrollPane sp = new JScrollPane(textArea);
            sp.setPreferredSize(new Dimension(500, 300));
            JOptionPane.showMessageDialog(this, sp, "Purchase History", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}

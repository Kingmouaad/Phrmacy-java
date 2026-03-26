package com.pharmacy.gui;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.interfaces.Expirable;
import com.pharmacy.models.products.product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Inventory Panel — stock levels, expiration checks, low stock alerts.
 */
public class InventoryPanel extends JPanel {

    private ProductDAO productDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel totalValueLabel;

    public InventoryPanel() {
        this.productDAO = new ProductDAO();
        setBackground(PharmacyTheme.BG_DARK);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = PharmacyTheme.createLabel("📦 Inventory Overview",
                PharmacyTheme.FONT_TITLE, PharmacyTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        totalValueLabel = PharmacyTheme.createLabel("Total Value: $0.00",
                PharmacyTheme.FONT_SUBTITLE, PharmacyTheme.ACCENT_GREEN);
        totalValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(totalValueLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Type", "Quantity", "Price", "Status", "Expiry"};
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

        JButton lowStockBtn = PharmacyTheme.createButton("Low Stock Only", PharmacyTheme.ACCENT_YELLOW);
        lowStockBtn.setPreferredSize(new Dimension(160, 36));
        lowStockBtn.addActionListener(e -> showLowStock());
        actions.add(lowStockBtn);

        JButton expiringBtn = PharmacyTheme.createButton("Expiring Soon", PharmacyTheme.ACCENT_RED);
        expiringBtn.setPreferredSize(new Dimension(160, 36));
        expiringBtn.addActionListener(e -> showExpiring());
        actions.add(expiringBtn);

        JButton restockBtn = PharmacyTheme.createButton("Restock", PharmacyTheme.ACCENT_GREEN);
        restockBtn.setPreferredSize(new Dimension(120, 36));
        restockBtn.addActionListener(e -> showRestockDialog());
        actions.add(restockBtn);

        JButton allBtn = PharmacyTheme.createButton("Show All", PharmacyTheme.ACCENT_BLUE);
        allBtn.setPreferredSize(new Dimension(120, 36));
        allBtn.addActionListener(e -> refresh());
        actions.add(allBtn);

        add(actions, BorderLayout.SOUTH);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        double totalValue = 0;
        try {
            List<product> products = productDAO.findAll();
            for (product p : products) {
                String stockStatus = p.getquantity() > 20 ? "OK" :
                                     p.getquantity() > 5  ? "Low" : "Critical";
                String expiry = "N/A";
                if (p instanceof Expirable) {
                    Expirable exp = (Expirable) p;
                    if (exp.getExpirationDate() != null) {
                        expiry = exp.isExpired() ? "EXPIRED" : exp.getExpirationDate().toString();
                    }
                }

                tableModel.addRow(new Object[]{
                    p.getid(), p.getname(), p.getProductType(),
                    p.getquantity(), String.format("$%.2f", p.getprice()),
                    stockStatus, expiry
                });
                totalValue += p.getprice() * p.getquantity();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        totalValueLabel.setText(String.format("Total Value: $%.2f", totalValue));
    }

    private void showLowStock() {
        tableModel.setRowCount(0);
        try {
            List<product> products = productDAO.findAll();
            for (product p : products) {
                if (p.getquantity() <= 10) {
                    tableModel.addRow(new Object[]{
                        p.getid(), p.getname(), p.getProductType(),
                        p.getquantity(), String.format("$%.2f", p.getprice()),
                        "LOW", "—"
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showExpiring() {
        tableModel.setRowCount(0);
        try {
            List<product> products = productDAO.findExpiringWithin(30);
            for (product p : products) {
                String expiry = "N/A";
                if (p instanceof Expirable) {
                    Expirable exp = (Expirable) p;
                    if (exp.getExpirationDate() != null) {
                        long daysLeft = exp.getDaysUntilExpiration();
                        expiry = exp.getExpirationDate() + " (" + daysLeft + " days)";
                    }
                }
                tableModel.addRow(new Object[]{
                    p.getid(), p.getname(), p.getProductType(),
                    p.getquantity(), String.format("$%.2f", p.getprice()),
                    "EXPIRING", expiry
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showRestockDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to restock.");
            return;
        }
        String pid = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        String input = JOptionPane.showInputDialog(this,
                "Restock quantity for '" + name + "' (" + pid + "):");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int addQty = Integer.parseInt(input.trim());
                int currentQty = (int) tableModel.getValueAt(row, 3);
                productDAO.updateStock(pid, currentQty + addQty);
                refresh();
                JOptionPane.showMessageDialog(this,
                        "Restocked " + addQty + " units of " + name);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}

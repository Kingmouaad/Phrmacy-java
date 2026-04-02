package com.pharmacy.gui;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.models.products.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Product Management Panel — view, search, add, delete products.
 */
public class ProductPanel extends JPanel {

    private ProductDAO productDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ProductPanel() {
        this.productDAO = new ProductDAO();
        setBackground(PharmacyTheme.BG_DARK);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // ═══ Header ═══
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JLabel title = PharmacyTheme.createLabel("Product Management",
                PharmacyTheme.FONT_TITLE, PharmacyTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        searchField = PharmacyTheme.createTextField("Search products...");
        searchField.setPreferredSize(new Dimension(250, 36));
        searchField.addActionListener(e -> searchProducts());
        searchBar.add(searchField);

        JButton searchBtn = PharmacyTheme.createButton("Search", PharmacyTheme.ACCENT_BLUE);
        searchBtn.setPreferredSize(new Dimension(100, 36));
        searchBtn.addActionListener(e -> searchProducts());
        searchBar.add(searchBtn);

        header.add(searchBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ═══ Table ═══
        String[] cols = {"ID", "Name", "Type", "Price", "Quantity", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PharmacyTheme.styleTable(table);

        // Status column badge renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        PharmacyTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // ═══ Action Buttons — centered ═══
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        actions.setOpaque(false);

        JButton addBtn = PharmacyTheme.createButton("+ Add Product", PharmacyTheme.ACCENT_GREEN);
        addBtn.setPreferredSize(new Dimension(160, 38));
        addBtn.addActionListener(e -> showAddDialog());
        actions.add(addBtn);

        JButton deleteBtn = PharmacyTheme.createButton("Delete", PharmacyTheme.ACCENT_RED);
        deleteBtn.setPreferredSize(new Dimension(120, 38));
        deleteBtn.addActionListener(e -> deleteSelected());
        actions.add(deleteBtn);

        JButton refreshBtn = PharmacyTheme.createButton("Refresh", PharmacyTheme.ACCENT_BLUE);
        refreshBtn.setPreferredSize(new Dimension(120, 38));
        refreshBtn.addActionListener(e -> refresh());
        actions.add(refreshBtn);

        add(actions, BorderLayout.SOUTH);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        try {
            List<product> products = productDAO.findAll();
            for (product p : products) {
                String status = p.isAvailableForSale() ? "Available" : "Unavailable";
                tableModel.addRow(new Object[]{
                    p.getid(), p.getname(), p.getProductType(),
                    String.format("$%.2f", p.getprice()), p.getquantity(), status
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void searchProducts() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refresh();
            return;
        }
        tableModel.setRowCount(0);
        try {
            List<product> results = productDAO.findByName(query);
            for (product p : results) {
                String status = p.isAvailableForSale() ? "Available" : "Unavailable";
                tableModel.addRow(new Object[]{
                    p.getid(), p.getname(), p.getProductType(),
                    String.format("$%.2f", p.getprice()), p.getquantity(), status
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBackground(PharmacyTheme.BG_CARD);

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField qtyField = new JTextField();
        String[] types = {"OTCMedicine", "PrescriptionMedicine", "MedicalDevice", "Supplement"};
        JComboBox<String> typeBox = new JComboBox<>(types);

        JTextField ingredientField = new JTextField();
        JTextField formField = new JTextField();
        JTextField strengthField = new JTextField();
        JTextField mfgField = new JTextField();

        form.add(new JLabel("Product ID:")); form.add(idField);
        form.add(new JLabel("Name:"));       form.add(nameField);
        form.add(new JLabel("Price:"));      form.add(priceField);
        form.add(new JLabel("Quantity:"));   form.add(qtyField);
        form.add(new JLabel("Type:"));       form.add(typeBox);
        form.add(new JLabel("Active Ingredient:")); form.add(ingredientField);
        form.add(new JLabel("Dosage Form:"));       form.add(formField);
        form.add(new JLabel("Strength:"));           form.add(strengthField);
        form.add(new JLabel("Manufacturer:"));       form.add(mfgField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());
                String type = (String) typeBox.getSelectedItem();

                product newProduct = null;
                switch (type) {
                    case "OTCMedicine":
                        newProduct = new otcmedicine(id, name, price, qty,
                                ingredientField.getText(), formField.getText(),
                                strengthField.getText(), mfgField.getText());
                        break;
                    case "PrescriptionMedicine":
                        newProduct = new PrescriptionMedicine(id, name, price, qty,
                                ingredientField.getText(), formField.getText(),
                                strengthField.getText(), mfgField.getText());
                        break;
                    case "MedicalDevice":
                        newProduct = new medicaledevice(id, name, price, qty,
                                formField.getText(), 12, mfgField.getText());
                        break;
                    case "Supplement":
                        newProduct = new Supplement(id, name, price, qty,
                                ingredientField.getText(), formField.getText());
                        break;
                }

                if (newProduct != null) {
                    productDAO.insertWithStock(newProduct, qty, 10);
                    refresh();
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete product '" + name + "' (ID: " + id + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productDAO.delete(id);
                refresh();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    /**
     * Custom renderer for status column — renders colored pill badges.
     */
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            String status = value != null ? value.toString() : "";
            Color badgeColor = PharmacyTheme.getStatusColor(status);

            JLabel badge = new JLabel(status, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected) {
                        g2.setColor(new Color(52, 211, 153, 50));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    } else {
                        g2.setColor(row % 2 == 0 ? PharmacyTheme.BG_CARD : PharmacyTheme.BG_CARD_ALT);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }
                    // Pill background
                    g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 35));
                    int px = 8, py = 6;
                    g2.fillRoundRect(px, py, getWidth() - px * 2, getHeight() - py * 2, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setForeground(badgeColor);
            badge.setOpaque(false);
            badge.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return badge;
        }
    }
}

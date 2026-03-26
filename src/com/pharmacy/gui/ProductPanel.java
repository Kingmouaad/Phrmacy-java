package com.pharmacy.gui;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.models.products.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // ═══ Header ═══
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JLabel title = PharmacyTheme.createLabel("💊 Product Management",
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

        JScrollPane scrollPane = new JScrollPane(table);
        PharmacyTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // ═══ Action Buttons ═══
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actions.setOpaque(false);

        JButton addBtn = PharmacyTheme.createButton("+ Add Product", PharmacyTheme.ACCENT_GREEN);
        addBtn.setPreferredSize(new Dimension(160, 36));
        addBtn.addActionListener(e -> showAddDialog());
        actions.add(addBtn);

        JButton deleteBtn = PharmacyTheme.createButton("Delete", PharmacyTheme.ACCENT_RED);
        deleteBtn.setPreferredSize(new Dimension(120, 36));
        deleteBtn.addActionListener(e -> deleteSelected());
        actions.add(deleteBtn);

        JButton refreshBtn = PharmacyTheme.createButton("Refresh", PharmacyTheme.ACCENT_BLUE);
        refreshBtn.setPreferredSize(new Dimension(120, 36));
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

        // Medicine-specific fields
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
}

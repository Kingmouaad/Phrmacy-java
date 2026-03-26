package com.pharmacy.gui;

import com.pharmacy.db.*;
import com.pharmacy.models.persons.Pharmacist;
import com.pharmacy.models.persons.Customer;
import com.pharmacy.models.products.product;
import com.pharmacy.models.transactions.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales Terminal Panel — process new sales and view recent transactions.
 */
public class SalesPanel extends JPanel {

    private final Pharmacist pharmacist;
    private ProductDAO productDAO;
    private CustomerDAO customerDAO;
    private SaleDAO saleDAO;

    // Sale builder
    private DefaultTableModel cartModel;
    private JTextField customerIdField;
    private JTextField productIdField;
    private JTextField qtyField;
    private JLabel subtotalLabel;
    private List<String> cartProductIds = new ArrayList<>();
    private List<Integer> cartQuantities = new ArrayList<>();
    private List<Double> cartPrices = new ArrayList<>();
    private double subtotal = 0;

    public SalesPanel(Pharmacist pharmacist) {
        this.pharmacist = pharmacist;
        this.productDAO = new ProductDAO();
        this.customerDAO = new CustomerDAO();
        this.saleDAO = new SaleDAO();
        setBackground(PharmacyTheme.BG_DARK);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        // Header
        JLabel title = PharmacyTheme.createLabel("🛒 Sales Terminal",
                PharmacyTheme.FONT_TITLE, PharmacyTheme.TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        // Left: Cart Table
        String[] cols = {"Product ID", "Name", "Qty", "Price", "Line Total"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        PharmacyTheme.styleTable(cartTable);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        PharmacyTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // Right: Input form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setPreferredSize(new Dimension(280, 0));
        formPanel.setBackground(PharmacyTheme.BG_PANEL);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(PharmacyTheme.createLabel("Customer ID", PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_SECONDARY));
        customerIdField = PharmacyTheme.createTextField("e.g. CUS001");
        customerIdField.setMaximumSize(new Dimension(250, 40));
        formPanel.add(customerIdField);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(PharmacyTheme.createLabel("Product ID", PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_SECONDARY));
        productIdField = PharmacyTheme.createTextField("e.g. MED001");
        productIdField.setMaximumSize(new Dimension(250, 40));
        formPanel.add(productIdField);
        formPanel.add(Box.createVerticalStrut(8));

        formPanel.add(PharmacyTheme.createLabel("Quantity", PharmacyTheme.FONT_SMALL, PharmacyTheme.TEXT_SECONDARY));
        qtyField = PharmacyTheme.createTextField("1");
        qtyField.setMaximumSize(new Dimension(250, 40));
        formPanel.add(qtyField);
        formPanel.add(Box.createVerticalStrut(12));

        JButton addToCartBtn = PharmacyTheme.createButton("+ Add to Cart", PharmacyTheme.ACCENT_BLUE);
        addToCartBtn.setMaximumSize(new Dimension(250, 40));
        addToCartBtn.addActionListener(e -> addToCart());
        formPanel.add(addToCartBtn);

        formPanel.add(Box.createVerticalStrut(20));

        subtotalLabel = PharmacyTheme.createLabel("Subtotal: $0.00",
                PharmacyTheme.FONT_SUBTITLE, PharmacyTheme.ACCENT_GREEN);
        formPanel.add(subtotalLabel);

        formPanel.add(Box.createVerticalGlue());

        JButton completeBtn = PharmacyTheme.createButton("Complete Sale", PharmacyTheme.ACCENT_GREEN);
        completeBtn.setMaximumSize(new Dimension(250, 44));
        completeBtn.addActionListener(e -> completeSale());
        formPanel.add(completeBtn);

        formPanel.add(Box.createVerticalStrut(8));

        JButton clearBtn = PharmacyTheme.createButton("Clear Cart", PharmacyTheme.ACCENT_RED);
        clearBtn.setMaximumSize(new Dimension(250, 36));
        clearBtn.addActionListener(e -> clearCart());
        formPanel.add(clearBtn);

        add(formPanel, BorderLayout.EAST);
    }

    private void addToCart() {
        String pid = productIdField.getText().trim();
        String qtyStr = qtyField.getText().trim();

        if (pid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Product ID.");
            return;
        }

        try {
            product p = productDAO.findById(pid);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Product not found: " + pid);
                return;
            }

            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0 || qty > p.getquantity()) {
                JOptionPane.showMessageDialog(this, "Invalid quantity (available: " + p.getquantity() + ")");
                return;
            }

            double lineTotal = p.getprice() * qty;
            cartProductIds.add(pid);
            cartQuantities.add(qty);
            cartPrices.add(p.getprice());
            subtotal += lineTotal;

            cartModel.addRow(new Object[]{
                pid, p.getname(), qty, String.format("$%.2f", p.getprice()),
                String.format("$%.2f", lineTotal)
            });

            subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
            productIdField.setText("");
            qtyField.setText("1");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void completeSale() {
        if (cartProductIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        String custId = customerIdField.getText().trim();
        if (custId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Customer ID.");
            return;
        }

        try {
            Customer customer = customerDAO.findById(custId);
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Customer not found: " + custId);
                return;
            }

            String txnId = saleDAO.getNextTransactionId();
            Sale sale = new Sale(txnId, pharmacist.getPersonId(), custId);
            for (int i = 0; i < cartProductIds.size(); i++) {
                sale.addProduct(cartProductIds.get(i), cartQuantities.get(i));
            }
            sale.setPaymentMethod("CARD");
            sale.calculateTotal(subtotal);
            sale.completeSale();

            saleDAO.processSale(sale, cartProductIds, cartQuantities, cartPrices, subtotal, custId);

            JOptionPane.showMessageDialog(this,
                    "Sale completed!\nTransaction: " + txnId + "\nTotal: $" + String.format("%.2f", subtotal),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            clearCart();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Sale failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        cartProductIds.clear();
        cartQuantities.clear();
        cartPrices.clear();
        subtotal = 0;
        subtotalLabel.setText("Subtotal: $0.00");
    }
}

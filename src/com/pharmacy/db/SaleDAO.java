package com.pharmacy.db;

import com.pharmacy.models.transactions.Sale;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Sale operations.
 * Contains the CRITICAL JDBC transaction for sale processing
 * (commit/rollback across sales, sale_items, and stock tables).
 */
public class SaleDAO {

    private final Connection connection;

    public SaleDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Process a complete sale using a JDBC TRANSACTION.
     * This atomically:
     * 1. Inserts the sale record
     * 2. Inserts all sale items
     * 3. Updates stock quantities
     * 4. Updates customer loyalty points
     *
     * If ANY step fails, the ENTIRE transaction is ROLLED BACK.
     */
    public void processSale(Sale sale, List<String> productIds, List<Integer> quantities,
                            List<Double> unitPrices, double loyaltyPointsToAdd,
                            String customerId) throws SQLException {

        boolean autoCommit = connection.getAutoCommit();
        try {
            // BEGIN TRANSACTION
            connection.setAutoCommit(false);

            // Step 1: Insert the sale record
            String saleSql = "INSERT INTO sales (transaction_id, customer_id, pharmacist_id, "
                           + "sale_date, subtotal, discount, total_amount, payment_method, status) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(saleSql)) {
                pstmt.setString(1, sale.getTransactionId());
                pstmt.setString(2, sale.getCustomerId());
                pstmt.setString(3, sale.getPharmacistId());
                pstmt.setString(4, LocalDateTime.now().toString());
                pstmt.setDouble(5, sale.getTotalAmount() + sale.getDiscount());
                pstmt.setDouble(6, sale.getDiscount());
                pstmt.setDouble(7, sale.getTotalAmount());
                pstmt.setString(8, sale.getPaymentMethod());
                pstmt.setString(9, sale.getStatus());
                pstmt.executeUpdate();
            }

            // Step 2: Insert sale items + Step 3: Update stock
            String itemSql = "INSERT INTO sale_items (transaction_id, product_id, quantity, unit_price, line_total) "
                           + "VALUES (?, ?, ?, ?, ?)";
            String stockSql = "UPDATE stock SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";

            for (int i = 0; i < productIds.size(); i++) {
                String pid = productIds.get(i);
                int qty = quantities.get(i);
                double price = unitPrices.get(i);
                double lineTotal = price * qty;

                // Insert sale item
                try (PreparedStatement pstmt = connection.prepareStatement(itemSql)) {
                    pstmt.setString(1, sale.getTransactionId());
                    pstmt.setString(2, pid);
                    pstmt.setInt(3, qty);
                    pstmt.setDouble(4, price);
                    pstmt.setDouble(5, lineTotal);
                    pstmt.executeUpdate();
                }

                // Update stock (subtract sold quantity)
                try (PreparedStatement pstmt = connection.prepareStatement(stockSql)) {
                    pstmt.setInt(1, qty);
                    pstmt.setString(2, pid);
                    pstmt.setInt(3, qty); // WHERE quantity >= qty (safety check)
                    int updated = pstmt.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Insufficient stock for product: " + pid);
                    }
                }
            }

            // Step 4: Update customer loyalty points
            if (customerId != null && !customerId.isEmpty()) {
                String loyaltySql = "UPDATE customers SET loyalty_points = loyalty_points + ? WHERE customer_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(loyaltySql)) {
                    pstmt.setDouble(1, loyaltyPointsToAdd);
                    pstmt.setString(2, customerId);
                    pstmt.executeUpdate();
                }
            }

            // COMMIT TRANSACTION — all steps succeeded
            connection.commit();

        } catch (SQLException e) {
            // ROLLBACK — something failed, undo everything
            connection.rollback();
            throw new SQLException("Sale transaction failed and was rolled back: " + e.getMessage(), e);
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    /**
     * Find a sale by transaction ID (with sale items).
     */
    public Sale findById(String transactionId) throws SQLException {
        String sql = "SELECT * FROM sales WHERE transaction_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                loadSaleItems(sale);
                return sale;
            }
        }
        return null;
    }

    /**
     * Get all sales.
     */
    public List<Sale> findAll() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                loadSaleItems(sale);
                sales.add(sale);
            }
        }
        return sales;
    }

    /**
     * Find all sales for a specific customer.
     */
    public List<Sale> findByCustomer(String customerId) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE customer_id = ? ORDER BY sale_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                loadSaleItems(sale);
                sales.add(sale);
            }
        }
        return sales;
    }

    /**
     * Find sales within a date range.
     * Used for the customer purchase history with date filter.
     */
    public List<Sale> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE DATE(sale_date) BETWEEN ? AND ? ORDER BY sale_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, from.toString());
            pstmt.setString(2, to.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                loadSaleItems(sale);
                sales.add(sale);
            }
        }
        return sales;
    }

    /**
     * Get today's sales count — used in the dashboard.
     */
    public int getTodaySalesCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = DATE('now') AND status = 'COMPLETED'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get today's total revenue.
     */
    public double getTodaysRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales "
                   + "WHERE DATE(sale_date) = DATE('now') AND status = 'COMPLETED'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Get the next available transaction ID.
     */
    public String getNextTransactionId() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sales";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return "TXN" + String.format("%03d", count);
            }
        }
        return "TXN001";
    }

    /**
     * Get sales statistics: total sales per day for the last N days.
     * Used for the chart/statistics panel.
     */
    public List<Object[]> getSalesPerDay(int lastNDays) throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT DATE(sale_date) as sale_day, COUNT(*), COALESCE(SUM(total_amount), 0) "
                   + "FROM sales WHERE DATE(sale_date) >= DATE('now', '-' || ? || ' days') "
                   + "AND status = 'COMPLETED' "
                   + "GROUP BY DATE(sale_date) ORDER BY sale_day";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, lastNDays);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stats.add(new Object[]{
                    rs.getString("sale_day"),
                    rs.getInt(2),
                    rs.getDouble(3)
                });
            }
        }
        return stats;
    }

    // ==========================================
    // Helpers
    // ==========================================

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale(
            rs.getString("transaction_id"),
            rs.getString("pharmacist_id"),
            rs.getString("customer_id")
        );
        sale.setDiscount(rs.getDouble("discount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setStatus(rs.getString("status"));
        return sale;
    }

    /**
     * Load sale items into a Sale object.
     */
    private void loadSaleItems(Sale sale) throws SQLException {
        String sql = "SELECT * FROM sale_items WHERE transaction_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sale.getTransactionId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sale.addProduct(rs.getString("product_id"), rs.getInt("quantity"));
            }
        }
    }
}

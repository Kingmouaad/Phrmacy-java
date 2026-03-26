package com.pharmacy.db;

import com.pharmacy.models.transactions.Restock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for RESTOCK transactions.
 * Inserts restock + restock_items atomically and updates stock quantities.
 */
public class RestockDAO {

    private final Connection connection;

    public RestockDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void processRestock(Restock restock,
                                 List<String> productIds,
                                 List<Integer> quantities,
                                 double totalCost,
                                 String supplierId) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            // Insert restock header
            String restockSql = "INSERT INTO restocks (transaction_id, pharmacist_id, supplier_id, restock_date, total_cost, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(restockSql)) {
                pstmt.setString(1, restock.getTransactionId());
                pstmt.setString(2, restock.getPharmacistId());
                pstmt.setString(3, supplierId);
                // Store timestamp as ISO string for simplicity
                pstmt.setString(4, LocalDateTime.now().toString());
                pstmt.setDouble(5, totalCost);
                pstmt.setString(6, restock.getStatus());
                pstmt.executeUpdate();
            }

            // Insert items
            String itemSql = "INSERT INTO restock_items (transaction_id, product_id, quantity) VALUES (?, ?, ?)";
            String stockSql = "UPDATE stock SET quantity = quantity + ? WHERE product_id = ?";

            for (int i = 0; i < productIds.size(); i++) {
                String pid = productIds.get(i);
                int qty = quantities.get(i);

                try (PreparedStatement itemPstmt = connection.prepareStatement(itemSql)) {
                    itemPstmt.setString(1, restock.getTransactionId());
                    itemPstmt.setString(2, pid);
                    itemPstmt.setInt(3, qty);
                    itemPstmt.executeUpdate();
                }

                try (PreparedStatement stockPstmt = connection.prepareStatement(stockSql)) {
                    stockPstmt.setInt(1, qty);
                    stockPstmt.setString(2, pid);
                    stockPstmt.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    public List<Restock> findAll() throws SQLException {
        List<Restock> restocks = new ArrayList<>();
        String sql = "SELECT * FROM restocks ORDER BY restock_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Restock restock = mapHeader(rs);
                loadItems(restock);
                restocks.add(restock);
            }
        }
        return restocks;
    }

    private Restock mapHeader(ResultSet rs) throws SQLException {
        String transactionId = rs.getString("transaction_id");
        String pharmacistId = rs.getString("pharmacist_id");
        String supplierId = rs.getString("supplier_id");

        Restock restock = new Restock(transactionId, pharmacistId, supplierId);
        restock.setTotalCost(rs.getDouble("total_cost"));
        restock.setStatus(rs.getString("status"));
        return restock;
    }

    private void loadItems(Restock restock) throws SQLException {
        String sql = "SELECT product_id, quantity FROM restock_items WHERE transaction_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, restock.getTransactionId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                restock.addProduct(rs.getString("product_id"), rs.getInt("quantity"));
            }
        }
    }
}


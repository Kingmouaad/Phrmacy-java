package com.pharmacy.db;

import com.pharmacy.models.transactions.Return;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for RETURN transactions.
 * Inserts return + return_items atomically and updates stock quantities.
 */
public class ReturnDAO {

    private final Connection connection;

    public ReturnDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void processReturn(Return returnTxn,
                                List<String> productIds,
                                List<Integer> quantities,
                                double refundTotal) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            // Insert return header
            String returnSql = "INSERT INTO returns (transaction_id, customer_id, pharmacist_id, original_sale_id, " +
                    "return_date, reason, refund_method, total_refund, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(returnSql)) {
                pstmt.setString(1, returnTxn.getTransactionId());
                pstmt.setString(2, returnTxn.getCustomerId());
                pstmt.setString(3, returnTxn.getPharmacistId());
                pstmt.setString(4, returnTxn.getOriginalSaleId());
                pstmt.setString(5, LocalDateTime.now().toString());
                pstmt.setString(6, returnTxn.getReason());
                pstmt.setString(7, returnTxn.getRefundMethod());
                pstmt.setDouble(8, refundTotal);
                pstmt.setString(9, returnTxn.getStatus());
                pstmt.executeUpdate();
            }

            // Insert items + restore stock
            String itemSql = "INSERT INTO return_items (transaction_id, product_id, quantity) VALUES (?, ?, ?)";
            String stockSql = "UPDATE stock SET quantity = quantity + ? WHERE product_id = ?";

            for (int i = 0; i < productIds.size(); i++) {
                String pid = productIds.get(i);
                int qty = quantities.get(i);

                try (PreparedStatement itemPstmt = connection.prepareStatement(itemSql)) {
                    itemPstmt.setString(1, returnTxn.getTransactionId());
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

    public List<Return> findAll() throws SQLException {
        List<Return> returns = new ArrayList<>();
        String sql = "SELECT * FROM returns ORDER BY return_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Return returnTxn = mapHeader(rs);
                loadItems(returnTxn);
                returns.add(returnTxn);
            }
        }

        return returns;
    }

    private Return mapHeader(ResultSet rs) throws SQLException {
        Return r = new Return(
                rs.getString("transaction_id"),
                rs.getString("pharmacist_id"),
                rs.getString("customer_id"),
                rs.getString("original_sale_id")
        );
        r.setReason(rs.getString("reason"));
        r.setRefundMethod(rs.getString("refund_method"));
        r.setTotalAmount(rs.getDouble("total_refund"));
        r.setStatus(rs.getString("status"));
        return r;
    }

    private void loadItems(Return r) throws SQLException {
        String sql = "SELECT product_id, quantity FROM return_items WHERE transaction_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, r.getTransactionId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                r.addProduct(rs.getString("product_id"), rs.getInt("quantity"));
            }
        }
    }
}


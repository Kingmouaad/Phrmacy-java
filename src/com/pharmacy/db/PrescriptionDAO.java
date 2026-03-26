package com.pharmacy.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Prescription operations.
 * All queries use PreparedStatement.
 */
public class PrescriptionDAO {

    private final Connection connection;

    public PrescriptionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Insert a new prescription.
     */
    public void insert(String prescriptionId, String customerId, String productId,
                       String doctorName, String issueDate, String expiryDate,
                       boolean isRenewable) throws SQLException {
        String sql = "INSERT INTO prescriptions (prescription_id, customer_id, product_id, "
                   + "doctor_name, issue_date, expiry_date, is_renewable, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, prescriptionId);
            pstmt.setString(2, customerId);
            pstmt.setString(3, productId);
            pstmt.setString(4, doctorName);
            pstmt.setString(5, issueDate);
            pstmt.setString(6, expiryDate);
            pstmt.setInt(7, isRenewable ? 1 : 0);
            pstmt.executeUpdate();
        }
    }

    /**
     * Find a prescription by ID.
     */
    public ResultSet findById(String prescriptionId) throws SQLException {
        String sql = "SELECT * FROM prescriptions WHERE prescription_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, prescriptionId);
        return pstmt.executeQuery();
    }

    /**
     * Find all active prescriptions for a customer.
     */
    public List<Object[]> findByCustomer(String customerId) throws SQLException {
        List<Object[]> prescriptions = new ArrayList<>();
        String sql = "SELECT p.*, pr.name as product_name FROM prescriptions p "
                   + "JOIN products pr ON p.product_id = pr.product_id "
                   + "WHERE p.customer_id = ? ORDER BY p.issue_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prescriptions.add(new Object[]{
                    rs.getString("prescription_id"),
                    rs.getString("customer_id"),
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    rs.getString("doctor_name"),
                    rs.getString("issue_date"),
                    rs.getString("expiry_date"),
                    rs.getInt("is_renewable") == 1,
                    rs.getString("status")
                });
            }
        }
        return prescriptions;
    }

    /**
     * Find active prescriptions for a specific product of a customer.
     * Used during sale to validate prescription.
     */
    public boolean hasValidPrescription(String customerId, String productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prescriptions "
                   + "WHERE customer_id = ? AND product_id = ? "
                   + "AND status = 'ACTIVE' AND expiry_date >= DATE('now')";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            pstmt.setString(2, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Check if a prescription is valid (exists, active, not expired).
     */
    public boolean isValid(String prescriptionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prescriptions "
                   + "WHERE prescription_id = ? AND status = 'ACTIVE' "
                   + "AND expiry_date >= DATE('now')";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, prescriptionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Mark a prescription as used.
     */
    public void markAsUsed(String prescriptionId) throws SQLException {
        String sql = "UPDATE prescriptions SET status = 'USED' WHERE prescription_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, prescriptionId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Mark expired prescriptions automatically.
     */
    public int markExpiredPrescriptions() throws SQLException {
        String sql = "UPDATE prescriptions SET status = 'EXPIRED' "
                   + "WHERE status = 'ACTIVE' AND expiry_date < DATE('now')";
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Renew a prescription (create new entry based on existing one).
     */
    public void renew(String oldPrescriptionId, String newPrescriptionId,
                      String newExpiryDate) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // Get old prescription details
            String selectSql = "SELECT * FROM prescriptions WHERE prescription_id = ? AND is_renewable = 1";
            String customerId, productId, doctorName;

            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, oldPrescriptionId);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Prescription not found or not renewable: " + oldPrescriptionId);
                }
                customerId = rs.getString("customer_id");
                productId = rs.getString("product_id");
                doctorName = rs.getString("doctor_name");
            }

            // Mark old as USED
            markAsUsed(oldPrescriptionId);

            // Insert new prescription
            insert(newPrescriptionId, customerId, productId, doctorName,
                   java.time.LocalDate.now().toString(), newExpiryDate, true);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    /**
     * Get all prescriptions (for admin view).
     */
    public List<Object[]> findAll() throws SQLException {
        List<Object[]> prescriptions = new ArrayList<>();
        String sql = "SELECT p.*, pr.name as product_name, c.full_name as customer_name "
                   + "FROM prescriptions p "
                   + "JOIN products pr ON p.product_id = pr.product_id "
                   + "JOIN customers c ON p.customer_id = c.customer_id "
                   + "ORDER BY p.issue_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                prescriptions.add(new Object[]{
                    rs.getString("prescription_id"),
                    rs.getString("customer_id"),
                    rs.getString("customer_name"),
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    rs.getString("doctor_name"),
                    rs.getString("issue_date"),
                    rs.getString("expiry_date"),
                    rs.getInt("is_renewable") == 1,
                    rs.getString("status")
                });
            }
        }
        return prescriptions;
    }

    /**
     * Get the next prescription ID.
     */
    public String getNextPrescriptionId() throws SQLException {
        String sql = "SELECT COUNT(*) FROM prescriptions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return "RX" + String.format("%03d", count);
            }
        }
        return "RX001";
    }
}

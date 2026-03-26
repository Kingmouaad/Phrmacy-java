package com.pharmacy.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Drug Interaction operations.
 * Uses SQL JOIN on interactions table before finalizing multi-drug sales.
 */
public class InteractionDAO {

    private final Connection connection;

    public InteractionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Insert a new drug interaction pair.
     */
    public void insert(String drugA, String drugB, String severity, String description) throws SQLException {
        String sql = "INSERT INTO interactions (drug_a, drug_b, severity, description) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, drugA);
            pstmt.setString(2, drugB);
            pstmt.setString(3, severity);
            pstmt.setString(4, description);
            pstmt.executeUpdate();
        }
    }

    /**
     * Check if two drugs have a known interaction.
     * Checks both directions (drugA→drugB and drugB→drugA).
     * Returns null if no interaction found, or Object[] {severity, description} if found.
     */
    public Object[] checkInteraction(String drugA, String drugB) throws SQLException {
        String sql = "SELECT severity, description FROM interactions "
                   + "WHERE (LOWER(drug_a) = LOWER(?) AND LOWER(drug_b) = LOWER(?)) "
                   + "OR (LOWER(drug_a) = LOWER(?) AND LOWER(drug_b) = LOWER(?))";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, drugA);
            pstmt.setString(2, drugB);
            pstmt.setString(3, drugB);
            pstmt.setString(4, drugA);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Object[]{
                    rs.getString("severity"),
                    rs.getString("description")
                };
            }
        }
        return null;
    }

    /**
     * Check interactions between a new drug and a list of active drugs.
     * Used before finalizing a multi-drug sale.
     * Returns list of interactions found: {drugA, drugB, severity, description}.
     */
    public List<Object[]> checkInteractionsForSale(String newDrug, List<String> activeDrugs) throws SQLException {
        List<Object[]> interactions = new ArrayList<>();

        String sql = "SELECT drug_a, drug_b, severity, description FROM interactions "
                   + "WHERE (LOWER(drug_a) = LOWER(?) AND LOWER(drug_b) = LOWER(?)) "
                   + "OR (LOWER(drug_a) = LOWER(?) AND LOWER(drug_b) = LOWER(?))";

        for (String activeDrug : activeDrugs) {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, newDrug);
                pstmt.setString(2, activeDrug);
                pstmt.setString(3, activeDrug);
                pstmt.setString(4, newDrug);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    interactions.add(new Object[]{
                        rs.getString("drug_a"),
                        rs.getString("drug_b"),
                        rs.getString("severity"),
                        rs.getString("description")
                    });
                }
            }
        }
        return interactions;
    }

    /**
     * Find all interactions for a specific drug.
     */
    public List<Object[]> findAllForDrug(String drugName) throws SQLException {
        List<Object[]> interactions = new ArrayList<>();
        String sql = "SELECT * FROM interactions "
                   + "WHERE LOWER(drug_a) = LOWER(?) OR LOWER(drug_b) = LOWER(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, drugName);
            pstmt.setString(2, drugName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                interactions.add(new Object[]{
                    rs.getString("drug_a"),
                    rs.getString("drug_b"),
                    rs.getString("severity"),
                    rs.getString("description")
                });
            }
        }
        return interactions;
    }

    /**
     * Get all drug interactions.
     */
    public List<Object[]> findAll() throws SQLException {
        List<Object[]> interactions = new ArrayList<>();
        String sql = "SELECT * FROM interactions ORDER BY severity DESC, drug_a";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                interactions.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("drug_a"),
                    rs.getString("drug_b"),
                    rs.getString("severity"),
                    rs.getString("description")
                });
            }
        }
        return interactions;
    }

    /**
     * Delete an interaction.
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM interactions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Check drug against customer's active prescriptions for interaction warnings.
     * SQL JOIN on interactions table + prescriptions table.
     */
    public List<Object[]> checkAgainstActivePrescriptions(String newDrugIngredient,
                                                          String customerId) throws SQLException {
        List<Object[]> warnings = new ArrayList<>();
        String sql = "SELECT i.drug_a, i.drug_b, i.severity, i.description, "
                   + "p.product_id, pr.name as product_name "
                   + "FROM interactions i "
                   + "JOIN products pr ON (LOWER(pr.active_ingredient) = LOWER(i.drug_a) "
                   + "                     OR LOWER(pr.active_ingredient) = LOWER(i.drug_b)) "
                   + "JOIN prescriptions p ON p.product_id = pr.product_id "
                   + "WHERE p.customer_id = ? AND p.status = 'ACTIVE' "
                   + "AND p.expiry_date >= DATE('now') "
                   + "AND (LOWER(i.drug_a) = LOWER(?) OR LOWER(i.drug_b) = LOWER(?)) "
                   + "AND LOWER(pr.active_ingredient) != LOWER(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            pstmt.setString(2, newDrugIngredient);
            pstmt.setString(3, newDrugIngredient);
            pstmt.setString(4, newDrugIngredient);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                warnings.add(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("severity"),
                    rs.getString("description")
                });
            }
        }
        return warnings;
    }
}

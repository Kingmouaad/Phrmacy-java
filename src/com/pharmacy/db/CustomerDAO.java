package com.pharmacy.db;

import com.pharmacy.models.persons.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer CRUD operations.
 * All queries use PreparedStatement.
 */
public class CustomerDAO {

    private final Connection connection;

    public CustomerDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Insert a new customer.
     */
    public void insert(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (customer_id, full_name, phone, email, address, loyalty_points, allergens) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, c.getPersonId());
            pstmt.setString(2, c.getFullName());
            pstmt.setString(3, c.getPhoneNumber());
            pstmt.setString(4, c.getEmail());
            pstmt.setString(5, c.getAddress());
            pstmt.setDouble(6, c.getLoyaltyPoints());
            pstmt.setString(7, ""); // allergens — will be managed by GUI
            pstmt.executeUpdate();
        }
    }

    /**
     * Find a customer by ID.
     */
    public Customer findById(String customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        }
        return null;
    }

    /**
     * Get all customers.
     */
    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    /**
     * Update customer information.
     */
    public void update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ?, "
                   + "address = ?, loyalty_points = ? WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, c.getFullName());
            pstmt.setString(2, c.getPhoneNumber());
            pstmt.setString(3, c.getEmail());
            pstmt.setString(4, c.getAddress());
            pstmt.setDouble(5, c.getLoyaltyPoints());
            pstmt.setString(6, c.getPersonId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Update loyalty points for a customer.
     */
    public void updateLoyaltyPoints(String customerId, double points) throws SQLException {
        String sql = "UPDATE customers SET loyalty_points = ? WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, points);
            pstmt.setString(2, customerId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Update allergens for a customer.
     */
    public void updateAllergens(String customerId, String allergens) throws SQLException {
        String sql = "UPDATE customers SET allergens = ? WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, allergens);
            pstmt.setString(2, customerId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get allergens for a customer as a list.
     */
    public List<String> getAllergens(String customerId) throws SQLException {
        List<String> allergens = new ArrayList<>();
        String sql = "SELECT allergens FROM customers WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String allergensStr = rs.getString("allergens");
                if (allergensStr != null && !allergensStr.trim().isEmpty()) {
                    for (String allergen : allergensStr.split(",")) {
                        allergens.add(allergen.trim());
                    }
                }
            }
        }
        return allergens;
    }

    /**
     * Delete a customer by ID.
     */
    public void delete(String customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Search customers by name.
     */
    public List<Customer> findByName(String query) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE LOWER(full_name) LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    /**
     * Get the count of all customers.
     */
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // ==========================================
    // Helper
    // ==========================================

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer(
            rs.getString("customer_id"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getString("email") != null ? rs.getString("email") : "",
            rs.getString("address") != null ? rs.getString("address") : ""
        );
        double loyalty = rs.getDouble("loyalty_points");
        if (loyalty > 0) {
            c.addLoyaltyPoints(loyalty);
        }
        return c;
    }
}

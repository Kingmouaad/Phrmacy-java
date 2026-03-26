package com.pharmacy.db;

import com.pharmacy.models.persons.Pharmacist;

import java.sql.*;

/**
 * Data Access Object for User authentication.
 * Handles login validation against the database.
 */
public class UserDAO {

    private final Connection connection;

    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Authenticate a user with username and password.
     * Returns the Pharmacist object if authentication succeeds, null otherwise.
     */
    public Pharmacist authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String pharmacistId = rs.getString("pharmacist_id");
                String fullName = rs.getString("full_name");
                int accessLevel = rs.getInt("access_level");

                Pharmacist pharmacist = new Pharmacist(
                    // `Person` requires a non-empty phone number.
                    // The `users` table in schema.sql doesn't store phone/email/address,
                    // so we use safe placeholders.
                    pharmacistId,
                    fullName,
                    "N/A",
                    "N/A",
                    "N/A",
                    "LICENSE"
                );
                pharmacist.setAccessLevel(accessLevel);
                return pharmacist;
            }
        }
        return null;
    }

    /**
     * Register a new user.
     */
    public void insert(String username, String password, String pharmacistId,
                       String fullName, int accessLevel) throws SQLException {
        String sql = "INSERT INTO users (username, password, pharmacist_id, full_name, access_level) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, pharmacistId);
            pstmt.setString(4, fullName);
            pstmt.setInt(5, accessLevel);
            pstmt.executeUpdate();
        }
    }

    /**
     * Find user by username.
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Update user password.
     */
    public void updatePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get user count (to check if initialization is needed).
     */
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}

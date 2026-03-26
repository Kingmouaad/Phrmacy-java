package com.pharmacy.db;

import com.pharmacy.models.products.*;
import com.pharmacy.interfaces.Expirable;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product CRUD operations.
 * Uses PreparedStatement for all queries (no string concatenation).
 */
public class ProductDAO {

    private final Connection connection;

    public ProductDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Insert a new product into the database.
     */
    public void insert(product p) throws SQLException {
        String sql = "INSERT INTO products (product_id, name, price, product_type, "
                   + "active_ingredient, dosage_form, strength, manufacturer, expiration_date, "
                   + "purchase_limit, minimum_age, device_type, warranty_months, "
                   + "supplement_type, serving_size, benefits, requires_prescription) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, p.getid());
            pstmt.setString(2, p.getname());
            pstmt.setDouble(3, p.getprice());
            pstmt.setString(4, getProductTypeCode(p));

            // Medicine-specific fields
            if (p instanceof medicine) {
                medicine m = (medicine) p;
                pstmt.setString(5, m.getActiveIngredient());
                pstmt.setString(6, m.getDosageForm());
                pstmt.setString(7, m.getStrength());
                pstmt.setString(8, m.getManufacturer());
                if (m.getExpirationDate() != null) {
                    pstmt.setString(9, m.getExpirationDate().toString());
                } else {
                    pstmt.setNull(9, Types.VARCHAR);
                }
            } else {
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setNull(9, Types.VARCHAR);
            }

            // OTC-specific
            if (p instanceof otcmedicine) {
                otcmedicine otc = (otcmedicine) p;
                pstmt.setInt(10, otc.getPurchaseLimit());
                pstmt.setInt(11, otc.getMinimumAge());
            } else {
                pstmt.setInt(10, 0);
                pstmt.setInt(11, 0);
            }

            // Medical Device-specific
            if (p instanceof medicaledevice) {
                medicaledevice dev = (medicaledevice) p;
                pstmt.setString(12, dev.getDeviceType());
                pstmt.setInt(13, dev.getWarrantyMonths());
            } else {
                pstmt.setNull(12, Types.VARCHAR);
                pstmt.setInt(13, 0);
            }

            // Supplement-specific
            if (p instanceof Supplement) {
                Supplement sup = (Supplement) p;
                pstmt.setString(14, sup.getSupplementType());
                pstmt.setString(15, sup.getServingSize());
                pstmt.setString(16, sup.getBenefits());
                if (sup.getExpirationDate() != null) {
                    pstmt.setString(9, sup.getExpirationDate().toString());
                }
            } else {
                pstmt.setNull(14, Types.VARCHAR);
                pstmt.setNull(15, Types.VARCHAR);
                pstmt.setNull(16, Types.VARCHAR);
            }

            // Prescription flag
            if (p instanceof PrescriptionMedicine) {
                pstmt.setInt(17, 1);
            } else {
                pstmt.setInt(17, 0);
            }

            pstmt.executeUpdate();
        }
    }

    /**
     * Insert a product and its initial stock in a single transaction.
     */
    public void insertWithStock(product p, int quantity, int minThreshold) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            insert(p);

            String stockSql = "INSERT INTO stock (product_id, quantity, min_threshold, last_restocked) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(stockSql)) {
                pstmt.setString(1, p.getid());
                pstmt.setInt(2, quantity);
                pstmt.setInt(3, minThreshold);
                pstmt.setString(4, LocalDate.now().toString());
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    /**
     * Find a product by its ID.
     */
    public product findById(String productId) throws SQLException {
        String sql = "SELECT p.*, s.quantity FROM products p "
                   + "LEFT JOIN stock s ON p.product_id = s.product_id "
                   + "WHERE p.product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        }
        return null;
    }

    /**
     * Get all products from the database.
     */
    public List<product> findAll() throws SQLException {
        List<product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.quantity FROM products p "
                   + "LEFT JOIN stock s ON p.product_id = s.product_id "
                   + "ORDER BY p.product_type, p.name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                product p = mapResultSetToProduct(rs);
                if (p != null) {
                    products.add(p);
                }
            }
        }
        return products;
    }

    /**
     * Update a product's basic fields.
     */
    public void update(product p) throws SQLException {
        String sql = "UPDATE products SET name = ?, price = ?, "
                   + "active_ingredient = ?, dosage_form = ?, strength = ?, "
                   + "manufacturer = ?, expiration_date = ? WHERE product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, p.getname());
            pstmt.setDouble(2, p.getprice());

            if (p instanceof medicine) {
                medicine m = (medicine) p;
                pstmt.setString(3, m.getActiveIngredient());
                pstmt.setString(4, m.getDosageForm());
                pstmt.setString(5, m.getStrength());
                pstmt.setString(6, m.getManufacturer());
                if (m.getExpirationDate() != null) {
                    pstmt.setString(7, m.getExpirationDate().toString());
                } else {
                    pstmt.setNull(7, Types.VARCHAR);
                }
            } else if (p instanceof Supplement) {
                Supplement sup = (Supplement) p;
                pstmt.setNull(3, Types.VARCHAR);
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
                if (sup.getExpirationDate() != null) {
                    pstmt.setString(7, sup.getExpirationDate().toString());
                } else {
                    pstmt.setNull(7, Types.VARCHAR);
                }
            } else {
                pstmt.setNull(3, Types.VARCHAR);
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
            }

            pstmt.setString(8, p.getid());
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete a product by ID.
     */
    public void delete(String productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Search products by name (partial match).
     */
    public List<product> findByName(String query) throws SQLException {
        List<product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.quantity FROM products p "
                   + "LEFT JOIN stock s ON p.product_id = s.product_id "
                   + "WHERE LOWER(p.name) LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                product p = mapResultSetToProduct(rs);
                if (p != null) products.add(p);
            }
        }
        return products;
    }

    /**
     * Find products expiring within a given number of days.
     * Key query for the dashboard expiry alert system.
     */
    public List<product> findExpiringWithin(int days) throws SQLException {
        List<product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.quantity FROM products p "
                   + "LEFT JOIN stock s ON p.product_id = s.product_id "
                   + "WHERE p.expiration_date IS NOT NULL "
                   + "AND p.expiration_date <= date('now', '+' || ? || ' days') "
                   + "ORDER BY p.expiration_date ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                product p = mapResultSetToProduct(rs);
                if (p != null) products.add(p);
            }
        }
        return products;
    }

    /**
     * Find products with stock below their minimum threshold.
     * Used for reorder suggestions.
     */
    public List<product> findLowStock() throws SQLException {
        List<product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.quantity FROM products p "
                   + "JOIN stock s ON p.product_id = s.product_id "
                   + "WHERE s.quantity <= s.min_threshold "
                   + "ORDER BY s.quantity ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                product p = mapResultSetToProduct(rs);
                if (p != null) products.add(p);
            }
        }
        return products;
    }

    /**
     * Update stock quantity for a product.
     */
    public void updateStock(String productId, int newQuantity) throws SQLException {
        String sql = "UPDATE stock SET quantity = ? WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setString(2, productId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Get current stock quantity for a product.
     */
    public int getStockQuantity(String productId) throws SQLException {
        String sql = "SELECT quantity FROM stock WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }
        return 0;
    }

    // ==========================================
    // Helper methods
    // ==========================================

    /**
     * Maps a ResultSet row to the correct product subclass.
     */
    private product mapResultSetToProduct(ResultSet rs) throws SQLException {
        String type = rs.getString("product_type");
        String id = rs.getString("product_id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        int quantity = rs.getInt("quantity");

        product p = null;

        switch (type) {
            case "PrescriptionMedicine":
                PrescriptionMedicine pm = new PrescriptionMedicine(
                    id, name, price, quantity,
                    nonNull(rs.getString("active_ingredient")),
                    nonNull(rs.getString("dosage_form")),
                    nonNull(rs.getString("strength")),
                    nonNull(rs.getString("manufacturer"))
                );
                setExpirationDate(pm, rs.getString("expiration_date"));
                p = pm;
                break;

            case "OTCMedicine":
                otcmedicine otc = new otcmedicine(
                    id, name, price, quantity,
                    nonNull(rs.getString("active_ingredient")),
                    nonNull(rs.getString("dosage_form")),
                    nonNull(rs.getString("strength")),
                    nonNull(rs.getString("manufacturer"))
                );
                otc.setPurchaseLimit(rs.getInt("purchase_limit"));
                otc.setMinimumAge(rs.getInt("minimum_age"));
                setExpirationDate(otc, rs.getString("expiration_date"));
                p = otc;
                break;

            case "MedicalDevice":
                medicaledevice dev = new medicaledevice(
                    id, name, price, quantity,
                    nonNull(rs.getString("device_type")),
                    rs.getInt("warranty_months"),
                    nonNull(rs.getString("manufacturer"))
                );
                p = dev;
                break;

            case "Supplement":
                Supplement sup = new Supplement(
                    id, name, price, quantity,
                    nonNull(rs.getString("supplement_type")),
                    nonNull(rs.getString("serving_size"))
                );
                sup.setBenefits(rs.getString("benefits"));
                setExpirationDate(sup, rs.getString("expiration_date"));
                p = sup;
                break;
        }

        return p;
    }

    private String getProductTypeCode(product p) {
        if (p instanceof PrescriptionMedicine) return "PrescriptionMedicine";
        if (p instanceof otcmedicine) return "OTCMedicine";
        if (p instanceof medicaledevice) return "MedicalDevice";
        if (p instanceof Supplement) return "Supplement";
        return "Unknown";
    }

    private String nonNull(String s) {
        return s != null ? s : "";
    }

    private void setExpirationDate(Expirable exp, String dateStr) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                exp.setExpirationDate(LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignore invalid dates
            }
        }
    }
}

package com.pharmacy.datastructures;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.models.products.product;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * HashMap-based product catalog for O(1) product lookups by ID.
 *
 * WHY WE USE HashMap:
 * - Before: every time we needed a product, we looped through the entire
 *   ArrayList one by one (O(n) — slow when there are thousands of products).
 * - Now: we store products in a HashMap keyed by product_id. Looking up
 *   a product by its ID is instant O(1), like opening a locker with a key.
 *
 * HOW IT WORKS:
 * - On startup, we load all products from the DB into the HashMap.
 * - When someone scans a barcode (types a product ID), we instantly
 *   find the product without searching through all of them.
 * - Any add/update/delete also updates both the HashMap AND the database.
 */
public class ProductCatalog {

    // The core data structure: product_id -> product object
    private final HashMap<String, product> catalog;
    private final ProductDAO productDAO;

    public ProductCatalog() {
        this.catalog = new HashMap<>();
        this.productDAO = new ProductDAO();
        loadFromDatabase();
    }

    /**
     * Load all products from the DB into the HashMap cache.
     */
    public void loadFromDatabase() {
        catalog.clear();
        try {
            List<product> products = productDAO.findAll();
            for (product p : products) {
                catalog.put(p.getid().toUpperCase(), p);
            }
            System.out.println("[ProductCatalog] Loaded " + catalog.size() + " products into HashMap.");
        } catch (SQLException e) {
            System.out.println("[ProductCatalog] Error loading products: " + e.getMessage());
        }
    }

    /**
     * O(1) instant lookup by product ID — the whole point of using HashMap.
     */
    public product getById(String productId) {
        return catalog.get(productId.trim().toUpperCase());
    }

    /**
     * Check if a product exists — O(1).
     */
    public boolean contains(String productId) {
        return catalog.containsKey(productId.trim().toUpperCase());
    }

    /**
     * Add a product to both the HashMap and the database.
     */
    public void add(product p, int quantity, int minThreshold) throws SQLException {
        productDAO.insertWithStock(p, quantity, minThreshold);
        catalog.put(p.getid().toUpperCase(), p);
    }

    /**
     * Update a product in both the HashMap and the database.
     */
    public void update(product p) throws SQLException {
        productDAO.update(p);
        catalog.put(p.getid().toUpperCase(), p);
    }

    /**
     * Delete a product from both the HashMap and the database.
     */
    public void delete(String productId) throws SQLException {
        productDAO.delete(productId);
        catalog.remove(productId.toUpperCase());
    }

    /**
     * Get all products as a collection — useful for iterating.
     */
    public Collection<product> getAll() {
        return catalog.values();
    }

    /**
     * Get the total number of products.
     */
    public int size() {
        return catalog.size();
    }

    /**
     * Search products by name (partial match).
     * Still iterates, but over the in-memory HashMap (much faster than DB query).
     */
    public List<product> searchByName(String query) {
        String lowerQuery = query.toLowerCase();
        return catalog.values().stream()
                .filter(p -> p.getname().toLowerCase().contains(lowerQuery))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Update stock quantity in both cache and database.
     */
    public void updateStock(String productId, int newQuantity) throws SQLException {
        productDAO.updateStock(productId, newQuantity);
        product p = catalog.get(productId.toUpperCase());
        if (p != null) {
            p.setquantity(newQuantity);
        }
    }

    /**
     * Refresh a single product from the database.
     */
    public void refresh(String productId) {
        try {
            product p = productDAO.findById(productId);
            if (p != null) {
                catalog.put(p.getid().toUpperCase(), p);
            } else {
                catalog.remove(productId.toUpperCase());
            }
        } catch (SQLException e) {
            System.out.println("[ProductCatalog] Error refreshing product: " + e.getMessage());
        }
    }
}

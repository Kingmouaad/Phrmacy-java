package com.pharmacy.generics;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.interfaces.Expirable;
import com.pharmacy.models.products.product;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic stock manager that works with ANY product type.
 *
 * WHY GENERICS:
 * - Without generics, we'd need separate stock checkers for medicines,
 *   devices, and supplements — duplicating code 3 times.
 * - With generics, we write ONE class: StockManager<T extends product>
 *   and Java's type system guarantees safety at compile time.
 *
 * HOW IT WORKS:
 * - T is a "type parameter" bounded by `product`. This means T can be
 *   any class that extends product: medicine, otcmedicine, Supplement, etc.
 * - The compiler checks types for us — no casting needed, no runtime errors.
 *
 * @param <T> Any subclass of product (medicine, medicaledevice, Supplement, etc.)
 */
public class StockManager<T extends product> {

    private final List<T> managedProducts;
    private final ProductDAO productDAO;
    private final String categoryName;

    /**
     * Create a StockManager for a specific product category.
     * @param categoryName Human-readable name like "Medicines" or "Devices"
     */
    public StockManager(String categoryName) {
        this.managedProducts = new ArrayList<>();
        this.productDAO = new ProductDAO();
        this.categoryName = categoryName;
    }

    /**
     * Add a product to this manager's inventory.
     * Type-safe: only accepts products of type T.
     */
    public void addProduct(T product) {
        managedProducts.add(product);
    }

    /**
     * Load products from a list (filtered by the caller to the correct type).
     */
    public void loadProducts(List<T> products) {
        managedProducts.clear();
        managedProducts.addAll(products);
    }

    /**
     * Get all products managed by this StockManager.
     * Returns List<T> — the caller knows the exact type.
     */
    public List<T> getAllProducts() {
        return managedProducts;
    }

    /**
     * Find a product by ID — type-safe return (no casting needed by caller).
     */
    public T findById(String productId) {
        for (T p : managedProducts) {
            if (p.getid().equalsIgnoreCase(productId.trim())) {
                return p;
            }
        }
        return null;
    }

    /**
     * Get products that are LOW on stock (below threshold).
     * Generic — works for any product type.
     */
    public List<T> getLowStock(int threshold) {
        List<T> lowStock = new ArrayList<>();
        for (T p : managedProducts) {
            if (p.getquantity() <= threshold) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }

    /**
     * Get products that are OUT OF STOCK.
     */
    public List<T> getOutOfStock() {
        return getLowStock(0);
    }

    /**
     * Get products that are AVAILABLE for sale.
     */
    public List<T> getAvailable() {
        List<T> available = new ArrayList<>();
        for (T p : managedProducts) {
            if (p.isAvailableForSale()) {
                available.add(p);
            }
        }
        return available;
    }

    /**
     * Get products that are EXPIRING SOON (only works if T implements Expirable).
     * Demonstrates generic method with interface checking.
     */
    public List<T> getExpiringSoon(int daysThreshold) {
        List<T> expiring = new ArrayList<>();
        LocalDate deadline = LocalDate.now().plusDays(daysThreshold);
        for (T p : managedProducts) {
            if (p instanceof Expirable) {
                Expirable exp = (Expirable) p;
                LocalDate expDate = exp.getExpirationDate();
                if (expDate != null && !expDate.isAfter(deadline)) {
                    expiring.add(p);
                }
            }
        }
        return expiring;
    }

    /**
     * Restock a specific product — type-safe.
     */
    public boolean restock(String productId, int quantity) {
        T product = findById(productId);
        if (product == null) return false;

        product.setquantity(product.getquantity() + quantity);
        try {
            productDAO.updateStock(productId, product.getquantity());
        } catch (SQLException e) {
            System.out.println("[StockManager] DB update failed: " + e.getMessage());
        }
        return true;
    }

    /**
     * Get total stock value (price × quantity for all products).
     */
    public double getTotalStockValue() {
        double total = 0;
        for (T p : managedProducts) {
            total += p.getprice() * p.getquantity();
        }
        return total;
    }

    /**
     * Print a stock report for this category.
     */
    public void printStockReport() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("  STOCK REPORT: " + categoryName);
        System.out.println("═══════════════════════════════════════");
        System.out.println("  Total products: " + managedProducts.size());
        System.out.printf("  Total stock value: $%.2f%n", getTotalStockValue());
        System.out.println("───────────────────────────────────────");

        for (T p : managedProducts) {
            String status = p.getquantity() > 20 ? "✅" :
                           p.getquantity() > 5  ? "🟡" : "🔴";
            System.out.printf("  %s %s — %s: %d units ($%.2f each)%n",
                    status, p.getid(), p.getname(), p.getquantity(), p.getprice());
        }
        System.out.println("═══════════════════════════════════════\n");
    }

    /**
     * Get the category name this manager handles.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Count products in this manager.
     */
    public int size() {
        return managedProducts.size();
    }
}

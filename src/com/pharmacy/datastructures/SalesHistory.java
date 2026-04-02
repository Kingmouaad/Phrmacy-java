package com.pharmacy.datastructures;

import com.pharmacy.db.SaleDAO;
import com.pharmacy.models.transactions.Sale;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * LinkedList-based sales history for chronological order.
 *
 * WHY WE USE LinkedList:
 * - Before: sales were stored in an ArrayList. Adding to the end is fine,
 *   but LinkedList is better for a chronological list because:
 *     • addFirst/addLast are O(1) — perfect for appending new sales
 *     • We mostly iterate through the history sequentially (receipts, reports)
 *     • We rarely need random access by index
 *
 * HOW IT WORKS:
 * - New sales are added to the END of the list (most recent last).
 * - When displaying, we iterate from end to start (most recent first).
 * - The list is loaded from the database on startup and stays in sync.
 */
public class SalesHistory {

    // The core data structure: chronological linked list of sales
    private final LinkedList<Sale> history;
    private final SaleDAO saleDAO;

    public SalesHistory() {
        this.history = new LinkedList<>();
        this.saleDAO = new SaleDAO();
        loadFromDatabase();
    }

    /**
     * Load all sales from the database into the LinkedList.
     */
    public void loadFromDatabase() {
        history.clear();
        try {
            List<Sale> sales = saleDAO.findAll();
            history.addAll(sales);
            System.out.println("[SalesHistory] Loaded " + history.size() + " sales into LinkedList.");
        } catch (SQLException e) {
            System.out.println("[SalesHistory] Error loading: " + e.getMessage());
        }
    }

    /**
     * Add a new sale to the history (O(1) — just links to the end).
     */
    public void addSale(Sale sale) {
        history.addLast(sale);
    }

    /**
     * Get the most recent sale — O(1) with LinkedList.
     */
    public Sale getMostRecent() {
        return history.isEmpty() ? null : history.getLast();
    }

    /**
     * Get the oldest sale — O(1) with LinkedList.
     */
    public Sale getOldest() {
        return history.isEmpty() ? null : history.getFirst();
    }

    /**
     * Get the last N sales (most recent first).
     */
    public List<Sale> getRecentSales(int count) {
        LinkedList<Sale> recent = new LinkedList<>();
        // Use descendingIterator() instead of get(i) — O(n) vs O(n²)
        java.util.Iterator<Sale> it = history.descendingIterator();
        while (it.hasNext() && recent.size() < count) {
            recent.add(it.next());
        }
        return recent;
    }

    /**
     * Get all sales for a specific customer.
     */
    public List<Sale> getByCustomer(String customerId) {
        List<Sale> result = new LinkedList<>();
        for (Sale sale : history) {
            if (sale.getCustomerId() != null && sale.getCustomerId().equalsIgnoreCase(customerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Print a summary of recent sales.
     */
    public void printRecentSummary(int count) {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("       RECENT SALES HISTORY            ");
        System.out.println("═══════════════════════════════════════");

        List<Sale> recent = getRecentSales(count);
        if (recent.isEmpty()) {
            System.out.println("  No sales recorded yet.");
        } else {
            for (Sale sale : recent) {
                System.out.println("  " + sale);
                System.out.println("  ─".repeat(25));
            }
        }
        System.out.println("  Total sales in history: " + history.size());
        System.out.println("═══════════════════════════════════════\n");
    }

    /**
     * Get total number of sales.
     */
    public int size() {
        return history.size();
    }

    /**
     * Get the full linked list (for iteration).
     */
    public LinkedList<Sale> getAll() {
        return history;
    }
}

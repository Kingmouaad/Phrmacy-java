package com.pharmacy.datastructures;

import com.pharmacy.interfaces.Expirable;
import com.pharmacy.models.products.product;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * TreeMap-based expiration tracker for sorted date-based lookups.
 *
 * WHY WE USE TreeMap:
 * - Before: to find products expiring soon, we looped through ALL products
 *   and checked each date one by one. Unsorted, unorganized.
 * - Now: TreeMap automatically sorts products by expiration date (earliest first).
 *   Think of it as a sorted filing cabinet where the front drawer always
 *   has the soonest-expiring items.
 *
 * HOW IT WORKS:
 * - Key   = expiration date (LocalDate)
 * - Value = list of products that expire on that date
 * - TreeMap keeps keys sorted, so we can instantly ask:
 *   "Give me everything expiring in the next 30 days" without scanning all products.
 */
public class ExpirationTracker {

    // The core data structure: expiration_date -> list of products expiring on that date
    private final TreeMap<LocalDate, List<product>> expirationMap;

    public ExpirationTracker() {
        this.expirationMap = new TreeMap<>();
    }

    /**
     * Load products into the tracker from any collection.
     */
    public void loadProducts(Collection<product> products) {
        expirationMap.clear();
        for (product p : products) {
            addProduct(p);
        }
    }

    /**
     * Add a single product to the tracker.
     * Only tracks products that implement the Expirable interface and have a date set.
     */
    public void addProduct(product p) {
        if (p instanceof Expirable) {
            Expirable exp = (Expirable) p;
            LocalDate date = exp.getExpirationDate();
            if (date != null) {
                expirationMap.computeIfAbsent(date, k -> new ArrayList<>()).add(p);
            }
        }
    }

    /**
     * Remove a product from the tracker.
     */
    public void removeProduct(product p) {
        if (p instanceof Expirable) {
            Expirable exp = (Expirable) p;
            LocalDate date = exp.getExpirationDate();
            if (date != null && expirationMap.containsKey(date)) {
                List<product> list = expirationMap.get(date);
                list.removeIf(prod -> prod.getid().equals(p.getid()));
                if (list.isEmpty()) {
                    expirationMap.remove(date);
                }
            }
        }
    }

    /**
     * Get all EXPIRED products (expiration date is before today).
     * Uses TreeMap's headMap for efficient range query — no full scan needed.
     */
    public List<product> getExpiredProducts() {
        List<product> expired = new ArrayList<>();
        // headMap(today, false) returns all entries with dates BEFORE today
        SortedMap<LocalDate, List<product>> expiredMap = expirationMap.headMap(LocalDate.now(), false);
        for (List<product> list : expiredMap.values()) {
            expired.addAll(list);
        }
        return expired;
    }

    /**
     * Get products expiring within the next N days.
     * Uses TreeMap's subMap for efficient range query.
     */
    public List<product> getExpiringWithin(int days) {
        List<product> expiring = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        // subMap(today, true, deadline, true) returns all entries between today and deadline
        SortedMap<LocalDate, List<product>> range = expirationMap.subMap(today, true, deadline, true);
        for (List<product> list : range.values()) {
            expiring.addAll(list);
        }
        return expiring;
    }

    /**
     * Get the earliest expiring product(s).
     * TreeMap keeps keys sorted, so firstEntry() is O(log n).
     */
    public List<product> getEarliestExpiring() {
        if (expirationMap.isEmpty()) return Collections.emptyList();
        Map.Entry<LocalDate, List<product>> first = expirationMap.firstEntry();
        return first != null ? first.getValue() : Collections.emptyList();
    }

    /**
     * Print a summary of upcoming expirations.
     */
    public void printExpirationReport() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("        EXPIRATION TRACKER REPORT       ");
        System.out.println("═══════════════════════════════════════");

        List<product> expired = getExpiredProducts();
        if (!expired.isEmpty()) {
            System.out.println("\n🔴 EXPIRED (" + expired.size() + " products):");
            for (product p : expired) {
                Expirable exp = (Expirable) p;
                System.out.println("   ❌ " + p.getname() + " (ID: " + p.getid() +
                        ") — Expired on " + exp.getExpirationDate());
            }
        }

        List<product> soon = getExpiringWithin(30);
        if (!soon.isEmpty()) {
            System.out.println("\n🟡 EXPIRING WITHIN 30 DAYS (" + soon.size() + " products):");
            for (product p : soon) {
                Expirable exp = (Expirable) p;
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), exp.getExpirationDate());
                System.out.println("   ⚠️ " + p.getname() + " (ID: " + p.getid() +
                        ") — " + daysLeft + " days left (expires " + exp.getExpirationDate() + ")");
            }
        }

        if (expired.isEmpty() && soon.isEmpty()) {
            System.out.println("\n✅ No immediate expiration concerns.");
        }

        System.out.println("═══════════════════════════════════════\n");
    }

    /**
     * Get total number of tracked expiration dates.
     */
    public int getTrackedDateCount() {
        return expirationMap.size();
    }

    /**
     * Get total number of tracked products.
     */
    public int getTrackedProductCount() {
        return expirationMap.values().stream().mapToInt(List::size).sum();
    }
}

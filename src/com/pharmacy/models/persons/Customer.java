package com.pharmacy.models.persons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Customer model with HashSet-based allergen tracking.
 *
 * WHY WE USE HashSet FOR ALLERGENS:
 * - Before: allergens were just a comma-separated string in the DB.
 *   Checking "is this customer allergic to Penicillin?" meant parsing a
 *   string and looping through it every single time.
 * - Now: we store allergens in a HashSet. Checking if a customer is
 *   allergic to something is O(1) — instant, like asking "is this key
 *   in the set?" Yes or no, no searching required.
 *
 * HOW IT WORKS:
 * - When a customer is loaded from the DB, the comma-separated allergens
 *   string is split and loaded into the HashSet.
 * - When dispensing medicine, we check: allergens.contains(activeIngredient)
 *   → instant O(1) check instead of string manipulation.
 */
public class Customer extends Person {
    private double loyaltyPoints;
    private List<String> purchaseHistory;  // List of transaction IDs

    // NEW: HashSet for O(1) allergen lookup during dispensing
    private HashSet<String> allergens;
    
    public Customer(String personId, String name, String phone, String email, String address) {
        super(personId, name, phone, email, address);
        this.loyaltyPoints = 0.0;
        this.purchaseHistory = new ArrayList<>();
        this.allergens = new HashSet<>();
    }
    
    @Override
    public String getRole() {
        return "Customer";
    }
    
    // ═══════════════════════════════════
    // Loyalty points management
    // ═══════════════════════════════════
    public double getLoyaltyPoints() {
        return loyaltyPoints;
    }
    
    public void addLoyaltyPoints(double points) {
        this.loyaltyPoints += points;
    }
    
    public void useLoyaltyPoints(double points) {
        if (points <= loyaltyPoints) {
            this.loyaltyPoints -= points;
        } else {
            throw new IllegalArgumentException("Not enough loyalty points");
        }
    }
    
    // ═══════════════════════════════════
    // Purchase history management
    // ═══════════════════════════════════
    public List<String> getPurchaseHistory() {
        return purchaseHistory;
    }
    
    public void addPurchase(String transactionId) {
        purchaseHistory.add(transactionId);
    }
    
    public int getTotalPurchases() {
        return purchaseHistory.size();
    }
    
    // ═══════════════════════════════════
    // Allergen management (HashSet — O(1) lookup)
    // ═══════════════════════════════════

    /**
     * Add an allergen to this customer's profile.
     */
    public void addAllergen(String allergen) {
        if (allergen != null && !allergen.trim().isEmpty()) {
            allergens.add(allergen.trim());
        }
    }

    /**
     * Remove an allergen.
     */
    public void removeAllergen(String allergen) {
        allergens.remove(allergen.trim());
    }

    /**
     * Check if the customer is allergic to something — O(1) instant check.
     * This is the key advantage of using HashSet.
     */
    public boolean isAllergicTo(String substance) {
        return allergens.contains(substance.trim());
    }

    /**
     * Get all allergens as a Set.
     */
    public Set<String> getAllergens() {
        return allergens;
    }

    /**
     * Get allergens as a comma-separated string (for DB storage).
     */
    public String getAllergensAsString() {
        return String.join(",", allergens);
    }

    /**
     * Load allergens from a comma-separated string (from DB).
     */
    public void setAllergensFromString(String allergensStr) {
        allergens.clear();
        if (allergensStr != null && !allergensStr.trim().isEmpty()) {
            for (String a : allergensStr.split(",")) {
                allergens.add(a.trim());
            }
        }
    }

    /**
     * Get the number of known allergens.
     */
    public int getAllergenCount() {
        return allergens.size();
    }
    
    @Override
    public String toString() {
        return "CUSTOMER - " + super.toString() + 
               ", Loyalty Points: " + loyaltyPoints +
               ", Total Purchases: " + purchaseHistory.size() +
               ", Allergens: " + (allergens.isEmpty() ? "None" : allergens);
    }
}
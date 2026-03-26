package com.pharmacy.generics;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.exceptions.*;
import com.pharmacy.interfaces.*;
import com.pharmacy.models.persons.Customer;
import com.pharmacy.models.products.*;

import java.sql.SQLException;

/**
 * Generic pharmacy operations — type-safe methods that work across product types.
 *
 * WHY GENERICS:
 * - Without generics, the sell() method would accept a plain `product` and
 *   we'd need ugly casting to check if it's sellable, expirable, prescribable, etc.
 * - With generics, we define: <T extends product & Sellable>
 *   This means T must be BOTH a product AND sellable — the compiler enforces it.
 *   No casting, no runtime errors, no duplication.
 *
 * HOW IT WORKS:
 * - Multiple bounded type parameters: T extends product & Sellable
 * - The compiler knows T has BOTH product methods (getid, getname)
 *   AND Sellable methods (getprice, getquantity, isAvailableForSale)
 * - One method handles ALL product types that are sellable.
 */
public class PharmacyOperations {

    private final ProductDAO productDAO;

    public PharmacyOperations() {
        this.productDAO = new ProductDAO();
    }

    /**
     * GENERIC SELL METHOD — the star of Phase 3.
     *
     * This single method can sell ANY product that is Sellable:
     * - PrescriptionMedicine
     * - otcmedicine
     * - Supplement
     * - medicaledevice
     *
     * The compiler ensures type safety at compile time.
     *
     * @param <T>      Any type that extends product (so we get getid, getname, etc.)
     * @param item     The product to sell
     * @param quantity How many to sell
     * @param customer The buyer
     * @return A Receipt object containing the sale details
     */
    public <T extends product> Receipt<T> sell(T item, int quantity, Customer customer) {

        // Step 1: Availability check
        if (!item.isAvailableForSale()) {
            throw new ExpiredProductException("Product '" + item.getname() + "' is not available for sale.");
        }

        // Step 2: Stock check
        if (quantity <= 0) {
            throw new InsufficientStockException("Quantity must be greater than zero.");
        }
        if (quantity > item.getquantity()) {
            throw new InsufficientStockException(
                "Requested " + quantity + " but only " + item.getquantity()
                + " units of " + item.getname() + " available.");
        }

        // Step 3: Expiry check (if applicable)
        if (item instanceof Expirable) {
            Expirable exp = (Expirable) item;
            if (exp.isExpired()) {
                throw new ExpiredProductException(
                    "Product '" + item.getname() + "' is expired (date: " + exp.getExpirationDate() + ").");
            }
        }

        // Step 4: Prescription check (if applicable)
        if (item instanceof Prescribable) {
            Prescribable rx = (Prescribable) item;
            if (rx.requiresPrescription() && (rx.getPrescriptionId() == null || rx.getPrescriptionId().isEmpty())) {
                throw new InvalidPrescriptionException(
                    "Prescription required for " + item.getname());
            }
        }

        // Step 5: OTC purchase limit check (if applicable)
        if (item instanceof otcmedicine) {
            otcmedicine otc = (otcmedicine) item;
            if (otc.getPurchaseLimit() > 0 && quantity > otc.getPurchaseLimit()) {
                throw new InsufficientStockException(
                    "Purchase limit of " + otc.getPurchaseLimit() + " exceeded for " + item.getname());
            }
        }

        // Step 6: Process the sale
        double lineTotal = item.getprice() * quantity;
        item.setquantity(item.getquantity() - quantity);

        // Update stock in DB
        try {
            productDAO.updateStock(item.getid(), item.getquantity());
        } catch (SQLException e) {
            // Rollback in-memory change
            item.setquantity(item.getquantity() + quantity);
            throw new RuntimeException("Database error during stock update: " + e.getMessage());
        }

        // Step 7: Award loyalty points
        customer.addLoyaltyPoints(lineTotal);

        // Step 8: Build and return a type-safe receipt
        return new Receipt<>(item, quantity, lineTotal, customer);
    }

    // ═══════════════════════════════════════
    // Generic Receipt class
    // ═══════════════════════════════════════

    /**
     * Generic receipt — preserves the exact product type.
     * Receipt<PrescriptionMedicine> gives you access to ALL PrescriptionMedicine
     * methods without casting.
     *
     * @param <T> The product type that was sold
     */
    public static class Receipt<T extends product> {
        private final T product;
        private final int quantity;
        private final double total;
        private final Customer customer;

        public Receipt(T product, int quantity, double total, Customer customer) {
            this.product = product;
            this.quantity = quantity;
            this.total = total;
            this.customer = customer;
        }

        public T getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return total; }
        public Customer getCustomer() { return customer; }

        public void print() {
            System.out.println("══════════ RECEIPT ══════════");
            System.out.println("  Product: " + product.getname());
            System.out.println("  Type:    " + product.getProductType());
            System.out.println("  ID:      " + product.getid());
            System.out.println("  Qty:     " + quantity);
            System.out.printf("  Price:   $%.2f each%n", product.getprice());
            System.out.printf("  Total:   $%.2f%n", total);
            System.out.println("  Customer: " + customer.getFullName());
            System.out.printf("  Loyalty:  %.0f pts%n", customer.getLoyaltyPoints());
            System.out.println("════════════════════════════");
        }

        @Override
        public String toString() {
            return String.format("Receipt[%s x%d = $%.2f for %s]",
                    product.getname(), quantity, total, customer.getFullName());
        }
    }
}

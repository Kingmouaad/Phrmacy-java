package com.pharmacy.models.transactions;

import java.util.ArrayList;
import java.util.List;

public class Sale extends Transaction {
    private String customerId;
    private List<String> productIds;      // List of product IDs sold
    private List<Integer> quantities;      // Quantities for each product
    private double discount;
    private String paymentMethod;
    
    public Sale(String transactionId, String pharmacistId, String customerId) {
        super(transactionId, pharmacistId);
        this.customerId = customerId;
        this.productIds = new ArrayList<>();
        this.quantities = new ArrayList<>();
        this.discount = 0.0;
    }
    
    @Override
    public String getTransactionType() {
        return "SALE";
    }
    
    // Getters
    public String getCustomerId() {
        return customerId;
    }
    
    public List<String> getProductIds() {
        return productIds;
    }
    
    public List<Integer> getQuantities() {
        return quantities;
    }
    
    public double getDiscount() {
        return discount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    // Add a product to the sale
    public void addProduct(String productId, int quantity) {
        productIds.add(productId);
        quantities.add(quantity);
    }
    
    // Remove a product from the sale
    public void removeProduct(String productId) {
        int index = productIds.indexOf(productId);
        if (index >= 0) {
            productIds.remove(index);
            quantities.remove(index);
        }
    }
    
    // Set discount amount
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    
    // Set payment method
    public void setPaymentMethod(String method) {
        this.paymentMethod = method;
    }
    
    // Calculate total
    public void calculateTotal(double subtotal) {
        this.totalAmount = subtotal - discount;
    }
    
    // Complete the sale
    public void completeSale() {
        if (productIds.isEmpty()) {
            System.out.println("Error: Cannot complete sale with no products");
            return;
        }
        if (paymentMethod == null) {
            System.out.println("Error: Payment method not set");
            return;
        }
        setStatus("COMPLETED");
    }
    
    // Print receipt
    public void printReceipt() {
        System.out.println("========== RECEIPT ==========");
        System.out.println("Transaction ID: " + this.transactionId);
        System.out.println("Customer ID: " + this.customerId);
        System.out.println("Date: " + this.dateTime);
        System.out.println("----------------------------");
        System.out.println("Products purchased: " + this.productIds.size());
        for (int i = 0; i < this.productIds.size(); i++) {
            System.out.println("  " + this.productIds.get(i) + " x " + this.quantities.get(i));
        }
        System.out.println("----------------------------");
        System.out.println("Subtotal: $" + (this.totalAmount + this.discount));
        System.out.println("Discount: -$" + this.discount);
        System.out.println("TOTAL: $" + this.totalAmount);
        System.out.println("Payment: " + this.paymentMethod);
        System.out.println("============================");
    }
    
    @Override
    public String toString() {
        return "SALE - " + super.toString() +
               ", Customer: " + this.customerId +
               ", Items: " + this.productIds.size() +
               ", Discount: $" + this.discount +
               ", Payment: " + this.paymentMethod;
    }
}

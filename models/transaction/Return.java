package models.transaction;
import java.util.ArrayList;
import java.util.List;

public class Return extends Transaction {
    private String customerId;
    private String originalSaleId;     // Which sale are we returning from?
    private List<String> productIds;   // Products being returned
    private List<Integer> quantities;  // Quantities being returned
    private String reason;
    private String refundMethod;
    
    public Return(String transactionId, String pharmacistId, 
                  String customerId, String originalSaleId) {
        super(transactionId, pharmacistId);
        this.customerId = customerId;
        this.originalSaleId = originalSaleId;
        this.productIds = new ArrayList<>();
        this.quantities = new ArrayList<>();
    }
    
    @Override
    public String getTransactionType() {
        return "RETURN";
    }
    
    // Getters
    public String getCustomerId() {
        return customerId;
    }
    
    public String getOriginalSaleId() {
        return originalSaleId;
    }
    
    public List<String> getProductIds() {
        return productIds;
    }
    
    public List<Integer> getQuantities() {
        return quantities;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getRefundMethod() {
        return refundMethod;
    }
    
    // Add a product to return
    public void addProduct(String productId, int quantity) {
        productIds.add(productId);
        quantities.add(quantity);
    }
    
    // Set return reason
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    // Set refund method
    public void setRefundMethod(String method) {
        this.refundMethod = method;
    }
    
    // Calculate refund amount
    public void calculateRefund(double refundAmount) {
        this.totalAmount = refundAmount;
    }
    
    // Complete the return
    public void completeReturn() {
        if (productIds.isEmpty()) {
            System.out.println("Error: Cannot complete return with no products");
            return;
        }
        if (reason == null) {
            System.out.println("Error: Return reason not specified");
            return;
        }
        if (refundMethod == null) {
            System.out.println("Error: Refund method not set");
            return;
        }
        setStatus("COMPLETED");
    }
    
    // Print return receipt
    public void printReturnReceipt() {
        System.out.println("====== RETURN RECEIPT ======");
        System.out.println("Return ID: " + transactionId);
        System.out.println("Original Sale: " + originalSaleId);
        System.out.println("Customer ID: " + customerId);
        System.out.println("Date: " + dateTime);
        System.out.println("----------------------------");
        System.out.println("Returned products: " + productIds.size());
        for (int i = 0; i < productIds.size(); i++) {
            System.out.println("  " + productIds.get(i) + " x " + quantities.get(i));
        }
        System.out.println("----------------------------");
        System.out.println("Reason: " + reason);
        System.out.println("REFUND: $" + totalAmount);
        System.out.println("Refund Method: " + refundMethod);
        System.out.println("============================");
    }
    
    @Override
    public String toString() {
        return "RETURN - " + super.toString() +
               ", Customer: " + customerId +
               ", Original Sale: " + originalSaleId +
               ", Items: " + productIds.size() +
               ", Reason: " + reason +
               ", Refund Method: " + refundMethod;
    }
}
package models.transaction;
import java.util.ArrayList;
import java.util.List;

public class Restock extends Transaction {
    private String supplierId;
    private List<String> productIds;   // Products being restocked
    private List<Integer> quantities;  // Quantities received
    private double totalCost;          // What we paid the supplier
    
    public Restock(String transactionId, String pharmacistId, String supplierId) {
        super(transactionId, pharmacistId);
        this.supplierId = supplierId;
        this.productIds = new ArrayList<>();
        this.quantities = new ArrayList<>();
        this.totalCost = 0.0;
    }
    
    @Override
    public String getTransactionType() {
        return "RESTOCK";
    }
    
    // Getters
    public String getSupplierId() {
        return supplierId;
    }
    
    public List<String> getProductIds() {
        return productIds;
    }
    
    public List<Integer> getQuantities() {
        return quantities;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    // Add a product to restock
    public void addProduct(String productId, int quantity) {
        productIds.add(productId);
        quantities.add(quantity);
    }
    
    // Set total cost (what we paid supplier)
    public void setTotalCost(double cost) {
        this.totalCost = cost;
        this.totalAmount = cost;  // For restock, amount is the cost
    }
    
    // Complete the restock
    public void completeRestock() {
        if (productIds.isEmpty()) {
            System.out.println("Error: Cannot complete restock with no products");
            return;
        }
        setStatus("COMPLETED");
    }
    
    // Print restock receipt
    public void printRestockReceipt() {
        System.out.println("===== RESTOCK RECEIPT =====");
        System.out.println("Restock ID: " + transactionId);
        System.out.println("Supplier: " + supplierId);
        System.out.println("Date: " + dateTime);
        System.out.println("Received by: " + pharmacistId);
        System.out.println("---------------------------");
        System.out.println("Products received: " + productIds.size());
        for (int i = 0; i < productIds.size(); i++) {
            System.out.println("  " + productIds.get(i) + " x " + quantities.get(i));
        }
        System.out.println("---------------------------");
        System.out.println("Total Cost: $" + totalCost);
        System.out.println("===========================");
    }
    
    @Override
    public String toString() {
        return "RESTOCK - " + super.toString() +
               ", Supplier: " + supplierId +
               ", Items: " + productIds.size() +
               ", Cost: $" + totalCost;
    }
}

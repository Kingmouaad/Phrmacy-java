package models.transaction;
import java.time.LocalDateTime;
public abstract class Transaction {
    protected String transactionId;
    protected LocalDateTime dateTime;
    protected String pharmacistId;
    protected double totalAmount;
    protected String status;  // COMPLETED, PENDING, CANCELLED
    
    public Transaction(String transactionId, String pharmacistId) {
        this.transactionId = transactionId;
        this.dateTime = LocalDateTime.now();
        this.pharmacistId = pharmacistId;
        this.totalAmount = 0.0;
        this.status = "PENDING";
    }
    
    // children must implement
    public abstract String getTransactionType();
    
    // Getters and Setters
    public String getTransactionId() {
        return this.transactionId;
    }
    
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }
    
    public String getPharmacistId() {
        return this.pharmacistId;
    }
    
    public double getTotalAmount() {
        return this.totalAmount;
    }
    
    public void setTotalAmount(double amount) {
        this.totalAmount = amount;
    }
    
    public String getStatus() {
        return this.status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    @Override
    public String toString() {
        return "Transaction ID: " + this.transactionId +
               ", Type: " + this.getTransactionType() +
               ", Date: " + this.dateTime +
               ", Pharmacist: " + this.pharmacistId +
               ", Total: $" + this.totalAmount +
               ", Status: " + this.status;
    }
}
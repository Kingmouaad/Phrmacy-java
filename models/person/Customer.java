package models.person;




import java.util.ArrayList;
import java.util.List;

public class Customer extends Person {
    private double loyaltyPoints;
    private List<String> purchaseHistory;  // List of transaction IDs
    
    public Customer(String personId, String name, String phone, String email,String address) {
        super(personId, name, phone, email,address);
        this.loyaltyPoints = 0.0;
        this.purchaseHistory = new ArrayList<>();
    }
    
    @Override
    public String getRole() {
        return "Customer";
    }
    
    // Loyalty points management
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
    
    // Purchase history management
    public List<String> getPurchaseHistory() {
        return purchaseHistory;
    }
    
    public void addPurchase(String transactionId) {
        purchaseHistory.add(transactionId);
    }
    
    public int getTotalPurchases() {
        return purchaseHistory.size();
    }
    
    @Override
    public String toString() {
        return "CUSTOMER - " + super.toString() + 
               ", Loyalty Points: " + loyaltyPoints +
               ", Total Purchases: " + purchaseHistory.size();
    }
}
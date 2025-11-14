package models.person;



import java.util.ArrayList;
import java.util.List;

public class Customer extends Person {
    private double loyaltyPoints;
    private List<String> purchaseHistory;  // List of transaction IDs
    private List<String> allergies;        // List of substances customer is allergic to
    private String dateOfBirth;            // For age verification
    private List<String> currentMedications; // Active medications for interaction checking
    
    public Customer(String personId, String fullName, String phoneNumber, 
                   String email, String address) {
        super(personId, fullName, phoneNumber, email, address);
        this.loyaltyPoints = 0.0;
        this.purchaseHistory = new ArrayList<>();
        this.allergies = new ArrayList<>();
        this.currentMedications = new ArrayList<>();
    }
    
    // Constructor with date of birth
    public Customer(String personId, String fullName, String phoneNumber, 
                   String email, String address, String dateOfBirth) {
        this(personId, fullName, phoneNumber, email, address);
        this.dateOfBirth = dateOfBirth;
    }
    
    @Override
    public String getRole() {
        return "Customer";
    }
    
    // Loyalty Points Management
    public double getLoyaltyPoints() {
        return loyaltyPoints;
    }
    
    public void addLoyaltyPoints(double points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot add negative loyalty points");
        }
        this.loyaltyPoints += points;
    }
    
    public boolean redeemLoyaltyPoints(double points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot redeem negative points");
        }
        if (points > loyaltyPoints) {
            return false; // Not enough points
        }
        this.loyaltyPoints -= points;
        return true;
    }
    
    public double calculateDiscount(double points) {
        // Example: 100 points = $1 discount
        return points / 100.0;
    }
    
    // Purchase History Management
    public List<String> getPurchaseHistory() {
        return new ArrayList<>(purchaseHistory); // Return copy for safety
    }
    
    public void addPurchase(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be empty");
        }
        purchaseHistory.add(transactionId);
    }
    
    public int getPurchaseCount() {
        return purchaseHistory.size();
    }
    
    // Allergy Management
    public List<String> getAllergies() {
        return new ArrayList<>(allergies); // Return copy for safety
    }
    
    public void addAllergy(String allergen) {
        if (allergen == null || allergen.trim().isEmpty()) {
            throw new IllegalArgumentException("Allergen cannot be empty");
        }
        if (!allergies.contains(allergen.toLowerCase())) {
            allergies.add(allergen.toLowerCase());
        }
    }
    
    public void removeAllergy(String allergen) {
        allergies.remove(allergen.toLowerCase());
    }
    
    public boolean isAllergicTo(String substance) {
        return allergies.contains(substance.toLowerCase());
    }
    
    // Current Medications Management (for drug interaction checking)
    public List<String> getCurrentMedications() {
        return new ArrayList<>(currentMedications);
    }
    
    public void addCurrentMedication(String medicationId) {
        if (!currentMedications.contains(medicationId)) {
            currentMedications.add(medicationId);
        }
    }
    
    public void removeCurrentMedication(String medicationId) {
        currentMedications.remove(medicationId);
    }
    
    // Age verification
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    @Override
    public String toString() {
        return "CUSTOMER - " + super.toString() + 
               ", Loyalty Points: " + loyaltyPoints +
               ", Total Purchases: " + purchaseHistory.size() +
               ", Allergies: " + (allergies.isEmpty() ? "None" : allergies.size());
    }
}
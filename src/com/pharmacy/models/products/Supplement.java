package com.pharmacy.models.products;

import java.time.LocalDate;
import com.pharmacy.interfaces.Expirable;
public class Supplement extends product implements Expirable {
    private String supplementType;  // "Vitamin", "Mineral", "Herbal", etc.
    private String servingSize;
    private String benefits;        // Health benefits description
     private LocalDate expirationDate;
    
    public Supplement(String productId, String name, double price, 
                     int quantity, String supplementType, 
                     String servingSize) {
        super(productId, name, price, quantity);
        this.supplementType = supplementType;
        this.servingSize = servingSize;
    }
    
    public String getSupplementType() { return supplementType; }
    public void setSupplementType(String supplementType) { 
        this.supplementType = supplementType; 
    }
    
    public String getServingSize() { return servingSize; }
    public void setServingSize(String servingSize) { 
        this.servingSize = servingSize; 
    }
    
    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { 
        this.benefits = benefits; 
    }
    
    @Override
    public boolean isAvailableForSale() {
        return this.getquantity() > 0;
    }
    
    @Override
    public String getProductType() {
        return "Supplement";
    }
    
    @Override
    public String toString() {
        return "SUPPLEMENT - " + super.toString() +
               ", Type: " + supplementType +
               ", Serving: " + servingSize +
               (benefits != null ? ", Benefits: " + benefits : "");
    }
    @Override
    public LocalDate getExpirationDate() {
        return expirationDate;
    }
    
    @Override
    public void setExpirationDate(LocalDate date) {
        this.expirationDate = date;
    }
    
    @Override
    public boolean isExpired() {
        if (expirationDate == null) return false;
        return LocalDate.now().isAfter(expirationDate);
    }
    
    @Override
    public long getDaysUntilExpiration() {
        if (expirationDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(), expirationDate
        );
    }
}
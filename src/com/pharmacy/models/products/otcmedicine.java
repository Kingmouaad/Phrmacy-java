package com.pharmacy.models.products;



public class otcmedicine extends medicine {
    private int purchaseLimit;  // Max quantity per purchase to prevent abuse
    private int minimumAge;     // Age restriction if any
    
    public otcmedicine(String productId, String name, double price, 
                      int quantity, String activeIngredient, 
                      String dosageForm, String strength, 
                      String manufacturer) {
        super(productId, name, price, quantity, activeIngredient, 
              dosageForm, strength, manufacturer);
        this.purchaseLimit = 0;  // 0 means no limit
        this.minimumAge = 0;     // 0 means no age restriction
    }
    
    public int getPurchaseLimit() { return purchaseLimit; }
    public void setPurchaseLimit(int limit) { 
        if (limit < 0) throw new IllegalArgumentException("Limit cannot be negative");
        this.purchaseLimit = limit; 
    }
    
    public int getMinimumAge() { return minimumAge; }
    public void setMinimumAge(int age) { 
        if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
        this.minimumAge = age; 
    }
    
    @Override
    public boolean isAvailableForSale() {
        return this.getquantity() > 0;
    }
    
    @Override
    public String getProductType() {
        return "Over-The-Counter Medicine";
    }
    
    @Override
    public String toString() {
        String restrictions = "";
        if (this.purchaseLimit > 0) restrictions += ", Limit: " + this.purchaseLimit + " per purchase";
        if (this.minimumAge > 0) restrictions += ", Min Age: " + this.minimumAge;
        
        return "OTC MEDICINE - " + super.toString() + restrictions;
    }
}
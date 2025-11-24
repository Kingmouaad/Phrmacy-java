package models.products;

import java.time.LocalDate;

import models.interfaces.Expirable;

public abstract class medicine extends product  implements Expirable {
    protected String activeIngredient;
    protected String dosageform;
    protected String volume;
    protected String manufacteur;
    protected LocalDate expirationDate;

    public medicine(String id,String name,double price,int quantity,String activeIngredient,String dosageform,String volume,String manufacteur){
        super(id,name,price,quantity);
         if (activeIngredient == null || activeIngredient.trim().isEmpty()) {
            throw new IllegalArgumentException("Active ingredient cannot be empty");
        }
        this.activeIngredient=activeIngredient;
        this.dosageform=dosageform;
        this.id=id;
        this.manufacteur=manufacteur;


    }
    public String getActiveIngredient() { return this.activeIngredient; }
    public String getDosageForm() { return this.dosageform; }
    public String getStrength() { return this.volume; }
    public String getManufacturer() { return this.manufacteur; }
    public void setActiveIngredient(String activeIngredient) {
        if (activeIngredient == null || activeIngredient.trim().isEmpty()) {
            throw new IllegalArgumentException("Active ingredient cannot be empty");
        }
        this.activeIngredient = activeIngredient;
    }
    
    public void setDosageForm(String dosageform) {
        this.dosageform = dosageform;
    }
    
    public void setStrength(String volume) {
        this.volume = volume;
    }
    
    public void setManufacturer(String manufacteur) {
        this.manufacteur = manufacteur;
    }
    
    @Override
    public String toString() {
        return super.toString() + 
               ", Active Ingredient: " + this.activeIngredient +
               ", Form: " + this.dosageform +
               ", Volume: " + this.volume +
               ", Manufacteur: " + this.manufacteur;
    }
    @Override
    public boolean isAvailableForSale(){
        return (this.quantity > 0 && !isExpired());
    }
    @Override
    public String  getProductType(){
        return "Medicine";
    }
     // Implementing Expirable interface
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
        if (expirationDate == null) {
            return false;  // If no date set assume not expired
        }
        return LocalDate.now().isAfter(expirationDate);
    }
    
    @Override
    public long getDaysUntilExpiration() {
        if (expirationDate == null) {
            return Long.MAX_VALUE; 
        }
        //i did search for it i cant do that alone hh
        return java.time.temporal.ChronoUnit.DAYS.between(
            LocalDate.now(), 
            expirationDate
        );
    }
}

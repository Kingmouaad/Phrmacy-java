package models.interfaces;


public interface Sellable {
    // Check if product can be sold right now
    boolean isAvailableForSale();
    
    // Get the selling price
    double getprice();
    
    // Get how many are in stock
    int getquantity();
}
    


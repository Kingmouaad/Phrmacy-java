package com.pharmacy.interfaces;

import java.time.LocalDate;

public interface Expirable {
    // Get the expiration date
    LocalDate getExpirationDate();
    
    // Set the expiration date
    void setExpirationDate(LocalDate date);
    
    // Check if product is expired
    boolean isExpired();
    
    // Get days until expiration (negative if already expired)
    long getDaysUntilExpiration();
}

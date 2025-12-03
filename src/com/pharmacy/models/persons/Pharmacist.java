package com.pharmacy.models.persons;



public class Pharmacist extends Person {
    private String licenseNumber;
    private int accessLevel;  // 1 = Basic, 2 = Senior, 3 = Manager
    
    public Pharmacist(String personId, String name, String phone, 
                     String email,String address, String licenseNumber) {
        super(personId, name, phone, email,address);
        this.licenseNumber = licenseNumber;
        this.accessLevel = 1;  // Default 
    }
    
    @Override
    public String getRole() {
        return "Pharmacist";
    }
    
    // Getters
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public int getAccessLevel() {
        return accessLevel;
    }
    
    // Setters
    public void setAccessLevel(int level) {
    if (level >= 1 && level <= 3) {
        this.accessLevel = level;
    } else {
        throw new IllegalArgumentException("the level should be between 1 and 3");
    }
}
    
    // Permission checks
    public boolean canProcessSales() {
        return accessLevel >= 1;  // All pharmacists can process sales
    }
    
    public boolean canManageInventory() {
        return accessLevel >= 2;  // Only senior and above
    }
    
    public boolean canManageStaff() {
        return accessLevel >= 3;  // Only managers
    }
    
    public String getAccessLevelName() {
        if (accessLevel == 1) return "Basic Pharmacist";
        if (accessLevel == 2) return "Senior Pharmacist";
        if (accessLevel == 3) return "Pharmacy Manager";
        return "Unknown";
    }
    
    @Override
    public String toString() {
        return "PHARMACIST - " + super.toString() + 
               ", License: " + this.licenseNumber +
               ", Level: " + this.getAccessLevelName();
    }
}

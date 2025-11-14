package models.person;


import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

public class Pharmacist extends Person {
    private String licenseNumber;
    private LocalDate licenseExpiryDate;
    private String specialization;      // "Clinical", "Retail", "Hospital", etc.
    private int authorizationLevel;     // 1=Basic, 2=Senior, 3=Manager
    private List<String> certifications; // Additional certifications
    private boolean canOverridePrescription; // Emergency authorization
    
    public Pharmacist(String personId, String fullName, String phoneNumber,
                     String email, String address, String licenseNumber,
                     LocalDate licenseExpiryDate) {
        super(personId, fullName, phoneNumber, email, address);
        
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("License number is required for pharmacists");
        }
        if (licenseExpiryDate == null) {
            throw new IllegalArgumentException("License expiry date is required");
        }
        
        this.licenseNumber = licenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
        this.authorizationLevel = 1; // Default to basic level
        this.certifications = new ArrayList<>();
        this.canOverridePrescription = false; // Default: no override authority
    }
    
    @Override
    public String getRole() {
        return "Pharmacist";
    }
    
    // License Management
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate;
    }
    
    public void setLicenseExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date cannot be null");
        }
        this.licenseExpiryDate = expiryDate;
    }
    
    public boolean isLicenseValid() {
        return LocalDate.now().isBefore(licenseExpiryDate);
    }
    
    public long getDaysUntilLicenseExpiry() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), licenseExpiryDate);
    }
    
    // Authorization Level Management
    public int getAuthorizationLevel() {
        return authorizationLevel;
    }
    
    public void setAuthorizationLevel(int level) {
        if (level < 1 || level > 3) {
            throw new IllegalArgumentException("Authorization level must be 1-3");
        }
        this.authorizationLevel = level;
        
        // Senior and Manager levels can override prescriptions in emergencies
        if (level >= 2) {
            this.canOverridePrescription = true;
        }
    }
    
    public String getAuthorizationLevelName() {
        switch (authorizationLevel) {
            case 1: return "Basic Pharmacist";
            case 2: return "Senior Pharmacist";
            case 3: return "Pharmacy Manager";
            default: return "Unknown";
        }
    }
    
    // Specialization
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    // Certifications Management
    public List<String> getCertifications() {
        return new ArrayList<>(certifications);
    }
    
    public void addCertification(String certification) {
        if (certification != null && !certification.trim().isEmpty()) {
            if (!certifications.contains(certification)) {
                certifications.add(certification);
            }
        }
    }
    
    public void removeCertification(String certification) {
        certifications.remove(certification);
    }
    
    public boolean hasCertification(String certification) {
        return certifications.contains(certification);
    }
    
    // Override Authority
    public boolean canOverridePrescription() {
        return canOverridePrescription && isLicenseValid();
    }
    
    public void setCanOverridePrescription(boolean canOverride) {
        this.canOverridePrescription = canOverride;
    }
    
    // Authorization checks for specific operations
    public boolean canProcessSale() {
        return isLicenseValid();
    }
    
    public boolean canManageInventory() {
        return isLicenseValid() && authorizationLevel >= 2;
    }
    
    public boolean canManageUsers() {
        return isLicenseValid() && authorizationLevel >= 3;
    }
    
    public boolean canApproveReturns() {
        return isLicenseValid() && authorizationLevel >= 2;
    }
    
    @Override
    public String toString() {
        return "PHARMACIST - " + super.toString() +
               ", License: " + licenseNumber +
               ", Expires: " + licenseExpiryDate +
               ", Level: " + getAuthorizationLevelName() +
               ", Valid: " + (isLicenseValid() ? "YES" : "EXPIRED") +
               (specialization != null ? ", Specialization: " + specialization : "");
    }
}
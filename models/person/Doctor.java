package models.person;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Doctor extends Person {
    private String medicalLicenseNumber;
    private LocalDate licenseExpiryDate;
    private String specialization;          // "Cardiology", "Pediatrics", etc.
    private String clinicName;              // Where doctor practices
    private String clinicAddress;
    private List<String> prescriptionsIssued; // IDs of prescriptions written
    
    public Doctor(String personId, String fullName, String phoneNumber,
                 String email, String address, String medicalLicenseNumber,
                 String specialization) {
        super(personId, fullName, phoneNumber, email, address);
        
        if (medicalLicenseNumber == null || medicalLicenseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical license number is required");
        }
        if (specialization == null || specialization.trim().isEmpty()) {
            throw new IllegalArgumentException("Specialization is required");
        }
        
        this.medicalLicenseNumber = medicalLicenseNumber;
        this.specialization = specialization;
        this.prescriptionsIssued = new ArrayList<>();
    }
    
    @Override
    public String getRole() {
        return "Doctor";
    }
    
    // License Management
    public String getMedicalLicenseNumber() {
        return medicalLicenseNumber;
    }
    
    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate;
    }
    
    public void setLicenseExpiryDate(LocalDate expiryDate) {
        this.licenseExpiryDate = expiryDate;
    }
    
    public boolean isLicenseValid() {
        if (licenseExpiryDate == null) {
            return true; // If no expiry date set, assume valid
        }
        return LocalDate.now().isBefore(licenseExpiryDate);
    }
    
    // Specialization
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        if (specialization == null || specialization.trim().isEmpty()) {
            throw new IllegalArgumentException("Specialization cannot be empty");
        }
        this.specialization = specialization;
    }
    
    // Clinic Information
    public String getClinicName() {
        return clinicName;
    }
    
    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }
    
    public String getClinicAddress() {
        return clinicAddress;
    }
    
    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }
    
    // Prescription Management
    public List<String> getPrescriptionsIssued() {
        return new ArrayList<>(prescriptionsIssued);
    }
    
    public void addPrescription(String prescriptionId) {
        if (prescriptionId != null && !prescriptionId.trim().isEmpty()) {
            prescriptionsIssued.add(prescriptionId);
        }
    }
    
    public int getPrescriptionCount() {
        return prescriptionsIssued.size();
    }
    
    public boolean hasPrescribed(String prescriptionId) {
        return prescriptionsIssued.contains(prescriptionId);
    }
    
    // Verification method for prescription validation
    public boolean canPrescribe() {
        return isLicenseValid();
    }
    
    @Override
    public String toString() {
        return "DOCTOR - " + super.toString() +
               ", License: " + medicalLicenseNumber +
               ", Specialization: " + specialization +
               (clinicName != null ? ", Clinic: " + clinicName : "") +
               ", Prescriptions Issued: " + prescriptionsIssued.size();
    }
}

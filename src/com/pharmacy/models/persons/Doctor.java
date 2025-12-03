package com.pharmacy.models.persons;


import java.util.ArrayList;
import java.util.List;

public class Doctor extends Person {
    private String licenseNumber;
    private String specialization;
    private List<String> prescriptions;  // IDs of prescriptions they wrote
    
    public Doctor(String personId, String name, String phone, 
                 String email,String address, String licenseNumber, String specialization) {
        super(personId, name, phone, email,address);
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.prescriptions = new ArrayList<>();
    }
    
    @Override
    public String getRole() {
        return "Doctor";
    }
    
    // Getters
    public String getLicenseNumber() {
        return this.licenseNumber;
    }
    
    public String getSpecialization() {
        return this.specialization;
    }
    
    public List<String> getPrescriptions() {
        return this.prescriptions;
    }
    
    // Add a prescription they wrote
    public void addPrescription(String prescriptionId) {
        this.prescriptions.add(prescriptionId);
    }
    
    public int getTotalPrescriptions() {
        return this.prescriptions.size();
    }
    
    @Override
    public String toString() {
        return "DOCTOR - " + super.toString() + 
               ", License: " + this.licenseNumber +
               ", Specialization: " + this.specialization +
               ", Prescriptions Written: " + this.prescriptions.size();
    }
}


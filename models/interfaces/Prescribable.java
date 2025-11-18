package models.interfaces;

public interface Prescribable {
    // Does this product require a prescription?
    boolean requiresPrescription();
    
    // Set the prescription ID for this sale
    void setPrescriptionId(String prescriptionId);
    
    // Get the prescription ID
    String getPrescriptionId();
}

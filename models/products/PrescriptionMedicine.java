package models.products;

public class PrescriptionMedicine extends medicine {
    private boolean requiresPrescription;
    private String prescriptionId;  // Links to a valid prescription
    
    public PrescriptionMedicine(String productId, String name, double price, 
                               int quantity, String activeIngredient, 
                               String dosageForm, String strength, 
                               String manufacturer) {
        super(productId, name, price, quantity, activeIngredient, 
              dosageForm, strength, manufacturer);
        this.requiresPrescription = true;  // Always true for this type
    }
    
    public boolean requiresPrescription() {
        return requiresPrescription;
    }
    
    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
    
    public String getPrescriptionId() {
        return prescriptionId;
    }
    
    @Override
    public boolean isAvailableForSale() {
        // Prescription medicine needs valid prescription AND stock
        return this.getquantity() > 0 && prescriptionId != null;
    }
    
    @Override
    public String getProductType() {
        return "Prescription Medicine";
    }
    
    @Override
    public String toString() {
        return "PRESCRIPTION MEDICINE - " + super.toString() + 
               " [Requires Prescription: YES]";
    }
}

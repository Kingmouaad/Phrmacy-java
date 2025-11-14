package models.products;



public class medicaledevice extends product {
    private String deviceType;        // "Blood Pressure Monitor", "Thermometer"
    private int warrantyMonths;       //how long warranty last 
    private String manufacturer;
    private String certificationNumber;
    
    public medicaledevice(String productId, String name, double price, 
                        int quantity, String deviceType, 
                        int warrantyMonths, String manufacturer) {
        super(productId, name, price, quantity);
        this.deviceType = deviceType;
        this.warrantyMonths = warrantyMonths;
        this.manufacturer = manufacturer;
    }
    
    // Getters and setters
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int months) { 
        if (months < 0) throw new IllegalArgumentException("Warranty cannot be negative");
        this.warrantyMonths = months; 
    }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getCertificationNumber() { return certificationNumber; }
    public void setCertificationNumber(String certificationNumber) { 
        this.certificationNumber = certificationNumber; 
    }
    
    @Override
    public boolean isAvailableForSale() {
        return this.getquantity() > 0;
    }
    
    @Override
    public String getProductType() {
        return "Medical Device";
    }
    
    @Override
    public String toString() {
        return "MEDICAL DEVICE - " + super.toString() +
               ", Type: " + this.deviceType +
               ", Warranty: " + this.warrantyMonths + " months" +
               ", Manufacturer: " + this.manufacturer;
    }
}

package models.products;

public abstract class medicine extends product {
    protected String activeIngredient;
    protected String dosageform;
    protected String volume;
    protected String manufacteur;

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
        return (this.quantity<0);
    }
    @Override
    public String  getProductType(){
        return "Medicine";
    }
}

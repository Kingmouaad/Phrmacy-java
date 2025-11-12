package models.products;

public abstract class  product {
    protected String id;
    protected String name;
    protected double price;
    protected int quantity;

    public product(String id,String name,double price,int quantity){
        
        if(price<0 || name==null){
            throw new IllegalArgumentException("error enter the correct price or the write a name ");
        }
        this.id=id;
        this.name=name;
        this.price=price;
        this.quantity=quantity;
    }

    String getid(){
        return this.id;
    }
     String getname(){
        return this.name;
    }
     double getprice(){
        return this.price;
    }
     int getquantity(){
        return this.quantity;
    }
    void setid(String id){
        this.id=id;
    }
    void setname(String name){
        this.name=name;
    }
    void setprice(double price){
         if(price<0 ){
            throw new IllegalArgumentException("error enter the correct price  ");
        }
        this.price=price;
    }
    void setquantity(int quantity){
        this.quantity=quantity;
    }
    public abstract boolean isAvailableForSale();
    
    public abstract String getProductType();
     @Override
    public String toString() {
        return  
               ", Id of the poduct: " + this.id +
               ", Name: " + this.name +
               ", Price: " + this.price +
               ", Quantity: " + this.quantity;
    }

}

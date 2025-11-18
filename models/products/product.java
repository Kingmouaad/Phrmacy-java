package models.products;

import models.interfaces.Sellable;

public abstract class  product implements Sellable {
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

    public String getid(){
        return this.id;
    }
     public String getname(){
        return this.name;
    }
    @Override
    public double getprice(){
        return this.price;
    }
    @Override

     public int getquantity(){
        return this.quantity;
    }
    public void setid(String id){
        this.id=id;
    }
    public void setname(String name){
        this.name=name;
    }
    public void setprice(double price){
         if(price<0 ){
            throw new IllegalArgumentException("error enter the correct price  ");
        }
        this.price=price;
    }
    public void setquantity(int quantity){
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

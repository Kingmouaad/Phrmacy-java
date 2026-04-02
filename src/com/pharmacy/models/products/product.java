package com.pharmacy.models.products;

import com.pharmacy.interfaces.Sellable;

public abstract class  product implements Sellable {
    protected String id;
    protected String name;
    protected double price;
    protected int quantity;

    public product(String id,String name,double price,int quantity){
        
        if(id==null || id.trim().isEmpty()){
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if(name==null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if(price<0){
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if(quantity<0){
            throw new IllegalArgumentException("Quantity cannot be negative");
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
    // @Override
    public double getprice(){
        return this.price;
    }
    // @Override

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
               "Id: " + this.id +
               ", Name: " + this.name +
               ", Price: " + this.price +
               ", Quantity: " + this.quantity;
    }

}

package com.pharmacy.services;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.exceptions.*;
import com.pharmacy.interfaces.*;
import com.pharmacy.models.products.*;
import com.pharmacy.models.persons.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ProductService {
    private ProductDAO productDAO;
    private Scanner scanner;
    private Pharmacist currentPharmacist;
    
    public ProductService(Scanner scanner, Pharmacist currentPharmacist) {
        this.productDAO = new ProductDAO();
        this.scanner = scanner;
        this.currentPharmacist = currentPharmacist;
    }
    
    public void viewAllProducts() {
        try {
            List<product> products = productDAO.findAll();
            System.out.println("\n ALL PRODUCTS (" + products.size() + " items)");
            if (products.isEmpty()) {
                System.out.println("No products available.");
                return;
            }
            
            for (product p : products) {
                System.out.println("\n" + p);
                System.out.println("   Available: " + (p.isAvailableForSale() ? "YES" : "NO"));
                
                if (p instanceof Expirable) {
                    Expirable exp = (Expirable) p;
                    LocalDate date = exp.getExpirationDate();
                    if (date == null) {
                        System.out.println("   Expiration date: not set");
                    } else if (exp.isExpired()) {
                        System.out.println("   EXPIRED on " + date);
                    } else {
                        long days = exp.getDaysUntilExpiration();
                        System.out.println("   Expires in " + days + " days (on " + date + ")");
                    }
                }
                System.out.println("   " + "─".repeat(50));
            }
        } catch (SQLException e) {
            System.out.println(" Database error: " + e.getMessage());
        }
    }
    
    public void addProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println("Access Denied: Senior level required!");
            return;
        }
        
        System.out.println("\nADD NEW PRODUCT");
        System.out.println("1. Prescription Medicine");
        System.out.println("2. OTC Medicine");
        System.out.println("3. Medical Device");
        System.out.println("4. Supplement");
        
        int type = getIntInput("Product type: ");
        
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        
        if (findProductById(id) != null) {
            System.out.println("ID already exists!");
            return;
        }
        
        System.out.print("Name: ");
        String name = scanner.nextLine();
        double price = getDoubleInput("Price: $");
        int qty = getIntInput("Quantity: ");
        
        product newProduct = null;
        
        try {
            switch (type) {
                case 1: newProduct = createPrescriptionMedicine(id, name, price, qty); break;
                case 2: newProduct = createOTCMedicine(id, name, price, qty); break;
                case 3: newProduct = createMedicalDevice(id, name, price, qty); break;
                case 4: newProduct = createSupplement(id, name, price, qty); break;
                default: 
                    System.out.println(" Invalid type!");
                    return;
            }
            
            productDAO.insertWithStock(newProduct, qty, 10); // default threshold of 10
            System.out.println(" Product added successfully!");
        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
    
    public void searchProduct() {
        System.out.print("\n🔍 Search (ID or Name): ");
        String query = scanner.nextLine().toLowerCase();
        
        try {
            product byId = productDAO.findById(query.toUpperCase());
            if (byId != null) {
                System.out.println("\n FOUND BY ID: " + byId);
                return;
            }
            
            List<product> products = productDAO.findByName(query);
            if (products.isEmpty()) {
                System.out.println(" No products found.");
            } else {
                for (product p : products) {
                    System.out.println("\n FOUND: " + p);
                }
            }
        } catch (SQLException e) {
            System.out.println(" Database error: " + e.getMessage());
        }
    }
    
    public void updateProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println(" Access Denied!");
            return;
        }
        
        System.out.print("\nProduct ID to update: ");
        product p;
        try {
            p = getProductOrThrow(scanner.nextLine());
        } catch (ProductNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }
        
        System.out.println("Current: " + p.getname());
        System.out.println("1. Update Price");
        System.out.println("2. Update Quantity (Restock)");
        System.out.println("3. Update Name");
        
        int choice = getIntInput("Choose: ");
        
        try {
            switch (choice) {
                case 1:
                    double newPrice = getDoubleInput("New price: $");
                    p.setprice(newPrice);
                    productDAO.update(p);
                    System.out.println("Price updated!");
                    break;
                case 2:
                    int newQty = getIntInput("New total quantity: ");
                    p.setquantity(newQty);
                    productDAO.updateStock(p.getid(), newQty);
                    System.out.println(" Quantity updated!");
                    break;
                case 3:
                    System.out.print("New name: ");
                    String newName = scanner.nextLine();
                    p.setname(newName);
                    productDAO.update(p);
                    System.out.println(" Name updated!");
                    break;
                default:
                    System.out.println(" Invalid choice!");
            }
        } catch (SQLException e) {
            System.out.println(" Database error: " + e.getMessage());
        }
    }
    
    public void deleteProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println(" Access Denied!");
            return;
        }
        
        System.out.print("\n Product ID to delete: ");
        product p;
        try {
            p = getProductOrThrow(scanner.nextLine());
        } catch (ProductNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }
        
        System.out.print("Delete '" + p.getname() + "'? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            try {
                productDAO.delete(p.getid());
                System.out.println(" Product deleted!");
            } catch (SQLException e) {
                System.out.println(" Database error: " + e.getMessage());
            }
        } else {
            System.out.println("Cancelled.");
        }
    }
    
    public product findProductById(String id) {
        try {
            return productDAO.findById(id.trim());
        } catch (SQLException e) {
            System.out.println(" Database error: " + e.getMessage());
            return null;
        }
    }

    public product getProductOrThrow(String id) {
        product found = findProductById(id);
        if (found == null) {
            throw new ProductNotFoundException("Product with ID '" + id + "' not found.");
        }
        return found;
    }
    
    private PrescriptionMedicine createPrescriptionMedicine(String id, String name, double price, int qty) {
        System.out.print("Active Ingredient: ");
        String ingredient = scanner.nextLine();
        System.out.print("Form (Tablet/Capsule/Liquid): ");
        String form = scanner.nextLine();
        System.out.print("Strength (e.g., 500mg): ");
        String strength = scanner.nextLine();
        System.out.print("Manufacturer: ");
        String mfg = scanner.nextLine();
        
        PrescriptionMedicine med = new PrescriptionMedicine(id, name, price, qty, 
                                                            ingredient, form, strength, mfg);
        med.setExpirationDate(getExpirationDate());
        return med;
    }
    
    private otcmedicine createOTCMedicine(String id, String name, double price, int qty) {
        System.out.print("Active Ingredient: ");
        String ingredient = scanner.nextLine();
        System.out.print("Form: ");
        String form = scanner.nextLine();
        System.out.print("Strength: ");
        String strength = scanner.nextLine();
        System.out.print("Manufacturer: ");
        String mfg = scanner.nextLine();
        
        otcmedicine med = new otcmedicine(id, name, price, qty, ingredient, form, strength, mfg);
        int limit = getIntInput("Purchase Limit (0=no limit): ");
        med.setPurchaseLimit(limit);
        med.setExpirationDate(getExpirationDate());
        return med;
    }
    
    private medicaledevice createMedicalDevice(String id, String name, double price, int qty) {
        System.out.print("Device Type: ");
        String type = scanner.nextLine();
        int warranty = getIntInput("Warranty (months): ");
        System.out.print("Manufacturer: ");
        String mfg = scanner.nextLine();
        
        return new medicaledevice(id, name, price, qty, type, warranty, mfg);
    }
    
    private Supplement createSupplement(String id, String name, double price, int qty) {
        System.out.print("Type (Vitamin/Mineral/Herbal): ");
        String type = scanner.nextLine();
        System.out.print("Serving Size: ");
        String serving = scanner.nextLine();
        System.out.print("Benefits: ");
        String benefits = scanner.nextLine();
        
        Supplement sup = new Supplement(id, name, price, qty, type, serving);
        sup.setBenefits(benefits);
        sup.setExpirationDate(getExpirationDate());
        return sup;
    }
    
    private LocalDate getExpirationDate() {
        int year = getIntInput("Expiration Year (e.g : 2026): ");
        int month = getIntInput("Month (1-12): ");
        int day = getIntInput("Day (1-31): ");
        return LocalDate.of(year, month, day);
    }
    
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number!");
            }
        }
    }
    
    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number!");
            }
        }
    }
}

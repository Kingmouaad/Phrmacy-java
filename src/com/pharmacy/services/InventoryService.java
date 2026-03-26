package com.pharmacy.services;

import com.pharmacy.db.ProductDAO;
import com.pharmacy.db.RestockDAO;
import com.pharmacy.exceptions.*;
import com.pharmacy.interfaces.*;
import com.pharmacy.models.products.*;
import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InventoryService {
    private ProductDAO productDAO;
    private RestockDAO restockDAO;
    private Scanner scanner;
    private Pharmacist currentPharmacist;
    private ProductService productService;
    
    public InventoryService(Scanner scanner, Pharmacist currentPharmacist, ProductService productService) {
        this.productDAO = new ProductDAO();
        this.restockDAO = new RestockDAO();
        this.scanner = scanner;
        this.currentPharmacist = currentPharmacist;
        this.productService = productService;
    }
    
    public void checkStockLevels() {
        System.out.println("\nSTOCK LEVELS");
        try {
            List<product> products = productDAO.findAll();
            for (product p : products) {
                String status = p.getquantity() > 20 ? "u have more then 20 " : 
                               p.getquantity() > 5 ? "u have more then 5 " : "u have lesss then 5 ";
                System.out.println(status + " " + p.getid() + " - " + p.getname() + 
                                 ": " + p.getquantity() + " units");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    public void lowStockAlert() {
        int threshold = getIntInput("\nMinimum stock threshold: ");
        
        System.out.println("\nLOW STOCK ITEMS:");
        try {
            List<product> products = productDAO.findAll();
            boolean foundLow = false;
            for (product p : products) {
                if (p.getquantity() <= threshold) {
                    System.out.println("  - " + p.getname() + " - Only " + p.getquantity() + " left!");
                    foundLow = true;
                }
            }
            if (!foundLow) System.out.println("All products above threshold.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    public void checkExpirations() {
        System.out.println("\nEXPIRATION CHECK");
        try {
            List<product> products = productDAO.findAll();
            for (product p : products) {
                if (p instanceof Expirable) {
                    Expirable exp = (Expirable) p;
                    long days = exp.getDaysUntilExpiration();
                    
                    if (exp.isExpired()) {
                        System.out.println("EXPIRED: " + p.getname() + " - REMOVE NOW!");
                    } else if (days <= 30) {
                        System.out.println("WARNING: " + p.getname() + " - " + days + " days left");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    public void processRestock() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println("Access Denied!");
            return;
        }
        
        System.out.println("\nRESTOCK");
        String txnId = "RST" + System.currentTimeMillis(); // Simple unique ID generator
        
        System.out.print("Supplier ID: ");
        String supplierId = scanner.nextLine();
        
        Restock restock = new Restock(txnId, currentPharmacist.getPersonId(), supplierId);
        
        List<String> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        
        boolean adding = true;
        while (adding) {
            System.out.print("\nProduct ID: ");
            String pid = scanner.nextLine();
            try {
                product p = productService.getProductOrThrow(pid);
                int qty = getIntInput("Quantity received: ");
                if (qty <= 0) {
                    System.out.println("Quantity must be greater than 0.");
                    continue;
                }
                
                restock.addProduct(pid, qty);
                productIds.add(pid);
                quantities.add(qty);
                
                System.out.println("Added " + qty + " x " + p.getname() + " to restock list.");
            } catch (ProductNotFoundException e) {
                System.out.println(e.getMessage());
            }
            
            System.out.print("Add more? (yes/no): ");
            adding = scanner.nextLine().equalsIgnoreCase("yes");
        }
        
        if (productIds.isEmpty()) {
            System.out.println("Restock cancelled (no items).");
            return;
        }
        
        double cost = getDoubleInput("\nTotal cost: $");
        restock.setTotalCost(cost);
        restock.completeRestock();
        
        try {
            restockDAO.processRestock(restock, productIds, quantities, cost, supplierId);
            restock.printRestockReceipt();
            System.out.println("\nRestock completed and saved to database!");
        } catch (SQLException e) {
            System.out.println("Database error during restock: " + e.getMessage());
        }
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

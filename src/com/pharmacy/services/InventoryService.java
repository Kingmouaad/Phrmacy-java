package com.pharmacy.services;

import com.pharmacy.exceptions.*;
import com.pharmacy.interfaces.*;
import com.pharmacy.models.products.*;
import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;
import java.util.List;
import java.util.Scanner;

public class InventoryService {
    private List<product> products;
    private List<Transaction> transactions;
    private Scanner scanner;
    private Pharmacist currentPharmacist;
    private ProductService productService;
    
    public InventoryService(List<product> products, List<Transaction> transactions, 
                            Scanner scanner, Pharmacist currentPharmacist, ProductService productService) {
        this.products = products;
        this.transactions = transactions;
        this.scanner = scanner;
        this.currentPharmacist = currentPharmacist;
        this.productService = productService;
    }
    
    public void checkStockLevels() {
        System.out.println("\nSTOCK LEVELS");
        for (product p : products) {
            String status = p.getquantity() > 20 ? "u have more then 20 " : 
                           p.getquantity() > 5 ? "u have more then 5 " : "u have lesss then 5 ";
            System.out.println(status + " " + p.getid() + " - " + p.getname() + 
                             ": " + p.getquantity() + " units");
        }
    }
    
    public void lowStockAlert() {
        int threshold = getIntInput("\nMinimum stock threshold: ");
        
        System.out.println("\nLOW STOCK ITEMS:");
        boolean foundLow = false;
        for (product p : products) {
            if (p.getquantity() <= threshold) {
                System.out.println("  - " + p.getname() + " - Only " + p.getquantity() + " left!");
                foundLow = true;
            }
        }
        
        if (!foundLow) System.out.println("All products above threshold.");
    }
    
    public void checkExpirations() {
        System.out.println("\nEXPIRATION CHECK");
        
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
    }
    
    public void processRestock() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println("Access Denied!");
            return;
        }
        
        System.out.println("\nRESTOCK");
        String txnId = "RST" + String.format("%03d", transactions.size() + 1);
        
        System.out.print("Supplier ID: ");
        String supplierId = scanner.nextLine();
        
        Restock restock = new Restock(txnId, currentPharmacist.getPersonId(), supplierId);
        
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
                p.setquantity(p.getquantity() + qty);
                System.out.println("Added " + qty + " x " + p.getname());
            } catch (ProductNotFoundException e) {
                System.out.println(e.getMessage());
            }
            
            System.out.print("Add more? (yes/no): ");
            adding = scanner.nextLine().equalsIgnoreCase("yes");
        }
        
        double cost = getDoubleInput("\nTotal cost: $");
        restock.setTotalCost(cost);
        restock.completeRestock();
        transactions.add(restock);
        
        restock.printRestockReceipt();
        System.out.println("\nRestock completed!");
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


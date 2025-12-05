package com.pharmacy;

import com.pharmacy.models.persons.*;
import com.pharmacy.models.products.*;
import com.pharmacy.models.transactions.*;
import com.pharmacy.services.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    // Data storage
    private static List<product> products = new ArrayList<>();
    private static List<Customer> customers = new ArrayList<>();
    private static List<Pharmacist> pharmacists = new ArrayList<>();
    private static List<Transaction> transactions = new ArrayList<>();
    
    private static Scanner scanner = new Scanner(System.in);
    private static Pharmacist currentPharmacist = null;
    
    // Service instances
    private static ProductService productService;
    private static CustomerService customerService;
    private static InventoryService inventoryService;
    private static SaleService saleService;
    
    public static void main(String[] args) {
        printHeader();
        initializeTestData();
        
        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }
        
        initializeServices();
        runMainLoop();
        scanner.close();
    }
    
    private static void printHeader() {
        System.out.println("                                        ");
        System.out.println("      PHARMACY MANAGEMENT SYSTEM        ");
        System.out.println("                                        ");
    }
    
    private static void initializeTestData() {
        Pharmacist pharm1 = new Pharmacist("PHR111", "benmalti mouaad", "02398857578", "kjhfu@gmail.com", "mosta", "78487578392");
        pharm1.setAccessLevel(3);
        pharmacists.add(pharm1);

        if (DataService.dataFilesExist()) {
            DataService.loadProducts(products);
            DataService.loadCustomers(customers);
            DataService.loadStock(products);
            DataService.loadSales(transactions);
            System.out.println("Test data loaded from 'data'.");
        } else {
            DataService.saveAllData(products, customers, transactions);
            System.out.println("No data files found. Created initial data in 'data'.");
        }
        System.out.println("Login ID: PHR111\n");
    }
    
    private static void initializeServices() {
        productService = new ProductService(products, scanner, currentPharmacist);
        customerService = new CustomerService(customers, transactions, scanner);
        inventoryService = new InventoryService(products, transactions, scanner, currentPharmacist, productService);
        saleService = new SaleService(products, customers, transactions, scanner, currentPharmacist, productService, customerService);
    }
    
    private static boolean login() {
        System.out.println("PHARMACIST LOGIN");
        System.out.print("Enter Pharmacist ID: ");
        String id = scanner.nextLine().trim();
        
        for (Pharmacist pharm : pharmacists) {
            if (pharm.getPersonId().equalsIgnoreCase(id)) {
                currentPharmacist = pharm;
                System.out.println("\nWelcome, " + pharm.getFullName() + "!");
                System.out.println(" Access Level: " + pharm.getAccessLevelName() + "\n");
                return true;
            }
        }
        return false;
    }
    
    private static void runMainLoop() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Choose option: ");
            
            switch (choice) {
                case 1: productManagement(); break;
                case 2: inventoryManagement(); break;
                case 3: processSale(); break;
                case 4: customerManagement(); break;
                case 5: viewTransactions(); break;
                case 6: 
                    running = false;
                    System.out.println("\n Thank you for using PMS!");
                    break;
                default: 
                    System.out.println(" Invalid choice!");
            }
        }
    }
    
    private static void displayMainMenu() {
        System.out.println("\n     ═════════════════════      ");
        System.out.println("       |   MAIN MENU       |      ");
        System.out.println("                                  ");
        System.out.println(" 1.  Product Management           ");
        System.out.println(" 2.  Inventory Management         ");
        System.out.println(" 3.  Process Sale                 ");
        System.out.println(" 4.  Customer Management          ");
        System.out.println(" 5.  View Transactions            ");
        System.out.println(" 6. Exit                         ");
        System.out.println("");
    }
    
    // 1 PRODUCT MANAGEMENT 
    private static void productManagement() {
        while (true) {
            System.out.println("\n PRODUCT MANAGEMENT");
            System.out.println("1. View All Products");
            System.out.println("2. Add Product");
            System.out.println("3. Search Product");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Back");
            
            int choice = getIntInput("Choose: ");
            if (choice == 6) return;
            
            switch (choice) {
                case 1: productService.viewAllProducts(); break;
                case 2: 
                    productService.addProduct();
                    saveData();
                    break;
                case 3: productService.searchProduct(); break;
                case 4: 
                    productService.updateProduct();
                    saveData();
                    break;
                case 5: 
                    productService.deleteProduct();
                    saveData();
                    break;
                default: System.out.println(" Invalid choice!");
            }
        }
    }
    
    // 2 INVENTORY MANAGEMENT 
    private static void inventoryManagement() {
        while (true) {
            System.out.println("\nINVENTORY MANAGEMENT");
            System.out.println("1. Check Stock Levels");
            System.out.println("2. Low Stock Alert");
            System.out.println("3. Expiration Check");
            System.out.println("4. Process Restock");
            System.out.println("5. Back");
            
            int choice = getIntInput("Choose: ");
            if (choice == 5) return;
            
            switch (choice) {
                case 1: inventoryService.checkStockLevels(); break;
                case 2: inventoryService.lowStockAlert(); break;
                case 3: inventoryService.checkExpirations(); break;
                case 4: 
                    inventoryService.processRestock();
                    saveData();
                    break;
                default: System.out.println("Invalid choice!");
            }
        }
    }
    
    // 3. PROCESS SALE
    private static void processSale() {
        System.out.println("\nSALES MENU");
        System.out.println("1. New Sale");
        System.out.println("2. Process Return/Reimbursement");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) {
            saleService.processNewSale();
            saveData();
        } else if (choice == 2) {
            saleService.processReturn();
            saveData();
        } else {
            System.out.println("Invalid choice!");
        }
    }
    
    // 4. CUSTOMER MANAGEMENT
    private static void customerManagement() {
        while (true) {
            System.out.println("\n  CUSTOMER MANAGEMENT");
            System.out.println("1. View All Customers");
            System.out.println("2. Register Customer");
            System.out.println("3. View Details");
            System.out.println("4. Purchase History");
            System.out.println("5. Back");
            
            int choice = getIntInput("Choose: ");
            if (choice == 5) return;
            
            switch (choice) {
                case 1: customerService.viewAllCustomers(); break;
                case 2: 
                    customerService.registerCustomer();
                    saveData();
                    break;
                case 3: customerService.viewCustomerDetails(); break;
                case 4: customerService.viewPurchaseHistory(); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }
    
    // 5. VIEW TRANSACTIONS
    private static void viewTransactions() {
        System.out.println("\nALL TRANSACTIONS (" + transactions.size() + ")");
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        
        for (Transaction txn : transactions) {
            System.out.println("\n" + txn);
            System.out.println("─".repeat(50));
        }
    }
    
    private static void saveData() {
        DataService.saveAllData(products, customers, transactions);
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number!");
            }
        }
    }
}

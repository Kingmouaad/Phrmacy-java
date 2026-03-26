package com.pharmacy;

import com.pharmacy.db.DatabaseConnection;
import com.pharmacy.db.UserDAO;
import com.pharmacy.models.persons.*;
import com.pharmacy.services.*;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Pharmacist currentPharmacist = null;
    
    // Service instances
    private static ProductService productService;
    private static CustomerService customerService;
    private static InventoryService inventoryService;
    private static SaleService saleService;
    
    public static void main(String[] args) {
        printHeader();
        initializeDatabase();
        
        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }
        
        initializeServices();
        runMainLoop();
        
        System.out.println("\nClosing database connection...");
        DatabaseConnection.getInstance().closeConnection();
        scanner.close();
    }
    
    private static void printHeader() {
        System.out.println("                                        ");
        System.out.println("      PHARMACY MANAGEMENT SYSTEM        ");
        System.out.println("                                        ");
    }
    
    private static void initializeDatabase() {
        System.out.print("Initializing Database... ");
        DatabaseConnection db = DatabaseConnection.getInstance();
        db.initializeDatabase();
        System.out.println("Done.");
        System.out.println("Default Login ID: admin (Pass: admin123)\n");
    }
    
    private static void initializeServices() {
        productService = new ProductService(scanner, currentPharmacist);
        customerService = new CustomerService(scanner);
        inventoryService = new InventoryService(scanner, currentPharmacist, productService);
        saleService = new SaleService(scanner, currentPharmacist, productService, customerService);
    }
    
    private static boolean login() {
        System.out.println("PHARMACIST LOGIN");
        System.out.print("Username (admin): ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        UserDAO userDAO = new UserDAO();
        try {
            currentPharmacist = userDAO.authenticate(username, password);
            if (currentPharmacist != null) {
                System.out.println("\nWelcome, " + currentPharmacist.getFullName() + "!");
                System.out.println("Access Level: " + currentPharmacist.getAccessLevelName() + "\n");
                return true;
            } else {
                System.out.println("\nInvalid username or password.");
            }
        } catch (Exception e) {
            System.out.println("\nDatabase error during login: " + e.getMessage());
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
                    System.out.println("\nThank you for using PMS!");
                    break;
                default: 
                    System.out.println("Invalid choice!");
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
                case 2: productService.addProduct(); break;
                case 3: productService.searchProduct(); break;
                case 4: productService.updateProduct(); break;
                case 5: productService.deleteProduct(); break;
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
                case 4: inventoryService.processRestock(); break;
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
        } else if (choice == 2) {
            saleService.processReturn();
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
                case 2: customerService.registerCustomer(); break;
                case 3: customerService.viewCustomerDetails(); break;
                case 4: customerService.viewPurchaseHistory(); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }
    
    // 5. VIEW TRANSACTIONS
    private static void viewTransactions() {
        System.out.println("\nALL TRANSACTIONS is now managed via individual Customer Purchase History / Database GUI.");
        // We moved away from an all-encompassing in-memory transaction list.
        // We can print all sales, restocks, returns here if needed, but standard is customer history.
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

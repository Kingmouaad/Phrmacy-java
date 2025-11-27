package main;

import models.interfaces.*;
import models.products.*;
import models.person.*;
import models.transaction.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    // Data storage
    private static List<product> products = new ArrayList<>();
    private static List<Customer> customers = new ArrayList<>();
    private static List<Pharmacist> pharmacists = new ArrayList<>();
    private static List<Doctor> doctors = new ArrayList<>();
    private static List<Transaction> transactions = new ArrayList<>();
    
    private static Scanner scanner = new Scanner(System.in);
    private static Pharmacist currentPharmacist = null;
    
    public static void main(String[] args) {
        printHeader();
        initializeTestData();
        
        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }
        
        runMainLoop();
        scanner.close();
    }
    
    // setup for the project 
    private static void printHeader() {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║  PHARMACY MANAGEMENT SYSTEM v2.0   ║");
        System.out.println("╚════════════════════════════════════╝");
    }
    
    private static void initializeTestData() {
        // Pharmacist
        Pharmacist pharm = new Pharmacist("P001", "Dr. Sarah Johnson", 
                                         "555-5678", "sarah@pharmacy.com", 
                                         "123 Main St", "PHR123456");
        pharm.setAccessLevel(3);
        pharmacists.add(pharm);
        
        // Customers
        customers.add(new Customer("C001", "John Smith", "555-1234", 
                                  "john@email.com", "456 Oak Ave"));
        customers.add(new Customer("C002", "Mary Johnson", "555-5555", 
                                  "mary@email.com", "789 Pine St"));
        
        // Doctor
        doctors.add(new Doctor("D001", "Dr. Emily Chen", "555-9999",
                              "chen@clinic.com", "789 Elm St", "MED789456", 
                              "General Practice"));
        
        // Products
        PrescriptionMedicine med1 = new PrescriptionMedicine(
            "PM001", "Amoxicillin 500mg", 25.00, 50,
            "Amoxicillin", "Capsule", "500mg", "PharmaCorp");
        med1.setExpirationDate(LocalDate.of(2026, 12, 31));
        products.add(med1);
        
        otcmedicine med2 = new otcmedicine(
            "OTC001", "Ibuprofen 200mg", 8.99, 100,
            "Ibuprofen", "Tablet", "200mg", "HealthPlus");
        med2.setExpirationDate(LocalDate.of(2026, 6, 30));
        med2.setPurchaseLimit(2);
        products.add(med2);
        
        medicaledevice dev = new medicaledevice(
            "DEV001", "Digital Thermometer", 15.99, 25,
            "Thermometer", 12, "MedTech");
        products.add(dev);
        
        Supplement sup = new Supplement(
            "SUP001", "Vitamin C 1000mg", 12.99, 80,
            "Vitamin", "1 tablet daily");
        sup.setExpirationDate(LocalDate.of(2026, 9, 30));
        sup.setBenefits("Immune support");
        products.add(sup);
        
        System.out.println("Test data loaded!");
        System.out.println("Login ID: P001\n");
    }
    
    //  LOGIN SYSTEM 
    private static boolean login() {
        System.out.println("🔐 PHARMACIST LOGIN");
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
    
    // our menu 
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
        System.out.println("       |   MAIN MENU       |         ");
        System.out.println("");
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
                case 1: viewAllProducts(); break;
                case 2: addProduct(); break;
                case 3: searchProduct(); break;
                case 4: updateProduct(); break;
                case 5: deleteProduct(); break;
                default: System.out.println(" Invalid choice!");
            }
        }
    }
    
    private static void viewAllProducts() {
        System.out.println("\n ALL PRODUCTS (" + products.size() + " items)");
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        
        for (product p : products) {
            System.out.println("\n" + p);
            System.out.println("   Available: " + (p.isAvailableForSale() ? "✅ YES" : "❌ NO"));
            
            if (p instanceof Expirable) {
                Expirable exp = (Expirable) p;
                if (exp.isExpired()) {
                    System.out.println("   ⚠️ EXPIRED!");
                } else {
                    System.out.println("   📅 Expires in " + exp.getDaysUntilExpiration() + " days");
                }
            }
            System.out.println("   " + "─".repeat(50));
        }
    }
    
    private static void addProduct() {
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
            
            products.add(newProduct);
            System.out.println(" Product added successfully!");
        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
    
    private static PrescriptionMedicine createPrescriptionMedicine(String id, String name, double price, int qty) {
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
    
    private static otcmedicine createOTCMedicine(String id, String name, double price, int qty) {
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
    
    private static medicaledevice createMedicalDevice(String id, String name, double price, int qty) {
        System.out.print("Device Type: ");
        String type = scanner.nextLine();
        int warranty = getIntInput("Warranty (months): ");
        System.out.print("Manufacturer: ");
        String mfg = scanner.nextLine();
        
        return new medicaledevice(id, name, price, qty, type, warranty, mfg);
    }
    
    private static Supplement createSupplement(String id, String name, double price, int qty) {
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
    
    private static LocalDate getExpirationDate() {
        int year = getIntInput("Expiration Year (e.g., 2026): ");
        int month = getIntInput("Month (1-12): ");
        int day = getIntInput("Day (1-31): ");
        return LocalDate.of(year, month, day);
    }
    
    private static void searchProduct() {
        System.out.print("\n🔍 Search (ID or Name): ");
        String query = scanner.nextLine().toLowerCase();
        
        boolean found = false;
        for (product p : products) {
            if (p.getid().toLowerCase().contains(query) || 
                p.getname().toLowerCase().contains(query)) {
                System.out.println("\n FOUND: " + p);
                found = true;
            }
        }
        
        if (!found) System.out.println(" No products found.");
    }
    
    private static void updateProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println(" Access Denied!");
            return;
        }
        
        System.out.print("\nProduct ID to update: ");
        String id = scanner.nextLine();
        product p = findProductById(id);
        
        if (p == null) {
            System.out.println(" Product not found!");
            return;
        }
        
        System.out.println("Current: " + p.getname());
        System.out.println("1. Update Price");
        System.out.println("2. Update Quantity");
        System.out.println("3. Update Name");
        
        int choice = getIntInput("Choose: ");
        
        switch (choice) {
            case 1:
                double newPrice = getDoubleInput("New price: $");
                p.setprice(newPrice);
                System.out.println("Price updated!");
                break;
            case 2:
                int newQty = getIntInput("New quantity: ");
                p.setquantity(newQty);
                System.out.println(" Quantity updated!");
                break;
            case 3:
                System.out.print("New name: ");
                String newName = scanner.nextLine();
                p.setname(newName);
                System.out.println(" Name updated!");
                break;
            default:
                System.out.println(" Invalid choice!");
        }
    }
    
    private static void deleteProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println(" Access Denied!");
            return;
        }
        
        System.out.print("\n Product ID to delete: ");
        String id = scanner.nextLine();
        product p = findProductById(id);
        
        if (p == null) {
            System.out.println(" Product not found!");
            return;
        }
        
        System.out.print("Delete '" + p.getname() + "'? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            products.remove(p);
            System.out.println(" Product deleted!");
        } else {
            System.out.println("Cancelled.");
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
                case 1: checkStockLevels(); break;
                case 2: lowStockAlert(); break;
                case 3: checkExpirations(); break;
                case 4: processRestock(); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }
    
    private static void checkStockLevels() {
        System.out.println("\nSTOCK LEVELS");
        for (product p : products) {
            String status = p.getquantity() > 20 ? "u have more then 20 " : 
                           p.getquantity() > 5 ? "u have more then 5 " : "u have lesss then 5 ";
            System.out.println(status + " " + p.getid() + " - " + p.getname() + 
                             ": " + p.getquantity() + " units");
        }
    }
    
    private static void lowStockAlert() {
        int threshold = getIntInput("\nMinimum stock threshold: ");
        
        System.out.println("\nLOW STOCK ITEMS:");
        boolean foundLow = false;
        for (product p : products) {
            if (p.getquantity() <= threshold) {
                System.out.println("  • " + p.getname() + " - Only " + p.getquantity() + " left!");
                foundLow = true;
            }
        }
        
        if (!foundLow) System.out.println("All products above threshold.");
    }
    
    private static void checkExpirations() {
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
    
    private static void processRestock() {
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
            product p = findProductById(pid);
            
            if (p == null) {
                System.out.println("Product not found!");
                continue;
            }
            
            int qty = getIntInput("Quantity received: ");
            restock.addProduct(pid, qty);
            p.setquantity(p.getquantity() + qty);
            
            System.out.println("Added " + qty + " x " + p.getname());
            
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
    
    // ========== 3. PROCESS SALE ==========
    private static void processSale() {
        System.out.println("\nSALES MENU");
        System.out.println("1. New Sale");
        System.out.println("2. Process Return/Reimbursement");
        
        int choice = getIntInput("Choose: ");
        
        if (choice == 1) {
            processNewSale();
        } else if (choice == 2) {
            processReturn();
        } else {
            System.out.println("Invalid choice!");
        }
    }
    
    private static void processNewSale() {
        System.out.println("\nNEW SALE");
        
        System.out.print("Customer ID: ");
        String custId = scanner.nextLine();
        Customer customer = findCustomerById(custId);
        
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }
        
        System.out.println(" Customer: " + customer.getFullName());
        System.out.println("Loyalty Points: " + customer.getLoyaltyPoints());
        
        String txnId = "TXN" + String.format("%03d", transactions.size() + 1);
        Sale sale = new Sale(txnId, currentPharmacist.getPersonId(), custId);
        
        double subtotal = 0.0;
        boolean adding = true;
        
        while (adding) {
            System.out.print("\nProduct ID: ");
            String pid = scanner.nextLine();
            product p = findProductById(pid);
            
            if (p == null) {
                System.out.println("Product not found!");
                continue;
            }
            
            System.out.println(" " + p.getname() + " - $" + p.getprice() + " (Stock: " + p.getquantity() + ")");
            
            // Handle prescription
            if (p instanceof Prescribable) {
                Prescribable presc = (Prescribable) p;
                if (presc.requiresPrescription()) {
                    System.out.print("Prescription ID: ");
                    String rxId = scanner.nextLine();
                    presc.setPrescriptionId(rxId);
                }
            }
            
            // Check availability
            if (!p.isAvailableForSale()) {
                System.out.println(" Product not available for sale!");
                continue;
            }
            
            // Check expiration
            if (p instanceof Expirable && ((Expirable) p).isExpired()) {
                System.out.println("Product EXPIRED!");
                continue;
            }
            
            int qty = getIntInput("Quantity: ");
            
            if (qty > p.getquantity()) {
                System.out.println("Not enough stock!");
                continue;
            }
            
            // Check OTC limits
            if (p instanceof otcmedicine) {
                otcmedicine otc = (otcmedicine) p;
                if (otc.getPurchaseLimit() > 0 && qty > otc.getPurchaseLimit()) {
                    System.out.println("Exceeds purchase limit of " + otc.getPurchaseLimit());
                    continue;
                }
            }
            
            sale.addProduct(pid, qty);
            subtotal += p.getprice() * qty;
            p.setquantity(p.getquantity() - qty);
            
            System.out.println(" Added: " + qty + " x " + p.getname() + " = $" + (p.getprice() * qty));
            
            System.out.print("Add more? (yes/no): ");
            adding = scanner.nextLine().equalsIgnoreCase("yes");
        }
        
        if (sale.getProductIds().isEmpty()) {
            System.out.println(" No products in sale.");
            return;
        }
        
        // Apply discount
        System.out.println("\nSubtotal: $" + subtotal);
        System.out.print("Use loyalty points? (yes/no): ");
        
        double discount = 0.0;
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            double maxDiscount = customer.getLoyaltyPoints() / 100.0;
            System.out.println("Available discount: $" + maxDiscount);
            discount = getDoubleInput("Apply discount: $");
            
            if (discount > maxDiscount) discount = maxDiscount;
            
            customer.useLoyaltyPoints(discount * 100);
            sale.setDiscount(discount);
        }
        
        System.out.print("Payment method (CASH/CARD): ");
        String payment = scanner.nextLine();
        sale.setPaymentMethod(payment);
        
        sale.calculateTotal(subtotal);
        sale.completeSale();
        
        customer.addLoyaltyPoints(sale.getTotalAmount());
        customer.addPurchase(txnId);
        transactions.add(sale);
        
        sale.printReceipt();
        System.out.println("\nNew loyalty balance: " + customer.getLoyaltyPoints());
        System.out.println("Sale completed!");
    }
    
    private static void processReturn() {
        System.out.println("\n↩PROCESS RETURN");
        
        System.out.print("Customer ID: ");
        String custId = scanner.nextLine();
        Customer customer = findCustomerById(custId);
        
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }
        
        System.out.print("Original Sale Transaction ID: ");
        String originalSaleId = scanner.nextLine();
        
        Transaction originalTxn = findTransactionById(originalSaleId);
        if (originalTxn == null || !(originalTxn instanceof Sale)) {
            System.out.println("Sale transaction not found!");
            return;
        }
        
        String returnId = "RET" + String.format("%03d", transactions.size() + 1);
        Return returnTxn = new Return(returnId, currentPharmacist.getPersonId(), 
                                      custId, originalSaleId);
        
        System.out.print("Return Reason: ");
        String reason = scanner.nextLine();
        returnTxn.setReason(reason);
        
        double refundTotal = 0.0;
        boolean adding = true;
        
        while (adding) {
            System.out.print("\nProduct ID to return: ");
            String pid = scanner.nextLine();
            product p = findProductById(pid);
            
            if (p == null) {
                System.out.println("Product not found!");
                continue;
            }
            
            int qty = getIntInput("Quantity to return: ");
            
            returnTxn.addProduct(pid, qty);
            double refund = p.getprice() * qty;
            refundTotal += refund;
            
            // Return stock
            p.setquantity(p.getquantity() + qty);
            
            System.out.println("Returning " + qty + " x " + p.getname() + " = $" + refund);
            
            System.out.print("Return more items? (yes/no): ");
            adding = scanner.nextLine().equalsIgnoreCase("yes");
        }
        
        if (returnTxn.getProductIds().isEmpty()) {
            System.out.println("No items to return.");
            return;
        }
        
        System.out.print("Refund method (CASH/CARD): ");
        String refundMethod = scanner.nextLine();
        returnTxn.setRefundMethod(refundMethod);
        
        returnTxn.calculateRefund(refundTotal);
        returnTxn.completeReturn();
        
        transactions.add(returnTxn);
        returnTxn.printReturnReceipt();
        
        System.out.println("\n Return processed successfully!");
    }
    
    // ========== 4. CUSTOMER MANAGEMENT ==========
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
                case 1: viewAllCustomers(); break;
                case 2: registerCustomer(); break;
                case 3: viewCustomerDetails(); break;
                case 4: viewPurchaseHistory(); break;
                default: System.out.println("Invalid choice!");
            }
        }
    }
    
    private static void viewAllCustomers() {
        System.out.println("\nALL CUSTOMERS (" + customers.size() + ")");
        for (Customer c : customers) {
            System.out.println(c);
            System.out.println("─".repeat(50));
        }
    }
    
    private static void registerCustomer() {
        System.out.println("\nREGISTER CUSTOMER");
        System.out.print("Customer ID: ");
        String id = scanner.nextLine();
        
        if (findCustomerById(id) != null) {
            System.out.println("ID already exists!");
            return;
        }
        
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        
        customers.add(new Customer(id, name, phone, email, address));
        System.out.println("Customer registered!");
    }
    
    private static void viewCustomerDetails() {
        System.out.print("\nCustomer ID: ");
        Customer c = findCustomerById(scanner.nextLine());
        
        if (c == null) {
            System.out.println("Not found!");
        } else {
            System.out.println("\n" + c);
        }
    }
    
    private static void viewPurchaseHistory() {
        System.out.print("\nCustomer ID: ");
        Customer c = findCustomerById(scanner.nextLine());
        
        if (c == null) {
            System.out.println("Not found!");
            return;
        }
        
        System.out.println("\nPURCHASE HISTORY: " + c.getFullName());
        List<String> history = c.getPurchaseHistory();
        
        if (history.isEmpty()) {
            System.out.println("No purchases yet.");
            return;
        }
        
        for (String txnId : history) {
            Transaction txn = findTransactionById(txnId);
            if (txn != null) {
                System.out.println("\n" + txn);
                System.out.println("─".repeat(50));
            }
        }
    }
    
    // ========== 5. VIEW TRANSACTIONS ==========
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
    
    // helper methods so i try to make less code 
    private static product findProductById(String id) {
        for (product p : products) {
            if (p.getid().equalsIgnoreCase(id.trim())) return p;
        }
        return null;
    }
    
    private static Customer findCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getPersonId().equalsIgnoreCase(id.trim())) return c;
        }
        return null;
    }
    
    private static Transaction findTransactionById(String id) {
        for (Transaction t : transactions) {
            if (t.getTransactionId().equalsIgnoreCase(id.trim())) return t;
        }
        return null;
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
    
    private static double getDoubleInput(String prompt) {
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
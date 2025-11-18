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
    // Store everything in memory using lists
    private static List<product> products = new ArrayList<>();
    private static List<Customer> customers = new ArrayList<>();
    private static List<Pharmacist> pharmacists = new ArrayList<>();
    private static List<Doctor> doctors = new ArrayList<>();
    private static List<Transaction> transactions = new ArrayList<>();
    
    private static Scanner scanner = new Scanner(System.in);
    private static Pharmacist currentPharmacist = null;  // Who is logged in
    
    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("   PHARMACY MANAGEMENT SYSTEM");
        System.out.println("======================================");
        
        // Add some initial test data
        initializeTestData();
        
        // Login
        if (!login()) {
            System.out.println("Login failed. Exiting...");
            return;
        }
        
        // Main menu loop
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    medicationCatalogMenu();
                    break;
                case 2:
                    inventoryManagementMenu();
                    break;
                case 3:
                    processSaleMenu();
                    break;
                case 4:
                    customerManagementMenu();
                    break;
                case 5:
                    viewAllTransactions();
                    break;
                case 6:
                    running = false;
                    System.out.println("Thank you for using the Pharmacy Management System!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
    }
    
    // Initialize with some test data
    private static void initializeTestData() {
        // Add a pharmacist
        Pharmacist pharm1 = new Pharmacist("PHARM001", "benmalti mouaad", 
                                          "555-5678", "sarah@pharmacy.com", 
                                          "PHR123456","566");
        pharm1.setAccessLevel(3);  // Manager level
        pharmacists.add(pharm1);
        
        // Add some customers
        Customer cust1 = new Customer("CUST001", "John Smith", 
                                     "555-1234", "john@email.com","chmouma");
        Customer cust2 = new Customer("CUST002", "Mary Johnson", 
                                     "555-5555", "mary@email.com","cmouma");
        customers.add(cust1);
        customers.add(cust2);
        
        // Add a doctor
        Doctor doc1 = new Doctor("DOC001", "Dr. Emily Chen", "555-9999",
                                "chen@clinic.com", "MED789456", "General Practice","chronic");
        doctors.add(doc1);
        
        // Add some products
        PrescriptionMedicine med1 = new PrescriptionMedicine(
            "MED001", "Amoxicillin 500mg", 25.00, 50,
            "Amoxicillin", "Capsule", "500mg", "PharmaCorp"
        );
        med1.setExpirationDate(LocalDate.of(2026, 12, 31));
        
        otcmedicine med2 = new otcmedicine(
            "MED002", "Ibuprofen 200mg", 8.99, 100,
            "Ibuprofen", "Tablet", "200mg", "HealthPlus"
        );
        med2.setExpirationDate(LocalDate.of(2026, 6, 30));
        med2.setPurchaseLimit(2);
        
        medicaledevice dev1 = new medicaledevice(
            "DEV001", "Digital Thermometer", 15.99, 25,
            "Thermometer", 12, "MedTech"
        );
        
        Supplement sup1 = new Supplement(
            "SUP001", "Vitamin C 1000mg", 12.99, 80,
            "Vitamin", "1 tablet daily"
        );
        sup1.setExpirationDate(LocalDate.of(2026, 9, 30));
        sup1.setBenefits("Immune support");
        
        products.add(med1);
        products.add(med2);
        products.add(dev1);
        products.add(sup1);
        
        System.out.println("Test data loaded successfully!");
        System.out.println("Pharmacist login: PHARM001");
        System.out.println();
    }
    
    // Login system
    private static boolean login() {
        System.out.println("\n--- PHARMACIST LOGIN ---");
        System.out.print("Enter Pharmacist ID: ");
        String id = scanner.nextLine();
        
        for (Pharmacist pharm : pharmacists) {
            if (pharm.getPersonId().equals(id)) {
                currentPharmacist = pharm;
                System.out.println("Welcome, " + pharm.getFullName() + "!");
                System.out.println("Access Level: " + pharm.getAccessLevelName());
                return true;
            }
        }
        
        System.out.println("Pharmacist not found!");
        return false;
    }
    
    // Main menu
    private static void displayMainMenu() {
        System.out.println("\n======================================");
        System.out.println("         MAIN MENU");
        System.out.println("======================================");
        System.out.println("1. Medication Catalog");
        System.out.println("2. Inventory Management");
        System.out.println("3. Process Sale");
        System.out.println("4. Customer Management");
        System.out.println("5. View All Transactions");
        System.out.println("6. Exit");
        System.out.println("======================================");
    }
    
    // ========== MEDICATION CATALOG MENU ==========
    private static void medicationCatalogMenu() {
        while (true) {
            System.out.println("\n--- MEDICATION CATALOG ---");
            System.out.println("1. View All Products");
            System.out.println("2. Add New Product");
            System.out.println("3. Search Product");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Back to Main Menu");
            
            int choice = getIntInput("Enter choice: ");
            
            switch (choice) {
                case 1:
                    viewAllProducts();
                    break;
                case 2:
                    addNewProduct();
                    break;
                case 3:
                    searchProduct();
                    break;
                case 4:
                    updateProduct();
                    break;
                case 5:
                    deleteProduct();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void viewAllProducts() {
        System.out.println("\n=== ALL PRODUCTS ===");
        if (products.isEmpty()) {
            System.out.println("No products in catalog.");
            return;
        }
        
        for (product product : products) {
            System.out.println("\n" + product);
            System.out.println("Available for sale: " + 
                             (product.isAvailableForSale() ? "YES" : "NO"));
            
            // Check if expirable
            if (product instanceof Expirable) {
                Expirable exp = (Expirable) product;
                if (exp.isExpired()) {
                    System.out.println("*** EXPIRED ***");
                } else {
                    System.out.println("Expires in: " + exp.getDaysUntilExpiration() + " days");
                }
            }
            System.out.println("---");
        }
    }
    
    private static void addNewProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println("Access Denied: You need Senior level or above.");
            return;
        }
        
        System.out.println("\n--- ADD NEW PRODUCT ---");
        System.out.println("Product Type:");
        System.out.println("1. Prescription Medicine");
        System.out.println("2. OTC Medicine");
        System.out.println("3. Medical Device");
        System.out.println("4. Supplement");
        
        int type = getIntInput("Enter type: ");
        
        System.out.print("Product ID: ");
        String id = scanner.nextLine();
        
        // Check if ID already exists
        for (product p : products) {
            if (p.getid().equals(id)) {
                System.out.println("Error: Product ID already exists!");
                return;
            }
        }
        
        System.out.print("Product Name: ");
        String name = scanner.nextLine();
        
        double price = getDoubleInput("Price: $");
        int quantity = getIntInput("Quantity: ");
        
        product newProduct = null;
        
        switch (type) {
            case 1: // Prescription Medicine
                System.out.print("Active Ingredient: ");
                String ingredient = scanner.nextLine();
                System.out.print("Dosage Form (Tablet/Capsule/Liquid): ");
                String form = scanner.nextLine();
                System.out.print("Strength (e.g., 500mg): ");
                String strength = scanner.nextLine();
                System.out.print("Manufacturer: ");
                String manufacturer = scanner.nextLine();
                
                newProduct = new PrescriptionMedicine(id, name, price, quantity,
                                                     ingredient, form, strength, manufacturer);
                
                // Set expiration date and also can use .nextInt
                System.out.print("Expiration Year (e.g., 2026): ");
                int year = getIntInput("");
                System.out.print("Expiration Month (1-12): ");
                int month = getIntInput("");
                System.out.print("Expiration Day (1-31): ");
                int day = getIntInput("");
                ((PrescriptionMedicine)newProduct).setExpirationDate(LocalDate.of(year, month, day));
                break;
                
            case 2: // OTC Medicine
                System.out.print("Active Ingredient: ");
                ingredient = scanner.nextLine();
                System.out.print("Dosage Form: ");
                form = scanner.nextLine();
                System.out.print("Strength: ");
                strength = scanner.nextLine();
                System.out.print("Manufacturer: ");
                manufacturer = scanner.nextLine();
                
                newProduct = new otcmedicine(id, name, price, quantity,
                                            ingredient, form, strength, manufacturer);
                
                int limit = getIntInput("Purchase Limit (0 for no limit): ");
                ((otcmedicine)newProduct).setPurchaseLimit(limit);
                
                // Set expiration date
                System.out.print("Expiration Year: ");
                year = getIntInput("");
                System.out.print("Expiration Month: ");
                month = getIntInput("");
                System.out.print("Expiration Day: ");
                day = getIntInput("");
                ((otcmedicine)newProduct).setExpirationDate(LocalDate.of(year, month, day));
                break;
                
            case 3: // Medical Device
                System.out.print("Device Type: ");
                String deviceType = scanner.nextLine();
                int warranty = getIntInput("Warranty (months): ");
                System.out.print("Manufacturer: ");
                manufacturer = scanner.nextLine();
                
                newProduct = new medicaledevice(id, name, price, quantity,
                                              deviceType, warranty, manufacturer);
                break;
                
            case 4: // Supplement
                System.out.print("Supplement Type (Vitamin/Mineral/Herbal): ");
                String supType = scanner.nextLine();
                System.out.print("Serving Size: ");
                String serving = scanner.nextLine();
                
                newProduct = new Supplement(id, name, price, quantity,
                                           supType, serving);
                
                System.out.print("Benefits: ");
                String benefits = scanner.nextLine();
                ((Supplement)newProduct).setBenefits(benefits);
                
                // Set expiration date
                System.out.print("Expiration Year: ");
                year = getIntInput("");
                System.out.print("Expiration Month: ");
                month = getIntInput("");
                System.out.print("Expiration Day: ");
                day = getIntInput("");
                ((Supplement)newProduct).setExpirationDate(LocalDate.of(year, month, day));
                break;
                
            default:
                System.out.println("Invalid type.");
                return;
        }
        
        products.add(newProduct);
        System.out.println("Product added successfully!");
    }
    
    private static void searchProduct() {
        System.out.print("\nEnter Product ID or Name to search: ");
        String search = scanner.nextLine().toLowerCase();
        
        boolean found = false;
        for (product product : products) {
            if (product.getid().toLowerCase().contains(search) ||
                product.getname().toLowerCase().contains(search)) {
                System.out.println("\n" + product);
                found = true;
            }
        }
        
        if (!found) {
            System.out.println("No products found.");
        }
    }
    
    private static void updateProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println("Access Denied: You need Senior level or above.");
            return;
        }
        
        System.out.print("\nEnter Product ID to update: ");
        String id = scanner.nextLine();
        
        product product = findProductById(id);
        if (product == null) {
            System.out.println("Product not found!");
            return;
        }
        
        System.out.println("Current product: " + product.getname());
        System.out.println("What to update?");
        System.out.println("1. Price");
        System.out.println("2. Quantity");
        System.out.println("3. Name");
        
        int choice = getIntInput("Enter choice: ");
        
        switch (choice) {
            case 1:
                double newPrice = getDoubleInput("New price: $");
                product.setprice(newPrice);
                System.out.println("Price updated!");
                break;
            case 2:
                int newQty = getIntInput("New quantity: ");
                product.setquantity(newQty);
                System.out.println("Quantity updated!");
                break;
            case 3:
                System.out.print("New name: ");
                String newName = scanner.nextLine();
                product.setname(newName);
                System.out.println("Name updated!");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    private static void deleteProduct() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println("Access Denied: You need Senior level or above.");
            return;
        }
        
        System.out.print("\nEnter Product ID to delete: ");
        String id = scanner.nextLine();
        
        product product = findProductById(id);
        if (product == null) {
            System.out.println("Product not found!");
            return;
        }
        
        System.out.print("Are you sure you want to delete " + product.getname() + "? (yes/no): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            products.remove(product);
            System.out.println("Product deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    
    // ========== INVENTORY MANAGEMENT MENU ==========
    private static void inventoryManagementMenu() {
        while (true) {
            System.out.println("\n--- INVENTORY MANAGEMENT ---");
            System.out.println("1. Check Stock Levels");
            System.out.println("2. Low Stock Alert");
            System.out.println("3. Check Expirations");
            System.out.println("4. Process Restock");
            System.out.println("5. Back to Main Menu");
            
            int choice = getIntInput("Enter choice: ");
            
            switch (choice) {
                case 1:
                    checkStockLevels();
                    break;
                case 2:
                    lowStockAlert();
                    break;
                case 3:
                    checkExpirations();
                    break;
                case 4:
                    processRestock();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void checkStockLevels() {
        System.out.println("\n=== STOCK LEVELS ===");
        for (product product : products) {
            System.out.println(product.getid() + " - " + product.getname() + 
                             ": " + product.getquantity() + " units");
        }
    }
    
    private static void lowStockAlert() {
        System.out.println("\n=== LOW STOCK ALERT ===");
        int threshold = getIntInput("Enter minimum stock threshold: ");
        
        boolean foundLow = false;
        for (product product : products) {
            if (product.getquantity() <= threshold) {
                System.out.println("LOW: " + product.getname() + 
                                 " - Only " + product.getquantity() + " left!");
                foundLow = true;
            }
        }
        
        if (!foundLow) {
            System.out.println("All products are above threshold.");
        }
    }
    
    private static void checkExpirations() {
        System.out.println("\n=== EXPIRATION CHECK ===");
        
        for (product product : products) {
            if (product instanceof Expirable) {
                Expirable exp = (Expirable) product;
                
                if (exp.isExpired()) {
                    System.out.println("EXPIRED: " + product.getname() + 
                                     " - REMOVE FROM INVENTORY!");
                } else if (exp.getDaysUntilExpiration() <= 30) {
                    System.out.println("WARNING: " + product.getname() + 
                                     " expires in " + exp.getDaysUntilExpiration() + " days");
                }
            }
        }
    }
    
    private static void processRestock() {
        if (!currentPharmacist.canManageInventory()) {
            System.out.println("Access Denied: You need Senior level or above.");
            return;
        }
        
        System.out.println("\n--- PROCESS RESTOCK ---");
        
        // Generate transaction ID
        String transactionId = "RST" + String.format("%03d", transactions.size() + 1);
        
        System.out.print("Supplier ID: ");
        String supplierId = scanner.nextLine();
        
        Restock restock = new Restock(transactionId, currentPharmacist.getPersonId(), 
                                      supplierId);
        
        boolean addingProducts = true;
        while (addingProducts) {
            System.out.print("\nProduct ID to restock: ");
            String productId = scanner.nextLine();
            
            product product = findProductById(productId);
            if (product == null) {
                System.out.println("Product not found!");
                continue;
            }
            
            int quantity = getIntInput("Quantity received: ");
            
            // Add to restock transaction
            restock.addProduct(productId, quantity);
            
            // Update product quantity
            product.setquantity(product.getquantity() + quantity);
            
            System.out.println("Added " + quantity + " units of " + product.getname());
            
            System.out.print("Add more products? (yes/no): ");
            String more = scanner.nextLine();
            if (!more.equalsIgnoreCase("yes")) {
                addingProducts = false;
            }
        }
        
        double totalCost = getDoubleInput("\nTotal cost paid to supplier: $");
        restock.setTotalCost(totalCost);
        
        restock.completeRestock();
        transactions.add(restock);
        
        restock.printRestockReceipt();
        System.out.println("\nRestock completed successfully!");
    }
    
    // ========== SALES MENU ==========
    private static void processSaleMenu() {
        System.out.println("\n--- PROCESS SALE ---");
        
        // Select customer
        System.out.print("Customer ID: ");
        String customerId = scanner.nextLine();
        
        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }
        
        System.out.println("Customer: " + customer.getFullName());
        System.out.println("Loyalty Points: " + customer.getLoyaltyPoints());
        
        // Generate transaction ID
        String transactionId = "TXN" + String.format("%03d", transactions.size() + 1);
        
        Sale sale = new Sale(transactionId, currentPharmacist.getPersonId(), customerId);
        
        double subtotal = 0.0;
        boolean addingProducts = true;
        
        while (addingProducts) {
            System.out.print("\nProduct ID: ");
            String productId = scanner.nextLine();
            
            product product = findProductById(productId);
            if (product == null) {
                System.out.println("Product not found!");
                continue;
            }
            
            System.out.println("Product: " + product.getname());
            System.out.println("Price: $" + product.getprice());
            System.out.println("Available: " + product.getquantity());
            
            // Check if prescription is needed
            if (product instanceof Prescribable) {
                Prescribable prescItem = (Prescribable) product;
                if (prescItem.requiresPrescription()) {
                    System.out.print("Prescription ID required: ");
                    String rxId = scanner.nextLine();
                    prescItem.setPrescriptionId(rxId);
                }
            }
            
            // Check if available
            if (!product.isAvailableForSale()) {
                System.out.println("ERROR: Product not available for sale!");
                if (product instanceof Prescribable) {
                    System.out.println("Reason: Missing prescription or out of stock");
                }
                continue;
            }
            
            // Check expiration
            if (product instanceof Expirable) {
                Expirable exp = (Expirable) product;
                if (exp.isExpired()) {
                    System.out.println("ERROR: Product is EXPIRED! Cannot sell.");
                    continue;
                }
            }
            
            int quantity = getIntInput("Quantity: ");
            
            // Check stock
            if (quantity > product.getquantity()) {
                System.out.println("ERROR: Not enough stock! Only " + 
                                 product.getquantity() + " available.");
                continue;
            }
            
            // Check purchase limit for OTC
            if (product instanceof otcmedicine) {
                otcmedicine otc = (otcmedicine) product;
                if (otc.getPurchaseLimit() > 0 && quantity > otc.getPurchaseLimit()) {
                    System.out.println("ERROR: Purchase limit is " + 
                                     otc.getPurchaseLimit() + " units.");
                    continue;
                }
            }
            
            // Add to sale
            sale.addProduct(productId, quantity);
            subtotal += product.getprice() * quantity;
            
            // Decrease inventory
            product.setquantity(product.getquantity() - quantity);
            
            System.out.println("Added to sale: " + quantity + " x " + 
                             product.getname() + " = $" + (product.getprice() * quantity));
            
            System.out.print("\nAdd more products? (yes/no): ");
            String more = scanner.nextLine();
            if (!more.equalsIgnoreCase("yes")) {
                addingProducts = false;
            }
        }
        
        if (sale.getProductIds().isEmpty()) {
            System.out.println("No products in sale. Cancelled.");
            return;
        }
        
        System.out.println("\n--- SALE SUMMARY ---");
        System.out.println("Subtotal: $" + subtotal);
        
        // Apply discount
        System.out.print("Apply loyalty points discount? (yes/no): ");
        String usePoints = scanner.nextLine();
        
        double discount = 0.0;
        if (usePoints.equalsIgnoreCase("yes")) {
            double maxDiscount = customer.getLoyaltyPoints() / 100.0;  // 100 points = $1
            System.out.println("Available discount: $" + maxDiscount);
            discount = getDoubleInput("Enter discount amount to apply: $");
            
            if (discount > maxDiscount) {
                discount = maxDiscount;
                System.out.println("Maximum discount applied: $" + discount);
            }
            
            // Use points
            customer.useLoyaltyPoints(discount * 100);
            sale.setDiscount(discount);
        }
        
        System.out.print("Payment method (CASH/CARD): ");
        String payment = scanner.nextLine();
        sale.setPaymentMethod(payment);
        
        sale.calculateTotal(subtotal);
        sale.completeSale();
        
        // Add loyalty points (1 point per dollar spent)
        customer.addLoyaltyPoints(sale.getTotalAmount());
        
        // Add to customer history
        customer.addPurchase(transactionId);
        
        // Save transaction
        transactions.add(sale);
        
        // Print receipt
        sale.printReceipt();
        
        System.out.println("\nNew loyalty points balance: " + customer.getLoyaltyPoints());
        System.out.println("Sale completed successfully!");
    }
    
    // ========== CUSTOMER MANAGEMENT MENU ==========
    private static void customerManagementMenu() {
        while (true) {
            System.out.println("\n--- CUSTOMER MANAGEMENT ---");
            System.out.println("1. View All Customers");
            System.out.println("2. Register New Customer");
            System.out.println("3. View Customer Details");
            System.out.println("4. View Purchase History");
            System.out.println("5. Back to Main Menu");
            
            int choice = getIntInput("Enter choice: ");
            
            switch (choice) {
                case 1:
                    viewAllCustomers();
                    break;
                case 2:
                    registerCustomer();
                    break;
                case 3:
                    viewCustomerDetails();
                    break;
                case 4:
                    viewPurchaseHistory();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    private static void viewAllCustomers() {
        System.out.println("\n=== ALL CUSTOMERS ===");
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        
        for (Customer customer : customers) {
            System.out.println(customer);
            System.out.println("---");
        }
    }
    
    private static void registerCustomer() {
        System.out.println("\n--- REGISTER NEW CUSTOMER ---");
        
        System.out.print("Customer ID: ");
        String id = scanner.nextLine();
        
        // Check if exists
        if (findCustomerById(id) != null) {
            System.out.println("Customer ID already exists!");
            return;
        }
        
        System.out.print("Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        Customer customer = new Customer(id, name, phone, email,"alger");
        customers.add(customer);
        
        System.out.println("Customer registered successfully!");
    }
    
    private static void viewCustomerDetails() {
        System.out.print("\nEnter Customer ID: ");
        String id = scanner.nextLine();
        
        Customer customer = findCustomerById(id);
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }
        
        System.out.println("\n" + customer);
    }
    
    private static void viewPurchaseHistory() {
        System.out.print("\nEnter Customer ID: ");
        String id = scanner.nextLine();
        
        Customer customer = findCustomerById(id);
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }
        
        System.out.println("\n=== PURCHASE HISTORY: " + customer.getFullName() + " ===");
        List<String> history = customer.getPurchaseHistory();
        
        if (history.isEmpty()) {
            System.out.println("No purchases yet.");
            return;
        }
        
        for (String txnId : history) {
            Transaction txn = findTransactionById(txnId);
            if (txn != null) {
                System.out.println("\n" + txn);
                System.out.println("---");
            }
        }
    }
    
    // ========== VIEW TRANSACTIONS ==========
    private static void viewAllTransactions() {
        System.out.println("\n=== ALL TRANSACTIONS ===");
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        
        for (Transaction txn : transactions) {
            System.out.println("\n" + txn);
            System.out.println("---");
        }
    }
    
    // ========== HELPER METHODS ==========
    private static product findProductById(String id) {
        for (product product : products) {
            if (product.getid().equals(id)) {
                return product;
            }
        }
        return null;
    }
    
    private static Customer findCustomerById(String id) {
        for (Customer customer : customers) {
            if (customer.getPersonId().equals(id)) {
                return customer;
            }
        }
        return null;
    }
    
    private static Transaction findTransactionById(String id) {
        for (Transaction txn : transactions) {
            if (txn.getTransactionId().equals(id)) {
                return txn;
            }
        }
        return null;
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    
    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}

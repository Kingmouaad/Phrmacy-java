package com.pharmacy.services;

import com.pharmacy.models.products.*;
import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DataService {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path PRODUCTS_FILE = DATA_DIR.resolve("products.txt");
    private static final Path STOCK_FILE = DATA_DIR.resolve("stock.txt");
    private static final Path CUSTOMERS_FILE = DATA_DIR.resolve("customers.txt");
    private static final Path SALES_FILE = DATA_DIR.resolve("sales.txt");
    
    public static void saveAllData(List<product> products, List<Customer> customers, 
                                   List<Transaction> transactions) {
        try {
            if (Files.notExists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
            Files.writeString(PRODUCTS_FILE, buildProductFileContent(products));
            Files.writeString(STOCK_FILE, buildStockFileContent(products));
            Files.writeString(CUSTOMERS_FILE, buildCustomerFileContent(customers));
            Files.writeString(SALES_FILE, buildSalesFileContent(transactions));
        } catch (IOException e) {
            System.out.println("Warning: Unable to write data files: " + e.getMessage());
        }
    }
    
    public static void loadProducts(List<product> products) {
        try {
            if (Files.notExists(PRODUCTS_FILE)) return;
            List<String> lines = Files.readAllLines(PRODUCTS_FILE);

            for (String line : lines) {
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.toUpperCase().startsWith("TYPE") || line.startsWith("No ")) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                String type = parts[0].trim();
                String id = parts[1].trim();
                String name = parts[2].trim();
                double price = 0.0;
                int qty = 0;
                try { price = Double.parseDouble(parts[3].trim()); } catch (NumberFormatException ex) {}
                try { qty = Integer.parseInt(parts[4].trim()); } catch (NumberFormatException ex) {}

                product p = null;
                String lower = type.toLowerCase();
                if (lower.contains("prescription")) {
                    p = new PrescriptionMedicine(id, name, price, qty, "UnknownIngredient", "UnknownForm", "N/A", "UnknownManufacturer");
                } else if (lower.contains("over") || lower.contains("otc")) {
                    p = new otcmedicine(id, name, price, qty, "UnknownIngredient", "UnknownForm", "N/A", "UnknownManufacturer");
                } else if (lower.contains("medical")) {
                    p = new medicaledevice(id, name, price, qty, "GeneralDevice", 0, "UnknownManufacturer");
                } else if (lower.contains("supplement")) {
                    p = new Supplement(id, name, price, qty, "General", "1 serving");
                } else {
                    p = new medicaledevice(id, name, price, qty, "GeneralDevice", 0, "UnknownManufacturer");
                }

                products.add(p);
            }
        } catch (IOException e) {
            System.err.println("Error reading products: " + e.getMessage());
        }
    }

    public static void loadCustomers(List<Customer> customers) {
        try {
            if (Files.notExists(CUSTOMERS_FILE)) return;
            List<String> lines = Files.readAllLines(CUSTOMERS_FILE);
            for (String line : lines) {
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.toUpperCase().startsWith("ID|NAME") || line.startsWith("No ")) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                String id = parts[0].trim();
                String name = parts[1].trim();
                String phone = parts[2].trim();
                String email = parts[3].trim();
                double loyalty = 0.0;
                try { loyalty = Double.parseDouble(parts[4].trim()); } catch (NumberFormatException ex) {}

                Customer c = new Customer(id, name, phone, email, "");
                if (loyalty > 0) c.addLoyaltyPoints(loyalty);
                
                // Load purchase history if available (6th column)
                if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                    String purchaseHistoryStr = parts[5].trim();
                    String[] transactionIds = purchaseHistoryStr.split(",");
                    for (String txnId : transactionIds) {
                        String trimmedTxnId = txnId.trim();
                        if (!trimmedTxnId.isEmpty()) {
                            c.addPurchase(trimmedTxnId);
                        }
                    }
                }
                
                customers.add(c);
            }
        } catch (IOException e) {
            System.err.println("Error reading customers: " + e.getMessage());
        }
    }

    public static void loadStock(List<product> products) {
        try {
            if (Files.notExists(STOCK_FILE)) return;
            List<String> lines = Files.readAllLines(STOCK_FILE);
            for (String line : lines) {
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.toUpperCase().startsWith("ID|QUANTITY") || line.startsWith("No ")) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;
                String id = parts[0].trim();
                int qty = 0;
                try { qty = Integer.parseInt(parts[1].trim()); } catch (NumberFormatException ex) {}
                product p = findProductById(products, id);
                if (p != null) p.setquantity(qty);
            }
        } catch (IOException e) {
            System.err.println("Error reading stock: " + e.getMessage());
        }
    }
    
    private static product findProductById(List<product> products, String id) {
        for (product p : products) {
            if (p.getid().equalsIgnoreCase(id.trim())) return p;
        }
        return null;
    }

    public static void loadSales(List<Transaction> transactions) {
        try {
            if (Files.notExists(SALES_FILE)) return;
            List<String> lines = Files.readAllLines(SALES_FILE);
            if (lines.size() == 1 && lines.get(0).startsWith("No sales")) return;

            for (String line : lines) {
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.toUpperCase().startsWith("ID|TYPE") || line.startsWith("No ")) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;
                String id = parts[0].trim();
                String type = parts[1].trim().toUpperCase();
                double total = 0.0;
                try { total = Double.parseDouble(parts[2].trim()); } catch (NumberFormatException ex) {}
                String status = parts[3].trim();

                Transaction txn = null;
                if (type.contains("SALE")) {
                    txn = new Sale(id, "", "");
                } else if (type.contains("RESTOCK")) {
                    txn = new Restock(id, "", "");
                } else if (type.contains("RETURN")) {
                    txn = new Return(id, "", "", "");
                }
                if (txn != null) {
                    txn.setTotalAmount(total);
                    txn.setStatus(status);
                    transactions.add(txn);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading sales: " + e.getMessage());
        }
    }

    private static String buildProductFileContent(List<product> products) {
        StringBuilder builder = new StringBuilder("TYPE|ID|NAME|PRICE|QTY").append(System.lineSeparator());
        for (product p : products) {
            builder.append(p.getProductType()).append('|')
                   .append(p.getid()).append('|')
                   .append(p.getname()).append('|')
                   .append(String.format("%.2f", p.getprice())).append('|')
                   .append(p.getquantity()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private static String buildStockFileContent(List<product> products) {
        StringBuilder builder = new StringBuilder("ID|QUANTITY|STATUS").append(System.lineSeparator());
        for (product p : products) {
            String status = p.getquantity() > 20 ? "Healthy" :
                            p.getquantity() > 5 ? "Moderate" : "Low";
            builder.append(p.getid()).append('|')
                   .append(p.getquantity()).append('|')
                   .append(status).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private static String buildCustomerFileContent(List<Customer> customers) {
        StringBuilder builder = new StringBuilder("ID|NAME|PHONE|EMAIL|LOYALTY|PURCHASE_HISTORY").append(System.lineSeparator());
        for (Customer c : customers) {
            builder.append(c.getPersonId()).append('|')
                   .append(c.getFullName()).append('|')
                   .append(c.getPhoneNumber()).append('|')
                   .append(c.getEmail()).append('|')
                   .append(c.getLoyaltyPoints()).append('|');
            
            // Save purchase history as comma-separated transaction IDs
            List<String> purchaseHistory = c.getPurchaseHistory();
            if (purchaseHistory != null && !purchaseHistory.isEmpty()) {
                builder.append(String.join(",", purchaseHistory));
            } else {
                builder.append(""); // Empty if no purchase history
            }
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    private static String buildSalesFileContent(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "No sales recorded yet." + System.lineSeparator();
        }
        StringBuilder builder = new StringBuilder("ID|TYPE|TOTAL|STATUS").append(System.lineSeparator());
        for (Transaction txn : transactions) {
            builder.append(txn.getTransactionId()).append('|')
                   .append(txn.getTransactionType()).append('|')
                   .append(String.format("%.2f", txn.getTotalAmount())).append('|')
                   .append(txn.getStatus()).append(System.lineSeparator());
        }
        return builder.toString();
    }
    
    public static boolean dataFilesExist() {
        return Files.exists(PRODUCTS_FILE) || Files.exists(CUSTOMERS_FILE) || 
               Files.exists(STOCK_FILE) || Files.exists(SALES_FILE);
    }
}


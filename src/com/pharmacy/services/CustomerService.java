package com.pharmacy.services;

import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;
import java.util.List;
import java.util.Scanner;

public class CustomerService {
    private List<Customer> customers;
    private List<Transaction> transactions;
    private Scanner scanner;
    
    public CustomerService(List<Customer> customers, List<Transaction> transactions, Scanner scanner) {
        this.customers = customers;
        this.transactions = transactions;
        this.scanner = scanner;
    }
    
    public void viewAllCustomers() {
        System.out.println("\nALL CUSTOMERS (" + customers.size() + ")");
        for (Customer c : customers) {
            System.out.println(c);
            System.out.println("─".repeat(50));
        }
    }
    
    public void registerCustomer() {
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
    
    public void viewCustomerDetails() {
        System.out.print("\nCustomer ID: ");
        Customer c = findCustomerById(scanner.nextLine());
        
        if (c == null) {
            System.out.println("Not found!");
        } else {
            System.out.println("\n" + c);
        }
    }
    
    public void viewPurchaseHistory() {
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
    
    public Customer findCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getPersonId().equalsIgnoreCase(id.trim())) return c;
        }
        return null;
    }
    
    private Transaction findTransactionById(String id) {
        for (Transaction t : transactions) {
            if (t.getTransactionId().equalsIgnoreCase(id.trim())) return t;
        }
        return null;
    }
}


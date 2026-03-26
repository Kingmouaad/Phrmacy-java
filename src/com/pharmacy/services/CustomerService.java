package com.pharmacy.services;

import com.pharmacy.db.CustomerDAO;
import com.pharmacy.db.SaleDAO;
import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CustomerService {
    private CustomerDAO customerDAO;
    private SaleDAO saleDAO;
    private Scanner scanner;
    
    public CustomerService(Scanner scanner) {
        this.customerDAO = new CustomerDAO();
        this.saleDAO = new SaleDAO();
        this.scanner = scanner;
    }
    
    public void viewAllCustomers() {
        try {
            List<Customer> customers = customerDAO.findAll();
            System.out.println("\nALL CUSTOMERS (" + customers.size() + ")");
            for (Customer c : customers) {
                System.out.println(c);
                System.out.println("─".repeat(50));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
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
        
        try {
            customerDAO.insert(new Customer(id, name, phone, email, address));
            System.out.println("Customer registered!");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
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
        String customerId = scanner.nextLine();
        Customer c = findCustomerById(customerId);
        
        if (c == null) {
            System.out.println("Not found!");
            return;
        }
        
        System.out.println("\nPURCHASE HISTORY: " + c.getFullName());
        
        try {
            List<Sale> history = saleDAO.findByCustomer(customerId);
            
            if (history.isEmpty()) {
                System.out.println("No purchases yet.");
                return;
            }
            
            for (Sale sale : history) {
                System.out.println("\n" + sale);
                System.out.println("─".repeat(50));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    public Customer findCustomerById(String id) {
        try {
            return customerDAO.findById(id.trim());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return null;
        }
    }
}

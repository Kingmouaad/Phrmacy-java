package com.pharmacy.services;

import com.pharmacy.db.SaleDAO;
import com.pharmacy.db.ReturnDAO;
import com.pharmacy.exceptions.*;
import com.pharmacy.interfaces.*;
import com.pharmacy.models.products.*;
import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SaleService {
    private SaleDAO saleDAO;
    private ReturnDAO returnDAO;
    private Scanner scanner;
    private Pharmacist currentPharmacist;
    private ProductService productService;
    private CustomerService customerService;
    
    public SaleService(Scanner scanner, Pharmacist currentPharmacist, 
                       ProductService productService, CustomerService customerService) {
        this.saleDAO = new SaleDAO();
        this.returnDAO = new ReturnDAO();
        this.scanner = scanner;
        this.currentPharmacist = currentPharmacist;
        this.productService = productService;
        this.customerService = customerService;
    }
    
    public void processNewSale() {
        System.out.println("\nNEW SALE");
        
        System.out.print("Customer ID: ");
        String custId = scanner.nextLine();
        Customer customer = customerService.findCustomerById(custId);
        
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }
        
        System.out.println(" Customer: " + customer.getFullName());
        System.out.println("Loyalty Points: " + customer.getLoyaltyPoints());
        
        String txnId;
        try {
            txnId = saleDAO.getNextTransactionId();
        } catch (SQLException e) {
            txnId = "TXN" + System.currentTimeMillis();
        }
        
        Sale sale = new Sale(txnId, currentPharmacist.getPersonId(), custId);
        
        double subtotal = 0.0;
        List<String> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        List<Double> unitPrices = new ArrayList<>();
        
        boolean adding = true;
        
        while (adding) {
            System.out.print("\nProduct ID: ");
            String pid = scanner.nextLine();
            try {
                product p = productService.getProductOrThrow(pid);
                System.out.println(" " + p.getname() + " - $" + p.getprice() + " (Stock: " + p.getquantity() + ")");

                if (p instanceof Prescribable) {
                    Prescribable prescribable = (Prescribable) p;
                    if (prescribable.requiresPrescription()) {
                        System.out.print("Prescription ID: ");
                        String rxId = scanner.nextLine().trim();
                        if (rxId.isEmpty()) {
                            throw new InvalidPrescriptionException("Prescription ID is required for " + p.getname());
                        }
                        prescribable.setPrescriptionId(rxId);
                    }
                }

                if (!p.isAvailableForSale() && !(p instanceof PrescriptionMedicine)) {
                    throw new ExpiredProductException("Product '" + p.getname() + "' is not approved for sale.");
                }

                ensureNotExpired(p);

                int qty = getIntInput("Quantity: ");
                validateStockRequest(p, qty);

                if (p instanceof otcmedicine) {
                    otcmedicine otc = (otcmedicine) p;
                    if (otc.getPurchaseLimit() > 0 && qty > otc.getPurchaseLimit()) {
                        throw new InsufficientStockException(
                            "Purchase limit of " + otc.getPurchaseLimit() + " exceeded for " + p.getname());
                    }
                }

                ensureNoDrugInteraction(p, sale);

                sale.addProduct(pid, qty);
                productIds.add(pid);
                quantities.add(qty);
                unitPrices.add(p.getprice());
                
                subtotal += p.getprice() * qty;

                System.out.println(" Added: " + qty + " x " + p.getname() + " = $" + (p.getprice() * qty));
            } catch (ProductNotFoundException | InvalidPrescriptionException |
                     InsufficientStockException | ExpiredProductException |
                     DrugInteractionException e) {
                System.out.println(" Error: " + e.getMessage());
            }

            System.out.print("Add more? (yes/no): ");
            adding = scanner.nextLine().equalsIgnoreCase("yes");
        }
        
        if (productIds.isEmpty()) {
            System.out.println(" No products in sale.");
            return;
        }
        
        // Apply discount
        System.out.println("\nSubtotal: $" + subtotal);
        System.out.print("Use loyalty points? (yes/no): ");
        
        double discount = 0.0;
        double loyaltyPointsToAdd = subtotal; // Assuming 1 point per $1 spent
        double loyaltyPointsUsed = 0.0;
        
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            double maxDiscount = customer.getLoyaltyPoints() / 100.0;
            System.out.println("Available discount: $" + maxDiscount);
            discount = getDoubleInput("Apply discount: $");
            
            if (discount > maxDiscount) discount = maxDiscount;
            loyaltyPointsUsed = discount * 100;
            sale.setDiscount(discount);
        }
        
        System.out.print("Payment method (CASH/CARD): ");
        String payment = scanner.nextLine();
        sale.setPaymentMethod(payment);
        
        sale.calculateTotal(subtotal);
        sale.completeSale();
        
        try {
            // This triggers the JDBC transaction updating sales, items, customers, and stock atomically
            saleDAO.processSale(sale, productIds, quantities, unitPrices, loyaltyPointsToAdd - loyaltyPointsUsed, custId);
            sale.printReceipt();
            System.out.println("\nSale processed and saved to database successfully!");
        } catch (SQLException e) {
            System.out.println("Database transaction failed: " + e.getMessage());
        }
    }
    
    public void processReturn() {
        System.out.println("\nPROCESS RETURN");
        
        System.out.print("Customer ID: ");
        String custId = scanner.nextLine();
        Customer customer = customerService.findCustomerById(custId);
        
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }
        
        System.out.print("Original Sale Transaction ID: ");
        String originalSaleId = scanner.nextLine();
        
        Sale originalSale;
        try {
            originalSale = saleDAO.findById(originalSaleId);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        if (originalSale == null) {
            System.out.println("Sale transaction not found!");
            return;
        }
        
        String returnId = "RET" + System.currentTimeMillis();
        Return returnTxn = new Return(returnId, currentPharmacist.getPersonId(), 
                                      custId, originalSaleId);
        
        System.out.print("Return Reason: ");
        String reason = scanner.nextLine();
        returnTxn.setReason(reason);
        
        double refundTotal = 0.0;
        List<String> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        
        boolean adding = true;
        while (adding) {
            System.out.print("\nProduct ID to return: ");
            String pid = scanner.nextLine();
            try {
                product p = productService.getProductOrThrow(pid);
                int qty = getIntInput("Quantity to return: ");
                if (qty <= 0) {
                    System.out.println("Quantity must be greater than 0.");
                    continue;
                }
                
                returnTxn.addProduct(pid, qty);
                productIds.add(pid);
                quantities.add(qty);
                
                double refund = p.getprice() * qty;
                refundTotal += refund;
                System.out.println("Returning " + qty + " x " + p.getname() + " = $" + refund);
            } catch (ProductNotFoundException e) {
                System.out.println(e.getMessage());
            }
            
            System.out.print("Return more items? (yes/no): ");
            adding = scanner.nextLine().equalsIgnoreCase("yes");
        }
        
        if (productIds.isEmpty()) {
            System.out.println("No items to return.");
            return;
        }
        
        System.out.print("Refund method (CASH/CARD): ");
        String refundMethod = scanner.nextLine();
        returnTxn.setRefundMethod(refundMethod);
        
        returnTxn.calculateRefund(refundTotal);
        returnTxn.completeReturn();
        
        try {
            returnDAO.processReturn(returnTxn, productIds, quantities, refundTotal);
            returnTxn.printReturnReceipt();
            System.out.println("\n Return processed and saved to database successfully!");
        } catch (SQLException e) {
            System.out.println("Database transaction failed: " + e.getMessage());
        }
    }
    
    private void validateStockRequest(product p, int requestedQty) {
        if (requestedQty <= 0) {
            throw new InsufficientStockException("Quantity must be greater than zero.");
        }
        if (requestedQty > p.getquantity()) {
            throw new InsufficientStockException(
                "Requested " + requestedQty + " but only " + p.getquantity() + " units of " + p.getname() + " are available.");
        }
    }

    private void ensureNotExpired(product p) {
        if (p instanceof Expirable) {
            Expirable exp = (Expirable) p;
            if (exp.isExpired()) {
                throw new ExpiredProductException("Product '" + p.getname() + "' is expired and cannot be sold.");
            }
        }
    }

    private void ensureNoDrugInteraction(product candidate, Sale currentSale) {
        if (!(candidate instanceof medicine)) {
            return;
        }
        medicine candidateMed = (medicine) candidate;
        
        for (String pid : currentSale.getProductIds()) {
            product existing = productService.findProductById(pid);
            if (existing instanceof medicine) {
                medicine existingMed = (medicine) existing;
                boolean sameIngredient = existingMed.getActiveIngredient() != null &&
                                         existingMed.getActiveIngredient().equalsIgnoreCase(candidateMed.getActiveIngredient());
                if (!sameIngredient) {
                    continue;
                }
                boolean prescriptionWithOtc =
                    (existing instanceof PrescriptionMedicine && candidate instanceof otcmedicine) ||
                    (existing instanceof otcmedicine && candidate instanceof PrescriptionMedicine);
                if (prescriptionWithOtc) {
                    throw new DrugInteractionException(
                        "Combining " + existing.getname() + " with " + candidate.getname() +
                        " is blocked because they share the active ingredient " + candidateMed.getActiveIngredient() + ".");
                }
            }
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

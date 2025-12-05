package com.pharmacy.services;

import com.pharmacy.exceptions.*;
import com.pharmacy.interfaces.*;
import com.pharmacy.models.products.*;
import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;
import java.util.List;
import java.util.Scanner;

public class SaleService {
    private List<Transaction> transactions;
    private Scanner scanner;
    private Pharmacist currentPharmacist;
    private ProductService productService;
    private CustomerService customerService;
    
    public SaleService(List<product> products, List<Customer> customers, 
                       List<Transaction> transactions, Scanner scanner, 
                       Pharmacist currentPharmacist, ProductService productService,
                       CustomerService customerService) {
        this.transactions = transactions;
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
        
        String txnId = "TXN" + String.format("%03d", transactions.size() + 1);
        Sale sale = new Sale(txnId, currentPharmacist.getPersonId(), custId);
        
        double subtotal = 0.0;
        boolean adding = true;
        
        while (adding) {
            System.out.print("\nProduct ID: ");
            String pid = scanner.nextLine();
            try {
                product p = productService.getProductOrThrow(pid);
                System.out.println(" " + p.getname() + " - $" + p.getprice() + " (Stock: " + p.getquantity() + ")");

                if (p instanceof Prescribable prescribable && prescribable.requiresPrescription()) {
                    System.out.print("Prescription ID: ");
                    String rxId = scanner.nextLine().trim();
                    if (rxId.isEmpty()) {
                        throw new InvalidPrescriptionException("Prescription ID is required for " + p.getname());
                    }
                    prescribable.setPrescriptionId(rxId);
                }

                if (!p.isAvailableForSale() && !(p instanceof PrescriptionMedicine)) {
                    throw new ExpiredProductException("Product '" + p.getname() + "' is not approved for sale.");
                }

                ensureNotExpired(p);

                int qty = getIntInput("Quantity: ");
                validateStockRequest(p, qty);

                if (p instanceof otcmedicine otc && otc.getPurchaseLimit() > 0 && qty > otc.getPurchaseLimit()) {
                    throw new InsufficientStockException(
                        "Purchase limit of " + otc.getPurchaseLimit() + " exceeded for " + p.getname());
                }

                ensureNoDrugInteraction(p, sale);

                sale.addProduct(pid, qty);
                subtotal += p.getprice() * qty;
                p.setquantity(p.getquantity() - qty);

                System.out.println(" Added: " + qty + " x " + p.getname() + " = $" + (p.getprice() * qty));
            } catch (ProductNotFoundException | InvalidPrescriptionException |
                     InsufficientStockException | ExpiredProductException |
                     DrugInteractionException e) {
                System.out.println(" Error: " + e.getMessage());
            }

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
            try {
                product p = productService.getProductOrThrow(pid);
                int qty = getIntInput("Quantity to return: ");
                if (qty <= 0) {
                    System.out.println("Quantity must be greater than 0.");
                    continue;
                }
                returnTxn.addProduct(pid, qty);
                double refund = p.getprice() * qty;
                refundTotal += refund;
                p.setquantity(p.getquantity() + qty);
                System.out.println("Returning " + qty + " x " + p.getname() + " = $" + refund);
            } catch (ProductNotFoundException e) {
                System.out.println(e.getMessage());
            }
            
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
        if (p instanceof Expirable exp && exp.isExpired()) {
            throw new ExpiredProductException("Product '" + p.getname() + "' is expired and cannot be sold.");
        }
    }

    private void ensureNoDrugInteraction(product candidate, Sale currentSale) {
        if (!(candidate instanceof medicine candidateMed)) {
            return;
        }
        for (String pid : currentSale.getProductIds()) {
            product existing = productService.findProductById(pid);
            if (existing instanceof medicine existingMed) {
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
    
    private Transaction findTransactionById(String id) {
        for (Transaction t : transactions) {
            if (t.getTransactionId().equalsIgnoreCase(id.trim())) return t;
        }
        return null;
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


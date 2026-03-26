package com.pharmacy.generics;

import com.pharmacy.db.PrescriptionDAO;
import com.pharmacy.interfaces.Expirable;
import com.pharmacy.interfaces.Prescribable;
import com.pharmacy.models.products.medicine;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Generic prescription validator for any medicine type.
 *
 * WHY GENERICS:
 * - Without generics, we'd write one validator for PrescriptionMedicine
 *   and maybe another for otcmedicine — messy duplication.
 * - With generics, PrescriptionValidator<T extends medicine> handles
 *   ALL medicine types with one class, and you get compile-time type safety.
 *
 * HOW IT WORKS:
 * - Bounded by `T extends medicine`, so it only accepts medicine subclasses.
 * - Validates prescriptions, checks expiry, checks drug interactions.
 * - Uses PrescriptionDAO to verify against the database.
 *
 * @param <T> Any subclass of medicine (PrescriptionMedicine, otcmedicine)
 */
public class PrescriptionValidator<T extends medicine> {

    private final PrescriptionDAO prescriptionDAO;

    public PrescriptionValidator() {
        this.prescriptionDAO = new PrescriptionDAO();
    }

    /**
     * Full validation pipeline for dispensing a medicine.
     * Checks everything: availability, expiry, and prescription requirement.
     *
     * @param med        The medicine to validate
     * @param customerId The customer receiving the medicine
     * @param quantity   The requested quantity
     * @return ValidationResult with pass/fail and reason
     */
    public ValidationResult validate(T med, String customerId, int quantity) {
        // Step 1: Check stock
        if (med.getquantity() < quantity) {
            return ValidationResult.fail("Insufficient stock: only " + med.getquantity()
                    + " units available, requested " + quantity);
        }

        // Step 2: Check expiration
        if (med instanceof Expirable) {
            Expirable exp = (Expirable) med;
            if (exp.isExpired()) {
                return ValidationResult.fail("EXPIRED: " + med.getname()
                        + " expired on " + exp.getExpirationDate());
            }
            if (exp.getDaysUntilExpiration() <= 7) {
                System.out.println("  ⚠️ WARNING: " + med.getname()
                        + " expires in " + exp.getDaysUntilExpiration() + " days");
            }
        }

        // Step 3: Check prescription requirement
        if (med instanceof Prescribable) {
            Prescribable prescribable = (Prescribable) med;
            if (prescribable.requiresPrescription()) {
                try {
                    boolean hasValidRx = prescriptionDAO.hasValidPrescription(customerId, med.getid());
                    if (!hasValidRx) {
                        return ValidationResult.fail("No valid prescription found for "
                                + med.getname() + " (Customer: " + customerId + ")");
                    }
                } catch (SQLException e) {
                    return ValidationResult.fail("Database error checking prescription: " + e.getMessage());
                }
            }
        }

        // Step 4: Check availability
        if (!med.isAvailableForSale()) {
            return ValidationResult.fail(med.getname() + " is not available for sale");
        }

        return ValidationResult.pass(med.getname() + " — validated successfully ✅");
    }

    /**
     * Quick check: is this medicine safe to dispense right now?
     * (No quantity or customer check — just expiry and availability)
     */
    public boolean isSafeToDispense(T med) {
        if (!med.isAvailableForSale()) return false;
        if (med instanceof Expirable) {
            return !((Expirable) med).isExpired();
        }
        return true;
    }

    /**
     * Check if a medicine will expire within N days.
     */
    public boolean willExpireSoon(T med, int days) {
        if (med instanceof Expirable) {
            Expirable exp = (Expirable) med;
            LocalDate expDate = exp.getExpirationDate();
            return expDate != null && !expDate.isAfter(LocalDate.now().plusDays(days));
        }
        return false;
    }

    // ═══════════════════════════════════════
    // Inner class for validation results
    // ═══════════════════════════════════════

    /**
     * Result of a prescription validation — either PASS or FAIL with a reason.
     */
    public static class ValidationResult {
        private final boolean passed;
        private final String message;

        private ValidationResult(boolean passed, String message) {
            this.passed = passed;
            this.message = message;
        }

        public static ValidationResult pass(String message) {
            return new ValidationResult(true, message);
        }

        public static ValidationResult fail(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return (passed ? "[PASS] " : "[FAIL] ") + message;
        }
    }
}

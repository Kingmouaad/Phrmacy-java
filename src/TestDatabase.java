import com.pharmacy.db.*;
import com.pharmacy.models.products.*;
import com.pharmacy.models.persons.*;
import com.pharmacy.models.transactions.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Quick integration test for the database layer.
 * Verifies: connection, schema init, CRUD, transactions, and queries.
 */
public class TestDatabase {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  PHARMACY DATABASE INTEGRATION TEST");
        System.out.println("===========================================\n");

        // 1. Test connection
        test("Database Connection", () -> {
            DatabaseConnection db = DatabaseConnection.getInstance();
            assert db.getConnection() != null : "Connection is null";
        });

        // 2. Initialize schema
        test("Schema Initialization", () -> {
            DatabaseConnection db = DatabaseConnection.getInstance();
            db.initializeDatabase();
            assert db.isInitialized() : "Database not initialized";
        });

        // 3. Test UserDAO - Authentication
        test("User Authentication (valid)", () -> {
            UserDAO userDAO = new UserDAO();
            Pharmacist p = userDAO.authenticate("admin", "admin123");
            assert p != null : "Authentication failed for valid user";
            assert p.getFullName().equals("Benmalti Mouaad") : "Wrong name: " + p.getFullName();
            assert p.getAccessLevel() == 3 : "Wrong access level";
        });

        test("User Authentication (invalid)", () -> {
            UserDAO userDAO = new UserDAO();
            Pharmacist p = userDAO.authenticate("admin", "wrongpassword");
            assert p == null : "Should have failed for invalid password";
        });

        // 4. Test ProductDAO - Read
        test("Product FindAll", () -> {
            ProductDAO productDAO = new ProductDAO();
            List<product> products = productDAO.findAll();
            assert products.size() == 17 : "Expected 17 products, got " + products.size();
        });

        test("Product FindById", () -> {
            ProductDAO productDAO = new ProductDAO();
            product p = productDAO.findById("MED001");
            assert p != null : "Product MED001 not found";
            assert p.getname().equals("Amoxicillin 500mg") : "Wrong name: " + p.getname();
            assert p instanceof PrescriptionMedicine : "Wrong type: " + p.getClass().getSimpleName();
        });

        test("Product Type Mapping", () -> {
            ProductDAO productDAO = new ProductDAO();
            product otc = productDAO.findById("OTC001");
            product dev = productDAO.findById("DEV001");
            product sup = productDAO.findById("SUP001");
            assert otc instanceof otcmedicine : "OTC001 should be otcmedicine";
            assert dev instanceof medicaledevice : "DEV001 should be medicaledevice";
            assert sup instanceof Supplement : "SUP001 should be Supplement";
        });

        // 5. Test ProductDAO - Expiry Alert
        test("Expiring Products Query (within 30 days)", () -> {
            ProductDAO productDAO = new ProductDAO();
            List<product> expiring = productDAO.findExpiringWithin(30);
            System.out.println("    Products expiring within 30 days: " + expiring.size());
            // We know some products have near-expiry dates in sample data
        });

        // 6. Test ProductDAO - Low Stock
        test("Low Stock Query", () -> {
            ProductDAO productDAO = new ProductDAO();
            List<product> lowStock = productDAO.findLowStock();
            System.out.println("    Low stock products: " + lowStock.size());
            assert lowStock.size() > 0 : "Should have at least 1 low stock item (MED005 has 3 units)";
        });

        // 7. Test CustomerDAO
        test("Customer FindAll", () -> {
            CustomerDAO customerDAO = new CustomerDAO();
            List<Customer> customers = customerDAO.findAll();
            assert customers.size() == 5 : "Expected 5 customers, got " + customers.size();
        });

        test("Customer FindById", () -> {
            CustomerDAO customerDAO = new CustomerDAO();
            Customer c = customerDAO.findById("CUS001");
            assert c != null : "Customer CUS001 not found";
            assert c.getFullName().equals("Karim Boudiaf") : "Wrong name";
        });

        // 8. Test InteractionDAO
        test("Drug Interaction Check (positive)", () -> {
            InteractionDAO interactionDAO = new InteractionDAO();
            Object[] result = interactionDAO.checkInteraction("Aspirin", "Ibuprofen");
            assert result != null : "Should find Aspirin-Ibuprofen interaction";
            assert "HIGH".equals(result[0]) : "Severity should be HIGH";
        });

        test("Drug Interaction Check (negative)", () -> {
            InteractionDAO interactionDAO = new InteractionDAO();
            Object[] result = interactionDAO.checkInteraction("Paracetamol", "Vitamin C");
            assert result == null : "Should not find interaction";
        });

        // 9. Test PrescriptionDAO
        test("Prescription Validation", () -> {
            PrescriptionDAO prescDAO = new PrescriptionDAO();
            boolean valid = prescDAO.hasValidPrescription("CUS001", "MED001");
            assert valid : "CUS001 should have valid prescription for MED001";
        });

        // 10. Test SaleDAO - Stats
        test("Sales Count", () -> {
            SaleDAO saleDAO = new SaleDAO();
            List<Sale> sales = saleDAO.findAll();
            assert sales.size() == 5 : "Expected 5 sales, got " + sales.size();
        });

        test("Sales By Customer", () -> {
            SaleDAO saleDAO = new SaleDAO();
            List<Sale> sales = saleDAO.findByCustomer("CUS001");
            assert sales.size() == 2 : "CUS001 should have 2 sales, got " + sales.size();
        });

        // Done
        System.out.println("\n===========================================");
        System.out.println("  RESULTS: " + passed + " passed, " + failed + " failed");
        System.out.println("===========================================");

        // Clean up
        DatabaseConnection.getInstance().closeConnection();
        
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, TestRunnable test) {
        try {
            test.run();
            System.out.println("  [PASS] " + name);
            passed++;
        } catch (AssertionError e) {
            System.out.println("  [FAIL] " + name + " — " + e.getMessage());
            failed++;
        } catch (Exception e) {
            System.out.println("  [FAIL] " + name + " — Exception: " + e.getMessage());
            failed++;
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Exception;
    }
}

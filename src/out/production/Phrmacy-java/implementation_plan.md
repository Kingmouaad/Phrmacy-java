# Phase 1: Database Layer Implementation

Replacing [.txt](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/data/sales.txt) file persistence with SQLite (JDBC) database. This is the foundation that all other S2 features (GUI, data structures) will build upon.

## User Review Required

> [!IMPORTANT]
> **Database choice: SQLite** — No server installation needed. The `sqlite-jdbc` JAR is added to the classpath directly. If you prefer **MySQL**, let me know and I'll adjust the plan.

> [!WARNING]
> The existing [DataService.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/services/DataService.java) with [.txt](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/data/sales.txt) files will be preserved but superseded by DAO classes. We keep it so S1 features still work during transition.

## Proposed Changes

### Database Schema

#### [NEW] [schema.sql](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/schema.sql)

8 tables covering all domain entities:

| Table | Purpose |
|-------|---------|
| `users` | Pharmacist login authentication (username, password hash, pharmacist_id) |
| `products` | All product types with a `product_type` discriminator column |
| [stock](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/models/transactions/Restock.java#5-87) | Current stock levels, min_threshold for reorder alerts |
| `customers` | Customer data + loyalty points + allergens (comma-separated) |
| `sales` | Sale transactions with customer/pharmacist references |
| `sale_items` | Individual items in each sale (product_id, quantity, unit_price) |
| `prescriptions` | Prescription records linked to customers and products |
| `interactions` | Drug interaction pairs (drug_a, drug_b, severity, description) |

Includes `INSERT` statements with sample data for demo.

---

### Database Connection Layer

#### [NEW] [DatabaseConnection.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/DatabaseConnection.java)

Singleton pattern JDBC connection manager:
- `getConnection()` — returns SQLite connection
- `closeConnection()` — cleanup
- Auto-creates DB file at `pharmacy.db`
- Runs `schema.sql` on first launch if tables don't exist

---

### Data Access Objects (DAO Pattern)

#### [NEW] [ProductDAO.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/ProductDAO.java)

- `insert(product p)` — INSERT with PreparedStatement
- `findById(String id)` — SELECT by ID
- `findAll()` — SELECT all products
- [update(product p)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/services/ProductService.java#112-155) — UPDATE product fields
- [delete(String id)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/services/ProductService.java#156-179) — DELETE by ID
- `findByName(String query)` — search by name
- `findExpiringWithin(int days)` — expiry alert query

#### [NEW] [CustomerDAO.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/CustomerDAO.java)

- `insert(Customer c)` — INSERT
- `findById(String id)` — SELECT by ID
- `findAll()` — SELECT all
- [update(Customer c)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/services/ProductService.java#112-155) — UPDATE
- `updateLoyaltyPoints(String id, double pts)` — UPDATE loyalty
- [delete(String id)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/services/ProductService.java#156-179) — DELETE

#### [NEW] [SaleDAO.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/SaleDAO.java)

- `insertSale(Sale s)` — **Uses JDBC transaction (commit/rollback)**: inserts into `sales` + `sale_items` + updates [stock](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/models/transactions/Restock.java#5-87) atomically
- `findById(String id)` — SELECT with JOIN to sale_items
- `findAll()` — all sales
- `findByCustomer(String customerId)` — purchase history
- `findByDateRange(LocalDate from, LocalDate to)` — date-filtered sales
- `getTodaySalesCount()` — dashboard stat

#### [NEW] [PrescriptionDAO.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/PrescriptionDAO.java)

- `insert(...)` — INSERT prescription record
- `findByCustomer(String customerId)` — active prescriptions
- `findByProduct(String productId)` — prescriptions for a drug
- `isValid(String prescriptionId)` — check validity

#### [NEW] [InteractionDAO.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/InteractionDAO.java)

- `insert(String drugA, String drugB, String severity, String desc)` — add interaction
- `checkInteraction(String drugA, String drugB)` — check if two drugs interact
- `findAllForDrug(String drugName)` — all interactions for a given drug
- `findAll()` — load all interactions

#### [NEW] [UserDAO.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/UserDAO.java)

- `authenticate(String username, String password)` — login validation
- `insert(String username, String password, String pharmacistId)` — register user
- `findByUsername(String username)` — find user

---

### SQLite JDBC Driver

#### [NEW] SQLite JDBC JAR

We need the `sqlite-jdbc-3.45.1.0.jar` downloaded into a `lib/` folder. Compilation and run commands will reference it via classpath.

---

## Verification Plan

### Automated Tests

Since this is a raw Java project with no test framework, I'll create a simple **test main class**:

#### [NEW] `TestDatabase.java` — verifies:
1. Database connection opens successfully
2. Tables are created from schema
3. Product CRUD operations work
4. Customer CRUD operations work
5. Sale insertion with JDBC transaction works (commit + rollback test)
6. Expiry alert query returns correct results
7. Interaction check query works
8. Authentication works

**Run command:**
```
javac -cp "lib/sqlite-jdbc-3.45.1.0.jar;src" -d out src/com/pharmacy/db/*.java src/com/pharmacy/models/**/*.java src/com/pharmacy/interfaces/*.java src/com/pharmacy/exceptions/*.java src/TestDatabase.java
java -cp "lib/sqlite-jdbc-3.45.1.0.jar;out" TestDatabase
```

### Manual Verification
1. After running `TestDatabase`, check that `pharmacy.db` file is created
2. Open `pharmacy.db` with any SQLite viewer to confirm tables and sample data exist
3. Verify the console output shows all CRUD operations passing

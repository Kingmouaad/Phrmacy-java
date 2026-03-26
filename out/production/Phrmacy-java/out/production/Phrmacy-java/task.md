# Pharmacy Management System — S2 Extension Tasks

## Phase 1: Database Layer (JDBC)
- [/] Create `schema.sql` (tables + sample data)
- [ ] Create `DatabaseConnection.java` (connection manager)
- [ ] Create `ProductDAO.java` (CRUD for products)
- [ ] Create `CustomerDAO.java` (CRUD for customers)
- [ ] Create `SaleDAO.java` (CRUD for sales with JDBC transaction)
- [ ] Create `StockDAO.java` (stock operations)
- [ ] Create `PrescriptionDAO.java` (prescription operations)
- [ ] Create `InteractionDAO.java` (drug interactions)
- [ ] Create `UserDAO.java` (authentication)
- [ ] Update services to use DAOs instead of file persistence
- [ ] Verify database layer works

## Phase 2: Complex Data Structures
- [ ] HashMap for product catalog
- [ ] TreeMap for expiration tracking
- [ ] LinkedList for sales history
- [ ] HashSet for allergens per customer
- [ ] JGraphT drug interaction graph

## Phase 3: Generics
- [ ] StockManager<T extends product>
- [ ] PrescriptionValidator<T extends medicine>
- [ ] Generic sell() method

## Phase 4: Reflection
- [ ] ObjectInspector utility

## Phase 5: GUI (JavaFX/Swing)
- [ ] Login screen
- [ ] Main dashboard
- [ ] Sales terminal
- [ ] Stock dashboard
- [ ] Customer management
- [ ] Prescription management
- [ ] Statistics charts

## Phase 6: Integration & Polish
- [ ] Wire GUI ↔ Services ↔ DAOs
- [ ] README.md update
- [ ] Final testing

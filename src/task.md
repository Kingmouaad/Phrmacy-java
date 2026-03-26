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
- [x] Update services to use DAOs instead of file persistence
- [x] Verify database layer works

## Phase 2: Complex Data Structures
- [x] HashMap for product catalog
- [x] TreeMap for expiration tracking
- [x] LinkedList for sales history
- [x] HashSet for allergens per customer
- [x] JGraphT drug interaction graph

## Phase 3: Generics
- [x] StockManager<T extends product>
- [x] PrescriptionValidator<T extends medicine>
- [x] Generic sell() method

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

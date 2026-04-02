# 📊 Pharmacy Management System — Comprehensive Vibe Coder Progress Report

---

*This is the complete, detailed log of how we evolved the Pharmacy Management System from a basic Phase 1 concept into a fully-featured, GUI-driven Phase 6 app with databases, dynamic data structures, generics, and reflection.*

## 🗺️ Master Blueprints (MD Files)

Before writing any code, we laid down the architecture to keep ourselves organized:
- **[task.md](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/task.md)**: Our master checklist tracking every sub-feature across all 6 phases.
- **[implementation_plan.md](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/implementation_plan.md)**: The technical blueprint for how the Database and Data Access Objects (DAOs) wire together.
- **[README.md](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/README.md)**: The instruction manual for end-users on how to compile and run the application.

---

## 🗄️ Phase 1: Database Layer (JDBC) — 100% COMPLETE ✅

### What We Did
We threw away the old text-file persistence system (`data.txt`) and hooked the application up to a real relational database using **SQLite** and **JDBC**.

### Why We Did It (Vibe Check)
Text files are terrible for storing app data. If the app crashes while writing to `products.txt`, the file corrupts, and the pharmacy loses its inventory. Databases give us structured tables, lightning-fast SQL queries, and most importantly: **Transactions**. If a pharmacist sells a drug, we need to (1) log the sale, (2) deduct the stock, and (3) update loyalty points. If step 3 fails, a database transaction can "roll back" steps 1 and 2 automatically so our data doesn't get out of sync.

### Deep Dive: What Was Built
1. **[schema.sql](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/schema.sql)**: We created a 12-table layout. We have a `users` table, a `products` table, a `sales` table, and even a joined `sale_items` table so one transaction can contain multiple drugs. 
2. **DatabaseConnection.java (Singleton)**: This is our bouncer. It ensures the whole app only opens *one* connection to the SQLite file, preventing "database is locked" errors.
3. **The 9 DAOs**: We built Data Access Objects ([ProductDAO](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/ProductDAO.java#15-412), [CustomerDAO](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/CustomerDAO.java#13-202), etc.). These are the middlemen. The rest of the Java app doesn't write SQL queries; it just calls `productDAO.findById("MED001")`, and the DAO handles the messy SQL translation.
4. **JDBC Transactions in [SaleDAO](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/SaleDAO.java#16-295)**: We used `connection.setAutoCommit(false)` to bundle big sales into a single atomic operation.

### Core Functions That Do the Heavy Lifting

| Function | File | What It Does | Key Concept |
|---|---|---|---|
| [getInstance()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/DatabaseConnection.java#47-52) | `DatabaseConnection` | Returns the ONE shared DB connection. Uses `synchronized` + null-check to guarantee only one instance exists across the entire app. | **Singleton Pattern** — prevents "database is locked" errors |
| [initializeDatabase()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/DatabaseConnection.java#78-122) | `DatabaseConnection` | Reads `schema.sql`, splits it into individual statements (respecting quoted strings), and executes each one. Creates all 12 tables + sample data. | **Schema bootstrapping** — uses custom `splitSqlStatements()` parser |
| [insert(product)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/ProductDAO.java#26-103) | `ProductDAO` | Inserts a product using `PreparedStatement` with 17 parameters. Uses `instanceof` checks to set medicine/device/supplement-specific columns. | **Polymorphic persistence** — one INSERT handles 4 product types |
| [mapResultSetToProduct(ResultSet)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/ProductDAO.java#329-388) | `ProductDAO` | Reads a SQL row and constructs the correct Java subclass (`PrescriptionMedicine`, `otcmedicine`, `medicaledevice`, `Supplement`) based on the `product_type` column. | **Factory Method** — reverse-maps DB rows to polymorphic objects |
| [insertWithStock(product, qty, min)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/ProductDAO.java#108-132) | `ProductDAO` | Wraps `insert()` + stock INSERT in a **single transaction** (`setAutoCommit(false)`). If stock insert fails, the product insert is rolled back. | **JDBC Transaction** — atomicity across two tables |
| [findExpiringWithin(days)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/ProductDAO.java#255-272) | `ProductDAO` | Uses `date('now', '+N days')` SQL to find products expiring within N days. Critical for the dashboard alert system. | **SQL date arithmetic** — avoids Java-side filtering |
| [processSale(...)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/SaleDAO.java#34-114) | `SaleDAO` | The **most critical function** in the system. Atomically: (1) inserts sale record, (2) inserts each sale item, (3) deducts stock with `WHERE quantity >= ?` safety check, (4) awards loyalty points. If ANY step fails → `connection.rollback()`. | **4-step JDBC Transaction** with rollback safety |
| [getTodaySalesCount()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/SaleDAO.java#195-205) / [getTodaysRevenue()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/SaleDAO.java#210-221) | `SaleDAO` | SQL aggregate queries (`COUNT`, `SUM`) filtered by today's date. Powers the Dashboard stat cards. | **Aggregate SQL queries** for real-time stats |

---

## 🧱 Phase 2: Complex Data Structures — 100% COMPLETE ✅

### What We Did
We added advanced in-memory Java data structures (`HashMap`, `TreeMap`, `LinkedList`, `HashSet`, and a `JGraphT` network) to make caching and querying instant without constantly hammering the SQLite database.

### Why We Did It (Vibe Check)
Hitting a database for every single click is slow. If we want to check if a product exists, we shouldn't ask the DB; we should ask an in-memory `HashMap` which returns the answer instantly in $O(1)$ time. Data structures are like choosing the right tool for the job.

### Deep Dive: What Was Built
1. **[ProductCatalog](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ProductCatalog.java#26-144) (`HashMap<String, product>`)**:
   - **Why HashMap?** Fast lookups. You give it an ID (`"MED123"`), it hashes it, and instantly points to the [product](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/models/products/product.java#5-65) object in memory. $O(1)$ speed.
2. **[ExpirationTracker](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ExpirationTracker.java#26-166) (`TreeMap<LocalDate, List<product>>`)**:
   - **Why TreeMap?** It automatically sorts keys. When we ask "what expires in the next 30 days?", the `TreeMap` does a hyper-fast range query without looking at the whole inventory.
3. **[SalesHistory](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/SalesHistory.java#25-132) (`LinkedList<Sale>`)**:
   - **Why LinkedList?** We add new sales constantly to the front/end of the ledger. Arrays would have to shift all elements, but a `LinkedList` just updates a pointer instantly.
4. **[Customer](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/models/persons/Customer.java#25-147) allergens (`HashSet<String>`)**:
   - **Why HashSet?** When a customer buys a drug, we need to check if `"Penicillin"` is in their allergy list. A `HashSet` does this instantly, whereas a normal list would require scanning every entry.
5. **[DrugInteractionGraph](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/DrugInteractionGraph.java#32-191) (JGraphT)**:
   - **Why a Graph?** Drugs are vertices (dots), and interactions are edges (lines connecting them). If someone buys Med A and Med B, we check if an edge exists between them. If yes, we block the sale to save a life.

### Core Functions That Do the Heavy Lifting

| Function | File | What It Does | Key Concept |
|---|---|---|---|
| [getById(productId)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ProductCatalog.java#57-59) | `ProductCatalog` | Calls `catalog.get(id.toUpperCase())` — instant O(1) lookup by hashing the product ID. The entire reason we use HashMap. | **HashMap O(1) lookup** |
| [searchByName(query)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ProductCatalog.java#110-115) | `ProductCatalog` | Uses Java Streams to filter the HashMap values by partial name match. Faster than a DB query because it searches in-memory. | **Stream API** + in-memory filtering |
| [loadFromDatabase()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ProductCatalog.java#41-52) | `ProductCatalog` | Clears the HashMap and reloads all products from DB. Called once at startup to hydrate the cache. | **Cache warm-up pattern** |
| [getExpiringWithin(days)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ExpirationTracker.java#94-104) | `ExpirationTracker` | Uses `TreeMap.subMap(today, deadline)` — a **range query** that returns only products expiring between now and N days. No full scan. | **TreeMap range query** — O(log n) |
| [getExpiredProducts()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ExpirationTracker.java#80-88) | `ExpirationTracker` | Uses `TreeMap.headMap(today)` to grab all entries with dates BEFORE today. Extremely efficient. | **TreeMap headMap** — sorted subview |
| [addProduct(product)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/ExpirationTracker.java#49-57) | `ExpirationTracker` | Uses `computeIfAbsent()` — if the date key doesn't exist, create a new list; then add the product. One-liner instead of 5 lines. | **Map.computeIfAbsent()** — functional Java |
| [checkInteraction(drugA, drugB)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/DrugInteractionGraph.java#111-116) | `DrugInteractionGraph` | O(1) edge lookup: `graph.getEdge(drugA, drugB)`. Returns an `InteractionEdge` with severity & description, or null if safe. | **Graph edge query** — instant interaction check |
| [checkAgainstList(newDrug, currentDrugs)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/DrugInteractionGraph.java#122-133) | `DrugInteractionGraph` | Loops through a patient's current medications and checks each pair against the graph. Returns ALL conflicts. | **Multi-drug safety check** |
| [getInteractingDrugs(drugName)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/datastructures/DrugInteractionGraph.java#138-148) | `DrugInteractionGraph` | Gets ALL neighbors of a vertex — every drug that interacts with the given one. | **Graph neighbor traversal** |

---

## 🔧 Phase 3: Generics — 100% COMPLETE ✅

### What We Did
We built master classes using `<T>` that can handle *any* type of product (Medicines, Devices, Supplements) without duplicating the code three times.

### Why We Did It (Vibe Check)
Without generics, if you want a system to check stock, you have to write `checkMedicineStock()`, `checkDeviceStock()`, and `checkSupplementStock()`. If you find a bug, you have to fix it three times. Generics (`<T>`) let us write the logic *once*. The compiler guarantees the types are safe, so we get no `ClassCastExceptions` at runtime.

### Deep Dive: What Was Built
1. **`StockManager<T extends product>`**:
   - Because it `extends product`, Java knows whatever `T` is, it has a `.getQuantity()` method. We wrote *one* [getLowStock()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/StockManager.java#80-93) method that works flawlessly for syringes, vitamins, and painkillers alike.
2. **`PrescriptionValidator<T extends medicine>`**:
   - This validates prescriptions, so we restrict it to *only* subclasses of [medicine](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/models/products/medicine.java#7-96). If you try to pass a `medicaledevice` through this validator, the compiler stops you before the app even runs.
3. **[PharmacyOperations](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/PharmacyOperations.java#27-165) & `Receipt<T>`**:
   - The master [sell()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/PharmacyOperations.java#35-115) method takes `T item`. No matter what you sell, it returns a `Receipt<T>`. This "preserves the type" so you know exactly what you sold later without guessing.

### Core Functions That Do the Heavy Lifting

| Function | File | What It Does | Key Concept |
|---|---|---|---|
| [getLowStock(threshold)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/StockManager.java#84-92) | `StockManager<T>` | Iterates `List<T>` and returns products below the threshold. Works identically for medicines, devices, and supplements — zero code duplication. | **Bounded type parameter** `<T extends product>` |
| [getExpiringSoon(days)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/StockManager.java#118-131) | `StockManager<T>` | Checks if each `T` implements `Expirable` interface at runtime. If yes, compares expiry date. Demonstrates generics + interface checking together. | **Runtime type check** inside generics |
| [restock(productId, qty)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/StockManager.java#136-147) | `StockManager<T>` | Finds product by ID (`findById` returns `T`), updates in-memory quantity, then persists to DB via `productDAO.updateStock()`. | **Generic + DB sync** |
| [getTotalStockValue()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/StockManager.java#152-158) | `StockManager<T>` | Sums `price × quantity` for all products of type T. The compiler knows T has `.getprice()` and `.getquantity()` because T extends product. | **Type-safe computation** |
| [sell(T item, qty, customer)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/PharmacyOperations.java#52-114) | `PharmacyOperations` | The **star function** of Phase 3. Runs 6 validation checks: (1) availability, (2) stock, (3) expiry via `Expirable`, (4) prescription via `Prescribable`, (5) OTC purchase limit, (6) deducts stock + awards loyalty. Returns `Receipt<T>`. | **Multiple bounded generics** + **multi-interface validation** |
| [Receipt.print()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/PharmacyOperations.java#145-156) | `Receipt<T>` | Prints a formatted receipt. Because `T` preserves the exact type, `receipt.getProduct()` returns the specific subclass (not just `product`). | **Type preservation** with generic class |

---

## 🪞 Phase 4: Reflection — 100% COMPLETE ✅

### What We Did
We built an [ObjectInspector](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#24-329) utility that can "X-Ray" Java objects while the app is running to analyze their internals, modify private fields, and call methods dynamically.

### Why We Did It (Vibe Check)
Normally in Java, `private` means private. You can't touch it. But what if you are building an admin debugging panel or a deep object-comparison tool? Reflection breaks the rules. It lets the code study *itself* at runtime. It's powerful, slightly dangerous, and incredibly useful for advanced system diagnostics.

### Deep Dive: What Was Built
1. **[inspect(Object obj)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#26-44)**: Takes any random object, uses `.getClass()`, and prints out every field, the raw current value, method signatures, and constructors.
2. **Bypassing Access Controls**: We used `field.setAccessible(true)` which literally turns off Java's `private` visibility rules for that line of code, allowing us to read/write restricted data dynamically.
3. **Dynamic Invocation ([invokeMethod](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#213-239))**: You can pass the string `"getFullName"`, and reflection will find that method on the object and run it dynamically without compile-time binding.
4. **[compareObjects(obj1, obj2)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#240-283)**: A custom debugger tool that goes field-by-field between two instances of a class and highlights exactly what values differ between them.

### Core Functions That Do the Heavy Lifting

| Function | File | What It Does | Key Concept |
|---|---|---|---|
| [inspect(Object obj)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#30-43) | `ObjectInspector` | Entry point. Calls `obj.getClass()` to get the `Class` object, then delegates to `printClassInfo()`, `printFields()`, `printMethods()`, `printConstructors()`, and `printInterfaces()`. | **`Class<?>` introspection** |
| [printFields(obj, clazz)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#64-106) | `ObjectInspector` | Uses `getAllFields()` to collect fields from the ENTIRE inheritance chain. For each field: calls `setAccessible(true)` to bypass `private`, then reads the live value with `field.get(obj)`. Handles Collections, Maps, nulls. | **`field.setAccessible(true)`** — bypasses private |
| [getFieldValue(obj, fieldName)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#181-193) | `ObjectInspector` | Dynamically reads ANY field by name (even private). Searches up the superclass chain via `findField()`. | **Dynamic field access** |
| [setFieldValue(obj, fieldName, value)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#199-211) | `ObjectInspector` | Dynamically WRITES to any field (even private). Uses `field.set(obj, value)`. Dangerous but powerful for admin debugging. | **Dynamic field mutation** |
| [invokeMethod(obj, methodName, args)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#216-238) | `ObjectInspector` | Finds a method by name + arg count, calls `setAccessible(true)`, then `method.invoke(obj, args)`. Searches both declared and inherited methods. | **Dynamic method dispatch** |
| [compareObjects(obj1, obj2)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#244-282) | `ObjectInspector` | Gets all fields from both objects, reads each pair of values, and prints `≠` for any mismatches. Uses `Objects.equals()` for null-safe comparison. | **Deep object comparison** via reflection |
| [getAllFields(clazz)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/reflection/ObjectInspector.java#291-299) | `ObjectInspector` | Walks the entire `getSuperclass()` chain up to `Object.class`, collecting `getDeclaredFields()` from each level. Essential because `getDeclaredFields()` only returns the current class's fields. | **Class hierarchy traversal** |

---

## 🖥️ Phase 5: GUI (Java Swing) — 100% COMPLETE ✅

### What We Did
We threw away the old text-based command prompt menu ([Main.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/Main.java)) and replaced it with a professional, dark-mode graphical user interface.

### Why We Did It (Vibe Check)
Pharmacists don't type `1 to add product, 2 to sell`. They need a fast, mouse-driven dashboard with real-time tables, search bars, and pop-up alerts. A graphical interface makes the app actually usable in the real world. Real software has buttons.

### Deep Dive: What Was Built
1. **[PharmacyTheme](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/PharmacyTheme.java#10-187)**: We didn't just throw standard grey Java windows up. We built a custom theme class defining a sleek dark-mode palette (`BG_DARK`, `ACCENT_GREEN`), custom styled borders, and factory methods ([createButton()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/PharmacyTheme.java#44-73), [createTextField()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/PharmacyTheme.java#74-101)) so the whole app looks unified and modern.
2. **[LoginFrame](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/LoginFrame.java#14-163)**: A sleek gradient window that wires into our Phase 1 [UserDAO](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/db/UserDAO.java#11-114) to verify passwords via SQLite.
3. **[MainDashboard](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/MainDashboard.java#12-164)**: A shell utilizing a `BorderLayout` with a left-side navbar and a center `CardLayout`. Clicking a nav button instantly swaps the center panel without reloading the window.
4. **The Big 4 Panels**:
   - **[DashboardHome](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/DashboardHome.java#13-101)**: Shows live stats (Today's Sales, Total Customers) driven by live DB queries.
   - **[ProductPanel](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/ProductPanel.java#15-223) / [CustomerPanel](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/CustomerPanel.java#17-158)**: Full `JTable` data grids with real-time text-field searching, built-in "Register/Add" popups, and delete buttons. 
   - **[SalesPanel](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/SalesPanel.java#19-213)**: A full Point-of-Sale (POS) terminal. Pharmacists scan IDs to build a cart, see a running subtotal, and when they hit checkout, it triggers the Phase 1 JDBC Transaction to lock the sale.
   - **[InventoryPanel](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/InventoryPanel.java#16-186)**: Shows stock. Click "Low Stock" and it instantly filters the list using our Phase 3 Generic [StockManager](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/generics/StockManager.java#28-194) logic. Allows 1-click restocking.

### Core Functions That Do the Heavy Lifting

| Function | File | What It Does | Key Concept |
|---|---|---|---|
| [createButton(text, color)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/PharmacyTheme.java#56-72) | `PharmacyTheme` | Factory method that creates a `JButton` with a **custom `paintComponent()`** override — draws a rounded rectangle with hover brightening. No standard Swing look-and-feel. | **Custom painting** via `Graphics2D` |
| [createCard()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/PharmacyTheme.java#131-148) | `PharmacyTheme` | Returns a `JPanel` with anti-aliased rounded-rectangle background + subtle border glow. Used for every stat card and form container. | **Custom JPanel painting** |
| [createIconLabel(type, color, size)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/PharmacyTheme.java#170-220) | `PharmacyTheme` | Paints vector icons (cart, dollar, pill, users) using `Graphics2D` draw calls — guaranteed to render on every system, unlike emoji. | **Graphics2D vector drawing** |
| [styleTable(JTable)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/PharmacyTheme.java#225-265) | `PharmacyTheme` | Applies alternating row colors via a custom `DefaultTableCellRenderer`, styled header with accent underline, and increased row height. | **Custom cell renderer** |
| [buildSidebar()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/MainDashboard.java#66-122) | `MainDashboard` | Builds the navigation panel with `BoxLayout.Y_AXIS`. Uses `Box.createVerticalGlue()` to push user info to the bottom. Each nav button paints a colored dot + active-state bar. | **BoxLayout** + custom `paintComponent` |
| [addNavButton(sidebar, text, color, card)](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/MainDashboard.java#124-175) | `MainDashboard` | Creates nav buttons with `MouseListener` hover effects and `ActionListener` that swaps `CardLayout` panels. Tracks `activeNavButton` for highlight state. | **CardLayout switching** + event handling |
| [refresh()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/DashboardHome.java#107-118) | `DashboardHome` | Uses `SwingUtilities.invokeLater()` to safely update UI labels from the Event Dispatch Thread. Calls `SaleDAO`, `ProductDAO`, `CustomerDAO` for live stats. | **EDT thread safety** |
| [addToCart()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/SalesPanel.java#119-161) | `SalesPanel` | Finds product by ID via DAO, validates quantity vs. stock, calculates line total, adds row to `DefaultTableModel`, and updates running subtotal. | **Real-time cart management** |
| [completeSale()](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/SalesPanel.java#163-201) | `SalesPanel` | Builds a `Sale` object, calls `saleDAO.processSale()` (the Phase 1 transaction), then shows a success dialog and clears the cart. The GUI's direct link to the DB transaction layer. | **GUI → DAO → JDBC Transaction** pipeline |

---

## 🔗 Phase 6: Integration & Polish — 100% COMPLETE ✅

### What We Did
We wired all 5 separate phases together into one unified application ([GUIMain.java](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/GUIMain.java)) and ensured everything runs flawlessly.

### Why We Did It (Vibe Check)
A bunch of cool features sitting in separate packages don't help anyone. Phase 6 was the final assembly: ensuring the GUI (Phase 5) uses the Generic classes (Phase 3) which talk to the Complex Data Structures (Phase 2), which save directly down to the Database DAOs (Phase 1).

### Deep Dive: What Was Built
- **[GUIMain](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/GUIMain.java#9-30) entry point**: Created a new launcher that applies the system look-and-feel and boots the [LoginFrame](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/src/com/pharmacy/gui/LoginFrame.java#14-163). 
- **Compilation Check**: Eliminated unused imports, fixed all syntax issues across 20+ classes, and verified a 100% clean `javac` compilation process.
- **Documentation**: Rewrote the [README.md](file:///c:/Users/ELITEBOOK%20HP%20840%20G%208/Phrmacy/Phrmacy-java/README.md) the reflect the exact `bash/powershell` commands a professor or user would need to compile and run the new Swing GUI with the `sqlite` and `jgrapht` JAR libraries required.

---

# 🚀 FINAL STATUS: PROJECT 100% COMPLETE

The Pharmacy Management System is fully designed, built, integrated, documented, and compiled. It is ready for final delivery! 🎉

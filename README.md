# Pharmacy Management System (PMS)

A comprehensive Java console application for managing pharmacy operations including product inventory, customer management, sales processing, and transaction tracking. Built with object-oriented design principles and clean architecture.

## Project Overview

The Pharmacy Management System is a command-line application that helps pharmacy staff manage daily operations efficiently. It provides features for product management, inventory control, customer relationship management, and sales processing with built-in safety checks for drug interactions, prescription validation, and expiration tracking.

## Key Features

### 1. **Product Management**

- Support for multiple product types:
  - **Prescription Medicine**: Requires valid prescription ID
  - **OTC Medicine**: Over-the-counter medications with purchase limits
  - **Medical Devices**: Equipment with warranty tracking
  - **Supplements**: Vitamins and dietary supplements
- Product CRUD operations (Create, Read, Update, Delete)
- Product search functionality
- Expiration date tracking for medicines and supplements

### 2. **Inventory Management**

- Real-time stock level monitoring
- Low stock alerts with customizable thresholds
- Expiration date checking and warnings
- Automated restocking process with supplier tracking
- Stock status indicators (Healthy, Moderate, Low)

### 3. **Sales Processing**

- Complete sale transaction processing
- Prescription validation for prescription-only medicines
- Drug interaction detection (prevents dangerous combinations)
- Purchase limit enforcement for OTC medicines
- Expired product blocking
- Loyalty points system integration
- Multiple payment methods (Cash/Card)

### 4. **Customer Management**

- Customer registration and profile management
- Purchase history tracking
- Loyalty points accumulation and redemption
- Customer details viewing

### 5. **Transaction Management**

- Sale transactions
- Return/refund processing
- Restock transactions
- Complete transaction history viewing

### 6. **Security & Access Control**

- Pharmacist login system
- Three-tier access levels:
  - **Basic Pharmacist** (Level 1): Can process sales
  - **Senior Pharmacist** (Level 2): Can manage inventory
  - **Pharmacy Manager** (Level 3): Full system access

## Architecture

### Project Structure

```
pharmacy/
├── src/com/pharmacy/
│   ├── gui/                          # Phase 5: Swing GUI Layer
│   │   ├── GUIMain.java              # GUI Entry Point
│   │   ├── PharmacyTheme.java
│   │   └── *Panel.java               # Various Dashboard Panels
│   ├── db/                           # Phase 1: SQLite DAOs
│   │   ├── DatabaseConnection.java
│   │   └── *DAO.java
│   ├── datastructures/               # Phase 2: Complex Structures
│   │   ├── ProductCatalog.java       # HashMap
│   │   ├── ExpirationTracker.java    # TreeMap
│   │   ├── SalesHistory.java         # LinkedList
│   │   └── DrugInteractionGraph.java # JGraphT
│   ├── generics/                     # Phase 3: Generic Classes
│   │   ├── StockManager.java
│   │   ├── PrescriptionValidator.java
│   │   └── PharmacyOperations.java
│   ├── reflection/                   # Phase 4: Runtime Analysis
│   │   └── ObjectInspector.java
│   ├── exceptions/                   # Custom exceptions
│   ├── interfaces/                   # Interface definitions
│   └── models/                       # Data models
├── lib/                              # External Dependencies
│   ├── sqlite-jdbc-3.45.1.0.jar
│   └── jgrapht-core-1.5.2.jar
└── README.md
```

## Technical Details

### Design Patterns & Principles

- **Service Layer Pattern**: Business logic separated into service classes
- **Inheritance**: Product hierarchy with abstract base classes
- **Interface Segregation**: Expirable, Prescribable, Sellable interfaces
- **Exception Handling**: Custom exceptions for better error management
- **Data Persistence**: Backed by a relational database (SQLite) accessed through the DAO (Data Access Object) pattern.

### Custom Exceptions

| Exception                      | Purpose                                                                          |
| ------------------------------ | -------------------------------------------------------------------------------- |
| `ProductNotFoundException`     | Thrown when a product lookup fails                                               |
| `InsufficientStockException`   | Used for stock shortages and OTC purchase limits                                 |
| `ExpiredProductException`      | Prevents processing expired or blocked products                                  |
| `InvalidPrescriptionException` | Ensures prescription-only items include a prescription ID                        |
| `DrugInteractionException`     | Blocks dangerous OTC/prescription combinations with identical active ingredients |

### Data Persistence (SQLite)

The system uses a unified **SQLite** database (`pharmacy.db`) via the JDBC driver, replacing the legacy file-based storage.

- Fully relational schema (`src/schema.sql`) defining robust interactions between users, products, sales, and customers.
- Database access is abstracted entirely behind DAO components (Data Access Objects).
- Atomic **JDBC Transactions** in `SaleDAO` prevent partial state corruption (e.g., if inventory is deducted but loyalty point updates fail, the entire transaction rolls back automatically).

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- A terminal/command prompt

### Compilation & Running (New GUI Version)

The system now uses a modern Java Swing GUI, a SQLite database, and specialized data structures. You need to include the dependency JARs in the classpath when compiling and running. Ensure you are in the project root directory.

**Windows/PowerShell:**
```powershell
# 1. Compile all Java files
$libraries = "lib/sqlite-jdbc-3.45.1.0.jar;lib/jgrapht-core-1.5.2.jar"
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -cp "$libraries;src" -d out $javaFiles

# 2. Run the new GUI Application
java -cp "$libraries;out" com.pharmacy.gui.GUIMain
```

### Default Login (GUI)

- **Username**: `admin`
- **Password**: `admin123`
*(Auto-created on first run by the SQLite database script)*

## Usage Guide

### Main Menu Options

1. **Product Management**

   - View all products
   - Add new products (requires Senior level or above)
   - Search products by ID or name
   - Update product details (requires Senior level or above)
   - Delete products (requires Senior level or above)

2. **Inventory Management**

   - Check stock levels
   - Low stock alerts
   - Expiration date checking
   - Process restocking (requires Senior level or above)

3. **Process Sale**

   - Create new sale transactions
   - Process returns/refunds

4. **Customer Management**

   - View all customers
   - Register new customers
   - View customer details
   - View purchase history

5. **View Transactions**
   - View all transaction history

## Safety Features

1. **Drug Interaction Detection**: Prevents selling prescription and OTC medicines with the same active ingredient in a single transaction
2. **Prescription Validation**: Ensures prescription medicines are only sold with valid prescription IDs
3. **Expiration Checking**: Blocks sales of expired products
4. **Stock Validation**: Prevents overselling by checking available stock
5. **Purchase Limits**: Enforces quantity limits on OTC medicines to prevent abuse

## Class Count

The project consists of **15 main classes** organized into:

- **5 Exception classes**
- **3 Interface classes**
- **4 Person model classes**
- **6 Product model classes**
- **4 Transaction model classes**
- **5 GUI Panel classes & Theme classes**
- **10 Core Database DAO classes**
- **1 Main application class**

## Recent Improvements

- **Code Refactoring**: Extracted business logic from Main.java into dedicated service classes, reducing Main.java from 1,091 lines to 254 lines (~77% reduction)
- **Better Organization**: Separated concerns into service layer for improved maintainability
- **Clean Architecture**: Follows separation of concerns principle

## Notes

- All data is securely handled via **SQLite**, automatically initialized on the first run of the application.
- Advanced Generic Data Structures (`HashMap`, `TreeMap`, `LinkedList`) drastically improve speed by bypassing database hits for lookups and filtering operations. 
- Access control restricts specific panels (like Inventory) to senior pharmacists, using our defined object hierarchy.

## Author Benmalti Mouaad

Pharmacy Management System - Java Console Application

---

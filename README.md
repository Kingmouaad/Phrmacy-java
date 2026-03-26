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
│   ├── Main.java                    # Application entry point
│   ├── exceptions/                   # Custom exception classes
│   │   ├── DrugInteractionException.java
│   │   ├── ExpiredProductException.java
│   │   ├── InsufficientStockException.java
│   │   ├── InvalidPrescriptionException.java
│   │   └── ProductNotFoundException.java
│   ├── interfaces/                   # Interface definitions
│   │   ├── Expirable.java
│   │   ├── Prescribable.java
│   │   └── Sellable.java
│   ├── models/
│   │   ├── persons/                  # Person-related classes
│   │   │   ├── Person.java           # Base class
│   │   │   ├── Customer.java
│   │   │   ├── Pharmacist.java
│   │   │   └── Doctor.java
│   │   ├── products/                 # Product classes
│   │   │   ├── product.java          # Abstract base class
│   │   │   ├── medicine.java         # Abstract medicine class
│   │   │   ├── PrescriptionMedicine.java
│   │   │   ├── otcmedicine.java
│   │   │   ├── medicaledevice.java
│   │   │   └── Supplement.java
│   │   └── transactions/             # Transaction classes
│   │       ├── Transaction.java      # Abstract base class
│   │       ├── Sale.java
│   │       ├── Return.java
│   │       └── Restock.java
│   └── services/                     # Service layer (business logic)
│       ├── DataService.java          # File I/O operations
│       ├── ProductService.java       # Product management
│       ├── CustomerService.java      # Customer management
│       ├── InventoryService.java     # Inventory operations
│       └── SaleService.java          # Sales processing
├── data/                             # Data persistence files
│   ├── products.txt
│   ├── stock.txt
│   ├── customers.txt
│   └── sales.txt
└── README.md
```

## Technical Details

### Design Patterns & Principles

- **Service Layer Pattern**: Business logic separated into service classes
- **Inheritance**: Product hierarchy with abstract base classes
- **Interface Segregation**: Expirable, Prescribable, Sellable interfaces
- **Exception Handling**: Custom exceptions for better error management
- **Data Persistence**: Text-based file storage with pipe-delimited format

### Custom Exceptions

| Exception                      | Purpose                                                                          |
| ------------------------------ | -------------------------------------------------------------------------------- |
| `ProductNotFoundException`     | Thrown when a product lookup fails                                               |
| `InsufficientStockException`   | Used for stock shortages and OTC purchase limits                                 |
| `ExpiredProductException`      | Prevents processing expired or blocked products                                  |
| `InvalidPrescriptionException` | Ensures prescription-only items include a prescription ID                        |
| `DrugInteractionException`     | Blocks dangerous OTC/prescription combinations with identical active ingredients |

### Data Persistence

The system uses text files for data persistence with pipe (`|`) delimiters:

- **products.txt**: Product type, ID, name, price, and quantity
- **stock.txt**: Product ID, quantity, and stock status
- **customers.txt**: Customer ID, name, phone, email, and loyalty points
- **sales.txt**: Transaction ID, type, total amount, and status

Data is automatically loaded on startup and saved after each modification.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- A terminal/command prompt

### Compilation

```bash
# Compile all Java files
javac -d build -sourcepath src src/com/pharmacy/Main.java

# Run the application
java -cp build com.pharmacy.Main
```

### Default Login

- **Pharmacist ID**: `PHR111`
- **Access Level**: Manager (Level 3)

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
- **5 Service classes**
- **1 Main application class**

## Recent Improvements

- **Code Refactoring**: Extracted business logic from Main.java into dedicated service classes, reducing Main.java from 1,091 lines to 254 lines (~77% reduction)
- **Better Organization**: Separated concerns into service layer for improved maintainability
- **Clean Architecture**: Follows separation of concerns principle

## Notes

- All data is persisted in text files in the `data/` directory
- The system automatically creates initial data files if they don't exist
- Access control is enforced based on pharmacist access level
- The system maintains transaction history for audit purposes

## Author Benmalti Mouaad

Pharmacy Management System - Java Console Application

---

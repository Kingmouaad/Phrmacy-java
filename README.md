# Pharmacy Management System

Java console application for managing products, inventory, customers, and transactions inside a pharmacy. The codebase now follows a clean `com.pharmacy` package tree with custom exception handling and lightweight text-based persistence.

## Project Layout

```
src/com/pharmacy
├── Main.java
├── exceptions
│   ├── DrugInteractionException.java
│   ├── ExpiredProductException.java
│   ├── InsufficientStockException.java
│   ├── InvalidPrescriptionException.java
│   └── ProductNotFoundException.java
├── interfaces
│   ├── Expirable.java
│   ├── Prescribable.java
│   └── Sellable.java
├── models
│   ├── persons (Customer, Doctor, Pharmacist, Person)
│   ├── products (PrescriptionMedicine, otcmedicine, Supplement, etc.)
│   └── transactions (Sale, Return, Restock, Transaction)
└── services
    └── package-info.java //empty because all the services logic are in main

data/
├── products.txt
├── stock.txt
├── customers.txt
└── sales.txt
```

## Custom Exceptions

| Exception                      | Purpose                                                                           |
| ------------------------------ | --------------------------------------------------------------------------------- |
| `ProductNotFoundException`     | Thrown when a product lookup fails.                                               |
| `InsufficientStockException`   | Used for stock shortages and OTC purchase limits.                                 |
| `ExpiredProductException`      | Prevents processing expired or blocked products.                                  |
| `InvalidPrescriptionException` | Ensures prescription-only items include a prescription ID.                        |
| `DrugInteractionException`     | Blocks dangerous OTC/prescription combinations with identical active ingredients. |

The `Main` workflow now throws and catches these exceptions instead of relying on generic errors. This keeps validations (stock, expiration, prescriptions, interactions) explicit and easier to debug.

## Data Initialization & Persistence

`Main.initializeTestData()` seeds sample pharmacists, customers, doctors, and inventory, then writes the data to the text files in `data/`. Each run refreshes the files so they always reflect the in-memory bootstrap state. Files use a pipe (`|`) delimiter to simplify future parsing:

- `products.txt`: product type, id, name, price, and quantity.
- `stock.txt`: basic availability view with a health indicator.
- `customers.txt`: customer contact info with loyalty balance.
- `sales.txt`: current sale summaries or a placeholder when empty.

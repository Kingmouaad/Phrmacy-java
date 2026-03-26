-- ============================================
-- Pharmacy Management System — Database Schema
-- SQLite version
-- ============================================

-- Drop tables if they exist (for re-creation)
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS interactions;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

-- ============================================
-- 1. USERS — Pharmacist authentication
-- ============================================
CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE,
    password      TEXT    NOT NULL,
    pharmacist_id TEXT    NOT NULL,
    full_name     TEXT    NOT NULL,
    access_level  INTEGER NOT NULL DEFAULT 1,
    created_at    TEXT    DEFAULT (datetime('now'))
);

-- ============================================
-- 2. PRODUCTS — All product types
-- ============================================
CREATE TABLE products (
    product_id        TEXT PRIMARY KEY,
    name              TEXT    NOT NULL,
    price             REAL    NOT NULL CHECK(price >= 0),
    product_type      TEXT    NOT NULL CHECK(product_type IN ('PrescriptionMedicine','OTCMedicine','MedicalDevice','Supplement')),
    -- Medicine-specific fields
    active_ingredient TEXT,
    dosage_form       TEXT,
    strength          TEXT,
    manufacturer      TEXT,
    expiration_date   TEXT,
    -- OTC-specific
    purchase_limit    INTEGER DEFAULT 0,
    minimum_age       INTEGER DEFAULT 0,
    -- Medical Device-specific
    device_type       TEXT,
    warranty_months   INTEGER DEFAULT 0,
    -- Supplement-specific
    supplement_type   TEXT,
    serving_size      TEXT,
    benefits          TEXT,
    -- Prescription-specific
    requires_prescription INTEGER DEFAULT 0
);

-- ============================================
-- 3. STOCK — Inventory tracking
-- ============================================
CREATE TABLE stock (
    product_id     TEXT PRIMARY KEY REFERENCES products(product_id) ON DELETE CASCADE,
    quantity       INTEGER NOT NULL DEFAULT 0 CHECK(quantity >= 0),
    min_threshold  INTEGER NOT NULL DEFAULT 10,
    last_restocked TEXT
);

-- ============================================
-- 4. CUSTOMERS
-- ============================================
CREATE TABLE customers (
    customer_id    TEXT PRIMARY KEY,
    full_name      TEXT NOT NULL,
    phone          TEXT NOT NULL,
    email          TEXT,
    address        TEXT,
    loyalty_points REAL NOT NULL DEFAULT 0.0,
    allergens      TEXT DEFAULT ''
);

-- ============================================
-- 5. SALES — Sale transactions
-- ============================================
CREATE TABLE sales (
    transaction_id TEXT PRIMARY KEY,
    customer_id    TEXT REFERENCES customers(customer_id),
    pharmacist_id  TEXT NOT NULL,
    sale_date      TEXT NOT NULL DEFAULT (datetime('now')),
    subtotal       REAL NOT NULL DEFAULT 0.0,
    discount       REAL NOT NULL DEFAULT 0.0,
    total_amount   REAL NOT NULL DEFAULT 0.0,
    payment_method TEXT,
    status         TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','COMPLETED','CANCELLED'))
);

-- ============================================
-- 6. SALE_ITEMS — Individual items per sale
-- ============================================
CREATE TABLE sale_items (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id TEXT NOT NULL REFERENCES sales(transaction_id) ON DELETE CASCADE,
    product_id     TEXT NOT NULL REFERENCES products(product_id),
    quantity       INTEGER NOT NULL CHECK(quantity > 0),
    unit_price     REAL NOT NULL CHECK(unit_price >= 0),
    line_total     REAL NOT NULL CHECK(line_total >= 0)
);

-- ============================================
-- 7. PRESCRIPTIONS
-- ============================================
CREATE TABLE prescriptions (
    prescription_id TEXT PRIMARY KEY,
    customer_id     TEXT NOT NULL REFERENCES customers(customer_id),
    product_id      TEXT NOT NULL REFERENCES products(product_id),
    doctor_name     TEXT NOT NULL,
    issue_date      TEXT NOT NULL,
    expiry_date     TEXT NOT NULL,
    is_renewable    INTEGER NOT NULL DEFAULT 0,
    status          TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','USED','EXPIRED'))
);

-- ============================================
-- 8. INTERACTIONS — Drug interaction pairs
-- ============================================
CREATE TABLE interactions (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    drug_a      TEXT NOT NULL,
    drug_b      TEXT NOT NULL,
    severity    TEXT NOT NULL CHECK(severity IN ('LOW','MODERATE','HIGH','CRITICAL')),
    description TEXT,
    UNIQUE(drug_a, drug_b)
);

-- ============================================
-- SAMPLE DATA
-- ============================================

-- Users (pharmacist logins)
INSERT INTO users (username, password, pharmacist_id, full_name, access_level) VALUES
    ('admin',   'admin123',  'PHR111', 'Benmalti Mouaad', 3),
    ('pharm2',  'pass456',   'PHR222', 'Ahmed Bensalem',  2),
    ('pharm3',  'pass789',   'PHR333', 'Sara Mehdaoui',   1);

-- Products: Prescription Medicines
INSERT INTO products (product_id, name, price, product_type, active_ingredient, dosage_form, strength, manufacturer, expiration_date, requires_prescription) VALUES
    ('MED001', 'Amoxicillin 500mg',    8.50,  'PrescriptionMedicine', 'Amoxicillin',    'Capsule', '500mg', 'Pfizer',      '2026-12-15', 1),
    ('MED002', 'Metformin 850mg',      12.00, 'PrescriptionMedicine', 'Metformin',      'Tablet',  '850mg', 'Sanofi',      '2027-03-20', 1),
    ('MED003', 'Omeprazole 20mg',      6.75,  'PrescriptionMedicine', 'Omeprazole',     'Capsule', '20mg',  'AstraZeneca', '2026-08-10', 1),
    ('MED004', 'Atorvastatin 40mg',    15.30, 'PrescriptionMedicine', 'Atorvastatin',   'Tablet',  '40mg',  'Pfizer',      '2026-06-01', 1),
    ('MED005', 'Ciprofloxacin 500mg',  9.20,  'PrescriptionMedicine', 'Ciprofloxacin',  'Tablet',  '500mg', 'Bayer',       '2026-09-30', 1);

-- Products: OTC Medicines
INSERT INTO products (product_id, name, price, product_type, active_ingredient, dosage_form, strength, manufacturer, expiration_date, purchase_limit, minimum_age) VALUES
    ('OTC001', 'Paracetamol 500mg',     3.50,  'OTCMedicine', 'Paracetamol',  'Tablet',  '500mg', 'Sanofi',       '2027-06-15', 5, 0),
    ('OTC002', 'Ibuprofen 400mg',       4.20,  'OTCMedicine', 'Ibuprofen',    'Tablet',  '400mg', 'Reckitt',      '2027-01-20', 3, 12),
    ('OTC003', 'Cetirizine 10mg',       5.80,  'OTCMedicine', 'Cetirizine',   'Tablet',  '10mg',  'UCB',          '2026-11-30', 0, 6),
    ('OTC004', 'Loperamide 2mg',        7.10,  'OTCMedicine', 'Loperamide',   'Capsule', '2mg',   'Johnson&Johnson','2026-04-15', 2, 12),
    ('OTC005', 'Aspirin 300mg',         2.90,  'OTCMedicine', 'Aspirin',      'Tablet',  '300mg', 'Bayer',        '2027-08-25', 4, 16);

-- Products: Medical Devices
INSERT INTO products (product_id, name, price, product_type, device_type, warranty_months, manufacturer) VALUES
    ('DEV001', 'Digital Thermometer',       25.00, 'MedicalDevice', 'Thermometer',           24, 'Omron'),
    ('DEV002', 'Blood Pressure Monitor',    65.00, 'MedicalDevice', 'Blood Pressure Monitor', 36, 'Omron'),
    ('DEV003', 'Glucose Meter Kit',         45.00, 'MedicalDevice', 'Glucose Meter',         12, 'Accu-Chek');

-- Products: Supplements
INSERT INTO products (product_id, name, price, product_type, supplement_type, serving_size, benefits, expiration_date) VALUES
    ('SUP001', 'Vitamin C 1000mg',      8.00,  'Supplement', 'Vitamin',  '1 tablet',  'Immune system support',      '2027-05-20'),
    ('SUP002', 'Omega-3 Fish Oil',      12.50, 'Supplement', 'Mineral',  '1 softgel', 'Heart and brain health',     '2026-10-15'),
    ('SUP003', 'Multivitamin Complex',   15.00, 'Supplement', 'Vitamin',  '1 tablet',  'Daily nutritional support',  '2027-02-28'),
    ('SUP004', 'Zinc 50mg',             6.50,  'Supplement', 'Mineral',  '1 tablet',  'Immune and skin health',     '2027-07-10');

-- Stock levels
INSERT INTO stock (product_id, quantity, min_threshold, last_restocked) VALUES
    ('MED001', 150,  20, '2026-03-01'),
    ('MED002', 80,   15, '2026-03-05'),
    ('MED003', 200,  25, '2026-02-28'),
    ('MED004', 45,   10, '2026-03-10'),
    ('MED005', 3,    20, '2026-01-15'),
    ('OTC001', 500,  50, '2026-03-01'),
    ('OTC002', 300,  30, '2026-03-01'),
    ('OTC003', 120,  20, '2026-02-20'),
    ('OTC004', 8,    15, '2026-02-01'),
    ('OTC005', 250,  30, '2026-03-05'),
    ('DEV001', 35,   5,  '2026-03-01'),
    ('DEV002', 15,   5,  '2026-02-15'),
    ('DEV003', 20,   5,  '2026-03-10'),
    ('SUP001', 180,  25, '2026-03-01'),
    ('SUP002', 90,   15, '2026-02-28'),
    ('SUP003', 60,   10, '2026-03-05'),
    ('SUP004', 110,  15, '2026-03-01');

-- Customers
INSERT INTO customers (customer_id, full_name, phone, email, address, loyalty_points, allergens) VALUES
    ('CUS001', 'Karim Boudiaf',    '0555123456', 'karim.b@email.com',   '12 Rue de la Paix, Mostaganem',  250.0,  'Penicillin'),
    ('CUS002', 'Fatima Zahra',     '0661987654', 'fatima.z@email.com',  '45 Bd Mohamed V, Oran',          120.0,  ''),
    ('CUS003', 'Youcef Haddad',    '0770456789', 'youcef.h@email.com',  '8 Rue des Oliviers, Mostaganem', 75.5,   'Aspirin,Ibuprofen'),
    ('CUS004', 'Amina Benali',     '0698765432', 'amina.b@email.com',   '23 Cite 500 Lgts, Mostaganem',   0.0,    ''),
    ('CUS005', 'Mohamed Cherif',   '0542345678', 'mohamed.c@email.com', '67 Rue Emir AEK, Oran',          430.0,  'Sulfa drugs');

-- Sample Sales
INSERT INTO sales (transaction_id, customer_id, pharmacist_id, sale_date, subtotal, discount, total_amount, payment_method, status) VALUES
    ('TXN001', 'CUS001', 'PHR111', '2026-03-20 09:30:00', 17.00,  0.0,  17.00,  'CASH',  'COMPLETED'),
    ('TXN002', 'CUS002', 'PHR111', '2026-03-21 14:15:00', 25.00,  2.50, 22.50,  'CARD',  'COMPLETED'),
    ('TXN003', 'CUS003', 'PHR222', '2026-03-22 10:00:00', 8.00,   0.0,  8.00,   'CASH',  'COMPLETED'),
    ('TXN004', 'CUS001', 'PHR111', '2026-03-23 16:45:00', 65.00,  5.0,  60.00,  'CARD',  'COMPLETED'),
    ('TXN005', 'CUS005', 'PHR222', '2026-03-25 08:20:00', 12.50,  0.0,  12.50,  'CASH',  'COMPLETED');

-- Sale Items
INSERT INTO sale_items (transaction_id, product_id, quantity, unit_price, line_total) VALUES
    ('TXN001', 'OTC001', 2, 3.50,  7.00),
    ('TXN001', 'MED003', 1, 6.75,  6.75),
    ('TXN002', 'SUP001', 1, 8.00,  8.00),
    ('TXN002', 'OTC002', 2, 4.20,  8.40),
    ('TXN003', 'SUP001', 1, 8.00,  8.00),
    ('TXN004', 'DEV002', 1, 65.00, 65.00),
    ('TXN005', 'SUP002', 1, 12.50, 12.50);

-- Prescriptions
INSERT INTO prescriptions (prescription_id, customer_id, product_id, doctor_name, issue_date, expiry_date, is_renewable, status) VALUES
    ('RX001', 'CUS001', 'MED001', 'Dr. Rachid Amrani',  '2026-03-01', '2026-06-01', 1, 'ACTIVE'),
    ('RX002', 'CUS002', 'MED002', 'Dr. Leila Khediri',  '2026-03-10', '2026-09-10', 0, 'ACTIVE'),
    ('RX003', 'CUS005', 'MED004', 'Dr. Rachid Amrani',  '2026-02-15', '2026-05-15', 1, 'ACTIVE'),
    ('RX004', 'CUS003', 'MED005', 'Dr. Samir Bouzid',   '2026-01-20', '2026-03-20', 0, 'EXPIRED');

-- Drug Interactions
INSERT INTO interactions (drug_a, drug_b, severity, description) VALUES
    ('Amoxicillin',   'Metformin',     'LOW',      'Minor interaction; monitor blood sugar levels'),
    ('Aspirin',       'Ibuprofen',     'HIGH',     'Increased risk of gastrointestinal bleeding'),
    ('Omeprazole',    'Atorvastatin',  'MODERATE', 'Omeprazole may increase Atorvastatin levels'),
    ('Ciprofloxacin', 'Omeprazole',    'MODERATE', 'Reduced Ciprofloxacin absorption'),
    ('Amoxicillin',   'Ciprofloxacin', 'HIGH',     'Antagonistic antibiotics; avoid combination'),
    ('Ibuprofen',     'Atorvastatin',  'LOW',      'Possible increased muscle pain risk'),
    ('Aspirin',       'Amoxicillin',   'LOW',      'Minor interaction; generally safe'),
    ('Metformin',     'Atorvastatin',  'MODERATE', 'May affect glucose control; monitor closely');

PRAGMA foreign_keys = ON;

-- =========================
-- PRODUCTS
-- =========================
CREATE TABLE IF NOT EXISTS products (
                                        id         INTEGER PRIMARY KEY AUTOINCREMENT,
                                        code       TEXT NOT NULL UNIQUE,
                                        name       TEXT NOT NULL,
                                        unit       TEXT,
                                        buy_price  REAL NOT NULL DEFAULT 0,
                                        sell_price REAL NOT NULL DEFAULT 0,
                                        stock      REAL NOT NULL DEFAULT 0,
                                        created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_products_code ON products(code);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- =========================
-- CUSTOMERS
-- =========================
CREATE TABLE IF NOT EXISTS customers (
                                         id         INTEGER PRIMARY KEY AUTOINCREMENT,
                                         code       TEXT UNIQUE,
                                         name       TEXT NOT NULL,
                                         phone      TEXT,
                                         address    TEXT,
                                         created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customers_code ON customers(code);
CREATE INDEX IF NOT EXISTS idx_customers_name ON customers(name);

-- =========================
-- SUPPLIERS
-- =========================
CREATE TABLE IF NOT EXISTS suppliers (
                                         id         INTEGER PRIMARY KEY AUTOINCREMENT,
                                         code       TEXT UNIQUE,
                                         name       TEXT NOT NULL,
                                         phone      TEXT,
                                         address    TEXT,
                                         created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_suppliers_code ON suppliers(code);
CREATE INDEX IF NOT EXISTS idx_suppliers_name ON suppliers(name);

-- =========================
-- PURCHASE ORDERS (Phiếu nhập)
-- =========================
CREATE TABLE IF NOT EXISTS purchase_orders (
                                               id           INTEGER PRIMARY KEY AUTOINCREMENT,
                                               order_number TEXT    NOT NULL UNIQUE,
                                               supplier_id  INTEGER NOT NULL,
                                               order_date   TEXT    NOT NULL,
                                               total_amount REAL    NOT NULL DEFAULT 0 CHECK(total_amount >= 0),
    note         TEXT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_po_supplier ON purchase_orders(supplier_id);
CREATE INDEX IF NOT EXISTS idx_po_date     ON purchase_orders(order_date);

-- =========================
-- PURCHASE ORDER DETAILS
-- =========================
CREATE TABLE IF NOT EXISTS purchase_order_details (
                                                      id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                                      purchase_order_id INTEGER NOT NULL,
                                                      product_id        INTEGER NOT NULL,
                                                      quantity          REAL    NOT NULL CHECK(quantity > 0),
    unit_price        REAL    NOT NULL CHECK(unit_price >= 0),
    total             REAL    NOT NULL CHECK(total >= 0),
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id)
    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_po_details_po      ON purchase_order_details(purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_po_details_product ON purchase_order_details(product_id);

-- =========================
-- SALES ORDERS (Phiếu xuất)
-- =========================
CREATE TABLE IF NOT EXISTS sales_orders (
                                            id           INTEGER PRIMARY KEY AUTOINCREMENT,
                                            order_number TEXT    NOT NULL UNIQUE,
                                            customer_id  INTEGER NOT NULL,
                                            order_date   TEXT    NOT NULL,
                                            total_amount REAL    NOT NULL DEFAULT 0 CHECK(total_amount >= 0),
    paid_amount  REAL    NOT NULL DEFAULT 0 CHECK(paid_amount >= 0),  -- Đã thanh toán
    note         TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_so_customer ON sales_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_so_date     ON sales_orders(order_date);

-- =========================
-- SALES ORDER DETAILS
-- =========================
CREATE TABLE IF NOT EXISTS sales_order_details (
                                                   id             INTEGER PRIMARY KEY AUTOINCREMENT,
                                                   sales_order_id INTEGER NOT NULL,
                                                   product_id     INTEGER NOT NULL,
                                                   quantity       REAL    NOT NULL CHECK(quantity > 0),
    unit_price     REAL    NOT NULL CHECK(unit_price >= 0),
    total          REAL    NOT NULL CHECK(total >= 0),
    FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id)
    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_so_details_so      ON sales_order_details(sales_order_id);
CREATE INDEX IF NOT EXISTS idx_so_details_product ON sales_order_details(product_id);

-- =========================
-- STOCK MOVEMENTS (Lịch sử tồn kho)
-- =========================
CREATE TABLE IF NOT EXISTS stock_movements (
                                               id             INTEGER PRIMARY KEY AUTOINCREMENT,
                                               product_id     INTEGER NOT NULL,
                                               movement_date  TEXT    NOT NULL,
                                               reference_type TEXT    NOT NULL CHECK(reference_type IN ('PURCHASE','SALE','ADJUST')),
    reference_id   INTEGER,
    quantity       REAL    NOT NULL,   -- Dương: nhập, Âm: xuất
    unit_price     REAL,
    note           TEXT,
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE RESTRICT
    );

CREATE INDEX IF NOT EXISTS idx_stock_product ON stock_movements(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_date    ON stock_movements(movement_date);
CREATE INDEX IF NOT EXISTS idx_stock_ref     ON stock_movements(reference_type, reference_id);

-- =========================
-- PAYMENTS (Thanh toán công nợ)
-- =========================
CREATE TABLE IF NOT EXISTS payments (
                                        id           INTEGER PRIMARY KEY AUTOINCREMENT,
                                        payment_date TEXT    NOT NULL,
                                        target_type  TEXT    NOT NULL CHECK(target_type IN ('CUSTOMER','SUPPLIER')),
    target_id    INTEGER NOT NULL,
    reference_id INTEGER,              -- ID phiếu nhập/xuất liên quan
    amount       REAL    NOT NULL CHECK(amount > 0),
    note         TEXT
    );

CREATE INDEX IF NOT EXISTS idx_payment_target ON payments(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_payment_date   ON payments(payment_date);
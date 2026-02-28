-- =========================
-- PENDING SALES ORDERS (Đơn đặt hàng chờ xuất)
-- =========================
CREATE TABLE IF NOT EXISTS pending_orders (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_number TEXT    NOT NULL UNIQUE,
    customer_id  INTEGER NOT NULL,
    customer_name TEXT,
    order_date   TEXT    NOT NULL,
    expected_date TEXT,
    total_amount REAL    NOT NULL DEFAULT 0,
    status       TEXT    NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','EXPORTED','CANCELLED')),
    note         TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS pending_order_details (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    pending_order_id INTEGER NOT NULL,
    product_id       INTEGER NOT NULL,
    quantity         REAL    NOT NULL CHECK(quantity > 0),
    unit_price       REAL    NOT NULL CHECK(unit_price >= 0),
    total            REAL    NOT NULL CHECK(total >= 0),
    FOREIGN KEY (pending_order_id) REFERENCES pending_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)       REFERENCES products(id)        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_pending_customer ON pending_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_pending_status   ON pending_orders(status);

-- =========================
-- RETURN ORDERS (Trả hàng từ khách)
-- =========================
CREATE TABLE IF NOT EXISTS return_orders (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_number TEXT    NOT NULL UNIQUE,
    customer_id  INTEGER NOT NULL,
    customer_name TEXT,
    return_date  TEXT    NOT NULL,
    total_amount REAL    NOT NULL DEFAULT 0,
    note         TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS return_order_details (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    return_order_id INTEGER NOT NULL,
    product_id      INTEGER NOT NULL,
    quantity        REAL    NOT NULL CHECK(quantity > 0),
    unit_price      REAL    NOT NULL CHECK(unit_price >= 0),
    total           REAL    NOT NULL CHECK(total >= 0),
    FOREIGN KEY (return_order_id) REFERENCES return_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)      REFERENCES products(id)       ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_return_customer ON return_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_return_date     ON return_orders(return_date);


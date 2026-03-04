-- =========================
-- INVOICES (Hóa đơn bán hàng)
-- =========================
CREATE TABLE IF NOT EXISTS invoices (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number       TEXT    NOT NULL,
    invoice_symbol       TEXT,
    invoice_form_number  TEXT,
    customer_id          INTEGER,
    sales_order_id       INTEGER,
    issue_date           TEXT,
    status               TEXT    NOT NULL DEFAULT 'DRAFT',
    notes                TEXT,
    seller_name          TEXT,
    seller_tax_code      TEXT,
    seller_address       TEXT,
    seller_bank_account  TEXT,
    seller_phone         TEXT,
    buyer_name           TEXT,
    buyer_company        TEXT,
    buyer_tax_code       TEXT,
    buyer_address        TEXT,
    buyer_bank_account   TEXT,
    payment_method       TEXT,
    subtotal             REAL    NOT NULL DEFAULT 0,
    vat_rate             REAL    NOT NULL DEFAULT 10,
    vat_amount           REAL    NOT NULL DEFAULT 0,
    total_amount         REAL    NOT NULL DEFAULT 0,
    amount_in_words      TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_invoices_number   ON invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_invoices_customer ON invoices(customer_id);
CREATE INDEX IF NOT EXISTS idx_invoices_date     ON invoices(issue_date);

-- =========================
-- INVOICE ITEMS (Dòng hàng hóa trong hóa đơn)
-- =========================
CREATE TABLE IF NOT EXISTS invoice_items (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_id    INTEGER NOT NULL,
    product_id    INTEGER,
    item_index    INTEGER NOT NULL DEFAULT 1,
    product_name  TEXT,
    unit          TEXT,
    quantity      INTEGER NOT NULL DEFAULT 1,
    unit_price    REAL    NOT NULL DEFAULT 0,
    discount      REAL    NOT NULL DEFAULT 0,
    line_total    REAL    NOT NULL DEFAULT 0,
    total_price   REAL    NOT NULL DEFAULT 0,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice ON invoice_items(invoice_id);

-- =========================
-- WAREHOUSE EXPORTS (Phiếu xuất kho)
-- =========================
CREATE TABLE IF NOT EXISTS warehouse_exports (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_id    INTEGER,
    product_id    INTEGER,
    product_name  TEXT,
    sku           TEXT,
    quantity      INTEGER NOT NULL DEFAULT 0,
    export_date   TEXT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wh_exports_invoice ON warehouse_exports(invoice_id);
CREATE INDEX IF NOT EXISTS idx_wh_exports_date    ON warehouse_exports(export_date);


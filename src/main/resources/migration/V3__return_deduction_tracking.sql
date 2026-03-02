-- Lưu thông tin trừ nợ khi trả hàng để hoàn ngược chính xác
ALTER TABLE return_orders ADD COLUMN deducted_from_total REAL NOT NULL DEFAULT 0;
ALTER TABLE return_orders ADD COLUMN deducted_from_paid  REAL NOT NULL DEFAULT 0;


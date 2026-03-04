package com.dat.whmanagement.model;

import java.time.LocalDate;

/**
 * Phiếu xuất kho – liên kết 1 dòng hàng hóa với 1 hóa đơn.
 */
public class ExportRecord {

    private int id;
    private int invoiceId;
    private int productId;
    private String productName;
    private String sku;
    private int quantity;
    private LocalDate exportDate;
    private String invoiceNumber; // join field – chỉ dùng hiển thị

    public ExportRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDate getExportDate() { return exportDate; }
    public void setExportDate(LocalDate exportDate) { this.exportDate = exportDate; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    @Override
    public String toString() {
        return "Xuất kho #" + id + " – HĐ:" + invoiceNumber + " – " + productName + " x" + quantity;
    }
}


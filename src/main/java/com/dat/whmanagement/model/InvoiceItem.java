package com.dat.whmanagement.model;

/**
 * Dòng hàng hóa trong hóa đơn.
 */
public class InvoiceItem {

    private int id;
    private int invoiceId;
    private int productId;
    private int index;
    private String name;
    private String unit;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double discount;
    private String notes;

    public InvoiceItem() {}

    public InvoiceItem(int id, int invoiceId, int productId, int index,
                       String name, String unit, int quantity,
                       double unitPrice, double totalPrice, double discount) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.index = index;
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.discount = discount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProductName() { return name; }
    public void setProductName(String n) { this.name = n; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getLineTotal() { return totalPrice; }
    public void setLineTotal(double t) { this.totalPrice = t; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "InvoiceItem{id=" + id + ", name='" + name + "', qty=" + quantity
                + ", unitPrice=" + unitPrice + ", totalPrice=" + totalPrice + "}";
    }
}


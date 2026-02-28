package com.dat.whmanagement.model;

public class SalesOrderDetail {

    private Integer id;
    private int salesOrderId;
    private int productId;
    private String productCode; // for display
    private String productName; // for display
    private String unit;        // for display
    private double quantity;
    private double unitPrice;
    private double total;

    public SalesOrderDetail() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getSalesOrderId() { return salesOrderId; }
    public void setSalesOrderId(int salesOrderId) { this.salesOrderId = salesOrderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}

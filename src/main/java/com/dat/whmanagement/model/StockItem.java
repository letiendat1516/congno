package com.dat.whmanagement.model;

import java.time.LocalDateTime;

/**
 * Dùng cho hiển thị tồn kho: thông tin sản phẩm + số lượng tồn
 */
public class StockItem {

    private int productId;
    private String productCode;
    private String productName;
    private String unit;
    private double stock;
    private double buyPrice;
    private LocalDateTime lastUpdated;

    public StockItem() {}

    public StockItem(int productId, String productCode, String productName,
                     String unit, double stock, double buyPrice) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.unit = unit;
        this.stock = stock;
        this.buyPrice = buyPrice;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }

    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }


    public double getStockValue() { return stock * buyPrice; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}


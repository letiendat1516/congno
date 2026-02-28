package com.dat.whmanagement.model;

public class PendingOrderDetail {

    private Integer id;
    private int     pendingOrderId;
    private int     productId;
    private String  productCode;
    private String  productName;
    private String  unit;
    private double  quantity;
    private double  unitPrice;
    private double  total;

    public PendingOrderDetail() {}

    public Integer getId()                         { return id; }
    public void    setId(Integer id)               { this.id = id; }

    public int     getPendingOrderId()             { return pendingOrderId; }
    public void    setPendingOrderId(int i)        { this.pendingOrderId = i; }

    public int     getProductId()                  { return productId; }
    public void    setProductId(int i)             { this.productId = i; }

    public String  getProductCode()                { return productCode; }
    public void    setProductCode(String c)        { this.productCode = c; }

    public String  getProductName()                { return productName; }
    public void    setProductName(String n)        { this.productName = n; }

    public String  getUnit()                       { return unit; }
    public void    setUnit(String u)               { this.unit = u; }

    public double  getQuantity()                   { return quantity; }
    public void    setQuantity(double q)           { this.quantity = q; }

    public double  getUnitPrice()                  { return unitPrice; }
    public void    setUnitPrice(double p)          { this.unitPrice = p; }

    public double  getTotal()                      { return total; }
    public void    setTotal(double t)              { this.total = t; }
}


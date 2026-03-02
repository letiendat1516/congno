package com.dat.whmanagement.model;

import java.time.LocalDate;

/**
 * Một dòng đơn hàng (nhập hoặc xuất) dùng trong panel Thanh toán:
 * hiển thị ngày, số phiếu, sản phẩm, SL, đơn giá, thành tiền, SL trả, SL thực lấy
 */
public class OrderLine {

    private String    orderType;      // PURCHASE / SALE
    private int       orderId;
    private String    orderNumber;
    private LocalDate orderDate;
    private String    productCode;
    private String    productName;
    private String    unit;
    private double    quantity;
    private double    unitPrice;
    private double    lineTotal;       // SL × đơn giá
    private double    returnQuantity;  // SL hàng trả lại
    private double    actualQuantity;  // SL thực khách lấy = quantity - returnQuantity

    public OrderLine() {}

    public String    getOrderType()               { return orderType;    }
    public void      setOrderType(String t)       { this.orderType = t;  }

    public int       getOrderId()                 { return orderId;      }
    public void      setOrderId(int id)           { this.orderId = id;   }

    public String    getOrderNumber()             { return orderNumber;  }
    public void      setOrderNumber(String n)     { this.orderNumber = n;}

    public LocalDate getOrderDate()               { return orderDate;    }
    public void      setOrderDate(LocalDate d)    { this.orderDate = d;  }

    public String    getProductCode()             { return productCode;  }
    public void      setProductCode(String c)     { this.productCode = c;}

    public String    getProductName()             { return productName;  }
    public void      setProductName(String n)     { this.productName = n;}

    public String    getUnit()                    { return unit;         }
    public void      setUnit(String u)            { this.unit = u;       }

    public double    getQuantity()                { return quantity;     }
    public void      setQuantity(double q)        { this.quantity = q;   }

    public double    getUnitPrice()               { return unitPrice;    }
    public void      setUnitPrice(double p)       { this.unitPrice = p;  }

    public double    getLineTotal()               { return lineTotal;    }
    public void      setLineTotal(double t)       { this.lineTotal = t;  }

    public double    getReturnQuantity()           { return returnQuantity;    }
    public void      setReturnQuantity(double q)   { this.returnQuantity = q;  }

    public double    getActualQuantity()           { return actualQuantity;    }
    public void      setActualQuantity(double q)   { this.actualQuantity = q;  }
}

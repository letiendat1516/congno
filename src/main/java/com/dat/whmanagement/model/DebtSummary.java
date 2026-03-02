package com.dat.whmanagement.model;

/**
 * Tóm tắt công nợ của một khách hàng hoặc nhà cung cấp.
 * - CUSTOMER : tiền khách còn nợ (total_amount - paid_amount từ sales_orders)
 * - SUPPLIER : tiền ta còn nợ NCC (total_amount - paid_amount từ purchase_orders)
 */
public class DebtSummary {

    public enum TargetType { CUSTOMER, SUPPLIER }

    private final int         targetId;
    private final String      targetCode;
    private final String      targetName;
    private final String      phone;
    private final TargetType  targetType;
    private final double      totalAmount;   // Tổng giá trị đơn hàng
    private final double      paidAmount;    // Đã thanh toán
    private final double      debtAmount;    // Còn nợ = totalAmount - paidAmount

    public DebtSummary(int targetId, String targetCode, String targetName,
                       String phone, TargetType targetType,
                       double totalAmount, double paidAmount) {
        this.targetId    = targetId;
        this.targetCode  = targetCode;
        this.targetName  = targetName;
        this.phone       = phone;
        this.targetType  = targetType;
        // Nếu paid > total → nợ âm → trừ thẳng vào paid, để nợ = 0
        if (paidAmount > totalAmount) {
            this.totalAmount = totalAmount;
            this.paidAmount  = totalAmount;
            this.debtAmount  = 0;
        } else {
            this.totalAmount = totalAmount;
            this.paidAmount  = paidAmount;
            this.debtAmount  = totalAmount - paidAmount;
        }
    }

    public int         getTargetId()    { return targetId;    }
    public String      getTargetCode()  { return targetCode;  }
    public String      getTargetName()  { return targetName;  }
    public String      getPhone()       { return phone;       }
    public TargetType  getTargetType()  { return targetType;  }
    public double      getTotalAmount() { return totalAmount; }
    public double      getPaidAmount()  { return paidAmount;  }
    public double      getDebtAmount()  { return debtAmount;  }
}


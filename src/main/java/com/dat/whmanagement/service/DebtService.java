package com.dat.whmanagement.service;

import com.dat.whmanagement.model.DebtSummary;
import com.dat.whmanagement.model.OrderLine;
import com.dat.whmanagement.model.PaymentRecord;

import java.util.List;

public interface DebtService {

    /** Danh sách công nợ khách hàng (tiền khách còn nợ) */
    List<DebtSummary> getCustomerDebts();

    /** Danh sách công nợ nhà cung cấp (tiền ta còn nợ NCC) */
    List<DebtSummary> getSupplierDebts();

    /**
     * Ghi nhận thanh toán cho khách hàng hoặc nhà cung cấp.
     *
     * @param targetType  "CUSTOMER" hoặc "SUPPLIER"
     * @param targetId    ID của khách hàng / NCC
     * @param amount      Số tiền (> 0)
     * @param note        Ghi chú
     */
    void recordPayment(String targetType, int targetId, double amount, String note);

    void updatePayment(int paymentId, double amount, String note);

    void deletePayment(int paymentId);

    List<OrderLine>     getOrderLines(String targetType, int targetId);

    List<PaymentRecord> getPaymentHistory(String targetType, int targetId);
}

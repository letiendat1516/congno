package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.DebtSummary;
import com.dat.whmanagement.model.OrderLine;
import com.dat.whmanagement.model.PaymentRecord;

import java.util.List;

public interface DebtDAO {

    /** Công nợ khách hàng: tiền khách còn nợ ta */
    List<DebtSummary> findCustomerDebts();

    /** Công nợ nhà cung cấp: tiền ta còn nợ NCC */
    List<DebtSummary> findSupplierDebts();

    /**
     * Ghi nhận một khoản thanh toán.
     *
     * @param targetType  "CUSTOMER" hoặc "SUPPLIER"
     * @param targetId    ID của khách hàng / NCC
     * @param amount      Số tiền thanh toán
     * @param note        Ghi chú
     */
    void recordPayment(String targetType, int targetId, double amount, String note);

    void updatePayment(int paymentId, double amount, String note);
    void deletePayment(int paymentId);

    /** Tất cả dòng sản phẩm của KH/NCC (từ sales_order_details hoặc purchase_order_details) */
    List<OrderLine> findOrderLines(String targetType, int targetId);

    /** Lịch sử thanh toán của KH/NCC */
    List<PaymentRecord> findPaymentHistory(String targetType, int targetId);
}

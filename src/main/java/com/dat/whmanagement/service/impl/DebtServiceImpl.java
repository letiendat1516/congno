package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.DebtDAO;
import com.dat.whmanagement.dao.impl.DebtDAOImpl;
import com.dat.whmanagement.model.DebtSummary;
import com.dat.whmanagement.model.OrderLine;
import com.dat.whmanagement.model.PaymentRecord;
import com.dat.whmanagement.service.DebtService;

import java.util.List;

public class DebtServiceImpl implements DebtService {

    private final DebtDAO debtDAO;

    public DebtServiceImpl() {
        this.debtDAO = new DebtDAOImpl();
    }

    public DebtServiceImpl(DebtDAO debtDAO) {
        this.debtDAO = debtDAO;
    }

    @Override
    public List<DebtSummary> getCustomerDebts() {
        return debtDAO.findCustomerDebts();
    }

    @Override
    public List<DebtSummary> getSupplierDebts() {
        return debtDAO.findSupplierDebts();
    }

    @Override
    public void recordPayment(String targetType, int targetId, double amount, String note) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }
        debtDAO.recordPayment(targetType, targetId, amount, note);
    }

    @Override
    public void updatePayment(int paymentId, double amount, String note) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }
        debtDAO.updatePayment(paymentId, amount, note);
    }

    @Override
    public void deletePayment(int paymentId) {
        debtDAO.deletePayment(paymentId);
    }

    @Override
    public List<OrderLine> getOrderLines(String targetType, int targetId) {
        return debtDAO.findOrderLines(targetType, targetId);
    }

    @Override
    public List<PaymentRecord> getPaymentHistory(String targetType, int targetId) {
        return debtDAO.findPaymentHistory(targetType, targetId);
    }
}

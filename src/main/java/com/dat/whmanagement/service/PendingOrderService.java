package com.dat.whmanagement.service;

import com.dat.whmanagement.model.PendingOrder;
import com.dat.whmanagement.model.PendingOrderDetail;

import java.util.List;

public interface PendingOrderService {
    void create(PendingOrder order);
    List<PendingOrder> getAll();
    List<PendingOrderDetail> getDetails(int orderId);
    /** Chuyển đơn đặt hàng thành phiếu xuất thực sự (xuất kho) */
    void exportOrder(int pendingOrderId);
    void cancelOrder(int pendingOrderId);
    void delete(int orderId);
    String nextOrderNumber();
}


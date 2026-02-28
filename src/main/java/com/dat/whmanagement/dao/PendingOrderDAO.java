package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.PendingOrder;
import com.dat.whmanagement.model.PendingOrderDetail;

import java.util.List;
import java.util.Optional;

public interface PendingOrderDAO {
    void insert(PendingOrder order);
    Optional<PendingOrder> findById(int id);
    List<PendingOrder> findAll();
    List<PendingOrderDetail> findDetailsByOrderId(int orderId);
    void updateStatus(int orderId, String status);
    void delete(int orderId);
    String nextOrderNumber();
}


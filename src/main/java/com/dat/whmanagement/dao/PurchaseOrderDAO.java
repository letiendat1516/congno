package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.PurchaseOrder;
import com.dat.whmanagement.model.PurchaseOrderDetail;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderDAO {
    void insert(PurchaseOrder order);
    void update(PurchaseOrder order);
    Optional<PurchaseOrder> findById(int id);
    List<PurchaseOrder> findAll();
    List<PurchaseOrderDetail> findDetailsByOrderId(int orderId);
    void delete(int id);
    String nextOrderNumber();
}


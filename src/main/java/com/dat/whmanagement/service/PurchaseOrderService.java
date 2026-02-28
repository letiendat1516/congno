package com.dat.whmanagement.service;

import com.dat.whmanagement.model.PurchaseOrder;
import com.dat.whmanagement.model.PurchaseOrderDetail;

import java.util.List;

public interface PurchaseOrderService {
    void create(PurchaseOrder order);
    void update(PurchaseOrder order);
    void delete(int orderId);
    List<PurchaseOrder> getAll();
    List<PurchaseOrderDetail> getDetails(int orderId);
    String nextOrderNumber();
}


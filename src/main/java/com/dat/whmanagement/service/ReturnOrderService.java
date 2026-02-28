package com.dat.whmanagement.service;

import com.dat.whmanagement.model.ReturnOrder;
import com.dat.whmanagement.model.ReturnOrderDetail;

import java.util.List;

public interface ReturnOrderService {
    void create(ReturnOrder order);
    void update(ReturnOrder order);
    void delete(int orderId);
    List<ReturnOrder> getAll();
    List<ReturnOrderDetail> getDetails(int orderId);
    String nextOrderNumber();
}


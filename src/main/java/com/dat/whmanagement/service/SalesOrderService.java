package com.dat.whmanagement.service;

import com.dat.whmanagement.model.SalesOrder;
import com.dat.whmanagement.model.SalesOrderDetail;

import java.util.List;

public interface SalesOrderService {
    void create(SalesOrder order);
    void update(SalesOrder order);
    void delete(int orderId);
    List<SalesOrder> getAll();
    List<SalesOrderDetail> getDetails(int orderId);
    String nextOrderNumber();
}


package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.SalesOrder;
import com.dat.whmanagement.model.SalesOrderDetail;

import java.util.List;
import java.util.Optional;

public interface SalesOrderDAO {
    void insert(SalesOrder order);
    void update(SalesOrder order);
    Optional<SalesOrder> findById(int id);
    List<SalesOrder> findAll();
    List<SalesOrderDetail> findDetailsByOrderId(int orderId);
    void delete(int id);
    String nextOrderNumber();
}


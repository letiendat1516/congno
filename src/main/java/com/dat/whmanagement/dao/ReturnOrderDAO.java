package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.ReturnOrder;
import com.dat.whmanagement.model.ReturnOrderDetail;

import java.util.List;
import java.util.Optional;

public interface ReturnOrderDAO {
    void insert(ReturnOrder order);
    void update(ReturnOrder order);
    Optional<ReturnOrder> findById(int id);
    List<ReturnOrder> findAll();
    List<ReturnOrderDetail> findDetailsByOrderId(int orderId);
    void delete(int orderId);
    String nextOrderNumber();
}


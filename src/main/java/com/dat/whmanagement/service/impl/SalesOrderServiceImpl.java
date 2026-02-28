package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.SalesOrderDAO;
import com.dat.whmanagement.dao.impl.SalesOrderDAOImpl;
import com.dat.whmanagement.model.SalesOrder;
import com.dat.whmanagement.model.SalesOrderDetail;
import com.dat.whmanagement.service.SalesOrderService;

import java.util.List;

public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderDAO dao;

    public SalesOrderServiceImpl() { this.dao = new SalesOrderDAOImpl(); }
    public SalesOrderServiceImpl(SalesOrderDAO dao) { this.dao = dao; }

    @Override
    public void create(SalesOrder order) {
        if (order.getDetails() == null || order.getDetails().isEmpty())
            throw new IllegalArgumentException("Phiếu xuất phải có ít nhất 1 sản phẩm");
        dao.insert(order);
    }

    @Override
    public void update(SalesOrder order) {
        if (order.getDetails() == null || order.getDetails().isEmpty())
            throw new IllegalArgumentException("Phiếu xuất phải có ít nhất 1 sản phẩm");
        dao.update(order);
    }

    @Override public List<SalesOrder> getAll() { return dao.findAll(); }
    @Override public void delete(int orderId) { dao.delete(orderId); }
    @Override public List<SalesOrderDetail> getDetails(int orderId) { return dao.findDetailsByOrderId(orderId); }
    @Override public String nextOrderNumber() { return dao.nextOrderNumber(); }
}


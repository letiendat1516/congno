package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.impl.ReturnOrderDAOImpl;
import com.dat.whmanagement.model.ReturnOrder;
import com.dat.whmanagement.model.ReturnOrderDetail;
import com.dat.whmanagement.service.ReturnOrderService;

import java.util.List;

public class ReturnOrderServiceImpl implements ReturnOrderService {

    private final ReturnOrderDAOImpl dao = new ReturnOrderDAOImpl();

    @Override
    public void create(ReturnOrder order) {
        if (order.getDetails() == null || order.getDetails().isEmpty())
            throw new IllegalArgumentException("Phiếu trả hàng phải có ít nhất 1 sản phẩm");
        dao.insert(order);
    }

    @Override
    public void update(ReturnOrder order) {
        if (order.getDetails() == null || order.getDetails().isEmpty())
            throw new IllegalArgumentException("Phiếu trả hàng phải có ít nhất 1 sản phẩm");
        dao.update(order);
    }

    @Override public List<ReturnOrder>       getAll()           { return dao.findAll(); }
    @Override public void                    delete(int orderId) { dao.delete(orderId); }
    @Override public List<ReturnOrderDetail> getDetails(int id) { return dao.findDetailsByOrderId(id); }
    @Override public String                  nextOrderNumber()   { return dao.nextOrderNumber(); }
}


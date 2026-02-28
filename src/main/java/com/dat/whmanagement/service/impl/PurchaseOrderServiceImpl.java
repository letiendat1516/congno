package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.PurchaseOrderDAO;
import com.dat.whmanagement.dao.impl.PurchaseOrderDAOImpl;
import com.dat.whmanagement.model.PurchaseOrder;
import com.dat.whmanagement.model.PurchaseOrderDetail;
import com.dat.whmanagement.service.PurchaseOrderService;

import java.util.List;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderDAO dao;

    public PurchaseOrderServiceImpl() { this.dao = new PurchaseOrderDAOImpl(); }
    public PurchaseOrderServiceImpl(PurchaseOrderDAO dao) { this.dao = dao; }

    @Override
    public void create(PurchaseOrder order) {
        if (order.getDetails() == null || order.getDetails().isEmpty())
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất 1 sản phẩm");
        dao.insert(order);
    }

    @Override
    public void update(PurchaseOrder order) {
        if (order.getDetails() == null || order.getDetails().isEmpty())
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất 1 sản phẩm");
        dao.update(order);
    }

    @Override public List<PurchaseOrder> getAll() { return dao.findAll(); }
    @Override public void delete(int orderId) { dao.delete(orderId); }
    @Override public List<PurchaseOrderDetail> getDetails(int orderId) { return dao.findDetailsByOrderId(orderId); }
    @Override public String nextOrderNumber() { return dao.nextOrderNumber(); }
}


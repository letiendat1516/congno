package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.SupplierDAO;
import com.dat.whmanagement.dao.impl.SupplierDAOImpl;
import com.dat.whmanagement.model.Supplier;
import com.dat.whmanagement.service.SupplierService;

import java.util.List;
import java.util.Optional;

public class SupplierServiceImpl implements SupplierService {

    private final SupplierDAO dao;

    public SupplierServiceImpl() { this.dao = new SupplierDAOImpl(); }
    public SupplierServiceImpl(SupplierDAO dao) { this.dao = dao; }

    @Override
    public Supplier create(Supplier supplier) {
        validate(supplier);
        if (supplier.getCode() != null && !supplier.getCode().isBlank()
                && dao.existsByCode(supplier.getCode())) {
            throw new IllegalArgumentException("Mã nhà cung cấp đã tồn tại");
        }
        dao.insert(supplier);
        return supplier;
    }

    @Override public List<Supplier> getAll() { return dao.findAll(); }
    @Override public Optional<Supplier> getById(int id) { return dao.findById(id); }

    @Override
    public void update(Supplier supplier) {
        validate(supplier);
        dao.update(supplier);
    }

    @Override
    public void delete(int id) {
        try { dao.delete(id); }
        catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("FOREIGN KEY"))
                throw new IllegalStateException("Không thể xóa NCC đã có đơn hàng");
            throw e;
        }
    }

    @Override public boolean existsByCode(String code) { return dao.existsByCode(code); }

    private void validate(Supplier s) {
        if (s == null) throw new IllegalArgumentException("Nhà cung cấp không được null");
        if (s.getName() == null || s.getName().isBlank())
            throw new IllegalArgumentException("Tên nhà cung cấp không được rỗng");
    }
}


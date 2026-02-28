package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.CustomerDAO;
import com.dat.whmanagement.dao.impl.CustomerDAOImpl;
import com.dat.whmanagement.model.Customer;
import com.dat.whmanagement.service.CustomerService;

import java.util.List;
import java.util.Optional;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerDAO dao;

    public CustomerServiceImpl() { this.dao = new CustomerDAOImpl(); }
    public CustomerServiceImpl(CustomerDAO dao) { this.dao = dao; }

    @Override
    public Customer create(Customer customer) {
        validate(customer);
        if (customer.getCode() != null && !customer.getCode().isBlank()
                && dao.existsByCode(customer.getCode())) {
            throw new IllegalArgumentException("Mã khách hàng đã tồn tại");
        }
        dao.insert(customer);
        return customer;
    }

    @Override public List<Customer> getAll() { return dao.findAll(); }
    @Override public Optional<Customer> getById(int id) { return dao.findById(id); }

    @Override
    public void update(Customer customer) {
        validate(customer);
        dao.update(customer);
    }

    @Override
    public void delete(int id) {
        try { dao.delete(id); }
        catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("FOREIGN KEY"))
                throw new IllegalStateException("Không thể xóa khách hàng đã có đơn hàng");
            throw e;
        }
    }

    @Override public boolean existsByCode(String code) { return dao.existsByCode(code); }

    private void validate(Customer c) {
        if (c == null) throw new IllegalArgumentException("Khách hàng không được null");
        if (c.getName() == null || c.getName().isBlank())
            throw new IllegalArgumentException("Tên khách hàng không được rỗng");
    }
}


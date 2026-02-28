package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    void insert(Customer customer);
    Optional<Customer> findById(int id);
    List<Customer> findAll();
    void update(Customer customer);
    void delete(int id);
    boolean existsByCode(String code);
}


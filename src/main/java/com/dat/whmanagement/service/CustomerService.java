package com.dat.whmanagement.service;

import com.dat.whmanagement.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    Customer create(Customer customer);
    List<Customer> getAll();
    Optional<Customer> getById(int id);
    void update(Customer customer);
    void delete(int id);
    boolean existsByCode(String code);
}


package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierDAO {
    void insert(Supplier supplier);
    Optional<Supplier> findById(int id);
    List<Supplier> findAll();
    void update(Supplier supplier);
    void delete(int id);
    boolean existsByCode(String code);
}


package com.dat.whmanagement.service;

import com.dat.whmanagement.model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierService {
    Supplier create(Supplier supplier);
    List<Supplier> getAll();
    Optional<Supplier> getById(int id);
    void update(Supplier supplier);
    void delete(int id);
    boolean existsByCode(String code);
}


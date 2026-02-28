package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    void insert(Product product);

    Optional<Product> findById(int id);

    Optional<Product> findByCode(String code);

    List<Product> findAll();

    List<Product> searchByName(String keyword); // ← Thêm vào

    void update(Product product);

    void delete(int id);

    boolean existsByCode(String code);             // ← Thêm vào
}

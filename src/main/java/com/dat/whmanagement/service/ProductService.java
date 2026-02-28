package com.dat.whmanagement.service;

import com.dat.whmanagement.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Product create(Product product);

    List<Product> getAll();

    Optional<Product> getById(int id);

    Optional<Product> getByCode(String code);

    void update(Product product);

    void delete(int id);


    // Thêm các method tiện ích
    List<Product> searchByName(String keyword);

    boolean existsByCode(String code);

    /** Trả về số lượng tồn kho hiện tại của sản phẩm */
    double getStock(int productId);
}

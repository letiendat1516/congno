package com.dat.whmanagement.dao.impl;  // ✅ Fix package

import com.dat.whmanagement.dao.ProductDAO;
import com.dat.whmanagement.model.Product;
import com.dat.whmanagement.service.ProductService;

import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;

    // ✅ Default constructor
    public ProductServiceImpl() {
        this.productDAO = new ProductDAOImpl();
    }

    // ✅ Constructor injection (tiện cho testing)
    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    // ─────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────
    @Override
    public Product create(Product product) {
        validateProduct(product);

        // ✅ Dùng existsByCode thay vì findByCode cho nhẹ hơn
        if (productDAO.existsByCode(product.getCode())) {
            throw new IllegalArgumentException(
                    "Mã sản phẩm '" + product.getCode() + "' đã tồn tại"
            );
        }

        productDAO.insert(product);
        return product;
    }

    // ─────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────
    @Override
    public List<Product> getAll() {
        return productDAO.findAll();
    }

    @Override
    public Optional<Product> getById(int id) {
        return productDAO.findById(id);
    }

    @Override
    public Optional<Product> getByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không được rỗng");
        }
        return productDAO.findByCode(code);
    }

    @Override
    public List<Product> searchByName(String keyword) {
        if (keyword == null) keyword = "";
        return productDAO.searchByName(keyword.trim());
    }

    @Override
    public boolean existsByCode(String code) {
        return false;
    }

    @Override
    public double getStock(int productId) {
        Optional<Product> opt = productDAO.findById(productId);
        if (opt.isPresent()) {
            return opt.get().getStock();
        }
        return 0.0;
    }

    // ─────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────
    @Override
    public void update(Product product) {
        validateProduct(product);

        // ✅ Kiểm tra product tồn tại
        if (product.getId() == null) {
            throw new IllegalArgumentException("ID sản phẩm không được null khi update");
        }

        productDAO.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sản phẩm với ID: " + product.getId()
                ));

        // ✅ Kiểm tra code mới có bị trùng với sản phẩm KHÁC không
        Optional<Product> sameCode = productDAO.findByCode(product.getCode());
        if (sameCode.isPresent() && !sameCode.get().getId().equals(product.getId())) {
            throw new IllegalArgumentException(
                    "Mã sản phẩm '" + product.getCode() + "' đã được dùng bởi sản phẩm khác"
            );
        }

        productDAO.update(product);
    }

    // ─────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────
    @Override
    public void delete(int id) {
        // ✅ Kiểm tra tồn tại trước khi xóa
        productDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sản phẩm với ID: " + id
                ));

        try {
            productDAO.delete(id);
        } catch (RuntimeException e) {
            // ✅ Bắt lỗi FOREIGN KEY từ SQLite
            if (e.getMessage() != null && e.getMessage().contains("FOREIGN KEY")) {
                throw new IllegalStateException(
                        "Không thể xóa sản phẩm đã có trong đơn hàng hoặc tồn kho"
                );
            }
            throw e;
        }
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không được null");
        }
        if (product.getCode() == null || product.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không được rỗng");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được rỗng");
        }
    }
}

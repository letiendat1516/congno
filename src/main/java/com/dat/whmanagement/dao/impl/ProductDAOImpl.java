package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.ProductDAO;
import com.dat.whmanagement.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    // SQLite lưu format: "2024-02-26 17:05:00"
    private static final DateTimeFormatter SQLITE_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ─────────────────────────────────────────
    // INSERT
    // ─────────────────────────────────────────
    @Override
    public void insert(Product product) {
        String sql = "INSERT INTO products(code, name, unit, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getCode().trim().toUpperCase());
            ps.setString(2, product.getName().trim());
            ps.setString(3, product.getUnit() != null ? product.getUnit().trim() : null);
            // ✅ Format đúng cho SQLite
            ps.setString(4, LocalDateTime.now().format(SQLITE_DATETIME));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Insert product failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────────────
    @Override
    public Optional<Product> findById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find product by id failed: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // ─────────────────────────────────────────
    // FIND BY CODE
    // ─────────────────────────────────────────
    @Override
    public Optional<Product> findByCode(String code) {
        String sql = "SELECT * FROM products WHERE code = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // ✅ Normalize code khi tìm kiếm
            ps.setString(1, code.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find product by code failed: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // ─────────────────────────────────────────
    // FIND ALL
    // ─────────────────────────────────────────
    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products ORDER BY name ASC";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find all products failed: " + e.getMessage(), e);
        }

        return products;
    }

    // ─────────────────────────────────────────
    // SEARCH BY NAME
    // ─────────────────────────────────────────
    @Override
    public List<Product> searchByName(String keyword) {
        String sql = "SELECT * FROM products WHERE name LIKE ? ORDER BY name ASC";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // ✅ Trim keyword, tránh search khoảng trắng
            ps.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Search products failed: " + e.getMessage(), e);
        }

        return products;
    }

    // ─────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────
    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET code = ?, name = ?, unit = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getCode().trim().toUpperCase());
            ps.setString(2, product.getName().trim());
            ps.setString(3, product.getUnit() != null ? product.getUnit().trim() : null);
            ps.setInt(4, product.getId());

            int affected = ps.executeUpdate();

            // ✅ Kiểm tra có update được không
            if (affected == 0) {
                throw new RuntimeException("No product found with id: " + product.getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Update product failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            int affected = ps.executeUpdate();

            // ✅ Kiểm tra có xóa được không
            if (affected == 0) {
                throw new RuntimeException("No product found with id: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Delete product failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────
    // EXISTS BY CODE
    // ─────────────────────────────────────────
    @Override
    public boolean existsByCode(String code) {
        // ✅ Dùng COUNT thay vì findByCode để nhẹ hơn
        String sql = "SELECT COUNT(1) FROM products WHERE code = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Check product code failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────
    // MAP ROW (private helper)
    // ─────────────────────────────────────────
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("unit"),
                parseDateTime(rs.getString("created_at"))
        );
        p.setStock(rs.getDouble("stock"));
        return p;
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;

        try {
            // Case 1: SQLite format "2024-02-26 17:05:00"
            return LocalDateTime.parse(raw, SQLITE_DATETIME);
        } catch (DateTimeParseException e1) {
            try {
                // Case 2: ISO format "2024-02-26T17:05:00" (fallback)
                return LocalDateTime.parse(raw);
            } catch (DateTimeParseException e2) {
                System.err.println("⚠️ Cannot parse datetime: " + raw);
                return null;
            }
        }
    }
}

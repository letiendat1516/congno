package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.StockDAO;
import com.dat.whmanagement.model.StockItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAOImpl implements StockDAO {

    @Override
    public List<StockItem> findAll() {
        String sql = """
                SELECT id, code, name, unit, stock, buy_price, sell_price
                FROM products
                ORDER BY name ASC
                """;
        List<StockItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new StockItem(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getDouble("stock"),
                        rs.getDouble("buy_price"),
                        rs.getDouble("sell_price")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find all stock failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void updateStock(int productId, double quantityDelta) {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, quantityDelta);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update stock failed: " + e.getMessage(), e);
        }
    }
}


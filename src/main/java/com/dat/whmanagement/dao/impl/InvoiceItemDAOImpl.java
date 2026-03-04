package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.InvoiceItemDAO;
import com.dat.whmanagement.model.InvoiceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceItemDAOImpl implements InvoiceItemDAO {

    @Override
    public InvoiceItem insert(InvoiceItem item) {
        String sql =
            "INSERT INTO invoice_items " +
            "(invoice_id, product_id, item_index, product_name, unit, quantity, unit_price, discount, line_total, total_price) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getInvoiceId());
            if (item.getProductId() > 0) ps.setInt(2, item.getProductId());
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, item.getIndex());
            ps.setString(4, item.getName());
            ps.setString(5, item.getUnit() != null ? item.getUnit() : "");
            ps.setInt(6, item.getQuantity());
            ps.setDouble(7, item.getUnitPrice());
            ps.setDouble(8, item.getDiscount());
            ps.setDouble(9, item.getTotalPrice());
            ps.setDouble(10, item.getTotalPrice());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Insert invoice item failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InvoiceItem> findByInvoice(int invoiceId) {
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ? ORDER BY item_index";
        List<InvoiceItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find invoice items failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void deleteByInvoice(int invoiceId) {
        String sql = "DELETE FROM invoice_items WHERE invoice_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete invoice items failed: " + e.getMessage(), e);
        }
    }

    private InvoiceItem mapRow(ResultSet rs) throws SQLException {
        InvoiceItem item = new InvoiceItem();
        item.setId(rs.getInt("id"));
        item.setInvoiceId(rs.getInt("invoice_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setIndex(rs.getInt("item_index"));
        item.setName(rs.getString("product_name"));
        item.setUnit(rs.getString("unit"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        item.setDiscount(rs.getDouble("discount"));
        double tp = rs.getDouble("total_price");
        item.setTotalPrice(tp != 0 ? tp : rs.getDouble("line_total"));
        return item;
    }
}


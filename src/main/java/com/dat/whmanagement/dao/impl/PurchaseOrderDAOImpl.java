package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.PurchaseOrderDAO;
import com.dat.whmanagement.model.PurchaseOrder;
import com.dat.whmanagement.model.PurchaseOrderDetail;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderDAOImpl implements PurchaseOrderDAO {

    @Override
    public void insert(PurchaseOrder order) {
        String sqlOrder = """
                INSERT INTO purchase_orders(order_number, supplier_id, order_date, total_amount, note)
                VALUES (?,?,?,?,?)
                """;
        String sqlDetail = """
                INSERT INTO purchase_order_details(purchase_order_id, product_id, quantity, unit_price, total)
                VALUES (?,?,?,?,?)
                """;
        String sqlStock = "UPDATE products SET stock = stock + ? WHERE id = ?";
        String sqlMovement = """
                INSERT INTO stock_movements(product_id, movement_date, reference_type, reference_id, quantity, unit_price, note)
                VALUES (?,?,?,?,?,?,?)
                """;

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert order header
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNumber());
                ps.setInt(2, order.getSupplierId());
                ps.setString(3, order.getOrderDate().toString());
                ps.setDouble(4, order.getTotalAmount());
                ps.setString(5, order.getNote());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    orderId = rs.getInt(1);
                    order.setId(orderId);
                }
            }

            // 2. Insert details + update stock + movement
            for (PurchaseOrderDetail d : order.getDetails()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, d.getProductId());
                    ps.setDouble(3, d.getQuantity());
                    ps.setDouble(4, d.getUnitPrice());
                    ps.setDouble(5, d.getTotal());
                    ps.executeUpdate();
                }
                // Update stock
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setDouble(1, d.getQuantity());
                    ps.setInt(2, d.getProductId());
                    ps.executeUpdate();
                }
                // Stock movement
                try (PreparedStatement ps = conn.prepareStatement(sqlMovement)) {
                    ps.setInt(1, d.getProductId());
                    ps.setString(2, order.getOrderDate().toString());
                    ps.setString(3, "PURCHASE");
                    ps.setInt(4, orderId);
                    ps.setDouble(5, d.getQuantity());
                    ps.setDouble(6, d.getUnitPrice());
                    ps.setString(7, "Nhập hàng: " + order.getOrderNumber());
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Insert purchase order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Optional<PurchaseOrder> findById(int id) {
        String sql = """
                SELECT po.*, s.name AS supplier_name
                FROM purchase_orders po
                LEFT JOIN suppliers s ON s.id = po.supplier_id
                WHERE po.id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find purchase order failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<PurchaseOrder> findAll() {
        String sql = """
                SELECT po.*, s.name AS supplier_name
                FROM purchase_orders po
                LEFT JOIN suppliers s ON s.id = po.supplier_id
                ORDER BY po.order_date DESC, po.id DESC
                """;
        List<PurchaseOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Find all purchase orders failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<PurchaseOrderDetail> findDetailsByOrderId(int orderId) {
        String sql = """
                SELECT pod.*, p.code AS product_code, p.name AS product_name, p.unit
                FROM purchase_order_details pod
                LEFT JOIN products p ON p.id = pod.product_id
                WHERE pod.purchase_order_id = ?
                ORDER BY pod.id
                """;
        List<PurchaseOrderDetail> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseOrderDetail d = new PurchaseOrderDetail();
                    d.setId(rs.getInt("id"));
                    d.setPurchaseOrderId(orderId);
                    d.setProductId(rs.getInt("product_id"));
                    d.setProductCode(rs.getString("product_code"));
                    d.setProductName(rs.getString("product_name"));
                    d.setUnit(rs.getString("unit"));
                    d.setQuantity(rs.getDouble("quantity"));
                    d.setUnitPrice(rs.getDouble("unit_price"));
                    d.setTotal(rs.getDouble("total"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find purchase order details failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM purchase_orders WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete purchase order failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String nextOrderNumber() {
        String sql = "SELECT MAX(CAST(SUBSTR(order_number, 3) AS INTEGER)) FROM purchase_orders WHERE order_number LIKE 'PN%'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = rs.next() ? rs.getInt(1) + 1 : 1;
            return String.format("PN%04d", next);
        } catch (SQLException e) {
            throw new RuntimeException("Next order number failed: " + e.getMessage(), e);
        }
    }

    private PurchaseOrder mapRow(ResultSet rs) throws SQLException {
        PurchaseOrder o = new PurchaseOrder();
        o.setId(rs.getInt("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setSupplierId(rs.getInt("supplier_id"));
        o.setSupplierName(rs.getString("supplier_name"));
        String dateStr = rs.getString("order_date");
        if (dateStr != null) o.setOrderDate(LocalDate.parse(dateStr));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setNote(rs.getString("note"));
        return o;
    }

    @Override
    public void update(PurchaseOrder order) {
        String sqlHeader      = "UPDATE purchase_orders SET supplier_id=?, order_date=?, total_amount=?, note=? WHERE id=?";
        String sqlOldDetails  = "SELECT product_id, quantity FROM purchase_order_details WHERE purchase_order_id=?";
        String sqlDelDetails  = "DELETE FROM purchase_order_details WHERE purchase_order_id=?";
        String sqlDelMovement = "DELETE FROM stock_movements WHERE reference_type='PURCHASE' AND reference_id=?";
        String sqlStock       = "UPDATE products SET stock = stock + ? WHERE id = ?";
        String sqlInsDetail   = "INSERT INTO purchase_order_details(purchase_order_id, product_id, quantity, unit_price, total) VALUES (?,?,?,?,?)";
        String sqlInsMovement = "INSERT INTO stock_movements(product_id, movement_date, reference_type, reference_id, quantity, unit_price, note) VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Reverse old stock (trừ lại số lượng cũ đã nhập)
            try (PreparedStatement ps = conn.prepareStatement(sqlOldDetails)) {
                ps.setInt(1, order.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try (PreparedStatement ps2 = conn.prepareStatement(sqlStock)) {
                            ps2.setDouble(1, -rs.getDouble("quantity")); // trừ lại
                            ps2.setInt(2, rs.getInt("product_id"));
                            ps2.executeUpdate();
                        }
                    }
                }
            }

            // 2. Delete old details & movements
            try (PreparedStatement ps = conn.prepareStatement(sqlDelDetails)) { ps.setInt(1, order.getId()); ps.executeUpdate(); }
            try (PreparedStatement ps = conn.prepareStatement(sqlDelMovement)) { ps.setInt(1, order.getId()); ps.executeUpdate(); }

            // 3. Update header
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setInt(1, order.getSupplierId());
                ps.setString(2, order.getOrderDate().toString());
                ps.setDouble(3, order.getTotalAmount());
                ps.setString(4, order.getNote());
                ps.setInt(5, order.getId());
                ps.executeUpdate();
            }

            // 4. Insert new details + update stock + movements
            for (PurchaseOrderDetail d : order.getDetails()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsDetail)) {
                    ps.setInt(1, order.getId()); ps.setInt(2, d.getProductId());
                    ps.setDouble(3, d.getQuantity()); ps.setDouble(4, d.getUnitPrice()); ps.setDouble(5, d.getTotal());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setDouble(1, d.getQuantity()); ps.setInt(2, d.getProductId()); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlInsMovement)) {
                    ps.setInt(1, d.getProductId()); ps.setString(2, order.getOrderDate().toString());
                    ps.setString(3, "PURCHASE"); ps.setInt(4, order.getId());
                    ps.setDouble(5, d.getQuantity()); ps.setDouble(6, d.getUnitPrice());
                    ps.setString(7, "Nhập hàng: " + order.getOrderNumber());
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Update purchase order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}


package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.SalesOrderDAO;
import com.dat.whmanagement.model.SalesOrder;
import com.dat.whmanagement.model.SalesOrderDetail;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesOrderDAOImpl implements SalesOrderDAO {

    @Override
    public void insert(SalesOrder order) {
        // ── Kiểm tra tồn kho trước khi xuất ──
        String sqlCheck = "SELECT stock, name FROM products WHERE id = ?";
        try (Connection connCheck = DatabaseConfig.getConnection()) {
            StringBuilder err = new StringBuilder();
            for (SalesOrderDetail d : order.getDetails()) {
                try (PreparedStatement ps = connCheck.prepareStatement(sqlCheck)) {
                    ps.setInt(1, d.getProductId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            double stock = rs.getDouble("stock");
                            if (stock < d.getQuantity()) {
                                err.append(String.format("\n• %s: cần %.2f, còn %.2f",
                                        rs.getString("name"), d.getQuantity(), stock));
                            }
                        }
                    }
                }
            }
            if (err.length() > 0)
                throw new IllegalStateException("Không đủ tồn kho:" + err);
        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new RuntimeException("Kiểm tra tồn kho thất bại: " + e.getMessage(), e);
        }

        String sqlOrder = """
                INSERT INTO sales_orders(order_number, customer_id, order_date, total_amount, paid_amount, note)
                VALUES (?,?,?,?,?,?)
                """;
        String sqlDetail = """
                INSERT INTO sales_order_details(sales_order_id, product_id, quantity, unit_price, total)
                VALUES (?,?,?,?,?)
                """;
        String sqlStock = "UPDATE products SET stock = stock - ? WHERE id = ?";
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
                ps.setInt(2, order.getCustomerId());
                ps.setString(3, order.getOrderDate().toString());
                ps.setDouble(4, order.getTotalAmount());
                ps.setDouble(5, order.getPaidAmount());
                ps.setString(6, order.getNote());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    orderId = rs.getInt(1);
                    order.setId(orderId);
                }
            }

            // 2. Insert details + update stock + movement
            for (SalesOrderDetail d : order.getDetails()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, d.getProductId());
                    ps.setDouble(3, d.getQuantity());
                    ps.setDouble(4, d.getUnitPrice());
                    ps.setDouble(5, d.getTotal());
                    ps.executeUpdate();
                }
                // Update stock (trừ tồn kho)
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setDouble(1, d.getQuantity());
                    ps.setInt(2, d.getProductId());
                    ps.executeUpdate();
                }
                // Stock movement (số âm = xuất kho)
                try (PreparedStatement ps = conn.prepareStatement(sqlMovement)) {
                    ps.setInt(1, d.getProductId());
                    ps.setString(2, order.getOrderDate().toString());
                    ps.setString(3, "SALE");
                    ps.setInt(4, orderId);
                    ps.setDouble(5, -d.getQuantity());
                    ps.setDouble(6, d.getUnitPrice());
                    ps.setString(7, "Xuất hàng: " + order.getOrderNumber());
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Insert sales order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Optional<SalesOrder> findById(int id) {
        String sql = """
                SELECT so.*, c.name AS customer_name
                FROM sales_orders so
                LEFT JOIN customers c ON c.id = so.customer_id
                WHERE so.id = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find sales order failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SalesOrder> findAll() {
        String sql = """
                SELECT so.*, c.name AS customer_name
                FROM sales_orders so
                LEFT JOIN customers c ON c.id = so.customer_id
                ORDER BY so.order_date DESC, so.id DESC
                """;
        List<SalesOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Find all sales orders failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<SalesOrderDetail> findDetailsByOrderId(int orderId) {
        String sql = """
                SELECT sod.*, p.code AS product_code, p.name AS product_name, p.unit
                FROM sales_order_details sod
                LEFT JOIN products p ON p.id = sod.product_id
                WHERE sod.sales_order_id = ?
                ORDER BY sod.id
                """;
        List<SalesOrderDetail> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalesOrderDetail d = new SalesOrderDetail();
                    d.setId(rs.getInt("id"));
                    d.setSalesOrderId(orderId);
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
            throw new RuntimeException("Find sales order details failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM sales_orders WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete sales order failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String nextOrderNumber() {
        String sql = "SELECT MAX(CAST(SUBSTR(order_number, 3) AS INTEGER)) FROM sales_orders WHERE order_number LIKE 'PX%'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = rs.next() ? rs.getInt(1) + 1 : 1;
            return String.format("PX%04d", next);
        } catch (SQLException e) {
            throw new RuntimeException("Next order number failed: " + e.getMessage(), e);
        }
    }

    private SalesOrder mapRow(ResultSet rs) throws SQLException {
        SalesOrder o = new SalesOrder();
        o.setId(rs.getInt("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setCustomerName(rs.getString("customer_name"));
        String dateStr = rs.getString("order_date");
        if (dateStr != null) o.setOrderDate(LocalDate.parse(dateStr));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setPaidAmount(rs.getDouble("paid_amount"));
        o.setNote(rs.getString("note"));
        return o;
    }

    @Override
    public void update(SalesOrder order) {
        String sqlHeader      = "UPDATE sales_orders SET customer_id=?, order_date=?, total_amount=?, paid_amount=?, note=? WHERE id=?";
        String sqlOldDetails  = "SELECT product_id, quantity FROM sales_order_details WHERE sales_order_id=?";
        String sqlDelDetails  = "DELETE FROM sales_order_details WHERE sales_order_id=?";
        String sqlDelMovement = "DELETE FROM stock_movements WHERE reference_type='SALE' AND reference_id=?";
        String sqlStock       = "UPDATE products SET stock = stock + ? WHERE id = ?";
        String sqlInsDetail   = "INSERT INTO sales_order_details(sales_order_id, product_id, quantity, unit_price, total) VALUES (?,?,?,?,?)";
        String sqlInsMovement = "INSERT INTO stock_movements(product_id, movement_date, reference_type, reference_id, quantity, unit_price, note) VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Reverse old stock (cộng lại vì xuất = trừ)
            try (PreparedStatement ps = conn.prepareStatement(sqlOldDetails)) {
                ps.setInt(1, order.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try (PreparedStatement ps2 = conn.prepareStatement(sqlStock)) {
                            ps2.setDouble(1, rs.getDouble("quantity")); // cộng lại
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
                ps.setInt(1, order.getCustomerId());
                ps.setString(2, order.getOrderDate().toString());
                ps.setDouble(3, order.getTotalAmount());
                ps.setDouble(4, order.getPaidAmount());
                ps.setString(5, order.getNote());
                ps.setInt(6, order.getId());
                ps.executeUpdate();
            }

            // 4. Insert new details + trừ stock + movements
            for (SalesOrderDetail d : order.getDetails()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsDetail)) {
                    ps.setInt(1, order.getId()); ps.setInt(2, d.getProductId());
                    ps.setDouble(3, d.getQuantity()); ps.setDouble(4, d.getUnitPrice()); ps.setDouble(5, d.getTotal());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setDouble(1, -d.getQuantity()); ps.setInt(2, d.getProductId()); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlInsMovement)) {
                    ps.setInt(1, d.getProductId()); ps.setString(2, order.getOrderDate().toString());
                    ps.setString(3, "SALE"); ps.setInt(4, order.getId());
                    ps.setDouble(5, -d.getQuantity()); ps.setDouble(6, d.getUnitPrice());
                    ps.setString(7, "Xuất hàng: " + order.getOrderNumber());
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Update sales order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}


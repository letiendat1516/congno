package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.ReturnOrderDAO;
import com.dat.whmanagement.model.ReturnOrder;
import com.dat.whmanagement.model.ReturnOrderDetail;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReturnOrderDAOImpl implements ReturnOrderDAO {

    @Override
    public void insert(ReturnOrder order) {
        String sqlOrder  = "INSERT INTO return_orders(order_number, customer_id, customer_name, return_date, total_amount, note) VALUES (?,?,?,?,?,?)";
        String sqlDetail = "INSERT INTO return_order_details(return_order_id, product_id, quantity, unit_price, total) VALUES (?,?,?,?,?)";
        String sqlStock  = "UPDATE products SET stock = stock + ? WHERE id = ?";
        String sqlMove   = "INSERT INTO stock_movements(product_id, movement_date, reference_type, reference_id, quantity, unit_price, note) VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNumber()); ps.setInt(2, order.getCustomerId());
                ps.setString(3, order.getCustomerName()); ps.setString(4, order.getReturnDate().toString());
                ps.setDouble(5, order.getTotalAmount()); ps.setString(6, order.getNote());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); orderId = rs.getInt(1); order.setId(orderId); }
            }

            for (ReturnOrderDetail d : order.getDetails()) {
                // Insert detail
                try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
                    ps.setInt(1, orderId); ps.setInt(2, d.getProductId());
                    ps.setDouble(3, d.getQuantity()); ps.setDouble(4, d.getUnitPrice()); ps.setDouble(5, d.getTotal());
                    ps.executeUpdate();
                }
                // Cộng lại tồn kho (trả hàng = nhập lại kho)
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setDouble(1, d.getQuantity()); ps.setInt(2, d.getProductId()); ps.executeUpdate();
                }
                // Ghi movement (dương = nhập lại)
                try (PreparedStatement ps = conn.prepareStatement(sqlMove)) {
                    ps.setInt(1, d.getProductId()); ps.setString(2, order.getReturnDate().toString());
                    ps.setString(3, "ADJUST"); ps.setInt(4, orderId);
                    ps.setDouble(5, d.getQuantity()); ps.setDouble(6, d.getUnitPrice());
                    ps.setString(7, "Trả hàng: " + order.getOrderNumber()); ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Insert return order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Optional<ReturnOrder> findById(int id) {
        String sql = "SELECT ro.*, c.name AS cname FROM return_orders ro LEFT JOIN customers c ON c.id = ro.customer_id WHERE ro.id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public List<ReturnOrder> findAll() {
        String sql = "SELECT ro.*, c.name AS cname FROM return_orders ro LEFT JOIN customers c ON c.id = ro.customer_id ORDER BY ro.return_date DESC, ro.id DESC";
        List<ReturnOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public List<ReturnOrderDetail> findDetailsByOrderId(int orderId) {
        String sql = "SELECT rod.*, p.code AS pcode, p.name AS pname, p.unit FROM return_order_details rod LEFT JOIN products p ON p.id = rod.product_id WHERE rod.return_order_id = ? ORDER BY rod.id";
        List<ReturnOrderDetail> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReturnOrderDetail d = new ReturnOrderDetail();
                    d.setId(rs.getInt("id")); d.setReturnOrderId(orderId);
                    d.setProductId(rs.getInt("product_id"));
                    d.setProductCode(rs.getString("pcode")); d.setProductName(rs.getString("pname"));
                    d.setUnit(rs.getString("unit")); d.setQuantity(rs.getDouble("quantity"));
                    d.setUnitPrice(rs.getDouble("unit_price")); d.setTotal(rs.getDouble("total"));
                    list.add(d);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public String nextOrderNumber() {
        String sql = "SELECT MAX(CAST(SUBSTR(order_number, 4) AS INTEGER)) FROM return_orders WHERE order_number LIKE 'TRH%'";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int next = rs.next() ? rs.getInt(1) + 1 : 1;
            return String.format("TRH%04d", next);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(int orderId) {
        String sqlOld  = "SELECT product_id, quantity FROM return_order_details WHERE return_order_id=?";
        String sqlStock = "UPDATE products SET stock = stock - ? WHERE id = ?";
        String sqlDelMov = "DELETE FROM stock_movements WHERE reference_type='ADJUST' AND reference_id=?";
        String sqlDel   = "DELETE FROM return_orders WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            // Reverse stock (trả hàng đã cộng kho, nên trừ lại)
            try (PreparedStatement ps = conn.prepareStatement(sqlOld)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try (PreparedStatement ps2 = conn.prepareStatement(sqlStock)) {
                            ps2.setDouble(1, rs.getDouble("quantity"));
                            ps2.setInt(2, rs.getInt("product_id"));
                            ps2.executeUpdate();
                        }
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDelMov)) { ps.setInt(1, orderId); ps.executeUpdate(); }
            try (PreparedStatement ps = conn.prepareStatement(sqlDel))    { ps.setInt(1, orderId); ps.executeUpdate(); }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Delete return order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    private ReturnOrder mapRow(ResultSet rs) throws SQLException {
        ReturnOrder o = new ReturnOrder();
        o.setId(rs.getInt("id")); o.setOrderNumber(rs.getString("order_number"));
        o.setCustomerId(rs.getInt("customer_id")); o.setCustomerName(rs.getString("cname"));
        String d = rs.getString("return_date"); if (d != null) o.setReturnDate(LocalDate.parse(d));
        o.setTotalAmount(rs.getDouble("total_amount")); o.setNote(rs.getString("note"));
        return o;
    }

    @Override
    public void update(ReturnOrder order) {
        String sqlHeader      = "UPDATE return_orders SET customer_id=?, customer_name=?, return_date=?, total_amount=?, note=? WHERE id=?";
        String sqlOldDetails  = "SELECT product_id, quantity FROM return_order_details WHERE return_order_id=?";
        String sqlDelDetails  = "DELETE FROM return_order_details WHERE return_order_id=?";
        String sqlDelMovement = "DELETE FROM stock_movements WHERE reference_type='ADJUST' AND reference_id=?";
        String sqlStock       = "UPDATE products SET stock = stock + ? WHERE id = ?";
        String sqlInsDetail   = "INSERT INTO return_order_details(return_order_id, product_id, quantity, unit_price, total) VALUES (?,?,?,?,?)";
        String sqlInsMovement = "INSERT INTO stock_movements(product_id, movement_date, reference_type, reference_id, quantity, unit_price, note) VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Reverse old stock (trả hàng cũ = đã cộng kho, nên trừ lại)
            try (PreparedStatement ps = conn.prepareStatement(sqlOldDetails)) {
                ps.setInt(1, order.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try (PreparedStatement ps2 = conn.prepareStatement(sqlStock)) {
                            ps2.setDouble(1, -rs.getDouble("quantity"));
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
                ps.setString(2, order.getCustomerName());
                ps.setString(3, order.getReturnDate().toString());
                ps.setDouble(4, order.getTotalAmount());
                ps.setString(5, order.getNote());
                ps.setInt(6, order.getId());
                ps.executeUpdate();
            }

            // 4. Insert new details + cộng stock lại + movements
            for (ReturnOrderDetail det : order.getDetails()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsDetail)) {
                    ps.setInt(1, order.getId()); ps.setInt(2, det.getProductId());
                    ps.setDouble(3, det.getQuantity()); ps.setDouble(4, det.getUnitPrice()); ps.setDouble(5, det.getTotal());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setDouble(1, det.getQuantity()); ps.setInt(2, det.getProductId()); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlInsMovement)) {
                    ps.setInt(1, det.getProductId()); ps.setString(2, order.getReturnDate().toString());
                    ps.setString(3, "ADJUST"); ps.setInt(4, order.getId());
                    ps.setDouble(5, det.getQuantity()); ps.setDouble(6, det.getUnitPrice());
                    ps.setString(7, "Trả hàng: " + order.getOrderNumber());
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Update return order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}


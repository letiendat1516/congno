package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.PendingOrderDAO;
import com.dat.whmanagement.model.PendingOrder;
import com.dat.whmanagement.model.PendingOrderDetail;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PendingOrderDAOImpl implements PendingOrderDAO {

    @Override
    public void insert(PendingOrder order) {
        String sqlOrder = """
                INSERT INTO pending_orders(order_number, customer_id, customer_name,
                    order_date, expected_date, total_amount, vat_rate, status, note)
                VALUES (?,?,?,?,?,?,?,?,?)
                """;
        String sqlDetail = """
                INSERT INTO pending_order_details(pending_order_id, product_id, quantity, unit_price, total)
                VALUES (?,?,?,?,?)
                """;
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNumber());
                ps.setInt   (2, order.getCustomerId());
                ps.setString(3, order.getCustomerName());
                ps.setString(4, order.getOrderDate().toString());
                ps.setString(5, order.getExpectedDate() != null ? order.getExpectedDate().toString() : null);
                ps.setDouble(6, order.getTotalAmount());
                ps.setDouble(7, order.getVatRate());
                ps.setString(8, order.getStatus() != null ? order.getStatus() : "PENDING");
                ps.setString(9, order.getNote());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); orderId = rs.getInt(1); order.setId(orderId); }
            }
            for (PendingOrderDetail d : order.getDetails()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
                    ps.setInt(1, orderId); ps.setInt(2, d.getProductId());
                    ps.setDouble(3, d.getQuantity()); ps.setDouble(4, d.getUnitPrice()); ps.setDouble(5, d.getTotal());
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Insert pending order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public Optional<PendingOrder> findById(int id) {
        String sql = "SELECT po.*, c.name AS cname FROM pending_orders po LEFT JOIN customers c ON c.id = po.customer_id WHERE po.id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return Optional.of(mapRow(rs)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public List<PendingOrder> findAll() {
        String sql = """
                SELECT po.*, c.name AS cname
                FROM pending_orders po
                LEFT JOIN customers c ON c.id = po.customer_id
                ORDER BY po.order_date DESC, po.id DESC
                """;
        List<PendingOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public List<PendingOrderDetail> findDetailsByOrderId(int orderId) {
        String sql = """
                SELECT pod.*, p.code AS pcode, p.name AS pname, p.unit
                FROM pending_order_details pod
                LEFT JOIN products p ON p.id = pod.product_id
                WHERE pod.pending_order_id = ?
                ORDER BY pod.id
                """;
        List<PendingOrderDetail> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PendingOrderDetail d = new PendingOrderDetail();
                    d.setId(rs.getInt("id")); d.setPendingOrderId(orderId);
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
    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE pending_orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, orderId); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(int orderId) {
        String sql = "DELETE FROM pending_orders WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Delete pending order failed: " + e.getMessage(), e); }
    }

    @Override
    public String nextOrderNumber() {
        String sql = "SELECT MAX(CAST(SUBSTR(order_number, 4) AS INTEGER)) FROM pending_orders WHERE order_number LIKE 'DDH%'";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int next = rs.next() ? rs.getInt(1) + 1 : 1;
            return String.format("DDH%04d", next);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private PendingOrder mapRow(ResultSet rs) throws SQLException {
        PendingOrder o = new PendingOrder();
        o.setId(rs.getInt("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setCustomerName(rs.getString("cname"));
        String d1 = rs.getString("order_date");    if (d1 != null) o.setOrderDate(LocalDate.parse(d1));
        String d2 = rs.getString("expected_date"); if (d2 != null) o.setExpectedDate(LocalDate.parse(d2));
        o.setTotalAmount(rs.getDouble("total_amount"));
        try { o.setVatRate(rs.getDouble("vat_rate")); } catch (SQLException ignored) { o.setVatRate(10); }
        o.setStatus(rs.getString("status"));
        o.setNote(rs.getString("note"));
        return o;
    }
}


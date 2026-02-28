package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.DebtDAO;
import com.dat.whmanagement.model.DebtSummary;
import com.dat.whmanagement.model.OrderLine;
import com.dat.whmanagement.model.PaymentRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DebtDAOImpl implements DebtDAO {

    // ─────────────────────────────────────────
    // CUSTOMER DEBTS
    // Tổng hợp từ sales_orders: total_amount - SUM(payments)
    // ─────────────────────────────────────────
    @Override
    public List<DebtSummary> findCustomerDebts() {
        String sql = """
                SELECT c.id,
                       c.code,
                       c.name,
                       c.phone,
                       COALESCE(SUM(so.total_amount), 0)                          AS total_amount,
                       COALESCE(SUM(so.paid_amount), 0)
                           + COALESCE((SELECT SUM(p.amount)
                                       FROM payments p
                                       WHERE p.target_type = 'CUSTOMER'
                                         AND p.target_id   = c.id), 0)            AS paid_amount
                FROM customers c
                         LEFT JOIN sales_orders so ON so.customer_id = c.id
                GROUP BY c.id
                HAVING COALESCE(SUM(so.total_amount), 0) > 0
                ORDER BY (COALESCE(SUM(so.total_amount), 0)
                    - COALESCE(SUM(so.paid_amount), 0)
                    - COALESCE((SELECT SUM(p.amount)
                                FROM payments p
                                WHERE p.target_type = 'CUSTOMER'
                                  AND p.target_id   = c.id), 0)) DESC
                """;
        return query(sql, DebtSummary.TargetType.CUSTOMER);
    }

    // ─────────────────────────────────────────
    // SUPPLIER DEBTS
    // Tổng hợp từ purchase_orders: total_amount - SUM(payments)
    // ─────────────────────────────────────────
    @Override
    public List<DebtSummary> findSupplierDebts() {
        String sql = """
                SELECT s.id,
                       s.code,
                       s.name,
                       s.phone,
                       COALESCE(SUM(po.total_amount), 0)                          AS total_amount,
                       COALESCE((SELECT SUM(p.amount)
                                 FROM payments p
                                 WHERE p.target_type = 'SUPPLIER'
                                   AND p.target_id   = s.id), 0)                  AS paid_amount
                FROM suppliers s
                         LEFT JOIN purchase_orders po ON po.supplier_id = s.id
                GROUP BY s.id
                HAVING COALESCE(SUM(po.total_amount), 0) > 0
                ORDER BY (COALESCE(SUM(po.total_amount), 0)
                    - COALESCE((SELECT SUM(p.amount)
                                FROM payments p
                                WHERE p.target_type = 'SUPPLIER'
                                  AND p.target_id   = s.id), 0)) DESC
                """;
        return query(sql, DebtSummary.TargetType.SUPPLIER);
    }

    // ─────────────────────────────────────────
    // RECORD PAYMENT
    // ─────────────────────────────────────────
    @Override
    public void recordPayment(String targetType, int targetId, double amount, String note) {
        String sql = """
                INSERT INTO payments(payment_date, target_type, target_id, amount, note)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, LocalDate.now().toString());
            ps.setString(2, targetType);
            ps.setInt   (3, targetId);
            ps.setDouble(4, amount);
            ps.setString(5, note);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Record payment failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────
    // UPDATE PAYMENT
    // ─────────────────────────────────────────
    @Override
    public void updatePayment(int paymentId, double amount, String note) {
        String sql = "UPDATE payments SET amount = ?, note = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, note);
            ps.setInt   (3, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update payment failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────
    // DELETE PAYMENT
    // ─────────────────────────────────────────
    @Override
    public void deletePayment(int paymentId) {
        String sql = "DELETE FROM payments WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete payment failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────
    // FIND ORDER LINES
    // ─────────────────────────────────────────
    @Override
    public List<OrderLine> findOrderLines(String targetType, int targetId) {
        List<OrderLine> list = new ArrayList<>();

        if ("CUSTOMER".equals(targetType)) {
            // Lấy từ sales_order_details JOIN sales_orders JOIN products
            String sql = """
                    SELECT so.id        AS order_id,
                           so.order_number,
                           so.order_date,
                           so.total_amount  AS order_total,
                           so.paid_amount   AS order_paid,
                           p.code           AS product_code,
                           p.name           AS product_name,
                           p.unit,
                           sod.quantity,
                           sod.unit_price,
                           sod.total        AS line_total
                    FROM sales_orders so
                    JOIN sales_order_details sod ON sod.sales_order_id = so.id
                    JOIN products p              ON p.id = sod.product_id
                    WHERE so.customer_id = ?
                    ORDER BY so.order_date DESC, so.id DESC
                    """;
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, targetId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        OrderLine ol = new OrderLine();
                        ol.setOrderType("SALE");
                        ol.setOrderId(rs.getInt("order_id"));
                        ol.setOrderNumber(rs.getString("order_number"));
                        String d = rs.getString("order_date");
                        if (d != null) ol.setOrderDate(LocalDate.parse(d));
                        ol.setProductCode(rs.getString("product_code"));
                        ol.setProductName(rs.getString("product_name"));
                        ol.setUnit(rs.getString("unit"));
                        ol.setQuantity(rs.getDouble("quantity"));
                        ol.setUnitPrice(rs.getDouble("unit_price"));
                        ol.setLineTotal(rs.getDouble("line_total"));
                        ol.setOrderTotal(rs.getDouble("order_total"));
                        ol.setOrderPaid(rs.getDouble("order_paid"));
                        ol.setOrderDebt(ol.getOrderTotal() - ol.getOrderPaid());
                        list.add(ol);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("findOrderLines CUSTOMER failed: " + e.getMessage(), e);
            }
        } else {
            // SUPPLIER: purchase_order_details JOIN purchase_orders JOIN products
            String sql = """
                    SELECT po.id        AS order_id,
                           po.order_number,
                           po.order_date,
                           po.total_amount  AS order_total,
                           0.0             AS order_paid,
                           p.code          AS product_code,
                           p.name          AS product_name,
                           p.unit,
                           pod.quantity,
                           pod.unit_price,
                           pod.total       AS line_total
                    FROM purchase_orders po
                    JOIN purchase_order_details pod ON pod.purchase_order_id = po.id
                    JOIN products p                 ON p.id = pod.product_id
                    WHERE po.supplier_id = ?
                    ORDER BY po.order_date DESC, po.id DESC
                    """;
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, targetId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        OrderLine ol = new OrderLine();
                        ol.setOrderType("PURCHASE");
                        ol.setOrderId(rs.getInt("order_id"));
                        ol.setOrderNumber(rs.getString("order_number"));
                        String d = rs.getString("order_date");
                        if (d != null) ol.setOrderDate(LocalDate.parse(d));
                        ol.setProductCode(rs.getString("product_code"));
                        ol.setProductName(rs.getString("product_name"));
                        ol.setUnit(rs.getString("unit"));
                        ol.setQuantity(rs.getDouble("quantity"));
                        ol.setUnitPrice(rs.getDouble("unit_price"));
                        ol.setLineTotal(rs.getDouble("line_total"));
                        ol.setOrderTotal(rs.getDouble("order_total"));
                        ol.setOrderPaid(rs.getDouble("order_paid"));
                        ol.setOrderDebt(ol.getOrderTotal() - ol.getOrderPaid());
                        list.add(ol);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("findOrderLines SUPPLIER failed: " + e.getMessage(), e);
            }
        }
        return list;
    }

    // ─────────────────────────────────────────
    // FIND PAYMENT HISTORY
    // ─────────────────────────────────────────
    @Override
    public List<PaymentRecord> findPaymentHistory(String targetType, int targetId) {
        String sql = """
                SELECT id, payment_date, amount, note
                FROM payments
                WHERE target_type = ? AND target_id = ?
                ORDER BY payment_date DESC, id DESC
                """;
        List<PaymentRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetType);
            ps.setInt   (2, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PaymentRecord pr = new PaymentRecord();
                    pr.setId(rs.getInt("id"));
                    String d = rs.getString("payment_date");
                    if (d != null) pr.setPaymentDate(LocalDate.parse(d));
                    pr.setAmount(rs.getDouble("amount"));
                    pr.setNote(rs.getString("note"));
                    pr.setTargetType(targetType);
                    pr.setTargetId(targetId);
                    list.add(pr);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("findPaymentHistory failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ─────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────
    private List<DebtSummary> query(String sql, DebtSummary.TargetType type) {
        List<DebtSummary> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new DebtSummary(
                        rs.getInt   ("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        type,
                        rs.getDouble("total_amount"),
                        rs.getDouble("paid_amount")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query debts failed: " + e.getMessage(), e);
        }
        return list;
    }
}


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
        String sqlOrder  = "INSERT INTO return_orders(order_number, customer_id, customer_name, return_date, total_amount, note, deducted_from_total, deducted_from_paid) VALUES (?,?,?,?,?,?,?,?)";
        String sqlDetail = "INSERT INTO return_order_details(return_order_id, product_id, quantity, unit_price, total) VALUES (?,?,?,?,?)";
        String sqlStock  = "UPDATE products SET stock = stock + ? WHERE id = ?";
        String sqlMove   = "INSERT INTO stock_movements(product_id, movement_date, reference_type, reference_id, quantity, unit_price, note) VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Tính trừ bao nhiêu vào đâu
            // Còn nợ → chỉ giảm total_amount (nợ giảm, paid giữ nguyên)
            // Không còn nợ → giảm CẢ total_amount VÀ paid_amount (nợ vẫn = 0)
            double returnAmount = order.getTotalAmount();
            double debt = getCustomerDebt(conn, order.getCustomerId());
            double fromTotal;  // phần giảm total_amount
            double fromPaid;   // phần giảm paid_amount

            if (debt >= returnAmount) {
                // Nợ đủ lớn → chỉ trừ total, nợ giảm
                fromTotal = returnAmount;
                fromPaid  = 0;
            } else {
                // Nợ < tiền trả → trừ hết nợ + phần dư trừ cả total lẫn paid
                fromTotal = returnAmount;                // total luôn giảm
                fromPaid  = returnAmount - debt;         // paid giảm phần không phải nợ
            }

            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNumber()); ps.setInt(2, order.getCustomerId());
                ps.setString(3, order.getCustomerName()); ps.setString(4, order.getReturnDate().toString());
                ps.setDouble(5, order.getTotalAmount()); ps.setString(6, order.getNote());
                ps.setDouble(7, fromTotal); ps.setDouble(8, fromPaid);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); orderId = rs.getInt(1); order.setId(orderId); }
            }

            for (ReturnOrderDetail d : order.getDetails()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
                    ps.setInt(1, orderId); ps.setInt(2, d.getProductId());
                    ps.setDouble(3, d.getQuantity()); ps.setDouble(4, d.getUnitPrice()); ps.setDouble(5, d.getTotal());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlStock)) {
                    ps.setDouble(1, d.getQuantity()); ps.setInt(2, d.getProductId()); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlMove)) {
                    ps.setInt(1, d.getProductId()); ps.setString(2, order.getReturnDate().toString());
                    ps.setString(3, "ADJUST"); ps.setInt(4, orderId);
                    ps.setDouble(5, d.getQuantity()); ps.setDouble(6, d.getUnitPrice());
                    ps.setString(7, "Trả hàng: " + order.getOrderNumber()); ps.executeUpdate();
                }
            }

            // Trừ trực tiếp vào sales_orders
            if (fromTotal > 0.001) reduceColumn(conn, order.getCustomerId(), "total_amount", fromTotal);
            if (fromPaid  > 0.001) reduceColumn(conn, order.getCustomerId(), "paid_amount", fromPaid);

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
        String sqlGetInfo = "SELECT customer_id, total_amount, deducted_from_total, deducted_from_paid FROM return_orders WHERE id=?";
        String sqlOld     = "SELECT product_id, quantity FROM return_order_details WHERE return_order_id=?";
        String sqlStock   = "UPDATE products SET stock = stock - ? WHERE id = ?";
        String sqlDelMov  = "DELETE FROM stock_movements WHERE reference_type='ADJUST' AND reference_id=?";
        String sqlDelDet  = "DELETE FROM return_order_details WHERE return_order_id=?";
        String sqlDel     = "DELETE FROM return_orders WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Lấy thông tin phiếu trả để hoàn ngược chính xác
            int customerId = 0;
            double totalAmount = 0, fromTotal = 0, fromPaid = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlGetInfo)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        customerId  = rs.getInt("customer_id");
                        totalAmount = rs.getDouble("total_amount");
                        fromTotal   = rs.getDouble("deducted_from_total");
                        fromPaid    = rs.getDouble("deducted_from_paid");
                    }
                }
            }

            // Phiếu trả cũ (trước V3) chưa có tracking → fallback
            if (fromTotal < 0.001 && fromPaid < 0.001 && totalAmount > 0.001) {
                fromTotal = totalAmount;
                fromPaid  = totalAmount; // cộng lại cả total lẫn paid (như chưa trả)
            }

            // Reverse stock
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

            // Hoàn ngược: cộng lại đúng cột đã trừ
            if (fromTotal > 0.001) addBackColumn(conn, customerId, "total_amount", fromTotal);
            if (fromPaid  > 0.001) addBackColumn(conn, customerId, "paid_amount", fromPaid);

            // Xóa chi tiết và phiếu trả
            try (PreparedStatement ps = conn.prepareStatement(sqlDelDet)) { ps.setInt(1, orderId); ps.executeUpdate(); }
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
        try { o.setDeductedFromTotal(rs.getDouble("deducted_from_total")); } catch (SQLException ignored) {}
        try { o.setDeductedFromPaid(rs.getDouble("deducted_from_paid")); }   catch (SQLException ignored) {}
        return o;
    }

    @Override
    public void update(ReturnOrder order) {
        String sqlGetOld      = "SELECT customer_id, deducted_from_total, deducted_from_paid FROM return_orders WHERE id=?";
        String sqlHeader      = "UPDATE return_orders SET customer_id=?, customer_name=?, return_date=?, total_amount=?, note=?, deducted_from_total=?, deducted_from_paid=? WHERE id=?";
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

            // 0. Lấy thông tin cũ để hoàn ngược
            int oldCustomerId = 0;
            double oldFromTotal = 0, oldFromPaid = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlGetOld)) {
                ps.setInt(1, order.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldCustomerId = rs.getInt("customer_id");
                        oldFromTotal = rs.getDouble("deducted_from_total");
                        oldFromPaid  = rs.getDouble("deducted_from_paid");
                    }
                }
            }

            // 1. Reverse old stock
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

            // 3. Hoàn ngược trả hàng cũ
            if (oldFromTotal > 0.001) addBackColumn(conn, oldCustomerId, "total_amount", oldFromTotal);
            if (oldFromPaid  > 0.001) addBackColumn(conn, oldCustomerId, "paid_amount", oldFromPaid);

            // 4. Tính trừ mới
            double returnAmount = order.getTotalAmount();
            double debt = getCustomerDebt(conn, order.getCustomerId());
            double newFromTotal;
            double newFromPaid;
            if (debt >= returnAmount) {
                newFromTotal = returnAmount;
                newFromPaid  = 0;
            } else {
                newFromTotal = returnAmount;
                newFromPaid  = returnAmount - debt;
            }

            // 5. Update header (kèm thông tin deducted)
            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setInt(1, order.getCustomerId());
                ps.setString(2, order.getCustomerName());
                ps.setString(3, order.getReturnDate().toString());
                ps.setDouble(4, order.getTotalAmount());
                ps.setString(5, order.getNote());
                ps.setDouble(6, newFromTotal);
                ps.setDouble(7, newFromPaid);
                ps.setInt(8, order.getId());
                ps.executeUpdate();
            }

            // 6. Insert new details + stock + movements
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

            // 7. Áp dụng trừ mới
            if (newFromTotal > 0.001) reduceColumn(conn, order.getCustomerId(), "total_amount", newFromTotal);
            if (newFromPaid  > 0.001) reduceColumn(conn, order.getCustomerId(), "paid_amount", newFromPaid);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Update return order failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Cộng lại giá trị vào cột (total_amount hoặc paid_amount) của sales_orders khi xóa/sửa phiếu trả.
     */
    private void addBackColumn(Connection conn, int customerId, String column, double amount) throws SQLException {
        double left = amount;
        // Cộng lại vào phiếu xuất gần nhất
        while (left > 0.001) {
            String sql = "SELECT id FROM sales_orders WHERE customer_id = ? ORDER BY order_date DESC LIMIT 1";
            int soId = -1;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) soId = rs.getInt("id");
                }
            }
            if (soId < 0) break;
            String sqlUpdate = "UPDATE sales_orders SET " + column + " = " + column + " + ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDouble(1, left);
                ps.setInt(2, soId);
                ps.executeUpdate();
            }
            left = 0;
        }
    }

    /**
     * Giảm 1 cột (total_amount hoặc paid_amount) từ các phiếu xuất gần nhất.
     * Lặp cho đến khi trừ hết amount hoặc không còn phiếu nào.
     */
    private void reduceColumn(Connection conn, int customerId, String column, double amount) throws SQLException {
        double left = amount;
        while (left > 0.001) {
            // Lấy phiếu gần nhất có cột > 0
            String sqlFind = "SELECT id, " + column + " FROM sales_orders WHERE customer_id = ? AND " + column + " > 0 ORDER BY order_date DESC LIMIT 1";
            int soId = -1;
            double available = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlFind)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { soId = rs.getInt("id"); available = rs.getDouble(column); }
                }
            }
            if (soId < 0) break; // Không còn phiếu nào

            double deduct = Math.min(left, available);
            String sqlUpdate = "UPDATE sales_orders SET " + column + " = " + column + " - ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDouble(1, deduct);
                ps.setInt(2, soId);
                ps.executeUpdate();
            }
            left -= deduct;
        }
    }

    /**
     * Tính nợ còn lại của khách hàng (trong cùng transaction).
     * Nợ = tổng phát sinh - đã trả (paid_amount + payments)
     */
    private double getCustomerDebt(Connection conn, int customerId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(so.total_amount), 0)
                     - COALESCE(SUM(so.paid_amount), 0)
                     - COALESCE((SELECT SUM(p.amount) FROM payments p
                                 WHERE p.target_type = 'CUSTOMER' AND p.target_id = ?), 0)
                  AS debt
                FROM sales_orders so WHERE so.customer_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Math.max(rs.getDouble("debt"), 0) : 0;
            }
        }
    }
}


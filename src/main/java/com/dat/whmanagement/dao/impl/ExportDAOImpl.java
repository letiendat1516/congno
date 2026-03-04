package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.ExportDAO;
import com.dat.whmanagement.model.ExportRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExportDAOImpl implements ExportDAO {

    @Override
    public ExportRecord insert(ExportRecord rec) {
        String sql = "INSERT INTO warehouse_exports "
                + "(invoice_id, product_id, product_name, sku, quantity, export_date) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rec.getInvoiceId());
            if (rec.getProductId() > 0) ps.setInt(2, rec.getProductId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, rec.getProductName());
            ps.setString(4, rec.getSku());
            ps.setInt(5, rec.getQuantity());
            ps.setString(6, rec.getExportDate() != null ? rec.getExportDate().toString() : LocalDate.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) rec.setId(keys.getInt(1));
            }
            return rec;
        } catch (SQLException e) {
            throw new RuntimeException("Insert export record failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExportRecord> findByInvoiceId(int invoiceId) {
        String sql = "SELECT e.*, i.invoice_number FROM warehouse_exports e "
                + "LEFT JOIN invoices i ON e.invoice_id = i.id "
                + "WHERE e.invoice_id = ? ORDER BY e.id";
        List<ExportRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding exports for invoice: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean existsByInvoiceId(int invoiceId) {
        String sql = "SELECT 1 FROM warehouse_exports WHERE invoice_id = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<ExportRecord> findAllWithInvoiceNumber() {
        String sql = "SELECT e.*, i.invoice_number FROM warehouse_exports e "
                + "LEFT JOIN invoices i ON e.invoice_id = i.id "
                + "ORDER BY e.export_date DESC, e.id DESC";
        List<ExportRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("Error fetching all exports: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteByInvoiceId(int invoiceId) {
        String sql = "DELETE FROM warehouse_exports WHERE invoice_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting exports for invoice: " + e.getMessage());
            return false;
        }
    }

    private ExportRecord mapRow(ResultSet rs) throws SQLException {
        ExportRecord rec = new ExportRecord();
        rec.setId(rs.getInt("id"));
        rec.setInvoiceId(rs.getInt("invoice_id"));
        rec.setProductId(rs.getInt("product_id"));
        rec.setProductName(rs.getString("product_name"));
        rec.setSku(rs.getString("sku"));
        rec.setQuantity(rs.getInt("quantity"));
        String date = rs.getString("export_date");
        if (date != null && !date.isBlank()) rec.setExportDate(LocalDate.parse(date));
        try { rec.setInvoiceNumber(rs.getString("invoice_number")); }
        catch (SQLException ignored) {}
        return rec;
    }
}


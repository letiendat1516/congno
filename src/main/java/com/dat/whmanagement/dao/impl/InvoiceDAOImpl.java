package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.InvoiceDAO;
import com.dat.whmanagement.model.Invoice;

import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceDAOImpl implements InvoiceDAO {

    @Override
    public Invoice insert(Invoice invoice) {
        String sql =
            "INSERT INTO invoices (" +
            "  invoice_number, invoice_symbol, invoice_form_number," +
            "  customer_id, sales_order_id, issue_date, status, notes," +
            "  seller_name, seller_tax_code, seller_address, seller_bank_account, seller_phone," +
            "  buyer_name, buyer_company, buyer_tax_code, buyer_address, buyer_bank_account, payment_method," +
            "  subtotal, vat_rate, vat_amount, total_amount, amount_in_words" +
            ") VALUES (?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?,?, ?,?,?,?,?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, invoice.getInvoiceNumber());
            ps.setString(2, invoice.getInvoiceSymbol());
            ps.setString(3, invoice.getInvoiceFormNumber());
            if (invoice.getCustomerId() > 0) ps.setInt(4, invoice.getCustomerId());
            else ps.setNull(4, Types.INTEGER);
            if (invoice.getSalesOrderId() > 0) ps.setInt(5, invoice.getSalesOrderId());
            else ps.setNull(5, Types.INTEGER);
            ps.setString(6, invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : null);
            ps.setString(7, invoice.getStatus() != null ? invoice.getStatus().name() : Invoice.Status.DRAFT.name());
            ps.setString(8, invoice.getNotes());

            ps.setString(9, invoice.getSellerName());
            ps.setString(10, invoice.getSellerTaxCode());
            ps.setString(11, invoice.getSellerAddress());
            ps.setString(12, invoice.getSellerBankAccount());
            ps.setString(13, invoice.getSellerPhone());

            ps.setString(14, invoice.getBuyerName());
            ps.setString(15, invoice.getBuyerCompany());
            ps.setString(16, invoice.getBuyerTaxCode());
            ps.setString(17, invoice.getBuyerAddress());
            ps.setString(18, invoice.getBuyerBankAccount());
            ps.setString(19, invoice.getPaymentMethod());

            ps.setDouble(20, invoice.getSubtotal());
            ps.setDouble(21, invoice.getVatRate());
            ps.setDouble(22, invoice.getVatAmount());
            ps.setDouble(23, invoice.getTotalAmount());
            ps.setString(24, invoice.getAmountInWords());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) invoice.setId(keys.getInt(1));
            }
            return invoice;
        } catch (SQLException e) {
            throw new RuntimeException("Insert invoice failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Invoice invoice) {
        String sql =
            "UPDATE invoices SET" +
            "  invoice_number=?, invoice_symbol=?, invoice_form_number=?," +
            "  customer_id=?, sales_order_id=?, issue_date=?, status=?, notes=?," +
            "  seller_name=?, seller_tax_code=?, seller_address=?, seller_bank_account=?, seller_phone=?," +
            "  buyer_name=?, buyer_company=?, buyer_tax_code=?, buyer_address=?, buyer_bank_account=?, payment_method=?," +
            "  subtotal=?, vat_rate=?, vat_amount=?, total_amount=?, amount_in_words=?" +
            " WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoice.getInvoiceNumber());
            ps.setString(2, invoice.getInvoiceSymbol());
            ps.setString(3, invoice.getInvoiceFormNumber());
            if (invoice.getCustomerId() > 0) ps.setInt(4, invoice.getCustomerId());
            else ps.setNull(4, Types.INTEGER);
            if (invoice.getSalesOrderId() > 0) ps.setInt(5, invoice.getSalesOrderId());
            else ps.setNull(5, Types.INTEGER);
            ps.setString(6, invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : null);
            ps.setString(7, invoice.getStatus() != null ? invoice.getStatus().name() : null);
            ps.setString(8, invoice.getNotes());
            ps.setString(9, invoice.getSellerName());
            ps.setString(10, invoice.getSellerTaxCode());
            ps.setString(11, invoice.getSellerAddress());
            ps.setString(12, invoice.getSellerBankAccount());
            ps.setString(13, invoice.getSellerPhone());
            ps.setString(14, invoice.getBuyerName());
            ps.setString(15, invoice.getBuyerCompany());
            ps.setString(16, invoice.getBuyerTaxCode());
            ps.setString(17, invoice.getBuyerAddress());
            ps.setString(18, invoice.getBuyerBankAccount());
            ps.setString(19, invoice.getPaymentMethod());
            ps.setDouble(20, invoice.getSubtotal());
            ps.setDouble(21, invoice.getVatRate());
            ps.setDouble(22, invoice.getVatAmount());
            ps.setDouble(23, invoice.getTotalAmount());
            ps.setString(24, invoice.getAmountInWords());
            ps.setInt(25, invoice.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Update invoice failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Invoice> findById(int id) {
        String sql = "SELECT * FROM invoices WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find invoice failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Invoice> findAll() {
        String sql = "SELECT * FROM invoices ORDER BY issue_date DESC";
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Find all invoices failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM invoices WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Delete invoice failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invoice> findByCustomer(int customerId) {
        String sql = "SELECT * FROM invoices WHERE customer_id = ? ORDER BY issue_date DESC";
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find invoices by customer failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public String generateInvoiceNumber() {
        int year = Year.now().getValue();
        String sql = "SELECT COUNT(*) FROM invoices WHERE invoice_number LIKE ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "INV-" + year + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                return String.format("INV-%d%04d", year, count + 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Generate invoice number failed: " + e.getMessage(), e);
        }
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setInvoiceSymbol(rs.getString("invoice_symbol"));
        inv.setInvoiceFormNumber(rs.getString("invoice_form_number"));
        inv.setCustomerId(rs.getInt("customer_id"));
        inv.setSalesOrderId(rs.getInt("sales_order_id"));

        String issueDate = rs.getString("issue_date");
        if (issueDate != null && !issueDate.isBlank()) inv.setIssueDate(LocalDate.parse(issueDate));

        String status = rs.getString("status");
        if (status != null) {
            try { inv.setStatus(Invoice.Status.valueOf(status)); }
            catch (IllegalArgumentException ignored) { inv.setStatus(Invoice.Status.DRAFT); }
        }
        inv.setNotes(rs.getString("notes"));

        inv.setSellerName(rs.getString("seller_name"));
        inv.setSellerTaxCode(rs.getString("seller_tax_code"));
        inv.setSellerAddress(rs.getString("seller_address"));
        inv.setSellerBankAccount(rs.getString("seller_bank_account"));
        inv.setSellerPhone(rs.getString("seller_phone"));

        inv.setBuyerName(rs.getString("buyer_name"));
        inv.setBuyerCompany(rs.getString("buyer_company"));
        inv.setBuyerTaxCode(rs.getString("buyer_tax_code"));
        inv.setBuyerAddress(rs.getString("buyer_address"));
        inv.setBuyerBankAccount(rs.getString("buyer_bank_account"));
        inv.setPaymentMethod(rs.getString("payment_method"));

        inv.setSubtotal(rs.getDouble("subtotal"));
        inv.setVatRate(rs.getDouble("vat_rate"));
        inv.setVatAmount(rs.getDouble("vat_amount"));
        inv.setTotalAmount(rs.getDouble("total_amount"));
        inv.setAmountInWords(rs.getString("amount_in_words"));

        return inv;
    }
}


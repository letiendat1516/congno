package com.dat.whmanagement.dao.impl;

import com.dat.whmanagement.config.DatabaseConfig;
import com.dat.whmanagement.dao.SupplierDAO;
import com.dat.whmanagement.model.Supplier;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDAOImpl implements SupplierDAO {

    private static final DateTimeFormatter SQLITE_DT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void insert(Supplier s) {
        String sql = "INSERT INTO suppliers(code, name, phone, address, created_at) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getCode() != null ? s.getCode().trim().toUpperCase() : null);
            ps.setString(2, s.getName().trim());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getAddress());
            ps.setString(5, LocalDateTime.now().format(SQLITE_DT));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Insert supplier failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Supplier> findById(int id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find supplier failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Supplier> findAll() {
        String sql = "SELECT * FROM suppliers ORDER BY name ASC";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Find all suppliers failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Supplier s) {
        String sql = "UPDATE suppliers SET code=?, name=?, phone=?, address=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getCode() != null ? s.getCode().trim().toUpperCase() : null);
            ps.setString(2, s.getName().trim());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getAddress());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update supplier failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete supplier failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByCode(String code) {
        String sql = "SELECT COUNT(1) FROM suppliers WHERE code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Check supplier code failed: " + e.getMessage(), e);
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("address"),
                parseDateTime(rs.getString("created_at"))
        );
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return LocalDateTime.parse(raw, SQLITE_DT); }
        catch (DateTimeParseException e) {
            try { return LocalDateTime.parse(raw); }
            catch (DateTimeParseException e2) { return null; }
        }
    }
}


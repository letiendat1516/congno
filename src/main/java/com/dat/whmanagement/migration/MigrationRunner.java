package com.dat.whmanagement.migration;

import com.dat.whmanagement.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class MigrationRunner {

    public static void runMigration() {
        runScript("migration/V1__init.sql");
        runScript("migration/V2__pending_return.sql");
        runScript("migration/V3__return_deduction_tracking.sql");
        runScript("migration/V4__invoice_export.sql");
        // Đảm bảo cột deducted_from_total/paid tồn tại (cho DB cũ)
        ensureReturnOrderColumns();
        // Đảm bảo cột vat_rate tồn tại trong pending_orders (cho DB cũ)
        ensurePendingOrderVatColumn();
    }

    /** Thêm cột nếu chưa có — phòng trường hợp ALTER TABLE trong V3 bị bỏ qua */
    private static void ensureReturnOrderColumns() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("SELECT deducted_from_total FROM return_orders LIMIT 1");
            } catch (Exception e) {
                // Cột chưa có → thêm
                try { stmt.execute("ALTER TABLE return_orders ADD COLUMN deducted_from_total REAL NOT NULL DEFAULT 0"); } catch (Exception ignored) {}
                try { stmt.execute("ALTER TABLE return_orders ADD COLUMN deducted_from_paid REAL NOT NULL DEFAULT 0"); } catch (Exception ignored) {}
                System.out.println("Migration: Đã thêm cột deducted_from_total/paid vào return_orders");
            }
        } catch (Exception e) {
            System.out.println("ensureReturnOrderColumns error: " + e.getMessage());
        }
    }

    private static void ensurePendingOrderVatColumn() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            try {
                stmt.execute("SELECT vat_rate FROM pending_orders LIMIT 1");
            } catch (Exception e) {
                try { stmt.execute("ALTER TABLE pending_orders ADD COLUMN vat_rate REAL NOT NULL DEFAULT 10"); } catch (Exception ignored) {}
                System.out.println("Migration: Đã thêm cột vat_rate vào pending_orders");
            }
        } catch (Exception ex) {
            System.out.println("ensurePendingOrderVatColumn error: " + ex.getMessage());
        }
    }

    private static void runScript(String resourcePath) {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            InputStream is = MigrationRunner.class.getClassLoader()
                    .getResourceAsStream(resourcePath);
            if (is == null) { System.out.println("Không tìm thấy: " + resourcePath); return; }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            for (String s : sql.split(";")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    try { stmt.execute(trimmed); }
                    catch (Exception ignored) { /* table may already exist */ }
                }
            }
            System.out.println("Migration OK: " + resourcePath);
        } catch (Exception e) {
            System.out.println("Migration error: " + resourcePath);
            e.printStackTrace();
        }
    }
}
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
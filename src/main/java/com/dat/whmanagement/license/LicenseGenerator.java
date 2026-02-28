package com.dat.whmanagement.license;

import java.util.Scanner;

/**
 * ═══════════════════════════════════════════════════
 * TOOL DÀNH CHO DEV — KHÔNG PHÁT HÀNH CHO KHÁCH HÀNG
 * ═══════════════════════════════════════════════════
 *
 * Chạy tool này trên MÁY CỦA BẠN để tạo license key cho khách.
 *
 * Cách dùng:
 * 1. Ultraview vào máy khách → chạy app → copy "Mã máy (Hardware ID)"
 * 2. Chạy tool này trên máy bạn
 * 3. Nhập Hardware ID của khách
 * 4. Chọn loại license (PERMANENT hoặc có thời hạn)
 * 5. Copy license key → dán vào app trên máy khách
 *
 * Chạy: java -cp target/classes com.dat.whmanagement.license.LicenseGenerator
 */
public class LicenseGenerator {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║   TOOL TẠO LICENSE KEY - Quản Lý Kho     ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();

        // Hardware ID hiện tại (của máy đang chạy tool)
        System.out.println("📌 Hardware ID máy hiện tại: " + HardwareId.get());
        System.out.println();

        System.out.print("Nhập Hardware ID của khách hàng: ");
        String hwId = sc.nextLine().trim();
        if (hwId.isEmpty()) {
            System.out.println("❌ Hardware ID không được để trống!");
            return;
        }

        System.out.println();
        System.out.println("Chọn loại license:");
        System.out.println("  1. PERMANENT (vĩnh viễn)");
        System.out.println("  2. Có thời hạn (nhập ngày hết hạn yyyy-MM-dd)");
        System.out.print("Lựa chọn (1/2): ");
        String choice = sc.nextLine().trim();

        String expiry;
        if ("2".equals(choice)) {
            System.out.print("Nhập ngày hết hạn (yyyy-MM-dd): ");
            expiry = sc.nextLine().trim();
        } else {
            expiry = "PERMANENT";
        }

        String key = LicenseManager.generateKey(hwId, expiry);

        System.out.println();
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("✅ LICENSE KEY ĐÃ TẠO:");
        System.out.println();
        System.out.println("  Hardware ID : " + hwId);
        System.out.println("  Loại        : " + expiry);
        System.out.println("  License Key : " + key);
        System.out.println();
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();
        System.out.println("📋 Dán key trên vào app khách hàng để kích hoạt.");
        System.out.println("   Chọn loại license: " + ("PERMANENT".equals(expiry) ? "PERMANENT" : "Có thời hạn → " + expiry));
    }
}


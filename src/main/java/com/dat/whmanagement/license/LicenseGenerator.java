package com.dat.whmanagement.license;

import java.util.Scanner;

/**
 * Tool tạo license key — chạy trong cửa sổ CMD.
 * Đóng gói với --win-console để hiện cửa sổ terminal.
 */
public class LicenseGenerator {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("  TOOL TAO LICENSE KEY - Quan Ly Kho Hang   ");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("Hardware ID may nay: " + HardwareId.get());
        System.out.println();

        System.out.print("Nhap Hardware ID cua khach hang: ");
        String hwId = sc.nextLine().trim();
        if (hwId.isEmpty()) {
            System.out.println("LOI: Hardware ID khong duoc de trong!");
            System.out.println("\nNhan Enter de thoat...");
            sc.nextLine();
            return;
        }

        System.out.println();
        System.out.println("Chon loai license:");
        System.out.println("  1. PERMANENT (vinh vien)");
        System.out.println("  2. Co thoi han (nhap ngay het han yyyy-MM-dd)");
        System.out.print("Lua chon (1/2): ");
        String choice = sc.nextLine().trim();

        String expiry;
        if ("2".equals(choice)) {
            System.out.print("Nhap ngay het han (yyyy-MM-dd, vd: 2026-12-31): ");
            expiry = sc.nextLine().trim();
        } else {
            expiry = "PERMANENT";
        }

        String key = LicenseManager.generateKey(hwId, expiry);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("  LICENSE KEY DA TAO:");
        System.out.println();
        System.out.println("  Hardware ID : " + hwId);
        System.out.println("  Han dung    : " + expiry);
        System.out.println("  License Key : " + key);
        System.out.println("==============================================");
        System.out.println();
        System.out.println("Dan key tren vao app cua khach de kich hoat.");
        System.out.println("\nNhan Enter de thoat...");
        sc.nextLine();
    }
}

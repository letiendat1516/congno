package com.dat.whmanagement.license;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Quản lý license key gắn với Hardware ID.
 *
 * License file: license.key (cùng thư mục app)
 * Nội dung file: LICENSE_KEY (1 dòng)
 *
 * License key = HMAC-SHA256(hardwareId + "|" + expiryDate, SECRET) → 32 hex chars viết hoa
 * Trong đó expiryDate = "PERMANENT" hoặc "yyyy-MM-dd"
 *
 * Flow:
 * 1. App khởi động → đọc license.key
 * 2. Tính lại HMAC từ hardwareId hiện tại
 * 3. So sánh → khớp = OK, không khớp = hiện dialog nhập license
 */
public class LicenseManager {

    // ⚠ ĐỔI SECRET NÀY THÀNH GIÁ TRỊ RIÊNG CỦA BẠN, GIỮ BÍ MẬT
    private static final String SECRET = "WHM-S3CR3T-K3Y-D4T-2026!@#";

    private static final String LICENSE_FILE = "license.key";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Kiểm tra license hiện tại có hợp lệ không.
     * @return LicenseStatus
     */
    public static LicenseStatus verify() {
        try {
            Path path = Paths.get(LICENSE_FILE);
            if (!Files.exists(path)) {
                return LicenseStatus.NO_LICENSE;
            }

            String content = Files.readString(path, StandardCharsets.UTF_8).trim();
            // File format: LINE 1 = license key, LINE 2 = expiry (PERMANENT or yyyy-MM-dd)
            String[] lines = content.split("\\R");
            if (lines.length < 2) return LicenseStatus.INVALID;

            String licenseKey = lines[0].trim();
            String expiry     = lines[1].trim();

            String hardwareId = HardwareId.get();
            String expected   = generateKey(hardwareId, expiry);

            if (!licenseKey.equalsIgnoreCase(expected)) {
                return LicenseStatus.INVALID;
            }

            // Kiểm tra hạn
            if (!"PERMANENT".equalsIgnoreCase(expiry)) {
                LocalDate expiryDate = LocalDate.parse(expiry, DATE_FMT);
                if (LocalDate.now().isAfter(expiryDate)) {
                    return LicenseStatus.EXPIRED;
                }
            }

            return LicenseStatus.VALID;
        } catch (Exception e) {
            return LicenseStatus.INVALID;
        }
    }

    /**
     * Lưu license key vào file.
     */
    public static boolean saveLicense(String licenseKey, String expiry) {
        try {
            String content = licenseKey.trim() + "\n" + expiry.trim();
            Files.writeString(Paths.get(LICENSE_FILE), content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Xóa file license (dùng khi hết hạn → buộc nhập lại key mới).
     */
    public static void deleteLicense() {
        try {
            Files.deleteIfExists(Paths.get(LICENSE_FILE));
        } catch (IOException ignored) {}
    }

    /**
     * Kiểm tra 1 license key nhập vào có đúng cho máy này không.
     * @param inputKey  license key người dùng nhập
     * @param expiry    ngày hết hạn (PERMANENT hoặc yyyy-MM-dd)
     * @return true nếu hợp lệ
     */
    public static boolean validateInput(String inputKey, String expiry) {
        String hardwareId = HardwareId.get();
        String expected   = generateKey(hardwareId, expiry);
        return inputKey.trim().equalsIgnoreCase(expected);
    }

    /**
     * Tạo license key từ hardware ID + expiry.
     * Dùng cho cả bên server (LicenseGenerator) và bên client (verify).
     */
    public static String generateKey(String hardwareId, String expiry) {
        try {
            String data = hardwareId + "|" + expiry;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Lấy 16 byte → 32 hex chars, chia nhóm 4 cho dễ đọc
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            // Format: XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX
            String raw = sb.toString();
            return raw.substring(0, 4) + "-" + raw.substring(4, 8) + "-"
                 + raw.substring(8, 12) + "-" + raw.substring(12, 16) + "-"
                 + raw.substring(16, 20) + "-" + raw.substring(20, 24) + "-"
                 + raw.substring(24, 28) + "-" + raw.substring(28, 32);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo license key", e);
        }
    }

    public enum LicenseStatus {
        VALID,       // License hợp lệ
        NO_LICENSE,  // Chưa có file license
        INVALID,     // License sai (khác máy hoặc sai key)
        EXPIRED      // License hết hạn
    }
}


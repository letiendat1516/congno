package com.dat.whmanagement.license;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Lấy Hardware ID duy nhất của máy tính (Windows).
 * Kết hợp: CPU ID + Motherboard Serial + Disk Serial
 * → Hash SHA-256 → Chuỗi 16 ký tự hex viết hoa.
 *
 * Nếu copy app sang máy khác → Hardware ID khác → License không hợp lệ.
 */
public class HardwareId {

    private static String cachedId;

    /**
     * Trả về Hardware ID duy nhất của máy (cache lại sau lần đầu).
     */
    public static String get() {
        if (cachedId != null) return cachedId;
        try {
            String cpu   = wmicQuery("wmic cpu get ProcessorId");
            String board = wmicQuery("wmic baseboard get SerialNumber");
            String disk  = wmicQuery("wmic diskdrive get SerialNumber");

            String raw = (cpu + "|" + board + "|" + disk).trim();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));

            // Lấy 8 byte đầu → 16 hex chars
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            cachedId = sb.toString();
        } catch (Exception e) {
            // Fallback nếu wmic không chạy được
            cachedId = "UNKNOWN_HW_" + System.getProperty("user.name", "X");
        }
        return cachedId;
    }

    /**
     * Chạy lệnh wmic và lấy dòng kết quả đầu tiên (bỏ header).
     */
    private static String wmicQuery(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.readLine(); // bỏ header
                String line = reader.readLine();
                return (line != null) ? line.trim() : "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}


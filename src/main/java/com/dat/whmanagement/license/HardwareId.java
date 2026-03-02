package com.dat.whmanagement.license;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;

/**
 * Lấy Hardware ID duy nhất của máy tính (Windows).
 * Dùng PowerShell thay vì wmic (wmic đã bị xóa trên Windows 11).
 * Kết hợp: CPU ID + Motherboard Serial + MAC Address đầu tiên
 * → Hash SHA-256 → 16 ký tự hex viết hoa.
 */
public class HardwareId {

    private static String cachedId;

    public static String get() {
        if (cachedId != null) return cachedId;
        try {
            String cpu   = psQuery("(Get-CimInstance Win32_Processor).ProcessorId");
            String board = psQuery("(Get-CimInstance Win32_BaseBoard).SerialNumber");
            String mac   = getMacAddress();

            // Bỏ qua giá trị rác không định danh được máy
            cpu   = sanitize(cpu);
            board = sanitize(board);
            mac   = sanitize(mac);

            String raw = cpu + "|" + board + "|" + mac;

            // Nếu tất cả đều rỗng → fallback theo volume serial của ổ C
            if (cpu.isEmpty() && board.isEmpty() && mac.isEmpty()) {
                String vol = psQuery("(Get-CimInstance Win32_LogicalDisk -Filter \"DeviceID='C:'\").VolumeSerialNumber");
                raw = "VOL:" + sanitize(vol);
            }

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            cachedId = sb.toString();
        } catch (Exception e) {
            // Fallback cuối cùng
            cachedId = "FB_" + System.getenv("COMPUTERNAME") + "_" + System.getProperty("user.name");
        }
        return cachedId;
    }

    /**
     * Chạy lệnh PowerShell và trả về kết quả đã trim.
     */
    private static String psQuery(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive",
                "-Command", command
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line.trim());
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Lấy MAC address của card mạng vật lý đầu tiên bằng Java API.
     * Bỏ qua loopback và virtual adapters.
     */
    private static String getMacAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) continue;
                byte[] mac = ni.getHardwareAddress();
                if (mac == null || mac.length == 0) continue;
                StringBuilder sb = new StringBuilder();
                for (byte b : mac) sb.append(String.format("%02X", b));
                String macStr = sb.toString();
                // Bỏ qua MAC toàn 0 hoặc broadcast
                if (!macStr.replaceAll("0", "").isEmpty()) return macStr;
            }
        } catch (Exception ignored) {}
        return "";
    }

    /**
     * Xóa các giá trị không hợp lệ / placeholder của nhà sản xuất.
     */
    private static String sanitize(String val) {
        if (val == null) return "";
        String v = val.trim();
        // Các giá trị rác phổ biến từ BIOS/OEM
        if (v.isEmpty()
            || v.equalsIgnoreCase("To Be Filled By O.E.M.")
            || v.equalsIgnoreCase("Default string")
            || v.equalsIgnoreCase("Not Applicable")
            || v.equalsIgnoreCase("None")
            || v.equalsIgnoreCase("N/A")
            || v.replaceAll("0", "").isEmpty()) {  // chuỗi toàn số 0
            return "";
        }
        return v;
    }
}

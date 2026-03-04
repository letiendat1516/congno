package com.dat.whmanagement.util;

/**
 * Converts a non-negative VND amount (long) to Vietnamese words.
 * Example: 36_428_789L → "Ba mươi sáu triệu, bốn trăm hai mươi tám nghìn, bảy trăm tám mươi chín đồng chẵn"
 */
public final class NumberToVietnameseWordsUtil {

    private static final String[] ONES = {
            "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    private static final String[] TEENS = {
            "mười", "mười một", "mười hai", "mười ba", "mười bốn",
            "mười lăm", "mười sáu", "mười bảy", "mười tám", "mười chín"
    };

    private NumberToVietnameseWordsUtil() {}

    public static String convert(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be non-negative: " + amount);
        if (amount == 0) return "Không đồng chẵn";

        StringBuilder sb = new StringBuilder();

        long ty    = amount / 1_000_000_000L;
        long trieu = (amount % 1_000_000_000L) / 1_000_000L;
        long nghin = (amount % 1_000_000L)     / 1_000L;
        long donvi = amount % 1_000L;

        if (ty > 0) {
            sb.append(threeDigits(ty, false)).append(" tỷ");
        }
        if (trieu > 0) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(threeDigits(trieu, !sb.isEmpty())).append(" triệu");
        }
        if (nghin > 0) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(threeDigits(nghin, !sb.isEmpty())).append(" nghìn");
        }
        if (donvi > 0) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(threeDigits(donvi, !sb.isEmpty()));
        }

        String result = sb.toString().trim();
        if (!result.isEmpty()) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        return result + " đồng chẵn";
    }

    private static String threeDigits(long n, boolean hasPrefix) {
        int hundreds  = (int) (n / 100);
        int remainder = (int) (n % 100);
        int tens      = remainder / 10;
        int ones      = remainder % 10;

        StringBuilder sb = new StringBuilder();

        if (hundreds > 0) {
            sb.append(ONES[hundreds]).append(" trăm");
            if (remainder > 0 && tens == 0) sb.append(" linh");
        } else if (hasPrefix && remainder > 0) {
            sb.append("không trăm linh");
        }

        if (tens == 1) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(TEENS[ones]);
        } else if (tens > 1) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(ONES[tens]).append(" mươi");
            if (ones == 1) sb.append(" mốt");
            else if (ones == 4) sb.append(" tư");
            else if (ones == 5) sb.append(" lăm");
            else if (ones > 0) sb.append(" ").append(ONES[ones]);
        } else if (tens == 0 && ones > 0) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(ONES[ones]);
        }

        return sb.toString().trim();
    }
}


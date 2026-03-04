package com.dat.whmanagement.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tiện ích format tiền tệ VND.
 */
public final class CurrencyUtil {

    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        FORMATTER = new DecimalFormat("###,###,###", symbols);
    }

    private CurrencyUtil() {}

    /** Format với ký hiệu VND. Ví dụ: "VND 1.500.000" */
    public static String format(double amount) {
        return "VND " + FORMATTER.format(Math.round(amount));
    }

    /** Format không ký hiệu. Ví dụ: "1.500.000" */
    public static String formatPlain(double amount) {
        return FORMATTER.format(Math.round(amount));
    }

    /** Parse chuỗi số tiền thành double */
    public static double parse(String value) {
        if (value == null || value.isBlank()) return 0;
        String cleaned = value.trim()
                .replace("VND", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}


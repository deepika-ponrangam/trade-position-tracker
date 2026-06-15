package com.tradepositiontracker.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class CurrencyFormatter {
    public static BigDecimal format(BigDecimal amount, String currencyCode) {
        if (amount == null || currencyCode == null) return amount;
        try {
            int decimals = Currency.getInstance(currencyCode).getDefaultFractionDigits();
            return decimals >= 0 ? amount.setScale(decimals, RoundingMode.HALF_UP) : amount;
        } catch (IllegalArgumentException e) {
            return amount;
        }
    }
}

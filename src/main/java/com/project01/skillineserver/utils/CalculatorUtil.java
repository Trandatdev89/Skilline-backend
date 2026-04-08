package com.project01.skillineserver.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorUtil {

    public static BigDecimal computedPriceWhenDiscount(BigDecimal originalPrice, BigDecimal discount) {
        BigDecimal discountAmount = originalPrice.multiply(discount)
                .divide(BigDecimal.valueOf(100));
        return originalPrice.subtract(discountAmount).setScale(0, RoundingMode.CEILING);
    }
}

package com.project01.skillineserver.utils;

import com.project01.skillineserver.enums.ExpireUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CalculatorUtil {

    public static BigDecimal computedPriceWhenDiscount(BigDecimal originalPrice, BigDecimal discount) {
        BigDecimal discountAmount = originalPrice.multiply(discount)
                .divide(BigDecimal.valueOf(100));
        return originalPrice.subtract(discountAmount).setScale(0, RoundingMode.CEILING);
    }

    public static Instant computedTimeExpireEnrollment(Integer durationExpireValue, ExpireUnit expireUnit) {
        Instant now = Instant.now();
        switch (expireUnit) {
            case DAY -> {
                return now.plus(durationExpireValue, ChronoUnit.DAYS);
            }
            case MONTH -> {
                return now.plus(durationExpireValue, ChronoUnit.MONTHS);
            }
            case YEAR -> {
                return now.plus(durationExpireValue, ChronoUnit.YEARS);
            }
            case WEEK -> {
                return now.plus(durationExpireValue, ChronoUnit.WEEKS);
            }
            default -> {
                return null;
            }
        }
    }
}

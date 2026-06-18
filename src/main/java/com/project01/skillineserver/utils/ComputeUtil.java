package com.project01.skillineserver.utils;

import com.project01.skillineserver.enums.ExpireUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class ComputeUtil {

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
                return LocalDateTime.now().plusMonths(durationExpireValue).toInstant(ZoneOffset.UTC);
            }
            case YEAR -> {
                return LocalDateTime.now().plusYears(durationExpireValue).toInstant(ZoneOffset.UTC);
            }
            case WEEK -> {
                return now.plus(durationExpireValue, ChronoUnit.WEEKS);
            }
            case HOURS -> {
                return now.plus(durationExpireValue, ChronoUnit.HOURS);
            }
            case MINUTE -> {
                return now.plus(durationExpireValue, ChronoUnit.MINUTES);
            }
            default -> {
                return null;
            }
        }
    }

    public static String generateSlug(String input) {

        if (input == null || input.isBlank()) {
            return "";
        }

        String slug = input
                .toLowerCase(Locale.ROOT)
                .trim();

        slug = slug.replace("đ", "d")
                .replace("Đ", "d");

        slug = Normalizer.normalize(slug, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        slug = slug.replaceAll("[^a-z0-9\\s-]", "");

        slug = slug.replaceAll("\\s+", "-");

        slug = slug.replaceAll("-+", "-");

        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }
}

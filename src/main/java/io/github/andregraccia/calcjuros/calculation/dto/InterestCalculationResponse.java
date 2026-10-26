package io.github.andregraccia.calcjuros.calculation.dto;

import java.math.BigDecimal;

/**
 * Result of a simple or compound interest calculation.
 *
 * @param type calculation type: SIMPLE or COMPOUND
 * @param principal initial capital
 * @param ratePercent percentage rate per period
 * @param periods number of periods
 * @param interestAmount interest accrued, rounded to two decimal places
 * @param finalAmount principal plus accrued interest
 */
public record InterestCalculationResponse(
        String type,
        BigDecimal principal,
        BigDecimal ratePercent,
        int periods,
        BigDecimal interestAmount,
        BigDecimal finalAmount) {
}

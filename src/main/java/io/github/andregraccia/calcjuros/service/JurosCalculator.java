package io.github.andregraccia.calcjuros.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calculates final amounts for simple and compound interest.
 *
 * <p>The rate is expressed as a percentage per period (for example, 2.5 means
 * 2.5% per period). Monetary results are rounded to two decimal places.</p>
 */
public class JurosCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;
    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

    /**
     * Calculates the final amount using A = P * (1 + i * n).
     *
     * @param principal initial amount, non-negative
     * @param ratePercent interest rate per period expressed as a percentage
     * @param periods number of periods, non-negative
     * @return final amount rounded to two decimal places
     */
    public BigDecimal calculateSimpleAmount(
            BigDecimal principal, BigDecimal ratePercent, int periods) {
        validateInputs(principal, ratePercent, periods);

        BigDecimal periodicRate = ratePercent.divide(ONE_HUNDRED, CALCULATION_CONTEXT);
        BigDecimal factor = ONE.add(periodicRate.multiply(BigDecimal.valueOf(periods), CALCULATION_CONTEXT));
        return principal.multiply(factor, CALCULATION_CONTEXT)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    /**
     * Calculates the final amount using A = P * (1 + i)^n.
     *
     * @param principal initial amount, non-negative
     * @param ratePercent interest rate per period expressed as a percentage
     * @param periods number of periods, non-negative
     * @return final amount rounded to two decimal places
     */
    public BigDecimal calculateCompoundAmount(
            BigDecimal principal, BigDecimal ratePercent, int periods) {
        validateInputs(principal, ratePercent, periods);

        BigDecimal periodicRate = ratePercent.divide(ONE_HUNDRED, CALCULATION_CONTEXT);
        BigDecimal factor = ONE.add(periodicRate);
        BigDecimal accumulatedFactor = factor.pow(periods, CALCULATION_CONTEXT);
        return principal.multiply(accumulatedFactor, CALCULATION_CONTEXT)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private static void validateInputs(
            BigDecimal principal, BigDecimal ratePercent, int periods) {
        Objects.requireNonNull(principal, "O capital inicial é obrigatório.");
        Objects.requireNonNull(ratePercent, "A taxa de juros é obrigatória.");

        if (principal.signum() < 0) {
            throw new IllegalArgumentException("O capital inicial não pode ser negativo.");
        }
        if (ratePercent.signum() < 0) {
            throw new IllegalArgumentException("A taxa de juros não pode ser negativa.");
        }
        if (periods < 0) {
            throw new IllegalArgumentException("O número de períodos não pode ser negativo.");
        }
    }
}

package io.github.andregraccia.calcjuros.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JurosCalculatorTest {

    private JurosCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new JurosCalculator();
    }

    @Test
    void calculatesSimpleInterestForMultiplePeriods() {
        BigDecimal result = calculator.calculateSimpleAmount(
                new BigDecimal("1000.00"), new BigDecimal("2"), 3);

        assertEquals(new BigDecimal("1060.00"), result);
    }

    @Test
    void calculatesCompoundInterestForMultiplePeriods() {
        BigDecimal result = calculator.calculateCompoundAmount(
                new BigDecimal("1000.00"), new BigDecimal("10"), 2);

        assertEquals(new BigDecimal("1210.00"), result);
    }

    @Test
    void roundsMonetaryResultHalfUpToTwoDecimalPlaces() {
        BigDecimal result = calculator.calculateSimpleAmount(
                new BigDecimal("10.00"), new BigDecimal("5"), 1);

        assertEquals(new BigDecimal("10.50"), result);
    }

    @Test
    void zeroPeriodsReturnsPrincipal() {
        BigDecimal principal = new BigDecimal("125.40");

        assertEquals(principal, calculator.calculateSimpleAmount(
                principal, new BigDecimal("8"), 0));
        assertEquals(principal, calculator.calculateCompoundAmount(
                principal, new BigDecimal("8"), 0));
    }

    @Test
    void zeroRateReturnsPrincipal() {
        BigDecimal principal = new BigDecimal("125.40");

        assertEquals(principal, calculator.calculateSimpleAmount(
                principal, BigDecimal.ZERO, 12));
        assertEquals(principal, calculator.calculateCompoundAmount(
                principal, BigDecimal.ZERO, 12));
    }

    @Test
    void rejectsNegativePrincipal() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateSimpleAmount(new BigDecimal("-1"), BigDecimal.ONE, 1));
    }

    @Test
    void rejectsNegativeRate() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateCompoundAmount(BigDecimal.ONE, new BigDecimal("-1"), 1));
    }

    @Test
    void rejectsNegativePeriods() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateSimpleAmount(BigDecimal.ONE, BigDecimal.ONE, -1));
    }

    @Test
    void rejectsNullPrincipalAndRate() {
        assertThrows(NullPointerException.class, () ->
                calculator.calculateSimpleAmount(null, BigDecimal.ONE, 1));
        assertThrows(NullPointerException.class, () ->
                calculator.calculateCompoundAmount(BigDecimal.ONE, null, 1));
    }
}

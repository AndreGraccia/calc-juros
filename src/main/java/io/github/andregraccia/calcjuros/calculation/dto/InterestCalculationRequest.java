package io.github.andregraccia.calcjuros.calculation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Input for an interest calculation.
 *
 * @param principal initial capital in the currency's major unit
 * @param ratePercent percentage rate per period (e.g. 2.5 means 2.5%)
 * @param periods number of periods
 */
public record InterestCalculationRequest(
        @NotNull(message = "O capital inicial é obrigatório.")
        @DecimalMin(value = "0.0", message = "O capital inicial não pode ser negativo.")
        @Digits(integer = 15, fraction = 2, message = "O capital inicial deve ter até 15 dígitos inteiros e 2 decimais.")
        BigDecimal principal,

        @NotNull(message = "A taxa de juros é obrigatória.")
        @DecimalMin(value = "0.0", message = "A taxa de juros não pode ser negativa.")
        @Digits(integer = 6, fraction = 4, message = "A taxa deve ter até 6 dígitos inteiros e 4 decimais.")
        BigDecimal ratePercent,

        @Min(value = 0, message = "O número de períodos não pode ser negativo.")
        @Max(value = 10000, message = "O número de períodos não pode exceder 10000.")
        int periods) {
}

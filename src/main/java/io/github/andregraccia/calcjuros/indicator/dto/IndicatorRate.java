package io.github.andregraccia.calcjuros.indicator.dto;

import java.math.BigDecimal;

/** Taxa anualizada (% a.a.) com a data do BCB; stale = veio do banco porque o BCB não respondeu. */
public record IndicatorRate(String name, BigDecimal rate, String unit, String date, boolean stale) {
}

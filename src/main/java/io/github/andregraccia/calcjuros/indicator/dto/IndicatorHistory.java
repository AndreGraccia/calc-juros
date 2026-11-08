package io.github.andregraccia.calcjuros.indicator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Histórico de um indicador (do mais antigo ao mais novo); stale = veio do banco porque o BCB não respondeu. */
public record IndicatorHistory(String id, String name, String unit, int series, boolean stale, List<Point> points) {

    public record Point(LocalDate date, BigDecimal value) {
    }
}

package io.github.andregraccia.calcjuros.indicator.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Um ponto de série do BCB guardado no banco (uma linha por indicador e data). */
@Entity
@Table(name = "indicator_point", uniqueConstraints = @UniqueConstraint(columnNames = { "indicator", "ref_date" }))
public class IndicatorPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String indicator;

    @Column(name = "ref_date", nullable = false)
    private LocalDate refDate;

    @Column(name = "point_value", nullable = false, precision = 14, scale = 6)
    private BigDecimal value; // na unidade publicada pelo BCB

    protected IndicatorPoint() {
    }

    public IndicatorPoint(String indicator, LocalDate refDate, BigDecimal value) {
        this.indicator = indicator;
        this.refDate = refDate;
        this.value = value;
    }

    public LocalDate getRefDate() { return refDate; }
    public BigDecimal getValue() { return value; }
}

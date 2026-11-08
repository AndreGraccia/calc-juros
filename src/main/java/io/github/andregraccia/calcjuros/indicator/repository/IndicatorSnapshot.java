package io.github.andregraccia.calcjuros.indicator.repository;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Uma leitura de indicador do BCB guardada no banco (uma linha por indicador e data). */
@Entity
@Table(name = "indicator_snapshot", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "ref_date" }))
public class IndicatorSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 5)
    private BigDecimal rate; // % a.a.

    @Column(name = "ref_date", nullable = false)
    private String refDate; // dd/MM/yyyy, como vem do BCB

    @Column(nullable = false)
    private Instant fetchedAt;

    protected IndicatorSnapshot() {
    }

    public IndicatorSnapshot(String name, BigDecimal rate, String refDate) {
        this.name = name;
        this.rate = rate;
        this.refDate = refDate;
        this.fetchedAt = Instant.now();
    }

    public String getName() { return name; }
    public BigDecimal getRate() { return rate; }
    public String getRefDate() { return refDate; }
}

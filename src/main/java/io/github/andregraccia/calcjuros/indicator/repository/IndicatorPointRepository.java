package io.github.andregraccia.calcjuros.indicator.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorPointRepository extends JpaRepository<IndicatorPoint, Long> {

    List<IndicatorPoint> findByIndicatorAndRefDateGreaterThanEqual(String indicator, LocalDate from);

    /** Tudo o que já foi salvo, do mais antigo ao mais novo (fallback quando o BCB não responde). */
    List<IndicatorPoint> findByIndicatorOrderByRefDateAsc(String indicator);
}

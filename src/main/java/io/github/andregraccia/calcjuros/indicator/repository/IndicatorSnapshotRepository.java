package io.github.andregraccia.calcjuros.indicator.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorSnapshotRepository extends JpaRepository<IndicatorSnapshot, Long> {

    boolean existsByNameAndRefDate(String name, String refDate);

    /** Leitura mais recente guardada (fallback quando o BCB não responde). */
    Optional<IndicatorSnapshot> findFirstByNameOrderByFetchedAtDescIdDesc(String name);
}

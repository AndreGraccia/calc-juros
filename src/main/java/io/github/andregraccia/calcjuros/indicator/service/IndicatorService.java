package io.github.andregraccia.calcjuros.indicator.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import io.github.andregraccia.calcjuros.indicator.dto.IndicatorRate;
import io.github.andregraccia.calcjuros.indicator.dto.SelicResponse;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.BcbClient;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.dto.BcbSeriesPoint;
import io.github.andregraccia.calcjuros.indicator.repository.IndicatorSnapshot;
import io.github.andregraccia.calcjuros.indicator.repository.IndicatorSnapshotRepository;

@Service
public class IndicatorService {

    private static final Logger log = LoggerFactory.getLogger(IndicatorService.class);

    private final BcbClient bcbClient;
    private final IndicatorSnapshotRepository snapshots;

    public IndicatorService(BcbClient bcbClient, IndicatorSnapshotRepository snapshots) {
        this.bcbClient = bcbClient;
        this.snapshots = snapshots;
    }

    public SelicResponse getSelic() {
        return new SelicResponse("Selic", latest("Selic", 432, false).rate(), "% a.a.");
    }

    public List<IndicatorRate> getRates() {
        return List.of(
                latest("Selic", 432, false),
                latest("CDI", 4389, false),
                latest("IPCA 12m", 13522, false),
                latest("Poupança", 195, true)); // 195 vem em % a.m.
    }

    /** BCB primeiro (e grava); se falhar, devolve a última leitura guardada; sem nada guardado, propaga o erro. */
    private IndicatorRate latest(String name, int series, boolean monthly) {
        try {
            BcbSeriesPoint[] points = bcbClient.getLatest(series);
            if (points == null || points.length == 0) {
                throw new IllegalStateException("Sem dados do BCB para " + name);
            }
            BigDecimal rate = new BigDecimal(points[0].valor());
            if (monthly) {
                rate = annualize(rate);
            }
            String date = points[0].data();
            if (!snapshots.existsByNameAndRefDate(name, date)) { // shortcut: histórico cresce sem limpeza, podar se passar de ~100k linhas
                snapshots.save(new IndicatorSnapshot(name, rate, date));
            }
            return new IndicatorRate(name, rate, "% a.a.", date, false);
        } catch (RestClientException | IllegalStateException e) {
            log.warn("BCB indisponível para {} ({}); usando o último valor salvo", name, e.getMessage());
            return snapshots.findFirstByNameOrderByFetchedAtDescIdDesc(name)
                    .map(s -> new IndicatorRate(name, s.getRate(), "% a.a.", s.getRefDate(), true))
                    .orElseThrow(() -> e);
        }
    }

    /** (1 + m)^12 - 1, em %. */
    static BigDecimal annualize(BigDecimal monthlyPercent) {
        double m = monthlyPercent.doubleValue() / 100;
        return BigDecimal.valueOf((Math.pow(1 + m, 12) - 1) * 100).round(new MathContext(6, RoundingMode.HALF_UP));
    }
}

package io.github.andregraccia.calcjuros.indicator.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import io.github.andregraccia.calcjuros.indicator.dto.IndicatorHistory;
import io.github.andregraccia.calcjuros.indicator.dto.SelicResponse;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.BcbClient;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.dto.BcbSeriesPoint;
import io.github.andregraccia.calcjuros.indicator.repository.IndicatorPoint;
import io.github.andregraccia.calcjuros.indicator.repository.IndicatorPointRepository;

@Service
public class IndicatorService {

    private static final Logger log = LoggerFactory.getLogger(IndicatorService.class);
    private static final DateTimeFormatter BCB_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BcbClient bcbClient;
    private final IndicatorPointRepository points;

    public IndicatorService(BcbClient bcbClient, IndicatorPointRepository points) {
        this.bcbClient = bcbClient;
        this.points = points;
    }

    public SelicResponse getSelic() {
        List<IndicatorHistory.Point> p = history("selic").points();
        return new SelicResponse("Selic", p.get(p.size() - 1).value(), Indicator.SELIC.unit());
    }

    /** BCB primeiro (e grava); se falhar, devolve o que já foi salvo, marcado como stale; sem nada salvo, propaga o erro. */
    public IndicatorHistory history(String id) {
        Indicator ind = Indicator.byId(id);
        try {
            List<IndicatorPoint> fresh = fetch(ind);
            save(ind, fresh);
            return toDto(ind, fresh, false);
        } catch (RestClientException | IllegalStateException | DateTimeParseException | NumberFormatException e) {
            log.warn("BCB indisponível para {} ({}); usando o histórico salvo", ind.id(), e.getMessage());
            List<IndicatorPoint> saved = points.findByIndicatorOrderByRefDateAsc(ind.id());
            if (saved.isEmpty()) {
                throw e;
            }
            return toDto(ind, saved, true);
        }
    }

    private List<IndicatorPoint> fetch(Indicator ind) {
        BcbSeriesPoint[] raw = bcbClient.getSince(ind.series(), LocalDate.now().minusMonths(ind.months()));
        if (raw == null || raw.length == 0) {
            throw new IllegalStateException("Sem dados do BCB para " + ind.id());
        }
        return Arrays.stream(raw)
                .map(p -> new IndicatorPoint(ind.id(), LocalDate.parse(p.data(), BCB_DATE), new BigDecimal(p.valor())))
                .toList();
    }

    private void save(Indicator ind, List<IndicatorPoint> fresh) {
        LocalDate oldest = fresh.stream().map(IndicatorPoint::getRefDate).min(LocalDate::compareTo).orElseThrow();
        Set<LocalDate> have = points.findByIndicatorAndRefDateGreaterThanEqual(ind.id(), oldest).stream()
                .map(IndicatorPoint::getRefDate).collect(Collectors.toSet());
        // shortcut: histórico cresce sem limpeza, podar se passar de ~100 mil linhas
        points.saveAll(fresh.stream().filter(p -> !have.contains(p.getRefDate())).toList());
    }

    private static IndicatorHistory toDto(Indicator ind, List<IndicatorPoint> pts, boolean stale) {
        var dto = pts.stream().map(p -> new IndicatorHistory.Point(p.getRefDate(), p.getValue())).toList();
        return new IndicatorHistory(ind.id(), ind.title(), ind.unit(), ind.series(), stale, dto);
    }
}

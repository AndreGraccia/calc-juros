package io.github.andregraccia.calcjuros.indicator;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.BcbClient;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.dto.BcbSeriesPoint;
import io.github.andregraccia.calcjuros.indicator.repository.IndicatorPointRepository;
import io.github.andregraccia.calcjuros.indicator.service.IndicatorService;

@SpringBootTest
class IndicatorServiceTest {

    @Autowired
    IndicatorPointRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    /** BCB falso: devolve os pontos dados (vazio = sem dados) ou está fora do ar (down). */
    private IndicatorService serviceWith(boolean down, BcbSeriesPoint... pts) {
        BcbClient stub = new BcbClient(RestClient.builder(), "http://localhost:0") {
            @Override
            public BcbSeriesPoint[] getSince(int series, java.time.LocalDate from) {
                if (down) throw new RestClientException("fora do ar");
                return pts;
            }
        };
        return new IndicatorService(stub, repo);
    }

    private static final BcbSeriesPoint A = new BcbSeriesPoint("01/03/2026", "10.50");
    private static final BcbSeriesPoint B = new BcbSeriesPoint("01/04/2026", "10.75");

    @Test
    void historyReturnsPointsInOrderAndIsNotStale() {
        var h = serviceWith(false, A, B).history("selic");
        assertEquals(432, h.series());
        assertFalse(h.stale());
        assertEquals(2, h.points().size());
        assertEquals(0, new BigDecimal("10.75").compareTo(h.points().get(1).value()));
    }

    @Test
    void successfulFetchIsSavedWithoutDuplicatesAndServedWhenBcbGoesDown() {
        serviceWith(false, A, B).history("cdi");
        serviceWith(false, A, B).history("cdi"); // mesmas datas: não duplica
        assertEquals(2, repo.count());

        var fallback = serviceWith(true).history("cdi");
        assertTrue(fallback.stale());
        assertEquals(2, fallback.points().size());
        assertEquals("2026-03-01", fallback.points().get(0).date().toString()); // do mais antigo ao mais novo
    }

    @Test
    void malformedBcbDataFallsBackToo() {
        serviceWith(false, A).history("ipca");
        assertTrue(serviceWith(false, new BcbSeriesPoint("data-ruim", "x")).history("ipca").stale());
    }

    @Test
    void bcbDownWithNothingSavedFails() {
        assertThrows(RestClientException.class, () -> serviceWith(true).history("poupanca"));
    }

    @Test
    void emptySeriesWithNothingSavedFails() {
        assertThrows(IllegalStateException.class, () -> serviceWith(false).history("selic"));
    }

    @Test
    void unknownIndicatorIsRejected() {
        assertThrows(NoSuchElementException.class, () -> serviceWith(false, A).history("dolar"));
    }
}

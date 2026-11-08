package io.github.andregraccia.calcjuros.indicator;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.BcbClient;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.dto.BcbSeriesPoint;
import io.github.andregraccia.calcjuros.indicator.repository.IndicatorSnapshotRepository;
import io.github.andregraccia.calcjuros.indicator.service.IndicatorService;

@SpringBootTest
class IndicatorServiceTest {

    @Autowired
    IndicatorSnapshotRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    /** BCB falso: valor fixo, vazio (valor == null) ou fora do ar (down). */
    private IndicatorService serviceWith(String valor, boolean down) {
        BcbClient stub = new BcbClient(RestClient.builder(), "http://localhost:0") {
            @Override
            public BcbSeriesPoint[] getLatest(int series) {
                if (down) throw new RestClientException("fora do ar");
                return valor == null ? new BcbSeriesPoint[0] : new BcbSeriesPoint[] { new BcbSeriesPoint("01/01/2026", valor) };
            }
        };
        return new IndicatorService(stub, repo);
    }

    @Test
    void ratesKeepAnnualValuesAndAnnualizePoupanca() {
        var rates = serviceWith("0.5", false).getRates();
        assertEquals(4, rates.size());
        assertEquals(0, new BigDecimal("0.5").compareTo(rates.get(0).rate()));
        assertEquals(0, new BigDecimal("6.16778").compareTo(rates.get(3).rate())); // 1,005^12 - 1
        assertFalse(rates.get(0).stale());
    }

    @Test
    void successfulFetchIsSavedOnceAndServedWhenBcbGoesDown() {
        serviceWith("10.5", false).getRates();
        serviceWith("10.5", false).getRates(); // mesma data: não duplica
        assertEquals(4, repo.count());

        var fallback = serviceWith(null, true).getRates();
        assertTrue(fallback.stream().allMatch(r -> r.stale()));
        assertEquals(0, new BigDecimal("10.5").compareTo(fallback.get(0).rate()));
        assertEquals("01/01/2026", fallback.get(0).date());
    }

    @Test
    void bcbDownWithNothingSavedStillFails() {
        assertThrows(RestClientException.class, () -> serviceWith(null, true).getRates());
    }

    @Test
    void emptySeriesWithNothingSavedFails() {
        assertThrows(IllegalStateException.class, () -> serviceWith(null, false).getRates());
    }
}

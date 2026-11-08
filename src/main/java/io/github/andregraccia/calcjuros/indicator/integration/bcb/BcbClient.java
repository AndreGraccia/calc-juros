package io.github.andregraccia.calcjuros.indicator.integration.bcb;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.dto.BcbSeriesPoint;

@Component
public class BcbClient {

    private static final DateTimeFormatter BCB_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;

    public BcbClient(RestClient.Builder builder, @Value("${bcb.base-url}") String baseUrl) {
        // Timeout curto: se o BCB travar, cai logo no valor salvo em vez de pendurar a tela.
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Pontos de uma série do SGS (ex.: 432 = Selic meta) desde a data, do mais antigo ao mais novo. */
    public BcbSeriesPoint[] getSince(int series, LocalDate from) {
        return restClient.get()
                .uri("/dados/serie/bcdata.sgs.{series}/dados?formato=json&dataInicial={from}",
                        series, from.format(BCB_DATE))
                .retrieve()
                .body(BcbSeriesPoint[].class);
    }
}

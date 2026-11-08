package io.github.andregraccia.calcjuros.indicator.integration.bcb;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import io.github.andregraccia.calcjuros.indicator.integration.bcb.dto.BcbSeriesPoint;

@Component
public class BcbClient {

    private final RestClient restClient;

    public BcbClient(RestClient.Builder builder, @Value("${bcb.base-url}") String baseUrl) {
        // Timeout curto: se o BCB travar, cai logo no valor salvo em vez de pendurar a tela.
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Último ponto de uma série do SGS (ex.: 432 = Selic meta). */
    public BcbSeriesPoint[] getLatest(int series) {
        return restClient.get()
                .uri("/dados/serie/bcdata.sgs.{series}/dados/ultimos/1?formato=json", series)
                .retrieve()
                .body(BcbSeriesPoint[].class);
    }
}

package io.github.andregraccia.calcjuros.indicator.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import io.github.andregraccia.calcjuros.indicator.dto.IndicatorRate;
import io.github.andregraccia.calcjuros.indicator.dto.SelicResponse;
import io.github.andregraccia.calcjuros.indicator.service.IndicatorService;

@RestController 
@RequestMapping ("/api/indicators")
public class IndicatorController {

    private final IndicatorService indicatorService;

    public IndicatorController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    @GetMapping
    public String getIndicators() {
        return "API de indicadores funcionando";
    }
    @GetMapping("/rates")
    public List<IndicatorRate> getRates() {
        return indicatorService.getRates();
    }

    @GetMapping("/selic")
    public SelicResponse getSelic() {
        return indicatorService.getSelic();
    }

    

}

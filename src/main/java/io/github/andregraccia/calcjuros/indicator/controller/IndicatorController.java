package io.github.andregraccia.calcjuros.indicator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.andregraccia.calcjuros.indicator.dto.IndicatorHistory;
import io.github.andregraccia.calcjuros.indicator.dto.SelicResponse;
import io.github.andregraccia.calcjuros.indicator.service.IndicatorService;

@RestController
@RequestMapping("/api/indicators")
public class IndicatorController {

    private final IndicatorService indicatorService;

    public IndicatorController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    @GetMapping
    public String getIndicators() {
        return "API de indicadores funcionando";
    }

    @GetMapping("/selic")
    public SelicResponse getSelic() {
        return indicatorService.getSelic();
    }

    /** id: selic | cdi | ipca | poupanca */
    @GetMapping("/{id}/history")
    public IndicatorHistory history(@PathVariable String id) {
        return indicatorService.history(id);
    }
}

package io.github.andregraccia.calcjuros.indicator.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController 
@RequestMapping ("/api/indicators")
public class IndicatorController {

    @GetMapping
    public String getIndicators() {
        return "API de indicadores funcionando";
    }

}

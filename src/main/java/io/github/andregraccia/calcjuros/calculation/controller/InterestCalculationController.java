package io.github.andregraccia.calcjuros.calculation.controller;

import io.github.andregraccia.calcjuros.calculation.dto.InterestCalculationRequest;
import io.github.andregraccia.calcjuros.calculation.dto.InterestCalculationResponse;
import io.github.andregraccia.calcjuros.service.JurosCalculator;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP API for interest simulations.
 */
@RestController
@RequestMapping("/api/calculations")
public class InterestCalculationController {

    private final JurosCalculator calculator;

    public InterestCalculationController() {
        this(new JurosCalculator());
    }

    InterestCalculationController(JurosCalculator calculator) {
        this.calculator = calculator;
    }

    @PostMapping("/simple")
    public InterestCalculationResponse calculateSimple(
            @Valid @RequestBody InterestCalculationRequest request) {
        BigDecimal finalAmount = calculator.calculateSimpleAmount(
                request.principal(), request.ratePercent(), request.periods());
        return toResponse("SIMPLE", request, finalAmount);
    }

    @PostMapping("/compound")
    public InterestCalculationResponse calculateCompound(
            @Valid @RequestBody InterestCalculationRequest request) {
        BigDecimal finalAmount = calculator.calculateCompoundAmount(
                request.principal(), request.ratePercent(), request.periods());
        return toResponse("COMPOUND", request, finalAmount);
    }

    private static InterestCalculationResponse toResponse(
            String type, InterestCalculationRequest request, BigDecimal finalAmount) {
        BigDecimal interestAmount = finalAmount.subtract(request.principal())
                .setScale(2, java.math.RoundingMode.HALF_UP);

        return new InterestCalculationResponse(
                type,
                request.principal(),
                request.ratePercent(),
                request.periods(),
                interestAmount,
                finalAmount);
    }
}

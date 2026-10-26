package io.github.andregraccia.calcjuros.calculation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.andregraccia.calcjuros.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InterestCalculationController.class)
@Import(GlobalExceptionHandler.class)
class InterestCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void calculatesSimpleInterest() throws Exception {
        mockMvc.perform(post("/api/calculations/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "principal": 1000.00,
                                  "ratePercent": 2,
                                  "periods": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SIMPLE"))
                .andExpect(jsonPath("$.interestAmount").value(60.00))
                .andExpect(jsonPath("$.finalAmount").value(1060.00));
    }

    @Test
    void calculatesCompoundInterest() throws Exception {
        mockMvc.perform(post("/api/calculations/compound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "principal": 1000.00,
                                  "ratePercent": 10,
                                  "periods": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("COMPOUND"))
                .andExpect(jsonPath("$.interestAmount").value(210.00))
                .andExpect(jsonPath("$.finalAmount").value(1210.00));
    }

    @Test
    void rejectsNegativePrincipalWithStandardError() throws Exception {
        mockMvc.perform(post("/api/calculations/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "principal": -1,
                                  "ratePercent": 2,
                                  "periods": 3
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/calculations/simple"));
    }

    @Test
    void rejectsMissingPeriods() throws Exception {
        mockMvc.perform(post("/api/calculations/compound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "principal": 100,
                                  "ratePercent": 2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void rejectsMalformedJsonWithStandardError() throws Exception {
        mockMvc.perform(post("/api/calculations/simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/calculations/simple"));
    }
}

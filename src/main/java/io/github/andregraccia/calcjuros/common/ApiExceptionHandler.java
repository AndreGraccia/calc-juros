package io.github.andregraccia.calcjuros.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

/** Erros do BCB viram RFC 7807 (502), sem expor stack trace. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ RestClientException.class, IllegalStateException.class })
    public ProblemDetail upstream(Exception ex) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Não foi possível obter os indicadores do Banco Central.");
        p.setTitle("Banco Central indisponível");
        return p;
    }
}

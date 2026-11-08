package io.github.andregraccia.calcjuros.common;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

/** Erros viram RFC 7807, sem expor stack trace. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail unknownIndicator(NoSuchElementException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ RestClientException.class, IllegalStateException.class,
            java.time.format.DateTimeParseException.class, NumberFormatException.class })
    public ProblemDetail upstream(Exception ex) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Não foi possível obter os indicadores do Banco Central e não há dados salvos.");
        p.setTitle("Banco Central indisponível");
        return p;
    }
}

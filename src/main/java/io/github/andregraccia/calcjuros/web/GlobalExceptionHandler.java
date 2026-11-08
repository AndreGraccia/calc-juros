package io.github.andregraccia.calcjuros.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts common client-side errors into a predictable JSON response.
 *
 * <p>Controllers should use Bean Validation annotations such as
 * {@code @NotNull}, {@code @PositiveOrZero}, and {@code @DecimalMin}
 * on request DTO fields.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalidBody(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": "
                        + (error.getDefaultMessage() == null
                                ? "valor inválido"
                                : error.getDefaultMessage()))
                .distinct()
                .collect(Collectors.joining("; "));

        return response(HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Os dados enviados são inválidos." : message,
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": "
                        + violation.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return response(HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Um ou mais parâmetros são inválidos." : message,
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou contém JSON inválido.",
                request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        String message = exception.getMessage();
        return response(HttpStatus.BAD_REQUEST,
                message == null || message.isBlank()
                        ? "Um ou mais parâmetros são inválidos."
                        : message,
                request);
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}

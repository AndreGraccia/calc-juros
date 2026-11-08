package io.github.andregraccia.calcjuros.web;

import java.time.Instant;

/**
 * Stable error payload returned by the HTTP API.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {
}

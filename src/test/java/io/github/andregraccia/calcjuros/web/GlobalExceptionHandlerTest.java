package io.github.andregraccia.calcjuros.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/calculations");
    }

    @Test
    void mapsIllegalArgumentToBadRequestWithStablePayload() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
                new IllegalArgumentException("A taxa não pode ser negativa."), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("A taxa não pode ser negativa.", response.getBody().message());
        assertEquals("/api/calculations", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void usesFallbackMessageWhenIllegalArgumentHasNoMessage() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(
                new IllegalArgumentException(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Um ou mais parâmetros são inválidos.", response.getBody().message());
    }
}

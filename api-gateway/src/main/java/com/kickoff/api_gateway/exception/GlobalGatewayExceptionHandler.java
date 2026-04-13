package com.kickoff.api_gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.net.ConnectException;
import java.net.SocketException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@Order(-2)
@Slf4j
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalGatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = determineStatus(ex);
        String message = determineMessage(ex, status);

        log.error("Gateway error [{}]: {} - path: {}",
                status.value(), message, exchange.getRequest().getPath());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorBody = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", exchange.getRequest().getPath().value(),
                "timestamp", OffsetDateTime.now().toString()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            return exchange.getResponse().setComplete();
        }
    }

    private HttpStatus determineStatus(Throwable ex) {
        return switch (ex) {
            case ResponseStatusException rse -> HttpStatus.resolve(rse.getStatusCode().value());
            case IllegalArgumentException e -> HttpStatus.BAD_REQUEST;
            case IllegalStateException e -> HttpStatus.CONFLICT;
            case ConnectException e -> HttpStatus.SERVICE_UNAVAILABLE;
            case SocketException e -> HttpStatus.SERVICE_UNAVAILABLE;
            case TimeoutException e -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String determineMessage(Throwable ex, HttpStatus status) {
        if (ex instanceof ResponseStatusException rse) {
            return rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        }

        return (status == HttpStatus.INTERNAL_SERVER_ERROR)
                ? "An unexpected internal error occurred"
                : ex.getMessage();
    }
}
package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output.dto;

/**
 * Cuerpo de error común definido en openapi.yml, compartido por todos los
 * microservicios (timestamp, status, error, message, path).
 */
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {}
package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleConcurrentModificationException;
import com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output.dto.ErrorResponse;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.SQLException;

@RestControllerAdvice
public class CustomizedExceptionAdapter {

    // Maneja el caso en el que no se encuentra un vehículo solicitado.
    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(VehicleNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Maneja errores de validación originados en la capa de dominio.
    @ExceptionHandler(InvalidVehicleException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidVehicleException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 409, no 400: coincide con openapi.yml y con el otro camino (DataIntegrityViolationException)
    // por el que llega el mismo error de negocio cuando hay choque de hilos.
    @ExceptionHandler(ExistingVehicleException.class)
    public ResponseEntity<ErrorResponse> handleExisting(ExistingVehicleException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Agrupa y formatea los errores de validación de los campos de entrada (@Valid).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String message = validationErrors.isBlank()
                ? "Validation failed"
                : "Validation failed: " + validationErrors;
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // --- MANEJO DE EXCEPCIONES DE BASE DE DATOS ---

    // Captura violaciones de restricciones estructurales, como índices únicos duplicados.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The operation violates database constraints or uniqueness requirements.", request);
    }

    // Maneja indisponibilidad de la base de datos o consultas que superan el tiempo máximo.
    @ExceptionHandler({DataAccessResourceFailureException.class, QueryTimeoutException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseTimeoutAndConnectionErrors(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "The database is unreachable or the operation timed out. Please try again later.", request);
    }

    // Resuelve bloqueos de concurrencia (deadlocks) al acceder a la base de datos simultáneamente.
    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<ErrorResponse> handleCannotAcquireLockException(CannotAcquireLockException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The resource is currently locked by another ongoing transaction. Please retry the operation.", request);
    }

    // Conflicto de concurrencia optimista: @Version detectó una modificación
    // simultánea. Se traduce a 409 para que el cliente pueda recargar y reintentar.
    @ExceptionHandler(VehicleConcurrentModificationException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentModification(VehicleConcurrentModificationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Red de seguridad: un choque optimista que no haya pasado por el adaptador
    // (p. ej. lanzado por Spring Data en otro punto) tampoco debe salir como 500.
    // Basta con declarar el supertipo: ObjectOptimisticLockingFailureException
    // extiende de él y queda cubierta.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(OptimisticLockingFailureException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT,
                "The resource was modified by another transaction. Please fetch the latest version and retry.", request);
    }

    // Captura genérica para errores SQL inesperados, evitando fugas de stacktraces al cliente.
    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ResponseEntity<ErrorResponse> handleGenericDatabaseException(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected database failure occurred. The request could not be processed.", request);
    }

    // --- MANEJO DE EXCEPCIONES DE SEGURIDAD ---

    /**
     * HTTP 401 Unauthorized: El token de autenticación está ausente, es inválido o ha caducado.
     * <p>
     * Diagnóstico para el equipo: El problema reside en la forma en que el frontend envía el token de autenticación.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication token is missing, invalid, or expired.", request);
    }

    /**
     * HTTP 403 Forbidden: El token de autenticación es válido pero el usuario no posee el rol necesario.
     * <p>
     * Diagnóstico para el equipo: El problema reside en los roles configurados asignados a la identidad.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                java.time.Instant.now().toString(), // 1. String (Timestamp)
                status.value(),                     // 2. int (Estado, ej: 404)
                status.getReasonPhrase(),           // 3. String (Título, ej: "Not Found")
                message,                            // 4. String (Detalle del error)
                request != null ? request.getRequestURI() : "" // 5. String (Ruta)
        );
        return ResponseEntity.status(status).body(body);
    }
}
package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.sql.SQLException;

@RestControllerAdvice
public class CustomizedExceptionAdapter {

    // Maneja el caso en el que no se encuentra un vehículo solicitado.
    @ExceptionHandler(VehicleNotFoundException.class)
    public ProblemDetail handleNotFound(VehicleNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Maneja errores de validación originados en la capa de dominio.
    @ExceptionHandler(InvalidVehicleException.class)
    public ProblemDetail handleInvalid(InvalidVehicleException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Maneja los intentos de registrar un vehículo que ya existe.
    @ExceptionHandler(ExistingVehicleException.class)
    public ProblemDetail handleExisting(ExistingVehicleException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Agrupa y formatea los errores de validación de los campos de entrada (@Valid).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setProperty("errors", validationErrors);
        return problemDetail;
    }

    // --- MANEJO DE EXCEPCIONES DE BASE DE DATOS ---

    // Captura violaciones de restricciones estructurales, como índices únicos duplicados.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The operation violates database constraints or uniqueness requirements.");
        problemDetail.setType(URI.create("https://api.tartis.com/errors/conflict"));
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    // Maneja indisponibilidad de la base de datos o consultas que superan el tiempo máximo.
    @ExceptionHandler({DataAccessResourceFailureException.class, QueryTimeoutException.class})
    public ProblemDetail handleDatabaseTimeoutAndConnectionErrors(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "The database is unreachable or the operation timed out. Please try again later.");
        problemDetail.setType(URI.create("https://api.tartis.com/errors/service-unavailable"));
        problemDetail.setTitle("Database Service Unavailable");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    // Resuelve bloqueos de concurrencia (deadlocks) al acceder a la base de datos simultáneamente.
    @ExceptionHandler(CannotAcquireLockException.class)
    public ProblemDetail handleCannotAcquireLockException(CannotAcquireLockException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The resource is currently locked by another ongoing transaction. Please retry the operation.");
        problemDetail.setType(URI.create("https://api.tartis.com/errors/concurrency-conflict"));
        problemDetail.setTitle("Concurrency Lock Conflict");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    // Captura genérica para errores SQL inesperados, evitando fugas de stacktraces al cliente.
    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ProblemDetail handleGenericDatabaseException(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected database failure occurred. The request could not be processed.");
        problemDetail.setType(URI.create("https://api.tartis.com/errors/internal-server-error"));
        problemDetail.setTitle("Internal Database Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    // --- MANEJO DE EXCEPCIONES DE SEGURIDAD ---

    /**
     * HTTP 401 Unauthorized: El token de autenticación está ausente, es inválido o ha caducado.
     * <p>
     * Diagnóstico para el equipo: El problema reside en la forma en que el frontend envía el token de autenticación.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleUnauthorized(AuthenticationException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication token is missing, invalid, or expired.");
        problemDetail.setType(URI.create("https://api.tartis.com/errors/unauthorized"));
        problemDetail.setTitle("Unauthorized Access");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    /**
     * HTTP 403 Forbidden: El token de autenticación es válido pero el usuario no posee el rol necesario.
     * <p>
     * Diagnóstico para el equipo: El problema reside en los roles configurados asignados a la identidad.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        problemDetail.setType(URI.create("https://api.tartis.com/errors/forbidden"));
        problemDetail.setTitle("Forbidden Access");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }
}

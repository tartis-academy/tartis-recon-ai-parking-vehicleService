package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleConcurrentModificationException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
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

    @ExceptionHandler(VehicleConcurrentModificationException.class)
    public ResponseEntity<ProblemDetail> handleConcurrentModification(VehicleConcurrentModificationException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
}

@ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class})
public ResponseEntity<ProblemDetail> handleObjectOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "El recurso ha sido modificado por otra transacción concurrente. Por favor, obtenga la última versión e intente de nuevo."
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
}
}

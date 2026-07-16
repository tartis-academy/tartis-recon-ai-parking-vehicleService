package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase; // Import necesario
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/vehicles")
public class VehicleRestAdapter {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;

    public VehicleRestAdapter(CreateVehicleUseCase createVehicleUseCase,
                              DeleteVehicleUseCase deleteVehicleUseCase,
                              UpdateVehicleUseCase updateVehicleUseCase) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.deleteVehicleUseCase = deleteVehicleUseCase;
        this.updateVehicleUseCase = updateVehicleUseCase;
    
                              }

    @PostMapping
    public ResponseEntity<?> createVehicle(@Valid @RequestBody VehicleRequest request) {
        try {
            // Conversión y creación del vehículo
            Vehicle newVehicle = new Vehicle(
                VehicleType.valueOf(request.type),
                request.plate,
                request.brand,
                request.model,
                request.color,
                request.numDoors,
                request.hasSidecar,
                request.active
            );
            Vehicle savedVehicle = createVehicleUseCase.execute(newVehicle);
            return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
        } catch (InvalidVehicleException e) {
            // Si hay un error de validación, devolvemos un 400 Bad Request
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Baja logica de un vehiculo 
     * PATCH /v1/vehicles/{id}/status
     *
     * 204 No Content -> vehiculo desactivado
     * 404 Not Found  -> no existe vehiculo con ese id (VehicleNotFoundException)
     */
    @PatchMapping("/{id}/status")
public ResponseEntity<Void> deactivateVehicle(@PathVariable Long id) {
    try {
        deleteVehicleUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    } catch (VehicleNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
    @PutMapping
    public ResponseEntity<?> updateVehicle(@Valid @RequestBody VehicleRequest request) {
        try {
            Vehicle vehicleData = new Vehicle(
                VehicleType.valueOf(request.type),
                request.plate, 
                request.brand,
                request.model,
                request.color,
                request.numDoors,
                request.hasSidecar,
                request.active
            );

            // Asegúrate de que aquí empiece por 'u' minúscula:
            Vehicle updatedVehicle = updateVehicleUseCase.execute(vehicleData);
            return new ResponseEntity<>(updatedVehicle, HttpStatus.OK);

        } catch (InvalidVehicleException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (VehicleNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
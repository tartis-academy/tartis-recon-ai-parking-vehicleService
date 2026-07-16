package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException; // Import necesario
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/vehicles")
public class VehicleRestAdapter {

    private final CreateVehicleUseCase createVehicleUseCase;

    public VehicleRestAdapter(CreateVehicleUseCase createVehicleUseCase) {
        this.createVehicleUseCase = createVehicleUseCase;
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
}
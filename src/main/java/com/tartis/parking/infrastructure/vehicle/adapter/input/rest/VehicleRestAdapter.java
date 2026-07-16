package com.tartis.parking.infrastructure.vehicle.adapter.input.rest;

import com.tartis.parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis.parking.domain.vehicle.model.Vehicle;
import com.tartis.parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleCreateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/vehicles")
public class VehicleRestAdapter {

    private final CreateVehicleUseCase createVehicleUseCase;

    public VehicleRestAdapter(CreateVehicleUseCase createVehicleUseCase) {
        this.createVehicleUseCase = createVehicleUseCase;
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody VehicleCreateDTO request) {
        
        Vehicle newVehicle = new Vehicle(
            request.type,
            request.plate,
            request.brand,
            request.model,
            request.color,
            request.numDoors,
            request.hasSidecar
        );

        Vehicle savedVehicle = createVehicleUseCase.execute(newVehicle);
        return new ResponseEntity<>(savedVehicle, HttpStatus.CREATED);
    }
}
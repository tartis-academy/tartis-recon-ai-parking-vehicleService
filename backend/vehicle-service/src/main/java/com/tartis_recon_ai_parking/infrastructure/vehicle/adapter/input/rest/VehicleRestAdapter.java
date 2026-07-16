package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/vehicles")
public class VehicleRestAdapter {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final VehicleRestMapper mapper;
    
    public VehicleRestAdapter(CreateVehicleUseCase createVehicleUseCase,
                              DeleteVehicleUseCase deleteVehicleUseCase,
                              GetVehicleUseCase getVehicleUseCase,
                              VehicleRestMapper mapper) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.deleteVehicleUseCase = deleteVehicleUseCase;
        this.getVehicleUseCase = getVehicleUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleResponse> getByPlate(@PathVariable String plate) throws VehicleNotFoundException {
        Vehicle vehicle = getVehicleUseCase.getByPlate(plate);
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        VehicleType type;
        try {
            type = VehicleType.valueOf(request.type);
        } catch (IllegalArgumentException e) {
            throw new InvalidVehicleException("Tipo de vehículo inválido: " + request.type);
        }

        Vehicle newVehicle = new Vehicle(
            type,
            request.plate,
            request.brand,
            request.model,
            request.color,
            request.numDoors,
            request.hasSidecar,
            request.active
        );
        Vehicle savedVehicle = createVehicleUseCase.execute(newVehicle);
        return new ResponseEntity<>(mapper.toResponse(savedVehicle), HttpStatus.CREATED);
    }

    /**
     * Baja logica de un vehiculo
     * PATCH /v1/vehicles/{id}/status
     *
     * 204 No Content -> vehiculo desactivado
     * 404 Not Found  -> no existe vehiculo con ese id (VehicleNotFoundException)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> deactivateVehicle(@PathVariable Long id) throws VehicleNotFoundException {
        deleteVehicleUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
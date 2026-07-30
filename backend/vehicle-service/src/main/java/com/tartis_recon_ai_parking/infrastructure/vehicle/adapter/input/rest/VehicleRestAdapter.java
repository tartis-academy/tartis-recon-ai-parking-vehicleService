package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import java.util.UUID;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.usecase.ActivateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/vehicles")
public class VehicleRestAdapter {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final ActivateVehicleUseCase activateVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final VehicleRestMapper mapper;

    public VehicleRestAdapter(CreateVehicleUseCase createVehicleUseCase,
                              DeleteVehicleUseCase deleteVehicleUseCase,
                              ActivateVehicleUseCase activateVehicleUseCase,
                              UpdateVehicleUseCase updateVehicleUseCase,
                              GetVehicleUseCase getVehicleUseCase,
                              VehicleRestMapper mapper) {
        this.createVehicleUseCase = createVehicleUseCase;
        this.deleteVehicleUseCase = deleteVehicleUseCase;
        this.activateVehicleUseCase = activateVehicleUseCase;
        this.updateVehicleUseCase = updateVehicleUseCase;
        this.getVehicleUseCase = getVehicleUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Iterable<VehicleResponse>> getAllVehicles() {
        Iterable<VehicleDTO> vehicles = getVehicleUseCase.execute();
        return ResponseEntity.ok(mapper.toResponseList(vehicles));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable UUID id) {
        VehicleDTO vehicle = getVehicleUseCase.getById(id);
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }

    @GetMapping("/plate/{plate}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERARIO')")
    public ResponseEntity<VehicleResponse> getByPlate(@PathVariable String plate) {
        VehicleDTO vehicle = getVehicleUseCase.getByPlate(plate);
        return ResponseEntity.ok(mapper.toResponse(vehicle));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) throws ExistingVehicleException {
        VehicleDTO savedVehicle = createVehicleUseCase.execute(mapper.toCreateDTO(request));
        return new ResponseEntity<>(mapper.toResponse(savedVehicle), HttpStatus.CREATED);
    }

    /**
     * Baja logica de un vehiculo (legacy endpoint)
     * PATCH /v1/vehicles/{id}/status
     *
     * 204 No Content -> vehiculo desactivado
     * 404 Not Found  -> no existe vehiculo con ese id (VehicleNotFoundException)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateVehicle(@PathVariable UUID id) {
        deleteVehicleUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Baja logica de un vehiculo
     * PATCH /v1/vehicles/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateVehicleExplicit(@PathVariable UUID id) {
        deleteVehicleUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Alta logica de un vehiculo
     * PATCH /v1/vehicles/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateVehicle(@PathVariable UUID id) {
        activateVehicleUseCase.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable UUID id,
                                                         @Valid @RequestBody VehicleRequest request) {
        VehicleDTO updatedVehicle = updateVehicleUseCase.execute(id, mapper.toCreateDTO(request));
        return ResponseEntity.ok(mapper.toResponse(updatedVehicle));
    }
}

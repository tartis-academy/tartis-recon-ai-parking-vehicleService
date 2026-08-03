package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;

public class VehicleStatusRequest {

    @NotNull(message = "El campo active es obligatorio")
    public Boolean active;

    public VehicleStatusRequest() {
    }

    public VehicleStatusRequest(Boolean active) {
        this.active = active;
    }
}

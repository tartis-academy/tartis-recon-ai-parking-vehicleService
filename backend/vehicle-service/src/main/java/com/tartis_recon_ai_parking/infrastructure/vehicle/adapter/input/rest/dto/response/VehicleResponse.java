package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response;

import java.util.UUID;

import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {

    private UUID uniqueId;
    private VehicleType type;
    private String plate;
    private String brand;
    private String model;
    private String color;
    private Integer numDoors;
    private Boolean hasSidecar;
    private Boolean active;
}

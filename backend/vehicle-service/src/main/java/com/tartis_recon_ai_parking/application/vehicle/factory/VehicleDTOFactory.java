package com.tartis_recon_ai_parking.application.vehicle.factory;

import java.util.List;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;

// Unico punto de traduccion entre los DTO de aplicacion y el dominio.
public final class VehicleDTOFactory {

    private VehicleDTOFactory() {
    }

    public static VehicleDTO toDTO(Vehicle vehicle) {
        return new VehicleDTO(
                vehicle.getUniqueId(),
                vehicle.getType().name(),
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getNumDoors(),
                vehicle.getHasSidecar(),
                vehicle.isActive());
    }

    public static List<VehicleDTO> toDTOList(List<Vehicle> vehicles) {
        return vehicles.stream().map(VehicleDTOFactory::toDTO).toList();
    }

    public static Vehicle toDomain(VehicleCreateDTO dto) {
        return new Vehicle(
                parseType(dto.type()),
                dto.plate(),
                dto.brand(),
                dto.model(),
                dto.color(),
                dto.numDoors(),
                dto.hasSidecar(),
                dto.active());
    }

    private static VehicleType parseType(String type) {
        try {
            return VehicleType.valueOf(type);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidVehicleException("Tipo de vehículo inválido: " + type);
        }
    }
}

package com.tartis_recon_ai_parking.application.vehicle.factory;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleDTOFactoryTest {

    @Test
    @DisplayName("Debe asignar 4 puertas por defecto a un coche cuando el DTO no especifica numDoors")
    void shouldDefaultToFourDoorsForCarWhenNumDoorsIsNull() {
        VehicleCreateDTO dto = new VehicleCreateDTO("CAR", "1234ABC", "Toyota", "Corolla", "Red", null, false);

        Vehicle vehicle = VehicleDTOFactory.toDomain(dto);

        assertThat(vehicle.getNumDoors()).isEqualTo(4);
    }

    @Test
    @DisplayName("Debe asignar 0 puertas por defecto a una moto cuando el DTO no especifica numDoors")
    void shouldDefaultToZeroDoorsForMotorbikeWhenNumDoorsIsNull() {
        VehicleCreateDTO dto = new VehicleCreateDTO("MOTORBIKE", "5678DEF", "Honda", "CBR", "Black", null, false);

        Vehicle vehicle = VehicleDTOFactory.toDomain(dto);

        assertThat(vehicle.getNumDoors()).isZero();
    }
}

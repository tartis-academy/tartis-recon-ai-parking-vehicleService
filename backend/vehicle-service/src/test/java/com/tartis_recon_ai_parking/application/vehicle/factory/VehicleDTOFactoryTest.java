package com.tartis_recon_ai_parking.application.vehicle.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;

class VehicleDTOFactoryTest {

    @Test
    @DisplayName("shouldDefaultToFourDoorsForCarWhenNumDoorsIsNull")
    void shouldDefaultToFourDoorsForCarWhenNumDoorsIsNull() {
        VehicleCreateDTO dto = new VehicleCreateDTO(
                "CAR", "1234BCD", "Toyota", "Corolla", "Red", null, false
        );

        Vehicle vehicle = VehicleDTOFactory.toDomain(dto);

        assertThat(vehicle.getNumDoors()).isEqualTo(4);
        assertThat(vehicle.getType()).isEqualTo(VehicleType.CAR);
    }

    @Test
    @DisplayName("shouldDefaultToZeroDoorsForMotorbikeWhenNumDoorsIsNull")
    void shouldDefaultToZeroDoorsForMotorbikeWhenNumDoorsIsNull() {
        VehicleCreateDTO dto = new VehicleCreateDTO(
                "MOTORBIKE", "5678DFG", "Honda", "CBR", "Black", null, false
        );

        Vehicle vehicle = VehicleDTOFactory.toDomain(dto);

        assertThat(vehicle.getNumDoors()).isEqualTo(0);
        assertThat(vehicle.getType()).isEqualTo(VehicleType.MOTORBIKE);
    }
}

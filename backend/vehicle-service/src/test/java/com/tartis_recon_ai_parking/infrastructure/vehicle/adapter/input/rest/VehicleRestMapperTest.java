package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;

class VehicleRestMapperTest {

    // Utiliza el cargador de MapStruct Mappers en lugar de instanciar directamente para evitar problemas de sincronizacion del compilador de la IDE.
    private final VehicleRestMapper mapper = Mappers.getMapper(VehicleRestMapper.class);

    @Test
    @DisplayName("Debe mapear un objeto de dominio Vehicle a un DTO VehicleResponse de forma correcta")
    void shouldMapVehicleToVehicleResponse() {
        // QUE HACE:
        // - Instancia un objeto Vehicle de dominio completo y valido con datos especificos.
        // - Llama al metodo toResponse del mapeador.
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        vehicle.setUniqueId(id);

        VehicleResponse response = mapper.toResponse(vehicle);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto VehicleResponse no nulo y comprobar mediante aserciones 
        // de AssertJ que cada campo del DTO resultante coincida exactamente con el de origen.
        assertThat(response).isNotNull();
        assertThat(response.getUniqueId()).isEqualTo(id);
        assertThat(response.getType()).isEqualTo(VehicleType.CAR);
        assertThat(response.getPlate()).isEqualTo("1234ABC");
        assertThat(response.getBrand()).isEqualTo("Toyota");
        assertThat(response.getModel()).isEqualTo("Corolla");
        assertThat(response.getColor()).isEqualTo("Red");
        assertThat(response.getNumDoors()).isEqualTo(4);
        assertThat(response.getHasSidecar()).isFalse();
        assertThat(response.getActive()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar null al mapear un vehiculo nulo")
    void shouldReturnNullWhenMappingNullVehicle() {
        // QUE HACE:
        // Llama al mapper pasando un parametro nulo.
        VehicleResponse response = mapper.toResponse(null);

        // QUE DEBERIA HACER:
        // El mapper debe manejar el parametro nulo de forma segura devolviendo null 
        // sin lanzar un NullPointerException.
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Debe mapear una lista de vehiculos de dominio a una lista de DTOs correctamente")
    void shouldMapVehicleListToResponseList() {
        // QUE HACE:
        // Instancia dos vehiculos de dominio en una lista, llama al metodo toResponseList del mapeador.
        UUID id1 = UUID.randomUUID();
        Vehicle vehicle1 = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        vehicle1.setUniqueId(id1);

        UUID id2 = UUID.randomUUID();
        Vehicle vehicle2 = new Vehicle(VehicleType.MOTORBIKE, "5678DEF", "Honda", "CBR", "Black", 0, true, true);
        vehicle2.setUniqueId(id2);

        List<Vehicle> vehicleList = List.of(vehicle1, vehicle2);

        Iterable<VehicleResponse> responseList = mapper.toResponseList(vehicleList);

        // QUE DEBERIA HACER:
        // Debe retornar un iterable con dos elementos mapeados correctamente,
        // asegurando la conversion de la coleccion entera utilizando la API fluida de AssertJ.
        assertThat(responseList)
                .isNotNull()
                .hasSize(2)
                .extracting(VehicleResponse::getPlate)
                .containsExactly("1234ABC", "5678DEF");

        assertThat(responseList)
                .extracting(VehicleResponse::getUniqueId)
                .containsExactly(id1, id2);
    }

    @Test
    @DisplayName("Debe retornar null al mapear una lista nula de vehiculos")
    void shouldReturnNullWhenMappingNullList() {
        // QUE HACE:
        // Llama al metodo toResponseList pasando un valor nulo.
        Iterable<VehicleResponse> responseList = mapper.toResponseList(null);

        // QUE DEBERIA HACER:
        // El mapper debe manejar la coleccion nula de manera segura y retornar null.
        assertThat(responseList).isNull();
    }
}

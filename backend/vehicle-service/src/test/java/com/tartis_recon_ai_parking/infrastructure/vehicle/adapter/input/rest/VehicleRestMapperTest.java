package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
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
    @DisplayName("Debe mapear un VehicleDTO de aplicacion a un DTO VehicleResponse de forma correcta")
    void shouldMapVehicleDTOToVehicleResponse() {
        // QUE HACE:
        // - Instancia un VehicleDTO completo y valido con datos especificos. El mapper ya no
        //   conoce el dominio: traduce entre los DTO de aplicacion y el contrato HTTP.
        // - Llama al metodo toResponse del mapeador.
        UUID id = UUID.randomUUID();
        VehicleDTO vehicle = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        VehicleResponse response = mapper.toResponse(vehicle);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto VehicleResponse no nulo y comprobar mediante aserciones
        // de AssertJ que cada campo del DTO resultante coincida exactamente con el de origen.
        assertThat(response).isNotNull();
        assertThat(response.getUniqueId()).isEqualTo(id);
        assertThat(response.getType()).isEqualTo("CAR");
        assertThat(response.getPlate()).isEqualTo("1234ABC");
        assertThat(response.getBrand()).isEqualTo("Toyota");
        assertThat(response.getModel()).isEqualTo("Corolla");
        assertThat(response.getColor()).isEqualTo("Red");
        assertThat(response.getNumDoors()).isEqualTo(4);
        assertThat(response.getHasSidecar()).isFalse();
        assertThat(response.getActive()).isTrue();
    }

    @Test
    @DisplayName("Debe mapear un VehicleRequest entrante a un VehicleCreateDTO de aplicacion")
    void shouldMapVehicleRequestToCreateDTO() {
        // QUE HACE:
        // - Instancia el request HTTP tal y como lo deserializa Spring.
        // - Llama al metodo toCreateDTO del mapeador.
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        VehicleCreateDTO createDTO = mapper.toCreateDTO(request);

        // QUE DEBERIA HACER:
        // Debe trasladar todos los campos al DTO de entrada de la capa de aplicacion sin
        // interpretarlos: validarlos es responsabilidad del dominio, no del mapper.
        assertThat(createDTO).isNotNull();
        assertThat(createDTO.type()).isEqualTo("CAR");
        assertThat(createDTO.plate()).isEqualTo("1234ABC");
        assertThat(createDTO.brand()).isEqualTo("Toyota");
        assertThat(createDTO.model()).isEqualTo("Corolla");
        assertThat(createDTO.color()).isEqualTo("Red");
        assertThat(createDTO.numDoors()).isEqualTo(4);
        assertThat(createDTO.hasSidecar()).isFalse();
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
    @DisplayName("Debe retornar null al mapear un request nulo")
    void shouldReturnNullWhenMappingNullRequest() {
        // QUE HACE:
        // Llama a toCreateDTO pasando un request nulo, la rama de guarda que
        // MapStruct genera al principio de cada metodo del mapper.
        VehicleCreateDTO createDTO = mapper.toCreateDTO(null);

        // QUE DEBERIA HACER:
        // Devolver null en lugar de lanzar NullPointerException, igual que hacen
        // toResponse y toResponseList.
        assertThat(createDTO).isNull();
    }

    @Test
    @DisplayName("Debe mapear una lista de VehicleDTO a una lista de DTOs de respuesta correctamente")
    void shouldMapVehicleListToResponseList() {
        // QUE HACE:
        // Instancia dos VehicleDTO en una lista, llama al metodo toResponseList del mapeador.
        UUID id1 = UUID.randomUUID();
        VehicleDTO vehicle1 = new VehicleDTO(id1, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        UUID id2 = UUID.randomUUID();
        VehicleDTO vehicle2 = new VehicleDTO(id2, "MOTORBIKE", "5678DEF", "Honda", "CBR", "Black", 0, true, true);

        List<VehicleDTO> vehicleList = List.of(vehicle1, vehicle2);

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

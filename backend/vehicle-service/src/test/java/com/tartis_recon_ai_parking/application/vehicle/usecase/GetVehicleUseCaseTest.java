package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class): Habilita el uso de Mockito en esta clase de JUnit
@ExtendWith(MockitoExtension.class)
class GetVehicleUseCaseTest {

    // @Mock: Crea un objeto de imitacion de la interfaz. Por defecto, sus metodos devuelven valores vacios
    // (null, false, 0, Optional.empty()), a menos que se configure su comportamiento con when().
    @Mock
    private VehiclePersistence vehiclePersistence;

    // @InjectMocks: Crea automaticamente la instancia del caso de uso e inyecta en su constructor
    // el mock 'vehiclePersistence' declarado arriba, evitando tener que instanciarlo a mano.
    @InjectMocks
    private GetVehicleUseCase getVehicleUseCase;

    @Test
    @DisplayName("Debe devolver el vehiculo buscado por matricula si existe")
    void shouldGetVehicleByPlate() throws InvalidVehicleException, VehicleNotFoundException {
        // QUE HACE:
        // 1. Crea un vehiculo de dominio valido, que es lo que devuelve la persistencia.
        // 2. Configura el mock para que, al buscar por matricula "1234BCD", retorne el vehiculo envuelto en un Optional.
        // 3. Llama al metodo 'getByPlate' en el caso de uso.
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findByPlate("1234BCD")).thenReturn(Optional.of(vehicle));

        VehicleDTO result = getVehicleUseCase.getByPlate("1234BCD");

        // QUE DEBERIA HACER:
        // Debe retornar el DTO de salida correcto sin lanzar excepciones, y con los datos correctos.
        assertThat(result).isNotNull();
        assertThat(result.plate()).isEqualTo("1234BCD");
    }

    @Test
    @DisplayName("Debe lanzar VehicleNotFoundException si la matricula no existe")
    void shouldThrowExceptionWhenPlateNotFound() {
        // QUE HACE:
        // 1. Configura el mock para retornar Optional.empty() cuando se busque la matricula "UNKNOWN".
        // 2. Llama al metodo 'getByPlate' del caso de uso.

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findByPlate("UNKNOWN")).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe lanzar VehicleNotFoundException con un mensaje de error descriptivo indicando que no se encontro el coche.
        assertThatThrownBy(() -> getVehicleUseCase.getByPlate("UNKNOWN"))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehicle with 'Plate = UNKNOWN' couldn't be found.");
    }

    @Test
    @DisplayName("Debe devolver el vehiculo buscado por ID si existe")
    void shouldGetVehicleById() throws InvalidVehicleException, VehicleNotFoundException {
        // QUE HACE:
        // 1. Genera un UUID y crea un vehiculo asignandole ese UUID.
        // 2. Configura el mock para que al buscar por ese UUID devuelva el vehiculo.
        // 3. Invoca a 'getById' en el caso de uso.
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.reconstruct(id, 1L,VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(vehicle));

        VehicleDTO result = getVehicleUseCase.getById(id);

        // QUE DEBERIA HACER:
        // Debe retornar el DTO correctamente verificado por su identificador unico.
        assertThat(result).isNotNull();
        assertThat(result.uniqueId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Debe lanzar VehicleNotFoundException si el ID no existe")
    void shouldThrowExceptionWhenIdNotFound() {
        // QUE HACE:
        // 1. Genera un UUID aleatorio.
        // 2. Configura el mock para retornar Optional.empty() cuando se busque por ese ID.
        // 3. Llama al metodo 'getById' del caso de uso.
        UUID id = UUID.randomUUID();

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe lanzar la excepcion VehicleNotFoundException indicando que no se encontro el vehiculo por ID.
        assertThatThrownBy(() -> getVehicleUseCase.getById(id))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehicle with 'ID = " + id + "' couldn't be found.");
    }

    @Test
    @DisplayName("Debe listar todos los vehiculos")
    void shouldListAllVehicles() throws InvalidVehicleException {
        // QUE HACE:
        // 1. Crea dos vehiculos de dominio diferentes.
        // 2. Configura el mock para retornar una lista que contenga ambos vehiculos cuando se llame a findAll().
        // 3. Llama al metodo 'execute' del caso de uso.
        Vehicle vehicle1 = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);
        Vehicle vehicle2 = Vehicle.create(VehicleType.MOTORBIKE, "5678BDF", "Honda", "CBR", "Black", 0, false, true);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findAll()).thenReturn(List.of(vehicle1, vehicle2));

        List<VehicleDTO> result = getVehicleUseCase.execute();

        // QUE DEBERIA HACER:
        // Debe retornar la lista completa traducida a DTOs, en el mismo orden y con los datos
        // de los vehiculos que el mock simulo.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(VehicleDTO::plate)
                .containsExactly("1234BCD", "5678BDF");
        assertThat(result)
                .extracting(VehicleDTO::type)
                .containsExactly("CAR", "MOTORBIKE");
    }
}

package com.tartis_recon_ai_parking.application.vehicle.usecase;


import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Habilita el uso de Mockito en esta clase de JUnit
@ExtendWith(MockitoExtension.class)
class UpdateVehicleUseCaseTest {

    // @Mock: Crea un objeto de imitacion de la interfaz. Por defecto, sus metodos devuelven valores vacios
    // (null, false, 0, Optional.empty()), a menos que se configure su comportamiento con when().
    @Mock
    private VehiclePersistence vehiclePersistence;

    // @InjectMocks: Crea automaticamente la instancia del caso de uso e inyecta en su constructor
    // el mock 'vehiclePersistence' declarado arriba, evitando tener que instanciarlo a mano.
    @InjectMocks
    private UpdateVehicleUseCase updateVehicleUseCase;

    @Test
    @DisplayName("Debe actualizar los datos de un vehiculo existente")
    void shouldUpdateVehicleSuccessfully() throws InvalidVehicleException, VehicleNotFoundException {
        // QUE HACE:
        // 1. Genera un ID comun y crea una entidad simulada existente en base de datos.
        // 2. Crea el DTO con los nuevos datos actualizados (color cambiado a Blue y estado a inactivo).
        //    El id ya no viaja en el DTO: se pasa como parametro porque identifica el recurso.
        // 3. Configura el mock para retornar la entidad existente al buscar por ID.
        // 4. Configura el mock para retornar el objeto guardado al llamar a save().
        UUID id = UUID.randomUUID();
        Vehicle existingVehicle = Vehicle.reconstruct(id, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        VehicleCreateDTO updatedData = new VehicleCreateDTO("CAR", "1234ABC", "Toyota", "Corolla", "Blue", 4, false, false);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(existingVehicle));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleDTO result = updateVehicleUseCase.execute(id, updatedData);

        // QUE DEBERIA HACER:
        // Debe mutar los atributos del vehiculo existente con los nuevos valores,
        // guardarlo en base de datos y retornar el DTO con los valores actualizados
        // (color "Blue" y activo a false), conservando el id del recurso.
        assertThat(result.color()).isEqualTo("Blue");
        assertThat(result.active()).isFalse();
        assertThat(result.uniqueId()).isEqualTo(id);
        verify(vehiclePersistence, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Debe lanzar VehicleNotFoundException si se intenta actualizar un vehiculo inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentVehicle() throws InvalidVehicleException {
        // QUE HACE:
        // 1. Crea datos de actualizacion para un ID aleatorio.
        // 2. Configura el mock para simular que no se encuentra ningun vehiculo con ese ID (Optional.empty()).
        // 3. Intenta ejecutar el caso de uso de actualizacion.
        UUID id = UUID.randomUUID();
        VehicleCreateDTO updatedData = new VehicleCreateDTO( "CAR", "1234ABC", "Toyota", "Corolla", "Blue", 4, false, false);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe fallar lanzando VehicleNotFoundException y garantizar que NUNCA se invoque al metodo 'save'.
        assertThatThrownBy(() -> updateVehicleUseCase.execute(id, updatedData))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehicle not found with ID: " + id);

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException sin consultar la BD si el tipo del DTO no es valido")
    void shouldThrowExceptionWhenTypeIsInvalid() {
        // QUE HACE:
        // 1. Crea un DTO con un tipo inexistente en VehicleType.
        // 2. Ejecuta el caso de uso sin configurar findById.
        UUID id = UUID.randomUUID();
        VehicleCreateDTO updatedData = new VehicleCreateDTO( "HELICOPTER", "1234ABC", "Toyota", "Corolla", "Blue", 4, false, false);

        // QUE DEBERIA HACER:
        // El cuerpo invalido debe ganar al 404: se valida antes de ir a buscar el vehiculo,
        // asi que no debe consultarse la persistencia ni guardarse nada.
        assertThatThrownBy(() -> updateVehicleUseCase.execute(id, updatedData))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Tipo de vehículo inválido: HELICOPTER");

        verify(vehiclePersistence, never()).findById(any(UUID.class));
        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }
}

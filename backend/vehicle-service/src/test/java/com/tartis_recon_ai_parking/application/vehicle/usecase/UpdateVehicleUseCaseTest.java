package com.tartis_recon_ai_parking.application.vehicle.usecase;

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
        // 2. Crea un objeto con los nuevos datos actualizados (color cambiado a Blue y estado a inactivo).
        // 3. Configura el mock para retornar la entidad existente al buscar por ID.
        // 4. Configura el mock para retornar el objeto guardado al llamar a save().
        UUID id = UUID.randomUUID();
        Vehicle existingVehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        existingVehicle.setUniqueId(id);

        Vehicle updatedData = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Blue", 4, false, false);
        updatedData.setUniqueId(id);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(existingVehicle));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = updateVehicleUseCase.execute(updatedData);

        // QUE DEBERIA HACER:
        // Debe mutar los atributos del vehiculo existente con los nuevos valores,
        // guardarlo en base de datos y retornar el vehiculo con los valores actualizados 
        // (color "Blue" y activo a false).
        assertThat(result.getColor()).isEqualTo("Blue");
        assertThat(result.isActive()).isFalse();
        verify(vehiclePersistence, times(1)).save(existingVehicle);
    }

    @Test
    @DisplayName("Debe lanzar VehicleNotFoundException si se intenta actualizar un vehiculo inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentVehicle() throws InvalidVehicleException {
        // QUE HACE:
        // 1. Crea datos de actualizacion para un ID aleatorio.
        // 2. Configura el mock para simular que no se encuentra ningun vehiculo con ese ID (Optional.empty()).
        // 3. Intenta ejecutar el caso de uso de actualizacion.
        UUID id = UUID.randomUUID();
        Vehicle updatedData = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Blue", 4, false, false);
        updatedData.setUniqueId(id);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe fallar lanzando VehicleNotFoundException y garantizar que NUNCA se invoque al metodo 'save'.
        assertThatThrownBy(() -> updateVehicleUseCase.execute(updatedData))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehicle not found with ID: " + id);

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }
}

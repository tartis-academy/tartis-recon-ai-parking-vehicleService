package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Habilita el uso de Mockito en esta clase de JUnit
@ExtendWith(MockitoExtension.class)
class DeleteVehicleUseCaseTest {

    // @Mock: Crea un objeto de imitacion de la interfaz. Por defecto, sus metodos devuelven valores vacios 
    // (null, false, 0, Optional.empty()), a menos que se configure su comportamiento con when().
    @Mock
    private VehiclePersistence vehiclePersistence;

    // @InjectMocks: Crea automaticamente la instancia del caso de uso e inyecta en su constructor
    // el mock 'vehiclePersistence' declarado arriba, evitando tener que instanciarlo a mano.
    @InjectMocks
    private DeleteVehicleUseCase deleteVehicleUseCase;

    @Test
    @DisplayName("Debe desactivar el vehiculo (active=false) y guardar si el vehiculo existe")
    void shouldDeactivateVehicleSuccessfully() throws InvalidVehicleException, VehicleNotFoundException {
        // QUE HACE:
        // 1. Instancia un objeto Vehicle activo.
        // 2. Configura el mock para retornar ese vehiculo al buscarlo por su ID.
        // 3. Ejecuta la desactivacion a traves del caso de uso.
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        vehicle.setUniqueId(id);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(vehicle));

        deleteVehicleUseCase.deactivate(id);

        // QUE DEBERIA HACER:
        // Debe recuperar el vehiculo de base de datos, establecer 'active' en false,
        // llamar al metodo save para persistir el cambio y asegurar mediante un ArgumentCaptor 
        // de Mockito que el estado del vehiculo enviado a persistir tiene 'active = false'.
        
        // ArgumentCaptor: Es una herramienta que te permite "atrapar" el objeto que el caso de uso envio 
        // al mock para guardarlo (en el metodo save). Esto te permite validar si el caso de uso modifico los 
        // datos de la forma deseada (en este caso, comprobar que seteo active a false).
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehiclePersistence, times(1)).save(vehicleCaptor.capture());
        
        Vehicle savedVehicle = vehicleCaptor.getValue();
        assertThat(savedVehicle.isActive()).isFalse();
    }

    @Test
    @DisplayName("Debe lanzar VehicleNotFoundException al desactivar si el vehiculo no existe")
    void shouldThrowExceptionWhenDeactivatingNonExistentVehicle() {
        // QUE HACE:
        // 1. Genera un ID aleatorio.
        // 2. Configura el mock para retornar Optional.empty() al buscar por ese ID.
        // 3. Intenta ejecutar la desactivacion en el caso de uso.
        UUID id = UUID.randomUUID();
        
        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.findById(id)).thenReturn(Optional.empty());

        // QUE DEBERIA HACER:
        // Debe fallar lanzando una excepcion VehicleNotFoundException y asegurarse de que 
        // nunca se invoque el metodo 'save' de la base de datos.
        assertThatThrownBy(() -> deleteVehicleUseCase.deactivate(id))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehículo no encontrado con id: " + id);

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }
}

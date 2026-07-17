package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Habilita el uso de Mockito en esta clase de JUnit
@ExtendWith(MockitoExtension.class)
class CreateVehicleUseCaseTest {

    // @Mock: Crea un objeto de imitacion de la interfaz. Por defecto, sus metodos devuelven valores vacios 
    // (null, false, 0, Optional.empty()), a menos que se configure su comportamiento con when().
    @Mock
    private VehiclePersistence vehiclePersistence;

    // @InjectMocks: Crea automaticamente la instancia del caso de uso e inyecta en su constructor
    // el mock 'vehiclePersistence' declarado arriba, evitando tener que instanciarlo a mano.
    @InjectMocks
    private CreateVehicleUseCase createVehicleUseCase;

    @Test
    @DisplayName("Debe crear y guardar un vehiculo si la matricula no existe previamente")
    void shouldCreateVehicleSuccessfully() throws InvalidVehicleException {
        // QUE HACE:
        // 1. Instancia un objeto Vehicle valido.
        // 2. Configura el mock del puerto de salida 'vehiclePersistence' para simular que 
        //    la matricula "1234ABC" NO existe previamente (existsByPlate -> false).
        // 3. Configura el mock para que devuelva el vehiculo cuando se llame al metodo 'save'.
        // 4. Ejecuta el caso de uso con el vehiculo creado.
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        
        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.existsByPlate("1234ABC")).thenReturn(false);
        when(vehiclePersistence.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle result = createVehicleUseCase.execute(vehicle);

        // QUE DEBERIA HACER:
        // Debe finalizar con exito, retornar el vehiculo persistido, y verificar mediante 
        // Mockito que se invoco exactamente una vez al metodo 'save' del puerto de persistencia.
        assertThat(result).isNotNull();
        assertThat(result.getPlate()).isEqualTo("1234ABC");
        verify(vehiclePersistence, times(1)).save(vehicle);
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si la matricula ya esta registrada")
    void shouldThrowExceptionWhenPlateExists() throws InvalidVehicleException {
        // QUE HACE:
        // 1. Instancia un objeto Vehicle.
        // 2. Configura el mock 'vehiclePersistence' para simular que la matricula 
        //    "1234ABC" SI existe previamente (existsByPlate -> true).
        // 3. Ejecuta el caso de uso con el vehiculo.
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        
        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.existsByPlate("1234ABC")).thenReturn(true);

        // QUE DEBERIA HACER:
        // Debe lanzar una excepcion InvalidVehicleException indicando que el vehiculo 
        // ya existe con esa matricula, y ademas asegurar que NUNCA se invoque el metodo 'save'
        // para evitar persistir duplicados.
        assertThatThrownBy(() -> createVehicleUseCase.execute(vehicle))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Ya existe un vehículo con la matrícula: 1234ABC");

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }
}

package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    // @Captor: Captura el Vehicle que el caso de uso construye internamente a partir del DTO,
    // ya que Vehicle no implementa equals() y no se puede comparar por valor.
    @Captor
    private ArgumentCaptor<Vehicle> vehicleCaptor;

    @Test
    @DisplayName("Debe crear y guardar un vehiculo si la matricula no existe previamente")
    void shouldCreateVehicleSuccessfully() throws InvalidVehicleException {
        // QUE HACE:
        // 1. Instancia el DTO de entrada valido que ahora recibe el caso de uso.
        // 2. Configura el mock del puerto de salida 'vehiclePersistence' para simular que
        //    la matricula "1234ABC" NO existe previamente (existsByPlate -> false).
        // 3. Configura el mock para que 'save' devuelva el vehiculo de dominio ya persistido.
        // 4. Ejecuta el caso de uso con el DTO.
        VehicleCreateDTO createDTO = new VehicleCreateDTO("CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false);
        Vehicle savedVehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.existsByPlate("1234ABC")).thenReturn(false);
        when(vehiclePersistence.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleDTO result = createVehicleUseCase.execute(createDTO);

        // QUE DEBERIA HACER:
        // Debe finalizar con exito, retornar el DTO de salida con los datos del vehiculo
        // persistido, y verificar que se invoco exactamente una vez al metodo 'save' del
        // puerto de persistencia con el Vehicle construido a partir del DTO.
        assertThat(result).isNotNull();
        assertThat(result.plate()).isEqualTo("1234ABC");
        assertThat(result.type()).isEqualTo("CAR");

        verify(vehiclePersistence, times(1)).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().getPlate()).isEqualTo("1234ABC");
        assertThat(vehicleCaptor.getValue().getType()).isEqualTo(VehicleType.CAR);
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si la matricula ya esta registrada")
    void shouldThrowExceptionWhenPlateExists() throws InvalidVehicleException {
        // QUE HACE:
        // 1. Instancia el DTO de entrada.
        // 2. Configura el mock 'vehiclePersistence' para simular que la matricula
        //    "1234ABC" SI existe previamente (existsByPlate -> true).
        // 3. Ejecuta el caso de uso con el DTO.
        VehicleCreateDTO createDTO = new VehicleCreateDTO("CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false);

        // when(...).thenReturn(...): Indica al mock: "Cuando te llamen con estos parametros, responde esto".
        when(vehiclePersistence.existsByPlate("1234ABC")).thenReturn(true);

        // QUE DEBERIA HACER:
        // Debe lanzar una excepcion InvalidVehicleException indicando que el vehiculo
        // ya existe con esa matricula, y ademas asegurar que NUNCA se invoque el metodo 'save'
        // para evitar persistir duplicados.
        assertThatThrownBy(() -> createVehicleUseCase.execute(createDTO))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Ya existe un vehículo con la matrícula: 1234ABC");

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si el tipo del DTO no existe en el enum")
    void shouldThrowExceptionWhenTypeIsInvalid() {
        // QUE HACE:
        // 1. Instancia un DTO con un tipo que no existe en VehicleType.
        // 2. Ejecuta el caso de uso sin configurar ningun mock: la conversion del DTO al
        //    dominio ocurre antes de tocar la persistencia.
        VehicleCreateDTO createDTO = new VehicleCreateDTO("HELICOPTER", "1234ABC", "Toyota", "Corolla", "Red", 4, false);

        // QUE DEBERIA HACER:
        // Debe fallar al traducir el DTO al dominio y no llegar a consultar la matricula
        // ni a guardar nada.
        assertThatThrownBy(() -> createVehicleUseCase.execute(createDTO))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Tipo de vehículo inválido: HELICOPTER");

        verify(vehiclePersistence, never()).existsByPlate(anyString());
        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }
}

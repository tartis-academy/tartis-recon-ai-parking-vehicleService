package com.tartis_recon_ai_parking.domain.vehicle;

import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    @Test
    @DisplayName("Debe crear un coche valido (CAR) con puertas permitidas (2, 4, 5) y sin sidecar")
    void shouldCreateValidCar() throws InvalidVehicleException {
        // QUE HACE: 
        // Instancia un objeto Vehicle de tipo CAR con matricula, marca, modelo, color, 
        // 5 puertas (valor permitido), sin sidecar (hasSidecar = false) y activo.
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 5, false, true);

        // QUE DEBERIA HACER:
        // Debe construirse con exito y comprobar mediante aserciones que los getters 
        // retornan exactamente los mismos valores con los que fue inicializado.
        assertThat(vehicle.getType()).isEqualTo(VehicleType.CAR);
        assertThat(vehicle.getPlate()).isEqualTo("1234ABC");
        assertThat(vehicle.getNumDoors()).isEqualTo(5);
        assertThat(vehicle.getHasSidecar()).isFalse();
        assertThat(vehicle.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe crear una moto valida (MOTORBIKE) con 0 puertas e independientemente de si tiene sidecar")
    void shouldCreateValidMotorbike() throws InvalidVehicleException {
        // QUE HACE: 
        // Instancia dos objetos de tipo MOTORBIKE con 0 puertas: uno con sidecar y otro sin sidecar.
        Vehicle motorbikeWithSidecar = new Vehicle(VehicleType.MOTORBIKE, "5678DEF", "Honda", "CBR", "Black", 0, true, true);
        Vehicle motorbikeWithoutSidecar = new Vehicle(VehicleType.MOTORBIKE, "5678DEG", "Honda", "CBR", "Black", 0, false, true);

        // QUE DEBERIA HACER:
        // Ambos objetos deben crearse exitosamente y verificar que el campo de sidecar 
        // refleja la entrada correcta, y que el numero de puertas es 0.
        assertThat(motorbikeWithSidecar.getHasSidecar()).isTrue();
        assertThat(motorbikeWithoutSidecar.getHasSidecar()).isFalse();
        assertThat(motorbikeWithSidecar.getNumDoors()).isZero();
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si el coche tiene sidecar")
    void shouldThrowExceptionWhenCarHasSidecar() {
        // QUE HACE: 
        // Intenta crear un Vehicle de tipo CAR pero estableciendo hasSidecar en true.
        // QUE DEBERIA HACER:
        // Debe fallar y lanzar InvalidVehicleException, ya que las reglas de negocio de dominio 
        // prohiben que los coches tengan sidecar. El mensaje de la excepcion debe indicar esto.
        assertThatThrownBy(() -> new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, true, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Cars do not have sidecar");
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 3, 6 })
    @DisplayName("Debe lanzar InvalidVehicleException si un coche no tiene 2, 4 o 5 puertas")
    void shouldThrowExceptionWhenCarHasInvalidDoors(int doors) {
        // QUE HACE: 
        // Intenta crear un coche (CAR) utilizando numeros de puertas no validos (1, 3, 6).
        // QUE DEBERIA HACER:
        // Debe lanzar InvalidVehicleException en cada iteracion del test parametrizado, 
        // con un mensaje que explique que el numero de puertas del coche es incorrecto.
        assertThatThrownBy(
                () -> new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", doors, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Incorrect number of doors for a car");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException con mensaje de numero negativo si las puertas son menores que 0")
    void shouldThrowExceptionWhenDoorsAreNegative() {
        // QUE HACE: 
        // Intenta instanciar un coche con un numero de puertas negativo (-1).
        // QUE DEBERIA HACER:
        // Debe lanzar InvalidVehicleException indicando que el numero de puertas no puede ser negativo,
        // validandose en el primer paso de comprobacion de puertas.
        assertThatThrownBy(() -> new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", -1, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Number of doors cannot be a negative number");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si una moto tiene puertas")
    void shouldThrowExceptionWhenMotorbikeHasDoors() {
        // QUE HACE: 
        // Intenta crear una moto (MOTORBIKE) con 2 puertas (un valor mayor a 0).
        // QUE DEBERIA HACER:
        // Debe lanzar una InvalidVehicleException porque las motos no pueden tener puertas, 
        // validando que el mensaje de error corresponda a este caso de negocio.
        assertThatThrownBy(() -> new Vehicle(VehicleType.MOTORBIKE, "5678DEF", "Honda", "CBR", "Black", 2, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Incorrect number of doors for a motorbike");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si algun parametro obligatorio es nulo")
    void shouldThrowExceptionWhenRequiredParametersAreNull() {
        // QUE HACE: 
        // Intenta instanciar un coche enviando el tipo como null, y luego con la matricula como null.
        // QUE DEBERIA HACER:
        // Debe lanzar InvalidVehicleException con mensajes de error descriptivos ("Vehicle type is null" 
        // o "Vehicle plate is null") impidiendo la construccion de un objeto invalido.
        assertThatThrownBy(() -> new Vehicle(null, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Vehicle type is null");

        assertThatThrownBy(() -> new Vehicle(VehicleType.CAR, null, "Toyota", "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Vehicle plate is null");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si el setter recibe valores nulos o invalidos")
    void shouldValidateSetters() throws InvalidVehicleException {
        // QUE HACE: 
        // Crea un coche valido y luego intenta modificar la matricula a null mediante setPlate(),
        // o intenta asignarle un sidecar mediante setHasSidecar(true).
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        // QUE DEBERIA HACER:
        // Ambos setters deben lanzar InvalidVehicleException porque violan las reglas del dominio,
        // asegurando que no se pueda corromper el estado del objeto despues de creado.
        assertThatThrownBy(() -> vehicle.setPlate(null))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Null vehicle plate");

        assertThatThrownBy(() -> vehicle.setHasSidecar(true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Cars cannot have sidecar");
    }
}

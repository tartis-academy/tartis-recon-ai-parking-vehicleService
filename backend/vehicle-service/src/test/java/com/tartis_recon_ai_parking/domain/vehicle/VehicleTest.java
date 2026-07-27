package com.tartis_recon_ai_parking.domain.vehicle;

import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// assertThat y assertThatThrownBy: Son metodos estaticos de AssertJ que permiten escribir 
// comprobaciones fluidas, faciles de leer y autoexplicativas sobre los resultados.
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
// assertDoesNotThrow: Metodo estatico de JUnit Jupiter que verifica que una expresion
// lambda se ejecuta sin lanzar ninguna excepcion.
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VehicleTest {

    // @Test: Indica a JUnit que este metodo es una prueba unitaria individual que debe ejecutarse.
    @Test
    @DisplayName("Debe crear un coche valido (CAR) con puertas permitidas (2, 4, 5) y sin sidecar")
    void shouldCreateValidCar() throws InvalidVehicleException {
        // QUE HACE: 
        // Instancia un objeto Vehicle de tipo CAR con matricula, marca, modelo, color, 
        // 5 puertas (valor permitido), sin sidecar (hasSidecar = false) y activo.
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 5, false, true);

        // QUE DEBERIA HACER:
        // Debe construirse con exito y comprobar mediante aserciones que los getters 
        // retornan exactamente los mismos valores con los que fue inicializado.
        
        // assertThat: Compara el valor real (obtenido del getter) con el valor esperado usando isEqualTo.
        assertThat(vehicle.getType()).isEqualTo(VehicleType.CAR);
        assertThat(vehicle.getPlate()).isEqualTo("1234BCD");
        assertThat(vehicle.getNumDoors()).isEqualTo(5);
        assertThat(vehicle.getHasSidecar()).isFalse();
        assertThat(vehicle.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe crear una moto valida (MOTORBIKE) con 0 puertas e independientemente de si tiene sidecar")
    void shouldCreateValidMotorbike() throws InvalidVehicleException {
        // QUE HACE: 
        // Instancia dos objetos de tipo MOTORBIKE con 0 puertas: uno con sidecar y otro sin sidecar.
        Vehicle motorbikeWithSidecar = Vehicle.create(VehicleType.MOTORBIKE, "5678DFG", "Honda", "CBR", "Black", 0, true, true);
        Vehicle motorbikeWithoutSidecar = Vehicle.create(VehicleType.MOTORBIKE, "5678DGH", "Honda", "CBR", "Black", 0, false, true);

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
        
        // assertThatThrownBy: Captura la excepcion que lance la expresion lambda.
        // isInstanceOf: Valida que la excepcion capturada sea exactamente de la clase indicada.
        // hasMessageContaining: Valida que el mensaje de error de la excepcion tenga ese fragmento de texto.
        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, true, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Cars do not have sidecar");
    }

    // @ParameterizedTest: Indica a JUnit que esta prueba se ejecutara varias veces usando
    // diferentes entradas (parametros) suministradas por un proveedor de datos.
    // @ValueSource: Proveedor de datos que pasa una lista de valores simples (en este caso enteros)
    // uno por uno en cada ejecucion de la prueba.
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
                () -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", doors, false, true))
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
        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", -1, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: numDoors can't be -1");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si una moto tiene puertas")
    void shouldThrowExceptionWhenMotorbikeHasDoors() {
        // QUE HACE: 
        // Intenta crear una moto (MOTORBIKE) con 2 puertas (un valor mayor a 0).
        // QUE DEBERIA HACER:
        // Debe lanzar una InvalidVehicleException porque las motos no pueden tener puertas, 
        // validando que el mensaje de error corresponda a este caso de negocio.
        assertThatThrownBy(() -> Vehicle.create(VehicleType.MOTORBIKE, "5678DFG", "Honda", "CBR", "Black", 2, false, true))
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
        assertThatThrownBy(() -> Vehicle.create(null, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: type can't be null");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, null, "Toyota", "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: plate can't be null");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", null, "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: brand can't be null");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", null, "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: model can't be null");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", null, 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: color can't be null");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si marca, modelo o color son nulos")
    void shouldThrowExceptionWhenTextParametersAreNull() {
        // QUE HACE:
        // Intenta crear un coche valido en todo lo demas pero con brand, model o
        // color a null. Estas tres comprobaciones ocurren ANTES de validPlate, asi
        // que el mensaje debe senalar el campo de texto y no la matricula.
        // QUE DEBERIA HACER:
        // Debe lanzar InvalidVehicleException nombrando el campo nulo concreto,
        // impidiendo persistir un vehiculo con datos incompletos.
        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", null, "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: brand can't be null");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", null, "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: model can't be null");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", null, 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: color can't be null");
    }

    @Test
    @DisplayName("update tambien debe rechazar marca, modelo o color nulos")
    void shouldThrowExceptionWhenUpdatingWithNullTextParameters() throws InvalidVehicleException {
        // QUE HACE:
        // Crea un vehiculo valido y luego intenta actualizarlo dejando a null cada
        // uno de los campos de texto obligatorios.
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        // QUE DEBERIA HACER:
        // update reutiliza validateData, asi que debe aplicar las mismas reglas que
        // create: el objeto no puede corromperse por una actualizacion parcial.
        assertThatThrownBy(() -> vehicle.update(VehicleType.CAR, "1234BCD", null, "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: brand can't be null");

        assertThatThrownBy(() -> vehicle.update(VehicleType.CAR, "1234BCD", "Toyota", null, "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: model can't be null");

        assertThatThrownBy(() -> vehicle.update(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", null, 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: color can't be null");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si parametros de texto obligatorios estan en blanco")
    void shouldThrowExceptionWhenStringParametersAreBlank() {
        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "  ", "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: brand can't be empty ( )");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "  ", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: model can't be empty ( )");

        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "  ", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: color can't be empty ( )");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si el setter recibe valores nulos o invalidos")
    void shouldValidateSetters() throws InvalidVehicleException {
        // QUE HACE: 
        // Crea un coche valido y luego intenta modificar la matricula a null mediante setPlate(),
        // o intenta asignarle un sidecar mediante setHasSidecar(true).
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        // QUE DEBERIA HACER:
        // Las actualizaciones deben lanzar InvalidVehicleException porque violan las reglas del dominio,
        // asegurando que no se pueda corromper el estado del objeto al actualizarlo.
        assertThatThrownBy(() -> vehicle.update(vehicle.getType(), null, vehicle.getBrand(), vehicle.getModel(), vehicle.getColor(), vehicle.getNumDoors(), vehicle.getHasSidecar(), vehicle.isActive()))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: plate can't be null");

        assertThatThrownBy(() -> vehicle.update(vehicle.getType(), vehicle.getPlate(), vehicle.getBrand(), vehicle.getModel(), vehicle.getColor(), vehicle.getNumDoors(), true, vehicle.isActive()))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Cars do not have sidecar");
    }

    // =============== Tests para validPlate ===============

    // @ParameterizedTest: Ejecuta el test una vez por cada valor proporcionado en @ValueSource.
    // @ValueSource(strings): Proporciona cadenas de texto como parametros de entrada.
    @ParameterizedTest
    @ValueSource(strings = { "1234BCD", "0000BBB", "9999ZZZ", "4567KLM", "1234DFG" })
    @DisplayName("No debe lanzar excepcion para matriculas modernas validas (4 digitos + 3 consonantes)")
    void shouldNotThrowExceptionForValidModernPlates(String plate) {
        // QUE HACE:
        // Llama a validPlate con matriculas en formato moderno correcto (4 digitos + 3 consonantes).
        // QUE DEBERIA HACER:
        // No debe lanzar ninguna excepcion, lo que confirma que la matricula es valida.

        // assertDoesNotThrow: Verifica que la expresion lambda NO lanza ninguna excepcion.
        assertDoesNotThrow(() -> Vehicle.validPlate(plate));
    }

    @ParameterizedTest
    @ValueSource(strings = { "M-1234-AB", "B-4567-CD", "MA-1234-AB", "GR-123456-XY", "BI-9999-ZZ" })
    @DisplayName("No debe lanzar excepcion para matriculas antiguas validas (provincia-digitos-letras)")
    void shouldNotThrowExceptionForValidOldPlates(String plate) {
        // QUE HACE:
        // Llama a validPlate con matriculas en formato antiguo correcto
        // (1-2 letras de provincia + guion + 4-6 digitos + guion + 2 letras).
        // QUE DEBERIA HACER:
        // No debe lanzar ninguna excepcion, lo que confirma que la matricula es valida.
        assertDoesNotThrow(() -> Vehicle.validPlate(plate));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1234ABC",   // Contiene vocal 'A' en la parte de letras modernas
        "1234aBC",   // Contiene minuscula
        "123BCD",    // Solo 3 digitos en formato moderno
        "12345BCD",  // 5 digitos en formato moderno
        "1234BCDF",  // 4 letras en vez de 3
        "ABCD1234",  // Letras antes de digitos
        "1234",      // Solo digitos
        "BCD",       // Solo letras
        "M1234AB",   // Formato antiguo sin guiones
        "MAD-1234-AB", // 3 letras de provincia (maximo son 2)
        "M-123-AB",  // Solo 3 digitos en formato antiguo
        "M-1234567-AB" // 7 digitos en formato antiguo
    })
    @DisplayName("Debe lanzar InvalidVehicleException para matriculas con formato invalido")
    void shouldThrowExceptionForInvalidPlateFormat(String plate) {
        // QUE HACE:
        // Llama a validPlate con matriculas que no cumplen ni el formato moderno ni el antiguo.
        // QUE DEBERIA HACER:
        // Debe lanzar InvalidVehicleException con un mensaje indicando que el patron es invalido.
        assertThatThrownBy(() -> Vehicle.validPlate(plate))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid plate pattern");
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si la matricula es null")
    void shouldThrowExceptionWhenPlateIsNullInValidPlate() {
        // QUE HACE:
        // Llama a validPlate pasando null como matricula.
        // QUE DEBERIA HACER:
        // Debe lanzar InvalidVehicleException indicando que la matricula es nula.
        assertThatThrownBy(() -> Vehicle.validPlate(null))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: plate can't be null");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t", "\n" })
    @DisplayName("Debe lanzar InvalidVehicleException si la matricula esta en blanco")
    void shouldThrowExceptionWhenPlateIsBlank(String plate) {
        // QUE HACE:
        // Llama a validPlate pasando cadenas vacias o con solo espacios en blanco.
        // QUE DEBERIA HACER:
        // Debe lanzar InvalidVehicleException indicando que el campo de matricula no puede estar vacio.
        assertThatThrownBy(() -> Vehicle.validPlate(plate))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid vehicle data: plate can't be empty ( )");
    }

    @Test
    @DisplayName("reconstruct no debe validar reglas de negocio — matrículas legacy de BD no deben lanzar excepción")
    void reconstructShouldNotValidateBusinessRules() {
        // "1234ABC" contiene la vocal 'A', así que create() la rechaza por el regex nuevo.
        // Pero reconstruct() debe aceptarla sin problema porque reconstruye datos ya persistidos.
        java.util.UUID id = java.util.UUID.randomUUID();

        // reconstruct NO debe lanzar excepción
        assertDoesNotThrow(() -> Vehicle.reconstruct(id, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true));

        // create SÍ debe lanzar excepción para la misma matrícula
        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Invalid plate pattern");

        // Verificamos que reconstruct asigna los campos correctamente
        Vehicle v = Vehicle.reconstruct(id, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        assertThat(v.getUniqueId()).isEqualTo(id);
        assertThat(v.getPlate()).isEqualTo("1234ABC");
        assertThat(v.getBrand()).isEqualTo("Toyota");
        assertThat(v.getType()).isEqualTo(VehicleType.CAR);
        assertThat(v.getModel()).isEqualTo("Corolla");
        assertThat(v.getColor()).isEqualTo("Red");
        assertThat(v.getNumDoors()).isEqualTo(4);
        assertThat(v.getHasSidecar()).isFalse();
        assertThat(v.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe crear un coche PMR (CAR_PMR) valido sin sidecar")
    void shouldCreateValidCarPMR() throws InvalidVehicleException {
        Vehicle vehicle = Vehicle.create(VehicleType.CAR_PMR, "1234BCD", "Toyota", "Prius", "White", 5, false, true);

        assertThat(vehicle.getType()).isEqualTo(VehicleType.CAR_PMR);
        assertThat(vehicle.getPlate()).isEqualTo("1234BCD");
        assertThat(vehicle.getNumDoors()).isEqualTo(5);
        assertThat(vehicle.getHasSidecar()).isFalse();
        assertThat(vehicle.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar InvalidVehicleException si un coche PMR tiene sidecar")
    void shouldThrowExceptionWhenCarPMRHasSidecar() {
        assertThatThrownBy(() -> Vehicle.create(VehicleType.CAR_PMR, "1234BCD", "Toyota", "Prius", "White", 4, true, true))
                .isInstanceOf(InvalidVehicleException.class)
                .hasMessageContaining("Cars do not have sidecar");
    }

    @Test
    @DisplayName("Debe actualizar, activar y desactivar correctamente un vehiculo")
    void shouldUpdateActivateAndDeactivate() throws InvalidVehicleException {
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, false);
        
        // Activar
        Vehicle activeVehicle = vehicle.activate();
        assertThat(activeVehicle.isActive()).isTrue();
        
        // Desactivar
        Vehicle inactiveVehicle = activeVehicle.deactivate();
        assertThat(inactiveVehicle.isActive()).isFalse();
        
        // Actualizar
        Vehicle updatedVehicle = vehicle.update(VehicleType.CAR, "1234BCD", "Toyota", "Yaris", "Blue", 5, false, true);
        assertThat(updatedVehicle.getModel()).isEqualTo("Yaris");
        assertThat(updatedVehicle.getColor()).isEqualTo("Blue");
        assertThat(updatedVehicle.getNumDoors()).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe crear un coche con 2 puertas")
    void shouldCreateCarWithTwoDoors() throws InvalidVehicleException {
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 2, false, true);
        assertThat(vehicle.getNumDoors()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe actualizar correctamente una moto añadiendo un sidecar")
    void shouldUpdateMotorbikeToAddSidecar() throws InvalidVehicleException {
        Vehicle motorbike = Vehicle.create(VehicleType.MOTORBIKE, "5678DFG", "Honda", "CBR", "Black", 0, false, true);
        Vehicle updatedMotorbike = motorbike.update(VehicleType.MOTORBIKE, "5678DFG", "Honda", "CBR", "Black", 0, true, true);
        
        assertThat(updatedMotorbike.getHasSidecar()).isTrue();
    }

    @Test
    @DisplayName("Debe mantener el estado inactivo al desactivar un vehiculo ya inactivo")
    void shouldKeepInactiveWhenDeactivatingAlreadyInactiveVehicle() throws InvalidVehicleException {
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, false);
        Vehicle deactivatedVehicle = vehicle.deactivate();
        
        assertThat(deactivatedVehicle.isActive()).isFalse();
    }

    @Test
    @DisplayName("Debe mantener el estado activo al activar un vehiculo ya activo")
    void shouldKeepActiveWhenActivatingAlreadyActiveVehicle() throws InvalidVehicleException {
        Vehicle vehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);
        Vehicle activatedVehicle = vehicle.activate();
        
        assertThat(activatedVehicle.isActive()).isTrue();
    }
}

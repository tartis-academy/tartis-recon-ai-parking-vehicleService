package com.tartis_recon_ai_parking.domain.vehicle.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Las excepciones de dominio llevan estado propio (el campo que fallo, el valor
// invalido, la matricula duplicada...) para que las capas de arriba puedan
// reaccionar sin parsear el mensaje. Ese estado se expone con getters que hasta
// ahora no cubria ningun test: Jacoco los contaba como lineas perdidas y
// SonarCloud ademas los marcaba como codigo potencialmente muerto.
class VehicleExceptionsTest {

    @Nested
    @DisplayName("VehicleNotFoundException")
    class VehicleNotFoundExceptionTest {

        @Test
        @DisplayName("Debe componer el mensaje y exponer el criterio y el valor buscado")
        void shouldExposeSearchCriteriaAndValue() {
            // QUE HACE:
            // Construye la excepcion simulando una busqueda por ID fallida.
            UUID id = UUID.randomUUID();
            VehicleNotFoundException ex = new VehicleNotFoundException("ID", id);

            // QUE DEBERIA HACER:
            // El mensaje debe nombrar el criterio y el valor, y los getters deben
            // devolver exactamente lo que se paso al constructor.
            assertThat(ex.getMessage())
                    .isEqualTo("Vehicle with 'ID = " + id + "' couldn't be found.");
            assertThat(ex.getsearchObjective()).isEqualTo("ID");
            assertThat(ex.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Debe funcionar igual buscando por matricula (criterio abierto)")
        void shouldWorkWithPlateAsCriteria() {
            // QUE HACE:
            // La misma excepcion sirve para cualquier criterio de busqueda; aqui se
            // usa la matricula en lugar del ID.
            VehicleNotFoundException ex = new VehicleNotFoundException("plate", "1234BCD");

            // QUE DEBERIA HACER:
            // Debe reflejar el nuevo criterio sin necesidad de una subclase distinta.
            assertThat(ex.getMessage()).contains("plate = 1234BCD");
            assertThat(ex.getsearchObjective()).isEqualTo("plate");
            assertThat(ex.getId()).isEqualTo("1234BCD");
        }

        @Test
        @DisplayName("Debe ser una excepcion comprobada (checked)")
        void shouldBeACheckedException() {
            // QUE HACE / QUE DEBERIA HACER:
            // Documenta la decision de diseno: al extender Exception obliga a los
            // casos de uso a declarar throws. Si alguien la cambia a
            // RuntimeException, este test avisa.
            assertThat(new VehicleNotFoundException("ID", UUID.randomUUID()))
                    .isInstanceOf(Exception.class)
                    .isNotInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("InvalidVehicleException")
    class InvalidVehicleExceptionTest {

        @Test
        @DisplayName("Constructor de dos argumentos: debe generar el mensaje por defecto")
        void shouldBuildDefaultMessage() {
            // QUE HACE:
            // Usa el constructor corto, el que emplea Vehicle.validateData para los
            // campos nulos o vacios.
            InvalidVehicleException ex = new InvalidVehicleException("brand", null);

            // QUE DEBERIA HACER:
            // Debe construir el mensaje estandar y conservar campo y valor invalido.
            assertThat(ex.getMessage()).isEqualTo("Invalid vehicle data: brand can't be null");
            assertThat(ex.getField()).isEqualTo("brand");
            assertThat(ex.getInvalidValue()).isNull();
        }

        @Test
        @DisplayName("Constructor de tres argumentos: debe respetar el mensaje personalizado")
        void shouldRespectCustomMessage() {
            // QUE HACE:
            // Usa el constructor largo, el de las reglas de negocio con explicacion
            // propia ("Cars do not have sidecar", etc.).
            InvalidVehicleException ex =
                    new InvalidVehicleException("hasSideCar", true, "Cars do not have sidecar");

            // QUE DEBERIA HACER:
            // El mensaje debe ser el pasado tal cual, sin el prefijo por defecto.
            assertThat(ex.getMessage()).isEqualTo("Cars do not have sidecar");
            assertThat(ex.getField()).isEqualTo("hasSideCar");
            assertThat(ex.getInvalidValue()).isEqualTo(true);
        }

        @Test
        @DisplayName("Debe ser una excepcion no comprobada (unchecked)")
        void shouldBeAnUncheckedException() {
            // QUE HACE / QUE DEBERIA HACER:
            // La validacion de dominio es un error del cliente de la API, no un
            // flujo alternativo que el codigo deba capturar: por eso extiende
            // RuntimeException y la traduce el @RestControllerAdvice a un 400.
            assertThat(new InvalidVehicleException("numDoors", 3))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("ExistingVehicleException")
    class ExistingVehicleExceptionTest {

        @Test
        @DisplayName("Debe componer el mensaje y exponer la matricula duplicada")
        void shouldExposeDuplicatedPlate() {
            // QUE HACE:
            // Simula el alta de un vehiculo cuya matricula ya existe en la BD.
            ExistingVehicleException ex = new ExistingVehicleException("1234BCD");

            // QUE DEBERIA HACER:
            // El mensaje debe incluir la matricula y getPlate() devolverla para que
            // el adaptador REST pueda usarla sin parsear texto.
            assertThat(ex.getMessage())
                    .isEqualTo("There's already a vehicle with the specified plate : 1234BCD");
            assertThat(ex.getPlate()).isEqualTo("1234BCD");
        }

        @Test
        @DisplayName("Debe ser una excepcion no comprobada (unchecked)")
        void shouldBeAnUncheckedException() {
            assertThat(new ExistingVehicleException("1234BCD"))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}

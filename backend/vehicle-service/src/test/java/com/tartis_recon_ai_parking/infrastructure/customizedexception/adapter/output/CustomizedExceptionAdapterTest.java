package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// El @RestControllerAdvice es el unico punto donde las excepciones de dominio se
// convierten en codigos HTTP. Se prueba como una clase normal (sin levantar el
// contexto de Spring) porque los @ExceptionHandler son metodos publicos
// corrientes: el test es instantaneo y no depende de Postgres ni del slice web.
class CustomizedExceptionAdapterTest {

    private final CustomizedExceptionAdapter advice = new CustomizedExceptionAdapter();

    @Test
    @DisplayName("VehicleNotFoundException debe traducirse a 404 Not Found con el mensaje de la excepcion")
    void shouldMapNotFoundTo404() {
        // QUE HACE:
        // Entrega al handler la excepcion que lanzan GetVehicleUseCase,
        // UpdateVehicleUseCase y DeleteVehicleUseCase cuando el ID no existe.
        UUID id = UUID.randomUUID();
        VehicleNotFoundException ex = new VehicleNotFoundException("ID", id);

        ResponseEntity<String> response = advice.handleNotFound(ex);

        // QUE DEBERIA HACER:
        // Devolver 404 y propagar el mensaje al cuerpo, sin envolverlo ni truncarlo.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(ex.getMessage());
        assertThat(response.getBody()).contains(id.toString());
    }

    @Test
    @DisplayName("InvalidVehicleException debe traducirse a 400 Bad Request con el mensaje de la excepcion")
    void shouldMapInvalidTo400() {
        // QUE HACE:
        // Entrega al handler la excepcion de validacion de dominio (Vehicle.validateData).
        InvalidVehicleException ex = new InvalidVehicleException("numDoors", 3, "Incorrect number of doors for a car");

        ResponseEntity<String> response = advice.handleInvalid(ex);

        // QUE DEBERIA HACER:
        // Devolver 400, porque el error lo ha provocado el cliente al enviar datos
        // que violan las reglas de negocio.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Incorrect number of doors for a car");
    }

    @Test
    @DisplayName("ExistingVehicleException debe traducirse a 400 Bad Request con la matricula duplicada")
    void shouldMapExistingTo400() {
        // QUE HACE:
        // Entrega al handler la excepcion de matricula duplicada que lanza
        // CreateVehicleUseCase.
        ExistingVehicleException ex = new ExistingVehicleException("1234BCD");

        ResponseEntity<String> response = advice.handleExisting(ex);

        // QUE DEBERIA HACER:
        // Devolver 400 y un cuerpo que identifique la matricula en conflicto.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(ex.getMessage());
        assertThat(response.getBody()).contains("1234BCD");
    }

    @Test
    @DisplayName("Ningun handler debe devolver un cuerpo vacio")
    void shouldNeverReturnAnEmptyBody() {
        // QUE HACE / QUE DEBERIA HACER:
        // Un error sin explicacion es inutil para el consumidor de la API: los tres
        // handlers deben responder siempre con texto.
        assertThat(advice.handleNotFound(new VehicleNotFoundException("plate", "1234BCD")).getBody())
                .isNotBlank();
        assertThat(advice.handleInvalid(new InvalidVehicleException("brand", null)).getBody())
                .isNotBlank();
        assertThat(advice.handleExisting(new ExistingVehicleException("1234BCD")).getBody())
                .isNotBlank();
    }
}

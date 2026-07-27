package com.tartis_recon_ai_parking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

// El main() es la unica linea del modulo que Jacoco no podia cubrir sin arrancar
// la aplicacion entera. En lugar de un @SpringBootTest (que exigiria un Postgres
// levantado y anadiria varios segundos al build) se sustituye SpringApplication
// por un mock estatico: se comprueba que main delega en el arranque de Spring con
// la clase de configuracion correcta, sin abrir ni un socket.
class VehicleServiceApplicationTest {

    @Test
    @DisplayName("main debe arrancar Spring Boot usando VehicleServiceApplication como clase principal")
    void mainShouldDelegateToSpringApplication() {
        String[] args = { "--spring.profiles.active=test" };

        // mockStatic: intercepta las llamadas a metodos estaticos de SpringApplication
        // solo dentro de este bloque try. Al cerrarse, la clase vuelve a su
        // comportamiento real y no contamina otros tests.
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {

            VehicleServiceApplication.main(args);

            // QUE DEBERIA HACER:
            // Delegar exactamente una vez en SpringApplication.run, pasando la clase
            // anotada con @SpringBootApplication y los argumentos recibidos sin tocar.
            springApplication.verify(() -> SpringApplication.run(VehicleServiceApplication.class, args));
        }
    }

    @Test
    @DisplayName("La clase principal debe ser instanciable (constructor por defecto)")
    void shouldBeInstantiable() {
        // Spring necesita el constructor por defecto para registrar la clase de
        // configuracion; ademas cubre la linea de la declaracion de clase.
        assertThat(new VehicleServiceApplication()).isNotNull();
    }
}

package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleConcurrentModificationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Test de integracion del bloqueo optimista.
//
// Por que no basta un test unitario: mockear el repositorio y hacer
// thenThrow(new OptimisticLockingFailureException(...)) solo demuestra que un catch
// captura una excepcion fabricada por el propio test. Aqui el choque lo produce
// Hibernate de verdad, comprobando la columna @Version contra un Postgres real.
//
// Piezas de la configuracion:
// - @AutoConfigureTestDatabase(replace = NONE): sin esto @DataJpaTest sustituiria el
//   DataSource por una H2 embebida y el contenedor no llegaria a usarse.
// - @ServiceConnection: apunta el DataSource al contenedor sin @DynamicPropertySource.
// - @Transactional(propagation = NOT_SUPPORTED): @DataJpaTest envuelve cada test en una
//   transaccion con rollback automatico. Con ella activa los hilos no verian los datos
//   del test (no estan commiteados) y no habria choque posible. La limpieza va a mano.
// - VehiclePersistenceMapperImpl: la implementacion que genera MapStruct es un
//   @Component, y el slice de @DataJpaTest no escanea componentes: hay que importarla.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({VehiclePersistenceAdapter.class, VehiclePersistenceMapperImpl.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class VehiclePersistenceAdapterConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.18-alpine");

    private static final String PLATE = "1234BCD";

    @Autowired
    private VehiclePersistenceAdapter adapter;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // Sin la transaccion envolvente de @DataJpaTest no hay rollback automatico:
    // cada test tiene que dejar la tabla como la encontro.
    @AfterEach
    void cleanUp() {
        vehicleRepository.deleteAll();
    }

    @Test
    @DisplayName("Dos transacciones concurrentes sobre la misma version: una gana y la otra lanza VehicleConcurrentModificationException")
    void shouldThrowConcurrentModificationWhenTwoTransactionsUpdateSameVersion() throws InterruptedException {
        // QUE HACE:
        // - Persiste un vehiculo y se queda con la copia inicial (version 0).
        // - Lanza dos hilos que arrancan a la vez desde ESA MISMA copia obsoleta, cada uno
        //   con su propia transaccion, e intentan guardar un color distinto.
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        Vehicle initial = txTemplate.execute(status ->
                adapter.save(Vehicle.create(VehicleType.CAR, PLATE, "Toyota", "Corolla", "Red", 4, false, true)));

        assertThat(initial).isNotNull();
        assertThat(initial.getVersion()).isZero();

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(2);
        AtomicReference<Throwable> failureA = new AtomicReference<>();
        AtomicReference<Throwable> failureB = new AtomicReference<>();

        Thread threadA = new Thread(saveInOwnTransaction(txTemplate, initial, "Blue", startGate, finished, failureA));
        Thread threadB = new Thread(saveInOwnTransaction(txTemplate, initial, "Green", startGate, finished, failureB));

        threadA.start();
        threadB.start();
        startGate.countDown();

        assertThat(finished.await(30, TimeUnit.SECONDS))
                .as("Los dos hilos deben terminar; si expira, alguno se quedo bloqueado en la BD")
                .isTrue();
        threadA.join();
        threadB.join();

        // QUE DEBERIA HACER:
        // Exactamente uno de los dos debe fallar (el que llegue con la version obsoleta),
        // traducido a la excepcion de dominio y conservando la causa original de Spring.
        // Cual de los dos gana la carrera no es determinista, por eso no se fija.
        Throwable loserFailure = failureA.get() != null ? failureA.get() : failureB.get();
        boolean exactlyOneFailed = (failureA.get() == null) != (failureB.get() == null);

        assertThat(exactlyOneFailed)
                .as("Uno de los hilos debe ganar y el otro chocar. A=%s, B=%s", failureA.get(), failureB.get())
                .isTrue();

        assertThat(loserFailure)
                .isInstanceOf(VehicleConcurrentModificationException.class)
                .hasMessageContaining(PLATE)
                .hasCauseInstanceOf(OptimisticLockingFailureException.class);

        assertThat(((VehicleConcurrentModificationException) loserFailure).getPlate()).isEqualTo(PLATE);

        // La fila conserva los datos del hilo ganador y la version incrementada una sola vez.
        Optional<VehicleEntity> persisted = vehicleRepository.findByPlate(PLATE);
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getVersion()).isEqualTo(1L);
        assertThat(persisted.get().getColor()).isIn("Blue", "Green");
    }

    @Test
    @DisplayName("Regresion: una matricula duplicada sigue traduciendose a ExistingVehicleException")
    void shouldStillThrowExistingVehicleExceptionOnDuplicatePlate() {
        // QUE HACE:
        // Guarda dos vehiculos distintos con la misma matricula. Es la regresion que
        // introdujo el PR #42 al condicionar el catch al nombre de una constraint
        // inexistente: aqui se comprueba que reordenar los catch no la ha roto.
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        txTemplate.executeWithoutResult(status ->
                adapter.save(Vehicle.create(VehicleType.CAR, "5678FGH", "Ford", "Focus", "Black", 4, false, true)));

        // QUE DEBERIA HACER:
        // La violacion de vehicles_plate_key debe salir como ExistingVehicleException,
        // no como VehicleConcurrentModificationException ni como error crudo de Spring.
        assertThatThrownBy(() -> txTemplate.executeWithoutResult(status ->
                adapter.save(Vehicle.create(VehicleType.CAR, "5678FGH", "Seat", "Leon", "White", 4, false, true))))
                .isInstanceOf(ExistingVehicleException.class)
                .hasMessageContaining("5678FGH");
    }

    // Cada hilo espera en la misma barrera para que ambos arranquen con la copia en
    // version 0 y el choque sea real, no un guardado detras de otro.
    private Runnable saveInOwnTransaction(TransactionTemplate txTemplate, Vehicle stale, String color,
                                          CountDownLatch startGate, CountDownLatch finished,
                                          AtomicReference<Throwable> failure) {
        return () -> {
            try {
                startGate.await();
                txTemplate.executeWithoutResult(status ->
                        adapter.save(stale.update(VehicleType.CAR, PLATE, "Toyota", "Corolla", color, 4, false, true)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                failure.set(e);
            } catch (RuntimeException e) {
                failure.set(e);
            } finally {
                finished.countDown();
            }
        };
    }
}

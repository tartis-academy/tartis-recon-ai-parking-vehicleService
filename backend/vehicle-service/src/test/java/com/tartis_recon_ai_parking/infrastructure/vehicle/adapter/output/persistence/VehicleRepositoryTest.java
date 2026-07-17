package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest: Configura un entorno de pruebas enfocado únicamente en la capa JPA.
// Levanta una base de datos embebida (H2) y autoconfigura los repositorios y EntityManager.
@DataJpaTest
class VehicleRepositoryTest {

    // TestEntityManager: Herramienta de Spring Boot para pruebas de persistencia que permite 
    // realizar operaciones basicas (persist, flush, etc.) en la BD de pruebas sin usar directamente 
    // el repositorio que estamos probando, aislando la fase de preparacion 
    @Autowired
    private TestEntityManager entityManager;

    // Repositorio bajo prueba
    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    @DisplayName("Debe retornar True si existe un vehiculo con la matricula consultada")
    void shouldReturnTrueWhenPlateExists() {
        // QUE HACE:
        // - Instancia y rellena un VehicleEntity.
        // - Persiste la entidad directamente en la BD usando el TestEntityManager.
        // - Ejecuta la consulta existsByPlate con la matricula guardada.
        UUID id = UUID.randomUUID();
        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setType(VehicleType.CAR);
        entity.setPlate("1234ABC");
        entity.setBrand("Toyota");
        entity.setModel("Corolla");
        entity.setColor("Red");
        entity.setNumDoors(4);
        entity.setHasSidecar(false);
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        boolean exists = vehicleRepository.existsByPlate("1234ABC");

        // QUE DEBERIA HACER:
        // Debe retornar true indicando que la matricula ya esta registrada en el sistema.
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe retornar False si no existe un vehiculo con la matricula consultada")
    void shouldReturnFalseWhenPlateDoesNotExist() {
        // QUE HACE:
        // Llama directamente a existsByPlate con una matricula inexistente sin guardar nada previo.
        boolean exists = vehicleRepository.existsByPlate("9999XYZ");

        // QUE DEBERIA HACER:
        // Debe retornar false puesto que ningun vehiculo posee dicha matricula en BD.
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Debe encontrar y retornar un vehiculo por su matricula si existe")
    void shouldFindVehicleByPlateSuccessfully() {
        // QUE HACE:
        // - Persiste un vehiculo de prueba con matricula "5678DEF".
        // - Realiza la busqueda a traves de findByPlate en el repositorio.
        UUID id = UUID.randomUUID();
        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setType(VehicleType.MOTORBIKE);
        entity.setPlate("5678DEF");
        entity.setBrand("Honda");
        entity.setModel("CBR");
        entity.setColor("Black");
        entity.setNumDoors(0);
        entity.setHasSidecar(true);
        entity.setActive(true);

        entityManager.persistAndFlush(entity);

        Optional<VehicleEntity> result = vehicleRepository.findByPlate("5678DEF");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con contenido (isPresent = true), y comprobar que 
        // los valores del vehiculo retornado correspondan con los que guardamos.
        assertThat(result).isPresent();
        assertThat(result.get().getUniqueId()).isEqualTo(id);
        assertThat(result.get().getPlate()).isEqualTo("5678DEF");
        assertThat(result.get().getType()).isEqualTo(VehicleType.MOTORBIKE);
    }

    @Test
    @DisplayName("Debe retornar un Optional vacio al buscar una matricula que no existe")
    void shouldReturnEmptyOptionalWhenPlateNotFound() {
        // QUE HACE:
        // Busca un vehiculo por la matricula "9999XYZ" en una BD vacia.
        Optional<VehicleEntity> result = vehicleRepository.findByPlate("9999XYZ");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional vacio (isPresent = false / isEmpty = true).
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe guardar un vehiculo en la base de datos y permitir recuperarlo por ID")
    void shouldSaveAndLoadVehicleEntity() {
        // QUE HACE:
        // - Crea un VehicleEntity de prueba.
        // - Llama al metodo save del repositorio para guardarlo.
        // - Recupera la entidad utilizando findById.
        UUID id = UUID.randomUUID();
        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setType(VehicleType.CAR);
        entity.setPlate("7777JKL");
        entity.setBrand("Ford");
        entity.setModel("Mustang");
        entity.setColor("Blue");
        entity.setNumDoors(2);
        entity.setHasSidecar(false);
        entity.setActive(true);

        VehicleEntity savedEntity = vehicleRepository.save(entity);

        // QUE DEBERIA HACER:
        // La entidad guardada debe tener un ID no nulo y ser recuperable mediante findById, 
        // coincidiendo en todos sus atributos persistidos.
        assertThat(savedEntity).isNotNull();
        
        Optional<VehicleEntity> loadedEntityOpt = vehicleRepository.findById(id);
        assertThat(loadedEntityOpt).isPresent();
        assertThat(loadedEntityOpt.get().getPlate()).isEqualTo("7777JKL");
        assertThat(loadedEntityOpt.get().getBrand()).isEqualTo("Ford");
    }
}

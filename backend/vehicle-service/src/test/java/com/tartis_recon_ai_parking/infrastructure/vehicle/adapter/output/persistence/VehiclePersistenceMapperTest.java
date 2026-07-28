package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class VehiclePersistenceMapperTest {

    // Utiliza el cargador de MapStruct Mappers en lugar de instanciar directamente para evitar problemas de sincronizacion del compilador de la IDE.
    private final VehiclePersistenceMapper mapper = Mappers.getMapper(VehiclePersistenceMapper.class);

    @Test
    @DisplayName("Debe mapear un objeto de dominio Vehicle a una entidad VehicleEntity de forma correcta")
    void shouldMapVehicleToEntity() {
        // QUE HACE:
        // Instancia un objeto Vehicle de dominio completo con datos especificos, llama al metodo toEntity del mapeador.
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.reconstruct(id, 1L,VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        VehicleEntity entity = mapper.toEntity(vehicle);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto VehicleEntity no nulo y comprobar mediante aserciones 
        // de AssertJ que cada campo de persistencia coincida exactamente con el de origen.
        assertThat(entity).isNotNull();
        assertThat(entity.getUniqueId()).isEqualTo(id);
        assertThat(entity.getType()).isEqualTo(VehicleType.CAR);
        assertThat(entity.getPlate()).isEqualTo("1234BCD");
        assertThat(entity.getBrand()).isEqualTo("Toyota");
        assertThat(entity.getModel()).isEqualTo("Corolla");
        assertThat(entity.getColor()).isEqualTo("Red");
        assertThat(entity.getNumDoors()).isEqualTo(4);
        assertThat(entity.getHasSidecar()).isFalse();
        assertThat(entity.getActive()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar null al mapear un vehiculo de dominio nulo a entidad")
    void shouldReturnNullWhenMappingNullVehicle() {
        // QUE HACE:
        // Llama al mapper pasando un parametro nulo.
        VehicleEntity entity = mapper.toEntity(null);

        // QUE DEBERIA HACER:
        // El mapper debe retornar null de forma segura y sin lanzar excepciones.
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Debe mapear una entidad VehicleEntity a un objeto de dominio Vehicle de forma correcta")
    void shouldMapEntityToVehicle() {
        // QUE HACE:
        // - Instancia y rellena un VehicleEntity con datos de prueba especificos, llama al metodo toDomain del mapeador.
        UUID id = UUID.randomUUID();
        Long version=1L;
        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setVersion(version);
        entity.setType(VehicleType.MOTORBIKE);
        entity.setPlate("5678BDF");
        entity.setBrand("Honda");
        entity.setModel("CBR");
        entity.setColor("Black");
        entity.setNumDoors(0);
        entity.setHasSidecar(true);
        entity.setActive(true);

        Vehicle vehicle = mapper.toDomain(entity);

        // QUE DEBERIA HACER:
        // Debe retornar un objeto de dominio Vehicle no nulo y comprobar mediante aserciones
        // que todos los campos del dominio se correspondan de forma exacta con los de la entidad.
        assertThat(vehicle).isNotNull();
        assertThat(vehicle.getUniqueId()).isEqualTo(id);
        assertThat(vehicle.getVersion()).isEqualTo(version);
        assertThat(vehicle.getType()).isEqualTo(VehicleType.MOTORBIKE);
        assertThat(vehicle.getPlate()).isEqualTo("5678BDF");
        assertThat(vehicle.getBrand()).isEqualTo("Honda");
        assertThat(vehicle.getModel()).isEqualTo("CBR");
        assertThat(vehicle.getColor()).isEqualTo("Black");
        assertThat(vehicle.getNumDoors()).isEqualTo(0);
        assertThat(vehicle.getHasSidecar()).isTrue();
        assertThat(vehicle.isActive()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar null al mapear una entidad nula a dominio")
    void shouldReturnNullWhenMappingNullEntity() {
        // QUE HACE:
        // Llama al mapper pasando una entidad nula.
        Vehicle vehicle = mapper.toDomain(null);

        // QUE DEBERIA HACER:
        // El mapper debe retornar null de forma segura y sin lanzar excepciones.
        assertThat(vehicle).isNull();
    }

    @Test
    @DisplayName("El ObjectFactory reconstruct debe retornar null de forma segura ante una entidad nula")
    void shouldReturnNullWhenReconstructingNullEntity() {
        Vehicle vehicle = mapper.reconstruct(null);

        assertThat(vehicle).isNull();
    }

    @Test
@DisplayName("Debe mapear un vehiculo nuevo con version null sin lanzar NullPointerException")
void shouldMapVehicleWithNullVersion() {
    // 1. Objeto de dominio recién creado (sin ID ni versión de BBDD)
    Vehicle newVehicle = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

    // 2. Mapeo a entidad
    VehicleEntity entity = mapper.toEntity(newVehicle);

    // 3. Verificación
    assertThat(entity).isNotNull();
    assertThat(entity.getVersion()).isNull(); // La versión debe ser null antes de guardar en BD

    // 4. Mapeo inverso (Entidad no guardada -> Dominio)
    Vehicle domainFromEntity = mapper.toDomain(entity);

    assertThat(domainFromEntity).isNotNull();
    assertThat(domainFromEntity.getVersion()).isNull();
}
}

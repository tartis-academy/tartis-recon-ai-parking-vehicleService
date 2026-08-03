package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Habilita el soporte de Mockito en JUnit para pruebas unitarias rapidas.
@ExtendWith(MockitoExtension.class)
class VehiclePersistenceAdapterTest {

    // @Mock: Genera mocks de las dependencias que requiere el adaptador.
    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehiclePersistenceMapper vehiclePersistenceMapper;

    // @InjectMocks: Crea la instancia de la clase bajo prueba e inyecta automaticamente los mocks anteriores.
    @InjectMocks
    private VehiclePersistenceAdapter vehiclePersistenceAdapter;

    @Test
    @DisplayName("Debe guardar un vehiculo mapeandolo a entidad y retornandolo convertido a dominio de nuevo")
    void shouldSaveVehicleSuccessfully() {
        // QUE HACE:
        // - Instancia un vehiculo de dominio.
        // - Simula una entidad de persistencia y el vehiculo de retorno mapeado.
        // - Configura los mocks del mapper y del repositorio.
        // - Ejecuta el metodo save del adaptador.
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.reconstruct(id, 1L,VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setPlate("1234BCD");

        when(vehiclePersistenceMapper.toEntity(vehicle)).thenReturn(entity);
        when(vehicleRepository.saveAndFlush(entity)).thenReturn(entity);
        when(vehiclePersistenceMapper.toDomain(entity)).thenReturn(vehicle);

        Vehicle result = vehiclePersistenceAdapter.save(vehicle);

        // QUE DEBERIA HACER:
        // Debe retornar el vehiculo persistido correctamente mapeado de vuelta y verificar que se
        // llamo exactamente una vez a los metodos del mapper y del repositorio.
        assertThat(result).isNotNull();
        assertThat(result.getPlate()).isEqualTo("1234BCD");
        verify(vehiclePersistenceMapper, times(1)).toEntity(vehicle);
        verify(vehicleRepository, times(1)).saveAndFlush(entity);
        verify(vehiclePersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar un Optional con el vehiculo si la matricula consultada existe en BD")
    void shouldFindVehicleByPlateSuccessfully() {
        // QUE HACE:
        // - Simula la respuesta del repositorio conteniendo una entidad.
        // - Configura el mapper para que traduzca dicha entidad al dominio.
        // - Ejecuta la busqueda findByPlate.
        UUID id = UUID.randomUUID();
        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setPlate("1234BCD");
        
        Vehicle vehicle = Vehicle.reconstruct(id, 1L,VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        when(vehicleRepository.findByPlate("1234BCD")).thenReturn(Optional.of(entity));
        when(vehiclePersistenceMapper.toDomain(entity)).thenReturn(vehicle);

        Optional<Vehicle> result = vehiclePersistenceAdapter.findByPlate("1234BCD");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con el objeto de dominio y verificar la interaccion de los mocks.
        assertThat(result).isPresent();
        assertThat(result.get().getPlate()).isEqualTo("1234BCD");
        verify(vehicleRepository, times(1)).findByPlate("1234BCD");
        verify(vehiclePersistenceMapper, times(1)).toDomain(entity);
    }

    @Test
    @DisplayName("Debe retornar un Optional vacio al buscar por matricula si no existe en BD")
    void shouldReturnEmptyOptionalWhenPlateNotFound() {
        // QUE HACE:
        // - Configura el mock del repositorio para retornar un Optional vacio.
        // - Invoca findByPlate.
        when(vehicleRepository.findByPlate("9999XYZ")).thenReturn(Optional.empty());

        Optional<Vehicle> result = vehiclePersistenceAdapter.findByPlate("9999XYZ");

        // QUE DEBERIA HACER:
        // Debe retornar un Optional vacio y asegurar que nunca se llamo al mapper.
        assertThat(result).isEmpty();
        verify(vehicleRepository, times(1)).findByPlate("9999XYZ");
        verify(vehiclePersistenceMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Debe retornar un Optional con el vehiculo si el ID existe en BD")
    void shouldFindVehicleByIdSuccessfully() {
        // QUE HACE:
        // - Genera un ID aleatorio.
        // - Simula que el repositorio encuentra la entidad con ese ID.
        // - Configura el mapper.
        // - Ejecuta la busqueda findById.
        UUID id = UUID.randomUUID();
        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);

        Vehicle vehicle = Vehicle.reconstruct(id, 1L,VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        when(vehicleRepository.findById(id)).thenReturn(Optional.of(entity));
        when(vehiclePersistenceMapper.toDomain(entity)).thenReturn(vehicle);

        Optional<Vehicle> result = vehiclePersistenceAdapter.findById(id);

        // QUE DEBERIA HACER:
        // Debe retornar un Optional con el vehiculo de dominio correspondiente.
        assertThat(result).isPresent();
        assertThat(result.get().getUniqueId()).isEqualTo(id);
        verify(vehicleRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe retornar true si la matricula ya esta registrada en BD")
    void shouldReturnTrueWhenPlateExists() {
        // QUE HACE:
        // - Configura el repositorio para indicar que la matricula si existe (true).
        // - Invoca existsByPlate en el adaptador.
        when(vehicleRepository.existsByPlate("1234BCD")).thenReturn(true);

        boolean exists = vehiclePersistenceAdapter.existsByPlate("1234BCD");

        // QUE DEBERIA HACER:
        // Debe retornar true.
        assertThat(exists).isTrue();
        verify(vehicleRepository, times(1)).existsByPlate("1234BCD");
    }

    @Test
    @DisplayName("Debe retornar la lista completa de vehiculos convertida a objetos de dominio")
    void shouldFindAllVehicles() {
        // QUE HACE:
        // - Prepara una lista de entidades en base de datos.
        // - Configura los mocks para retornar las entidades y mapear cada una de ellas a dominio.
        // - Llama al metodo findAll.
        VehicleEntity entity1 = new VehicleEntity();
        entity1.setPlate("1234BCD");
        VehicleEntity entity2 = new VehicleEntity();
        entity2.setPlate("5678BDF");

        Vehicle domain1 = Vehicle.create(VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);
        Vehicle domain2 = Vehicle.create(VehicleType.MOTORBIKE, "5678BDF", "Honda", "CBR", "Black", 0, true, true);

        when(vehicleRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(vehiclePersistenceMapper.toDomain(entity1)).thenReturn(domain1);
        when(vehiclePersistenceMapper.toDomain(entity2)).thenReturn(domain2);

        List<Vehicle> result = vehiclePersistenceAdapter.findAll();

        // QUE DEBERIA HACER:
        // Debe retornar una lista de tamaño 2 y verificar que cada elemento ha sido correctamente
        // traducido al dominio.
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getPlate()).isEqualTo("1234BCD");
        assertThat(result.get(1).getPlate()).isEqualTo("5678BDF");
        verify(vehicleRepository, times(1)).findAll();
        verify(vehiclePersistenceMapper, times(2)).toDomain(any());
    }

    @Test
    @DisplayName("Debe lanzar ExistingVehicleException cuando la BBDD detecta una matricula duplicada")
    void shouldThrowExistingVehicleExceptionWhenPlateIsDuplicate() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setPlate("1234BCD");

        when(vehiclePersistenceMapper.toEntity(vehicle)).thenReturn(entity);
        // Simulamos que la BD salta con error al hacer flush
        when(vehicleRepository.saveAndFlush(entity)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate plate"));

        // Verificamos que el adaptador lo captura y lanza la excepción de dominio
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> vehiclePersistenceAdapter.save(vehicle))
                .isInstanceOf(com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException.class);

        verify(vehiclePersistenceMapper, times(1)).toEntity(vehicle);
        verify(vehicleRepository, times(1)).saveAndFlush(entity);
        verify(vehiclePersistenceMapper, never()).toDomain(any());
    }

    // Complementa (no sustituye) a VehiclePersistenceAdapterConcurrencyTest: alli el choque
    // lo provoca Hibernate contra un Postgres real, que es lo que de verdad demuestra que
    // @Version funciona. Este solo cubre el cableado del catch y no depende de Docker.
    @Test
    @DisplayName("Debe lanzar VehicleConcurrentModificationException cuando salta el bloqueo optimista")
    void shouldThrowConcurrentModificationExceptionWhenOptimisticLockingFails() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.reconstruct(id, 0L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        VehicleEntity entity = new VehicleEntity();
        entity.setUniqueId(id);
        entity.setPlate("1234BCD");

        org.springframework.dao.OptimisticLockingFailureException cause =
                new org.springframework.orm.ObjectOptimisticLockingFailureException(VehicleEntity.class, id);

        when(vehiclePersistenceMapper.toEntity(vehicle)).thenReturn(entity);
        when(vehicleRepository.saveAndFlush(entity)).thenThrow(cause);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> vehiclePersistenceAdapter.save(vehicle))
                .isInstanceOf(com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleConcurrentModificationException.class)
                .hasMessageContaining("1234BCD")
                .hasCause(cause);

        verify(vehiclePersistenceMapper, times(1)).toEntity(vehicle);
        verify(vehicleRepository, times(1)).saveAndFlush(entity);
        verify(vehiclePersistenceMapper, never()).toDomain(any());
    }
}

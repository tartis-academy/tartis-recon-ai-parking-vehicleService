package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import tools.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.vehicle.usecase.ActivateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleConcurrentModificationException;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.VehicleRestAdapter;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.VehicleRestMapper;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleStatusRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import com.tartis_recon_ai_parking.infrastructure.config.SecurityConfig;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

// Desde SEC-04 hay que traer el SecurityFilterChain al slice con @Import (ver detalle en
// VehicleRestAdapterTest). Con el filtro activo, todas las llamadas necesitan .with(jwt())
// para simular una peticion autenticada, si no el filtro corta con 401 antes de que el caso
// de uso lance la excepcion que este test quiere comprobar.
@WebMvcTest(VehicleRestAdapter.class)
@Import(SecurityConfig.class)
class CustomizedExceptionAdapterMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateVehicleUseCase createVehicleUseCase;

    @MockitoBean
    private GetVehicleUseCase getVehicleUseCase;

    @MockitoBean
    private UpdateVehicleUseCase updateVehicleUseCase;

    @MockitoBean
    private DeleteVehicleUseCase deleteVehicleUseCase;

    @MockitoBean
    private ActivateVehicleUseCase activateVehicleUseCase;

    @MockitoBean
    private VehicleRestMapper mapper;

    @Test
    void shouldReturn404ProblemDetailWhenVehicleNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(getVehicleUseCase.getById(id))
                .thenThrow(new VehicleNotFoundException("ID", id));

        mockMvc.perform(get("/v1/vehicles/" + id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Vehicle with 'ID = " + id + "' couldn't be found."));
    }

    @Test
    void shouldReturn400ProblemDetailWhenInvalidVehicleException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(getVehicleUseCase.getById(id))
                .thenThrow(new InvalidVehicleException("Plate", "invalid", "Invalid plate pattern"));

        mockMvc.perform(get("/v1/vehicles/" + id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid plate pattern"));
    }

    @Test
    void shouldReturn400ProblemDetailWhenExistingVehicleException() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        Mockito.when(createVehicleUseCase.execute(Mockito.any()))
                .thenThrow(new ExistingVehicleException("1234ABC"));

        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("There's already a vehicle with the specified plate : 1234ABC"));
    }

    @Test
    void shouldReturn400ProblemDetailWithValidationErrorsWhenRequestBodyIsInvalid() throws Exception {
        VehicleRequest request = new VehicleRequest();
        // Missing required fields like plate, brand, etc.

        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldReturn409ProblemDetailWhenDataIntegrityViolation() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        Mockito.when(createVehicleUseCase.execute(Mockito.any()))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Data Integrity Violation"))
                .andExpect(jsonPath("$.detail").value("The operation violates database constraints or uniqueness requirements."))
                .andExpect(jsonPath("$.instance").value("/v1/vehicles"));
    }

    @Test
    void shouldReturn503ProblemDetailWhenDatabaseIsDown() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(getVehicleUseCase.getById(id))
                .thenThrow(new DataAccessResourceFailureException("DB connection refused"));

        mockMvc.perform(get("/v1/vehicles/" + id).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.title").value("Database Service Unavailable"))
                .andExpect(jsonPath("$.detail").value("The database is unreachable or the operation timed out. Please try again later."))
                .andExpect(jsonPath("$.instance").value("/v1/vehicles/" + id));
    }

    @Test
    void shouldReturn409ProblemDetailWhenDeadlockOccurs() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(deleteVehicleUseCase.deactivate(id))
                .thenThrow(new CannotAcquireLockException("Deadlock detected"));

        VehicleStatusRequest request = new VehicleStatusRequest(false);

        mockMvc.perform(patch("/v1/vehicles/" + id + "/status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Concurrency Lock Conflict"))
                .andExpect(jsonPath("$.detail").value("The resource is currently locked by another ongoing transaction. Please retry the operation."))
                .andExpect(jsonPath("$.instance").value("/v1/vehicles/" + id + "/status"));
    }

    @Test
    void shouldReturn409ProblemDetailWhenVehicleConcurrentModification() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        Mockito.when(createVehicleUseCase.execute(Mockito.any()))
                .thenThrow(new VehicleConcurrentModificationException("1234ABC",
                        new ObjectOptimisticLockingFailureException("VehicleEntity", "1234ABC")));

        mockMvc.perform(post("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Concurrent Modification Conflict"))
                .andExpect(jsonPath("$.detail").value("Vehicle with plate '1234ABC' was modified by another transaction. Please refresh and try again."))
                .andExpect(jsonPath("$.instance").value("/v1/vehicles"));
    }

    // Red de seguridad: un choque optimista que llegue sin pasar por el adaptador de
    // persistencia (lanzado por Spring Data en otro punto) tampoco debe salir como 500.
    // ObjectOptimisticLockingFailureException es subtipo de OptimisticLockingFailureException,
    // que es lo unico que declara el handler.
    @Test
    void shouldReturn409ProblemDetailWhenRawOptimisticLockingFailure() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new ObjectOptimisticLockingFailureException("VehicleEntity", id))
                .when(deleteVehicleUseCase).deactivate(id);

        mockMvc.perform(patch("/v1/vehicles/" + id + "/status").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Concurrent Modification Conflict"))
                .andExpect(jsonPath("$.detail").value("The resource was modified by another transaction. Please fetch the latest version and retry."))
                .andExpect(jsonPath("$.instance").value("/v1/vehicles/" + id + "/status"));
    }
}

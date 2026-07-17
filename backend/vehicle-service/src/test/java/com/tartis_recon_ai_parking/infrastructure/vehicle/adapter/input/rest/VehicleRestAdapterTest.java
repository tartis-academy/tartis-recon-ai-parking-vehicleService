package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import tools.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest: Se enfoca unicamente en la capa web (Spring MVC) e inicializa MockMvc.
// Solo carga VehicleRestAdapter en el contexto para hacer pruebas unitarias rapidas y aisladas de endpoints.
@WebMvcTest(VehicleRestAdapter.class)
class VehicleRestAdapterTest {

    // MockMvc: Permite realizar llamadas HTTP simuladas (GET, POST, etc.) a los endpoints
    // y verificar los codigos de estado, las cabeceras y el cuerpo de la respuesta.
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper: Utilizado para convertir objetos Java a JSON (serializacion) y viceversa.
    @Autowired
    private ObjectMapper objectMapper;

    // @MockitoBean: Registra un Mock en el contexto de Spring. Reemplaza el bean real por un simulacro
    // de Mockito, permitiendo definir su comportamiento y verificar sus invocaciones.
    @MockitoBean
    private CreateVehicleUseCase createVehicleUseCase;

    @MockitoBean
    private DeleteVehicleUseCase deleteVehicleUseCase;

    @MockitoBean
    private UpdateVehicleUseCase updateVehicleUseCase;

    @MockitoBean
    private GetVehicleUseCase getVehicleUseCase;

    @MockitoBean
    private VehicleRestMapper vehicleRestMapper;

    @Test
    @DisplayName("Debe obtener la lista de todos los vehiculos correctamente con estado 200 OK")
    void shouldGetAllVehiclesSuccessfully() throws Exception {
        // QUE HACE:
        // - Instancia dos vehiculos de prueba en una lista.
        // - Simula dos DTOs de respuesta correspondientes.
        // - Configura el mock del caso de uso getVehicleUseCase para retornar la lista.
        // - Configura el mapper para que convierta la lista de dominio a DTOs.
        // - Realiza una peticion GET a /v1/vehicles.
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Vehicle vehicle1 = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        vehicle1.setUniqueId(id1);
        Vehicle vehicle2 = new Vehicle(VehicleType.MOTORBIKE, "5678DEF", "Honda", "CBR", "Black", 0, true, true);
        vehicle2.setUniqueId(id2);

        List<Vehicle> domainList = List.of(vehicle1, vehicle2);

        VehicleResponse res1 = new VehicleResponse(id1, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse res2 = new VehicleResponse(id2, VehicleType.MOTORBIKE, "5678DEF", "Honda", "CBR", "Black", 0, true, true);
        List<VehicleResponse> responseList = List.of(res1, res2);

        when(getVehicleUseCase.execute()).thenReturn(domainList);
        when(vehicleRestMapper.toResponseList(domainList)).thenReturn(responseList);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK con la representacion JSON de la lista de vehiculos
        // y comprobar que la longitud de la lista es 2 y que los campos coinciden.
        mockMvc.perform(get("/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].uniqueId").value(id1.toString()))
                .andExpect(jsonPath("$[0].plate").value("1234ABC"))
                .andExpect(jsonPath("$[1].uniqueId").value(id2.toString()))
                .andExpect(jsonPath("$[1].plate").value("5678DEF"));

        verify(getVehicleUseCase, times(1)).execute();
        verify(vehicleRestMapper, times(1)).toResponseList(domainList);
    }

    @Test
    @DisplayName("Debe obtener un vehiculo por su ID correctamente con estado 200 OK")
    void shouldGetVehicleByIdSuccessfully() throws Exception {
        // QUE HACE:
        // - Genera un ID aleatorio y simula un vehiculo del dominio y su DTO de respuesta.
        // - Configura getVehicleUseCase para devolver el vehiculo al buscar por ese ID.
        // - Configura el mapper para convertir el vehiculo en su correspondiente respuesta DTO.
        // - Realiza una peticion GET a /v1/vehicles/{id}
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        vehicle.setUniqueId(id);
        VehicleResponse responseDto = new VehicleResponse(id, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(getVehicleUseCase.getById(id)).thenReturn(vehicle);
        when(vehicleRestMapper.toResponse(vehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe responder con 200 OK y el cuerpo JSON con los datos correctos del vehiculo.
        mockMvc.perform(get("/v1/vehicles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueId").value(id.toString()))
                .andExpect(jsonPath("$.plate").value("1234ABC"))
                .andExpect(jsonPath("$.type").value("CAR"));

        verify(getVehicleUseCase, times(1)).getById(id);
        verify(vehicleRestMapper, times(1)).toResponse(vehicle);
    }

    @Test
    @DisplayName("Debe retornar 404 Not Found cuando se busca un ID que no existe")
    void shouldReturn404WhenGetByIdNotFound() throws Exception {
        // QUE HACE:
        // - Genera un ID aleatorio.
        // - Configura getVehicleUseCase para lanzar VehicleNotFoundException.
        // - Realiza la peticion GET simulada.
        UUID id = UUID.randomUUID();
        when(getVehicleUseCase.getById(id)).thenThrow(new VehicleNotFoundException("No se encontro el vehiculo"));

        // QUE DEBERIA HACER:
        // Debe retornar estado 404 Not Found con el mensaje correspondiente en el cuerpo.
        mockMvc.perform(get("/v1/vehicles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se encontro el vehiculo"));

        verify(getVehicleUseCase, times(1)).getById(id);
    }

    @Test
    @DisplayName("Debe obtener un vehiculo por matricula con estado 200 OK")
    void shouldGetVehicleByPlateSuccessfully() throws Exception {
        // QUE HACE:
        // - Genera un ID, un vehiculo de dominio y su DTO.
        // - Configura getVehicleUseCase para devolver el vehiculo al buscar por la matricula "1234ABC".
        // - Configura el mapper.
        // - Realiza una peticion GET a /v1/vehicles/plate/1234ABC
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        vehicle.setUniqueId(id);
        VehicleResponse responseDto = new VehicleResponse(id, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(getVehicleUseCase.getByPlate("1234ABC")).thenReturn(vehicle);
        when(vehicleRestMapper.toResponse(vehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe responder con 200 OK y el cuerpo JSON conteniendo la informacion del vehiculo.
        mockMvc.perform(get("/v1/vehicles/plate/1234ABC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("1234ABC"));

        verify(getVehicleUseCase, times(1)).getByPlate("1234ABC");
    }

    @Test
    @DisplayName("Debe retornar 404 Not Found cuando la matricula no existe")
    void shouldReturn404WhenPlateNotFound() throws Exception {
        // QUE HACE:
        // - Configura getVehicleUseCase para lanzar VehicleNotFoundException con la matricula "9999XYZ".
        // - Realiza la llamada GET a /v1/vehicles/plate/9999XYZ
        when(getVehicleUseCase.getByPlate("9999XYZ")).thenThrow(new VehicleNotFoundException("No se encontro el vehiculo"));

        // QUE DEBERIA HACER:
        // Debe retornar estado 404 Not Found con el mensaje de error.
        mockMvc.perform(get("/v1/vehicles/plate/9999XYZ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se encontro el vehiculo"));

        verify(getVehicleUseCase, times(1)).getByPlate("9999XYZ");
    }

    @Test
    @DisplayName("Debe crear un vehiculo con exito y devolver 201 Created")
    void shouldCreateVehicleSuccessfully() throws Exception {
        // QUE HACE:
        // - Prepara el request DTO con datos validos.
        // - Genera los mocks de dominio y mapper correspondientes.
        // - Envia una peticion POST serializando el request.
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;
        request.active = true;

        UUID id = UUID.randomUUID();
        Vehicle savedVehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        savedVehicle.setUniqueId(id);
        VehicleResponse responseDto = new VehicleResponse(id, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(createVehicleUseCase.execute(any(Vehicle.class))).thenReturn(savedVehicle);
        when(vehicleRestMapper.toResponse(savedVehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe retornar 201 Created con el DTO mapeado que incluye el identificador unico generado.
        mockMvc.perform(post("/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uniqueId").value(id.toString()))
                .andExpect(jsonPath("$.plate").value("1234ABC"));

        verify(createVehicleUseCase, times(1)).execute(any(Vehicle.class));
    }

    @Test
    @DisplayName("Debe lanzar 400 Bad Request si el tipo de vehiculo no es valido al crear")
    void shouldReturn400WhenCreateWithInvalidType() throws Exception {
        // QUE HACE:
        // - Prepara un request con un tipo de vehiculo no contemplado en el enum (ej: "HELICOPTER").
        // - Lanza la peticion POST.
        VehicleRequest request = new VehicleRequest();
        request.type = "HELICOPTER";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;
        request.active = true;

        // QUE DEBERIA HACER:
        // Debe fallar al intentar convertir el tipo a enum en el controlador lanzando InvalidVehicleException,
        // devolviendo 400 Bad Request con el mensaje del error correspondiente.
        mockMvc.perform(post("/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tipo de vehículo inválido: HELICOPTER"));

        verify(createVehicleUseCase, never()).execute(any(Vehicle.class));
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request si fallan las validaciones de campos del DTO (matricula vacia)")
    void shouldReturn400WhenPlateIsBlank() throws Exception {
        // QUE HACE:
        // - Prepara un request con la matricula vacia (incumpliendo @NotBlank).
        // - Realiza la peticion POST.
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = ""; // Invalido
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;
        request.active = true;

        // QUE DEBERIA HACER:
        // Spring Boot interceptara la peticion por el validador @Valid y devolvera 400 Bad Request.
        mockMvc.perform(post("/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(createVehicleUseCase, never()).execute(any(Vehicle.class));
    }

    @Test
    @DisplayName("Debe desactivar un vehiculo logicamente y retornar 204 No Content")
    void shouldDeactivateVehicleSuccessfully() throws Exception {
        // QUE HACE:
        // - Genera un ID aleatorio.
        // - Realiza una llamada PATCH a /v1/vehicles/{id}/status.
        UUID id = UUID.randomUUID();
        doNothing().when(deleteVehicleUseCase).deactivate(id);

        // QUE DEBERIA HACER:
        // Debe retornar 204 No Content y verificar que se llamo a deleteVehicleUseCase.deactivate().
        mockMvc.perform(patch("/v1/vehicles/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(deleteVehicleUseCase, times(1)).deactivate(id);
    }

    @Test
    @DisplayName("Debe actualizar un vehiculo correctamente y retornar 200 OK")
    void shouldUpdateVehicleSuccessfully() throws Exception {
        // QUE HACE:
        // - Genera un ID aleatorio y un request DTO.
        // - Simula el vehiculo resultante de la actualizacion y el DTO de respuesta.
        // - Realiza la llamada PUT a /v1/vehicles/{id}.
        UUID id = UUID.randomUUID();
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;
        request.active = true;

        Vehicle updatedVehicle = new Vehicle(VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        updatedVehicle.setUniqueId(id);

        VehicleResponse responseDto = new VehicleResponse(id, VehicleType.CAR, "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(updateVehicleUseCase.execute(any(Vehicle.class))).thenReturn(updatedVehicle);
        when(vehicleRestMapper.toResponse(updatedVehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe retornar 200 OK con los datos actualizados del vehiculo.
        mockMvc.perform(put("/v1/vehicles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueId").value(id.toString()))
                .andExpect(jsonPath("$.plate").value("1234ABC"));

        verify(updateVehicleUseCase, times(1)).execute(any(Vehicle.class));
    }
}

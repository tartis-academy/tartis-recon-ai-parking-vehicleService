package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import tools.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.usecase.ActivateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleStatusRequest;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import com.tartis_recon_ai_parking.infrastructure.config.SecurityConfig;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

// @WebMvcTest: Se enfoca unicamente en la capa web (Spring MVC) e inicializa MockMvc.
// Solo carga VehicleRestAdapter en el contexto para hacer pruebas unitarias rapidas y aisladas de endpoints.
//
// Tras el refactor a DTOs el adaptador ya no construye objetos de dominio: pide al mapper
// un VehicleCreateDTO y se lo entrega al caso de uso, que le devuelve un VehicleDTO. Por eso
// aqui todo se simula con DTOs y no con Vehicle.
//
// Desde SEC-04 hay que traer el SecurityFilterChain al slice explicitamente: @WebMvcTest NO
// hace component-scan de clases @Configuration propias como SecurityConfig, asi que sin este
// @Import el bean SecurityFilterChain no existe en el contexto y no se aplica ningun filtro
// (todo pasaria con 200, sin necesidad de token). Con el @Import activo, las llamadas de
// negocio llevan .with(jwt()) para simular una peticion autenticada. El caso sin token se
// prueba aparte, al final de la clase.
@WebMvcTest(VehicleRestAdapter.class)
@Import(SecurityConfig.class)
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
    private ActivateVehicleUseCase activateVehicleUseCase;

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
        // - Simula dos VehicleDTO devueltos por el caso de uso.
        // - Simula dos DTOs de respuesta correspondientes.
        // - Configura el mock del caso de uso getVehicleUseCase para retornar la lista.
        // - Configura el mapper para que convierta la lista de DTOs a respuestas.
        // - Realiza una peticion GET a /v1/vehicles.
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        VehicleDTO vehicle1 = new VehicleDTO(id1, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleDTO vehicle2 = new VehicleDTO(id2, "MOTORBIKE", "5678DEF", "Honda", "CBR", "Black", 0, true, true);

        List<VehicleDTO> dtoList = List.of(vehicle1, vehicle2);

        VehicleResponse res1 = new VehicleResponse(id1, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse res2 = new VehicleResponse(id2, "MOTORBIKE", "5678DEF", "Honda", "CBR", "Black", 0, true, true);
        List<VehicleResponse> responseList = List.of(res1, res2);

        when(getVehicleUseCase.execute()).thenReturn(dtoList);
        when(vehicleRestMapper.toResponseList(dtoList)).thenReturn(responseList);

        // QUE DEBERIA HACER:
        // Debe retornar estado 200 OK con la representacion JSON de la lista de vehiculos
        // y comprobar que la longitud de la lista es 2 y que los campos coinciden.
        mockMvc.perform(get("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].uniqueId").value(id1.toString()))
                .andExpect(jsonPath("$[0].plate").value("1234ABC"))
                .andExpect(jsonPath("$[1].uniqueId").value(id2.toString()))
                .andExpect(jsonPath("$[1].plate").value("5678DEF"));

        verify(getVehicleUseCase, times(1)).execute();
        verify(vehicleRestMapper, times(1)).toResponseList(dtoList);
    }

    @Test
    @DisplayName("Debe obtener un vehiculo por su ID correctamente con estado 200 OK")
    void shouldGetVehicleByIdSuccessfully() throws Exception {
        // QUE HACE:
        // - Genera un ID aleatorio y simula el VehicleDTO devuelto por el caso de uso y su DTO de respuesta.
        // - Configura getVehicleUseCase para devolver el DTO al buscar por ese ID.
        // - Configura el mapper para convertirlo en su correspondiente respuesta.
        // - Realiza una peticion GET a /v1/vehicles/{id}
        UUID id = UUID.randomUUID();
        VehicleDTO vehicle = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse responseDto = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(getVehicleUseCase.getById(id)).thenReturn(vehicle);
        when(vehicleRestMapper.toResponse(vehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe responder con 200 OK y el cuerpo JSON con los datos correctos del vehiculo.
        mockMvc.perform(get("/v1/vehicles/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
        when(getVehicleUseCase.getById(id)).thenThrow(new VehicleNotFoundException("ID", id));

        // QUE DEBERIA HACER:
        // Debe retornar estado 404 Not Found con el mensaje correspondiente en el cuerpo.
        mockMvc.perform(get("/v1/vehicles/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Vehicle with 'ID = " + id + "' couldn't be found."));

        verify(getVehicleUseCase, times(1)).getById(id);
    }

    @Test
    @DisplayName("Debe obtener un vehiculo por matricula con estado 200 OK")
    void shouldGetVehicleByPlateSuccessfully() throws Exception {
        // QUE HACE:
        // - Genera un ID, el VehicleDTO devuelto por el caso de uso y su respuesta.
        // - Configura getVehicleUseCase para devolverlo al buscar por la matricula "1234ABC".
        // - Configura el mapper.
        // - Realiza una peticion GET a /v1/vehicles/plate/1234ABC
        UUID id = UUID.randomUUID();
        VehicleDTO vehicle = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse responseDto = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(getVehicleUseCase.getByPlate("1234ABC")).thenReturn(vehicle);
        when(vehicleRestMapper.toResponse(vehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe responder con 200 OK y el cuerpo JSON conteniendo la informacion del vehiculo.
        mockMvc.perform(get("/v1/vehicles/plate/1234ABC")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
        when(getVehicleUseCase.getByPlate("9999XYZ")).thenThrow(new VehicleNotFoundException("plate", "9999XYZ"));

        // QUE DEBERIA HACER:
        // Debe retornar estado 404 Not Found con el mensaje de error.
        mockMvc.perform(get("/v1/vehicles/plate/9999XYZ")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Vehicle with 'plate = 9999XYZ' couldn't be found."));

        verify(getVehicleUseCase, times(1)).getByPlate("9999XYZ");
    }

    @Test
    @DisplayName("Debe crear un vehiculo con exito y devolver 201 Created")
    void shouldCreateVehicleSuccessfully() throws Exception {
        // QUE HACE:
        // - Prepara el request DTO con datos validos.
        // - Simula la traduccion del request al DTO de aplicacion y la respuesta del caso de uso.
        // - Envia una peticion POST serializando el request.
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        UUID id = UUID.randomUUID();
        VehicleCreateDTO createDTO = new VehicleCreateDTO("CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false);
        VehicleDTO savedVehicle = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse responseDto = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        // any(VehicleRequest.class) y no el objeto 'request': Spring deserializa el JSON en una
        // instancia distinta y VehicleRequest no implementa equals().
        when(vehicleRestMapper.toCreateDTO(any(VehicleRequest.class))).thenReturn(createDTO);
        when(createVehicleUseCase.execute(createDTO)).thenReturn(savedVehicle);
        when(vehicleRestMapper.toResponse(savedVehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe retornar 201 Created con el DTO mapeado que incluye el identificador unico generado.
        mockMvc.perform(post("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uniqueId").value(id.toString()))
                .andExpect(jsonPath("$.plate").value("1234ABC"));

        verify(createVehicleUseCase, times(1)).execute(createDTO);
    }

    @Test
    @DisplayName("Debe lanzar 400 Bad Request si el tipo de vehiculo no es valido al crear")
    void shouldReturn400WhenCreateWithInvalidType() throws Exception {
        // QUE HACE:
        // - Prepara un request con un tipo de vehiculo no contemplado en el enum (ej: "HELICOPTER").
        // - Simula que el caso de uso rechaza ese tipo: tras el refactor la conversion a enum
        //   ocurre en VehicleDTOFactory, dentro de la capa de aplicacion, y ya no en el controlador.
        // - Lanza la peticion POST.
        VehicleRequest request = new VehicleRequest();
        request.type = "HELICOPTER";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        VehicleCreateDTO createDTO = new VehicleCreateDTO("HELICOPTER", "1234ABC", "Toyota", "Corolla", "Red", 4, false);

        when(vehicleRestMapper.toCreateDTO(any(VehicleRequest.class))).thenReturn(createDTO);
        when(createVehicleUseCase.execute(createDTO))
                .thenThrow(new InvalidVehicleException("VehicleType", request.type));

        // QUE DEBERIA HACER:
        // La InvalidVehicleException que sube desde la aplicacion debe traducirse en un
        // 400 Bad Request con el mensaje del error, via CustomizedExceptionAdapter.
        mockMvc.perform(post("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid vehicle data: VehicleType can't be HELICOPTER"));

        verify(createVehicleUseCase, times(1)).execute(createDTO);
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

        // QUE DEBERIA HACER:
        // Spring Boot interceptara la peticion por el validador @Valid y devolvera 400 Bad Request
        // sin llegar a ejecutar el caso de uso.
        mockMvc.perform(post("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(createVehicleUseCase, never()).execute(any(VehicleCreateDTO.class));
    }

    @Test
    @DisplayName("Debe desactivar un vehiculo a traves de PATCH /v1/vehicles/{id}/status con active=false y retornar 200 OK")
    void shouldDeactivateVehicleStatusSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleDTO dto = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, false);
        VehicleResponse response = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, false);

        when(deleteVehicleUseCase.deactivate(id)).thenReturn(dto);
        when(vehicleRestMapper.toResponse(dto)).thenReturn(response);

        VehicleStatusRequest request = new VehicleStatusRequest(false);

        mockMvc.perform(patch("/v1/vehicles/{id}/status", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueId").value(id.toString()))
                .andExpect(jsonPath("$.active").value(false));

        verify(deleteVehicleUseCase, times(1)).deactivate(id);
    }

    @Test
    @DisplayName("Debe activar un vehiculo a traves de PATCH /v1/vehicles/{id}/status con active=true y retornar 200 OK")
    void shouldActivateVehicleStatusSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleDTO dto = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse response = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(activateVehicleUseCase.activate(id)).thenReturn(dto);
        when(vehicleRestMapper.toResponse(dto)).thenReturn(response);

        VehicleStatusRequest request = new VehicleStatusRequest(true);

        mockMvc.perform(patch("/v1/vehicles/{id}/status", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueId").value(id.toString()))
                .andExpect(jsonPath("$.active").value(true));

        verify(activateVehicleUseCase, times(1)).activate(id);
    }

    @Test
    @DisplayName("Debe desactivar un vehiculo a traves del endpoint explícito /deactivate y retornar 200 OK")
    void shouldDeactivateVehicleExplicitlySuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleDTO dto = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, false);
        VehicleResponse response = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, false);

        when(deleteVehicleUseCase.deactivate(id)).thenReturn(dto);
        when(vehicleRestMapper.toResponse(dto)).thenReturn(response);

        mockMvc.perform(patch("/v1/vehicles/{id}/deactivate", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(deleteVehicleUseCase, times(1)).deactivate(id);
    }

    @Test
    @DisplayName("Debe activar un vehiculo a traves del endpoint /activate y retornar 200 OK")
    void shouldActivateVehicleSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleDTO dto = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse response = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(activateVehicleUseCase.activate(id)).thenReturn(dto);
        when(vehicleRestMapper.toResponse(dto)).thenReturn(response);

        mockMvc.perform(patch("/v1/vehicles/{id}/activate", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(activateVehicleUseCase, times(1)).activate(id);
    }

    @Test
    @DisplayName("Debe actualizar un vehiculo correctamente y retornar 200 OK")
    void shouldUpdateVehicleSuccessfully() throws Exception {
        // QUE HACE:
        // - Genera un ID aleatorio y un request DTO.
        // - Simula la traduccion del request y el DTO resultante de la actualizacion.
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

        VehicleCreateDTO createDTO = new VehicleCreateDTO("CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false);
        VehicleDTO updatedVehicle = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse responseDto = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(vehicleRestMapper.toCreateDTO(any(VehicleRequest.class))).thenReturn(createDTO);
        when(updateVehicleUseCase.execute(id, createDTO)).thenReturn(updatedVehicle);
        when(vehicleRestMapper.toResponse(updatedVehicle)).thenReturn(responseDto);

        // QUE DEBERIA HACER:
        // Debe retornar 200 OK con los datos actualizados del vehiculo, pasando al caso de uso
        // el id de la URL y el cuerpo por separado.
        mockMvc.perform(put("/v1/vehicles/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueId").value(id.toString()))
                .andExpect(jsonPath("$.plate").value("1234ABC"));

        verify(updateVehicleUseCase, times(1)).execute(id, createDTO);
    }

    // --- SEC-04 / SEC-12: verificacion propia del resource server, no de negocio ---
    //
    // Un unico test parametrizado sobre los 8 endpoints del adaptador. El filtro de
    // seguridad corta la peticion antes del DispatcherServlet, asi que ningun caso de
    // uso puede invocarse. El verifyNoInteractions sobre TODOS los colaboradores es la
    // asercion con senal (F1): si alguien rompe la cadena de seguridad, el caso de uso
    // del endpoint se invocaria y el test fallaria, en vez de aprobar en vacio.

    @ParameterizedTest
    @MethodSource("endpointsProtegidosSinToken")
    @DisplayName("SEC-12: sin token, los 8 endpoints devuelven 401 y ningun caso de uso se invoca")
    void shouldReturn401WhenNoTokenProvided(RequestBuilder request) throws Exception {
        // QUE HACE:
        // - Recorre los 8 endpoints del adaptador sin adjuntar ningun JWT (sin .with(jwt())).
        // QUE DEBERIA HACER:
        // El SecurityFilterChain debe cortar la peticion antes de llegar al controller:
        // 401 Unauthorized y ningun caso de uso ni mapper se invoca.
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(createVehicleUseCase, deleteVehicleUseCase, activateVehicleUseCase,
                updateVehicleUseCase, getVehicleUseCase, vehicleRestMapper);
    }

    static Stream<Arguments> endpointsProtegidosSinToken() {
        UUID id = UUID.randomUUID();
        String vehicleBody = "{\"type\":\"CAR\",\"plate\":\"1234ABC\"}";
        return Stream.of(
                arguments(get("/v1/vehicles")),
                arguments(get("/v1/vehicles/{id}", id)),
                arguments(get("/v1/vehicles/plate/1234ABC")),
                arguments(post("/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON).content(vehicleBody)),
                arguments(patch("/v1/vehicles/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")),
                arguments(patch("/v1/vehicles/{id}/deactivate", id)),
                arguments(patch("/v1/vehicles/{id}/activate", id)),
                arguments(put("/v1/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(vehicleBody))
        );
    }

    // --- SEC-10: pruebas de autorizacion fina para el rol OPERARIO ---

    @Test
    @DisplayName("OPERARIO: Debe denegar la consulta de todos los vehiculos (403)")
    void shouldDenyGetAllVehiclesForOperario() throws Exception {
        mockMvc.perform(get("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(getVehicleUseCase, never()).execute();
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la consulta de vehiculo por ID (403)")
    void shouldDenyGetVehicleByIdForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/v1/vehicles/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(getVehicleUseCase, never()).getById(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe permitir la consulta de vehiculo por matricula (200)")
    void shouldAllowGetVehicleByPlateForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleDTO vehicle = new VehicleDTO(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);
        VehicleResponse responseDto = new VehicleResponse(id, "CAR", "1234ABC", "Toyota", "Corolla", "Red", 4, false, true);

        when(getVehicleUseCase.getByPlate("1234ABC")).thenReturn(vehicle);
        when(vehicleRestMapper.toResponse(vehicle)).thenReturn(responseDto);

        mockMvc.perform(get("/v1/vehicles/plate/1234ABC")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la creacion de un vehiculo (403)")
    void shouldDenyCreateVehicleForOperario() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        mockMvc.perform(post("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        verify(createVehicleUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la desactivacion de un vehiculo (403)")
    void shouldDenyDeactivateVehicleForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleStatusRequest request = new VehicleStatusRequest(false);
        mockMvc.perform(patch("/v1/vehicles/{id}/status", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        verify(deleteVehicleUseCase, never()).deactivate(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la activacion explicita de un vehiculo (403)")
    void shouldDenyActivateVehicleExplicitForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/v1/vehicles/{id}/activate", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(activateVehicleUseCase, never()).activate(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la desactivacion explicita de un vehiculo (403)")
    void shouldDenyDeactivateVehicleExplicitForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/v1/vehicles/{id}/deactivate", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(deleteVehicleUseCase, never()).deactivate(any());
    }

    @Test
    @DisplayName("OPERARIO: Debe denegar la actualizacion de un vehiculo (403)")
    void shouldDenyUpdateVehicleForOperario() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        mockMvc.perform(put("/v1/vehicles/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERARIO")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        verify(updateVehicleUseCase, never()).execute(any(), any());
    }

    // --- SEC-10: pruebas de autorizacion fina para el rol USER ---

    @Test
    @DisplayName("USER: Debe denegar la consulta de todos los vehiculos (403)")
    void shouldDenyGetAllVehiclesForUser() throws Exception {
        mockMvc.perform(get("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Debe denegar la consulta de vehiculo por ID (403)")
    void shouldDenyGetVehicleByIdForUser() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/v1/vehicles/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Debe denegar la consulta de vehiculo por matricula (403)")
    void shouldDenyGetVehicleByPlateForUser() throws Exception {
        mockMvc.perform(get("/v1/vehicles/plate/1234ABC")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Debe denegar la creacion de un vehiculo (403)")
    void shouldDenyCreateVehicleForUser() throws Exception {
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        mockMvc.perform(post("/v1/vehicles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Debe denegar la desactivacion de un vehiculo (403)")
    void shouldDenyDeactivateVehicleForUser() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleStatusRequest request = new VehicleStatusRequest(false);
        mockMvc.perform(patch("/v1/vehicles/{id}/status", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Debe denegar la activacion explicita de un vehiculo (403)")
    void shouldDenyActivateVehicleExplicitForUser() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/v1/vehicles/{id}/activate", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Debe denegar la desactivacion explicita de un vehiculo (403)")
    void shouldDenyDeactivateVehicleExplicitForUser() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/v1/vehicles/{id}/deactivate", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER: Debe denegar la actualizacion de un vehiculo (403)")
    void shouldDenyUpdateVehicleForUser() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleRequest request = new VehicleRequest();
        request.type = "CAR";
        request.plate = "1234ABC";
        request.brand = "Toyota";
        request.model = "Corolla";
        request.color = "Red";
        request.numDoors = 4;
        request.hasSidecar = false;

        mockMvc.perform(put("/v1/vehicles/{id}", id)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

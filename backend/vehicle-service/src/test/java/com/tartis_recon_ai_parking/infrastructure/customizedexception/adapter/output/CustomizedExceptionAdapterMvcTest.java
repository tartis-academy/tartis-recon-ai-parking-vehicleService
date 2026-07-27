package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import tools.jackson.databind.ObjectMapper;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;
import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.VehicleRestAdapter;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.VehicleRestMapper;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleRestAdapter.class)
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
    private VehicleRestMapper mapper;

    @Test
    void shouldReturn404ProblemDetailWhenVehicleNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(getVehicleUseCase.getById(id))
                .thenThrow(new VehicleNotFoundException("ID", id));

        mockMvc.perform(get("/v1/vehicles/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Vehicle with 'ID = " + id + "' couldn't be found."));
    }

    @Test
    void shouldReturn400ProblemDetailWhenInvalidVehicleException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(getVehicleUseCase.getById(id))
                .thenThrow(new InvalidVehicleException("Plate", "invalid", "Invalid plate pattern"));

        mockMvc.perform(get("/v1/vehicles/" + id))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
    }
}

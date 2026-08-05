package com.tartis_recon_ai_parking.testutil;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Lista unica de los 8 endpoints protegidos del VehicleRestAdapter, compartida por los
 * tests de seguridad (SecurityConfigExceptionHandlingTest y VehicleRestAdapterTest).
 * Si se anade un endpoint nuevo, solo hay que tocarlo aqui.
 */
public final class SecuredEndpoints {

    private SecuredEndpoints() {
    }

    public static Stream<Arguments> all() {
        UUID id = UUID.randomUUID();
        String vehicleBody = "{\"type\":\"CAR\",\"plate\":\"1234ABC\"}";
        return Stream.of(
                arguments(get("/v1/vehicles"), "/v1/vehicles"),
                arguments(get("/v1/vehicles/{id}", id), "/v1/vehicles/" + id),
                arguments(get("/v1/vehicles/plate/{plate}", "1234ABC"), "/v1/vehicles/plate/1234ABC"),
                arguments(post("/v1/vehicles")
                                .contentType(MediaType.APPLICATION_JSON).content(vehicleBody),
                        "/v1/vehicles"),
                arguments(patch("/v1/vehicles/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"),
                        "/v1/vehicles/" + id + "/status"),
                arguments(patch("/v1/vehicles/{id}/deactivate", id),
                        "/v1/vehicles/" + id + "/deactivate"),
                arguments(patch("/v1/vehicles/{id}/activate", id),
                        "/v1/vehicles/" + id + "/activate"),
                arguments(put("/v1/vehicles/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON).content(vehicleBody),
                        "/v1/vehicles/" + id)
        );
    }
}

package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.vehicle.usecase.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SEC-11: verifica que el exceptionHandling de SecurityConfig enruta correctamente
 * los errores 401 (sin token y con JWT invalido) y 403 (rol insuficiente) a traves del
 * HandlerExceptionResolver → CustomizedExceptionAdapter, produciendo un ErrorResponse
 * con la estructura de error definida en lugar de la respuesta por defecto de Spring.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:vehicledb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class SecurityConfigExceptionHandlingTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean private CreateVehicleUseCase createVehicleUseCase;
    @MockitoBean private DeleteVehicleUseCase deleteVehicleUseCase;
    @MockitoBean private GetVehicleUseCase getVehicleUseCase;
    @MockitoBean private UpdateVehicleUseCase updateVehicleUseCase;
    @MockitoBean private ActivateVehicleUseCase activateVehicleUseCase;
    @MockitoBean private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("SEC-11: Sin token, el authenticationEntryPoint enruta a CustomizedExceptionAdapter → 401 con ErrorResponse y cabecera WWW-Authenticate")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/v1/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication token is missing, invalid, or expired."))
                .andExpect(jsonPath("$.path").value("/v1/vehicles"));
    }

    @Test
    @DisplayName("SEC-11: Con JWT sintacticamente invalido, el resource server enruta a CustomizedExceptionAdapter → 401 con ErrorResponse y cabecera WWW-Authenticate")
    void shouldReturn401WhenMalformedJwt() throws Exception {
        when(jwtDecoder.decode(anyString())).thenThrow(new BadJwtException("Invalid or expired JWT"));

        mockMvc.perform(get("/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication token is missing, invalid, or expired."))
                .andExpect(jsonPath("$.path").value("/v1/vehicles"));
    }

    @Test
    @DisplayName("SEC-11: Con rol insuficiente, @PreAuthorize enruta a CustomizedExceptionAdapter → 403 con ErrorResponse")
    void shouldReturn403WhenInsufficientRole() throws Exception {
        mockMvc.perform(get("/v1/vehicles")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action."))
                .andExpect(jsonPath("$.path").value("/v1/vehicles"));
    }

    /**
     * SEC-12: el health endpoint de actuator es la excepcion permitAll declarada en
     * SecurityConfig. Si alguien la quita, las sondas de Kubernetes recibirian 401 y
     * este test lo detecta.
     */
    @Test
    @DisplayName("SEC-12: /actuator/health es publico (200 sin token)")
    void shouldExposeActuatorHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    /**
     * SEC-12: extiende el contrato de SEC-11 (que solo cubria GET /v1/vehicles) a los
     * 8 endpoints del adaptador. Aqui (no en el slice @WebMvcTest) es donde el
     * CustomizedExceptionAdapter esta en contexto y el ErrorResponse se materializa de
     * verdad, de modo que el test blinda contra la regresion que motivo SEC-11
     * (restaurar la cabecera WWW-Authenticate en el 401).
     */
    @ParameterizedTest(name = "[{index}] 401 sin token en {1}")
    @MethodSource("com.tartis_recon_ai_parking.testutil.SecuredEndpoints#all")
    @DisplayName("SEC-12: sin token, todos los endpoints devuelven 401 con ErrorResponse y cabecera WWW-Authenticate")
    void shouldReturn401WithErrorResponseForAllEndpoints(RequestBuilder request, String path) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication token is missing, invalid, or expired."))
                .andExpect(jsonPath("$.path").value(path));
    }
}

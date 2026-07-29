package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Keycloak mete los roles del realm en el claim anidado "realm_access.roles",
// no en "scope"/"scp" que es lo que el JwtGrantedAuthoritiesConverter por defecto de
// Spring Security espera (y con el que antepone el prefijo SCOPE_, no ROLE_). Sin este
// converter, hasRole()/hasAuthority() en @PreAuthorize nunca encuentran
// coincidencia aunque el token traiga el rol correcto.
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
        if (realmAccess == null || realmAccess.get(ROLES_CLAIM) == null) {
            return List.of();
        }

        List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }
}

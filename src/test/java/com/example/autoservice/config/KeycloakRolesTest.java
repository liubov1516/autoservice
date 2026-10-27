package com.example.autoservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Завдання 1 (Лаба №2): тест без моків - чистий статичний метод
class KeycloakRolesTest {

    @Test
    void fromClaimsRealmAccessWithRolesReturnsRoleAuthoritiesWithRolePrefix() {
        Map<String, Object> claims = Map.of(
                "realm_access", Map.of("roles", List.of("ADMIN", "USER"))
        );

        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(claims);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void fromClaimsNullClaimsReturnsEmptyList() {
        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(null);

        assertThat(authorities).isEmpty();
    }

    @Test
    void fromClaimsNoRealmAccessKeyReturnsEmptyList() {
        Map<String, Object> claims = Map.of("sub", "user-123");

        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(claims);

        assertThat(authorities).isEmpty();
    }

    @Test
    void fromClaimsRealmAccessWithoutRolesFieldReturnsEmptyList() {
        Map<String, Object> claims = Map.of(
                "realm_access", Map.of("someOtherField", "value")
        );

        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(claims);

        assertThat(authorities).isEmpty();
    }
}

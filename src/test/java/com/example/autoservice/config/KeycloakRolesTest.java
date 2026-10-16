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
        // Arrange
        Map<String, Object> claims = Map.of(
                "realm_access", Map.of("roles", List.of("ADMIN", "USER"))
        );

        // Act
        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(claims);

        // Assert
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void fromClaimsNullClaimsReturnsEmptyList() {
        // Arrange: claims = null - невалідний вхід, перевіряємо null-обробник

        // Act
        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(null);

        // Assert
        assertThat(authorities).isEmpty();
    }

    @Test
    void fromClaimsNoRealmAccessKeyReturnsEmptyList() {
        // Arrange: токен без claim "realm_access"
        Map<String, Object> claims = Map.of("sub", "user-123");

        // Act
        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(claims);

        // Assert
        assertThat(authorities).isEmpty();
    }

    @Test
    void fromClaimsRealmAccessWithoutRolesFieldReturnsEmptyList() {
        // Arrange: realm_access є, але без ключа "roles"
        Map<String, Object> claims = Map.of(
                "realm_access", Map.of("someOtherField", "value")
        );

        // Act
        List<GrantedAuthority> authorities = KeycloakRoles.fromClaims(claims);

        // Assert
        assertThat(authorities).isEmpty();
    }
}

package com.example.autoservice.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// Keycloak кладе ролі в токен так: { "realm_access": { "roles": ["ADMIN"] } }. Перетворюємо на ROLE_ADMIN.
public final class KeycloakRoles {

    private KeycloakRoles() {
    }

    public static List<GrantedAuthority> fromClaims(Map<String, Object> claims) {
        if (claims == null) {
            // обробник null: немає вхідних даних - ролей теж немає
            return List.of();
        }
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> map && map.get("roles") instanceof Collection<?> roles) {
            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        }
        return List.of();
    }
}

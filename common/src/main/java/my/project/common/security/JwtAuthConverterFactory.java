package my.project.common.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtAuthConverterFactory {

    public static JwtAuthenticationConverter rolesFromClaim() {
        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName(JwtClaims.ROLES_CLAIM);
        gac.setAuthorityPrefix("");
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(gac);
        return c;
    }
}

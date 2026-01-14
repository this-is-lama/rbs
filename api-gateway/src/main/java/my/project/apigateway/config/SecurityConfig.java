package my.project.apigateway.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String ROLES_CLAIM = "roles";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         Converter<Jwt, Mono<AbstractAuthenticationToken>> authConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        // public
                        .pathMatchers("/auth/**", "/actuator/health", "/actuator/info").permitAll()

                        // bookings: user can book
                        .pathMatchers("/bookings/**").hasAnyAuthority("ROLE_USER", "ROLE_MANAGER", "ROLE_OWNER")

                        // admin area example (если сделаешь такие эндпоинты)
                        .pathMatchers("/bookings/admin/**").hasAuthority("ROLE_OWNER")

                        // everything else requires token
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(authConverter)))
                .build();
    }


    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret,
                                         @Value("${jwt.issuer:user-service}") String issuer) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> tokenTypeValidator = jwt -> {
            Object tokenType = jwt.getClaims().get("token_type");
            if ("access_token".equals(String.valueOf(tokenType))) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error err = new OAuth2Error("invalid_token", "token_type must be access_token", null);
            return OAuth2TokenValidatorResult.failure(err);
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, tokenTypeValidator));
        return decoder;
    }



    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> authConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object rolesObj = jwt.getClaims().get(ROLES_CLAIM);

            if (!(rolesObj instanceof Collection<?> roles)) {
                return List.of();
            }

			return roles.stream()
					.map(String::valueOf)
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }


}

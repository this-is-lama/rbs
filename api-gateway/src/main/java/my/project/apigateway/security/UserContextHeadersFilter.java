package my.project.apigateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class UserContextHeadersFilter implements GlobalFilter, Ordered {

    private static final String H_USER_ID = "X-User-Id";
    private static final String H_USER_ROLE = "X-User-Role";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitized = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(H_USER_ID);
                    h.remove(H_USER_ROLE);
                })
                .build();

        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitized).build();

        return sanitizedExchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {
                    if (!(auth instanceof JwtAuthenticationToken jwtAuth) || !auth.isAuthenticated()) {
                        return chain.filter(sanitizedExchange);
                    }

                    String userId = jwtAuth.getToken().getSubject(); // sub
                    String role = firstAuthority(jwtAuth.getAuthorities()); // ROLE_USER etc.

                    ServerHttpRequest mutated = sanitizedExchange.getRequest().mutate()
                            .header(H_USER_ID, userId)
                            .header(H_USER_ROLE, role)
                            .build();

                    return chain.filter(sanitizedExchange.mutate().request(mutated).build());
                })
                .switchIfEmpty(chain.filter(sanitizedExchange));
    }

    private String firstAuthority(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

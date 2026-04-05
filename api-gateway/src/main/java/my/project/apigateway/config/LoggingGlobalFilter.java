package my.project.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

		request.getMethod();
		String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String clientIp = request.getRemoteAddress() != null
                ? String.valueOf(request.getRemoteAddress().getAddress())
                : "unknown";

        long start = System.currentTimeMillis();

        log.info("Получен HTTP-запрос: method={}, path={}, query={}, clientIp={}",
                method, path, query, clientIp);

        return chain.filter(exchange)
                .doOnSuccess(unused -> {
                    long duration = System.currentTimeMillis() - start;
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("Запрос обработан успешно: method={}, path={}, status={}, durationMs={}",
                            method, path, status, duration);
                })
                .doOnError(ex -> {
                    long duration = System.currentTimeMillis() - start;

                    log.error("Ошибка при обработке запроса: method={}, path={}, durationMs={}",
                            method, path, duration, ex);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
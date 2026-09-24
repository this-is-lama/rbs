package my.project.apigateway.filter;

import io.micrometer.tracing.handler.TracingObservationHandler.TracingContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Входной фильтр api-gateway для сквозной трассировки запросов.
 * <p>
 * Сам trace создаёт Micrometer Tracing (ещё до этого фильтра) и сам передаёт его в сервисы
 * заголовком {@code traceparent}. Фильтр делает остальное:
 * <ul>
 *     <li>берёт {@code X-Request-Id} клиента или генерирует его (по умолчанию равен traceId)
 *     и передаёт в сервисы;</li>
 *     <li>возвращает клиенту {@code X-Request-Id} и {@code X-Trace-Id} — по traceId
 *     запрос находится в Grafana (трейс в Tempo, логи в Loki);</li>
 *     <li>пишет access-лог: метод, путь, статус, время ответа.</li>
 * </ul>
 */
@Slf4j
@Component
public class RequestTracingFilter implements WebFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** От клиента принимаем только короткий id из безопасных символов, чтобы в логи не попал мусор. */
    private static final Pattern VALID_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startNanos = System.nanoTime();
        String traceId = currentTraceId(exchange);
        String requestId = resolveRequestId(exchange.getRequest(), traceId);

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(REQUEST_ID_HEADER, requestId))
                .build();
        ServerWebExchange tracedExchange = exchange.mutate().request(request).build();

        ServerHttpResponse response = tracedExchange.getResponse();
        response.beforeCommit(() -> {
            response.getHeaders().set(REQUEST_ID_HEADER, requestId);
            if (traceId != null) {
                response.getHeaders().set(TRACE_ID_HEADER, traceId);
            }
            return Mono.empty();
        });

        return chain.filter(tracedExchange)
                .doFinally(signal -> logRequest(tracedExchange, requestId, traceId, startNanos));
    }

    /** Раньше Spring Security (-100), чтобы в лог и заголовки попадали и ответы 401/403. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /** traceId текущего запроса: Spring кладёт observation запроса в атрибуты exchange, а в ней лежит span. */
    private static String currentTraceId(ServerWebExchange exchange) {
        return ServerRequestObservationContext.findCurrent(exchange.getAttributes())
                .map(context -> context.<TracingContext>get(TracingContext.class))
                .map(TracingContext::getSpan)
                .map(span -> span.context().traceId())
                .orElse(null);
    }

    private static String resolveRequestId(ServerHttpRequest request, String traceId) {
        String fromClient = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (fromClient != null && VALID_REQUEST_ID.matcher(fromClient).matches()) {
            return fromClient;
        }
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

    private static void logRequest(ServerWebExchange exchange, String requestId, String traceId, long startNanos) {
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/actuator")) {
            return; // Prometheus опрашивает /actuator/prometheus каждые 15 секунд — не засоряем лог
        }

        HttpStatusCode status = exchange.getResponse().getStatusCode();
        int code = status != null ? status.value() : 200;
        long tookMs = (System.nanoTime() - startNanos) / 1_000_000;

        // doFinally может выполниться в другом потоке, поэтому traceId кладём в MDC явно —
        // так строка лога попадёт в Loki с тем же traceId, что и логи сервисов
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId != null ? traceId : "")) {
            if (code >= 500) {
                log.warn("{} {} -> {} ({} мс) requestId={}",
                        exchange.getRequest().getMethod(), path, code, tookMs, requestId);
            } else {
                log.info("{} {} -> {} ({} мс) requestId={}",
                        exchange.getRequest().getMethod(), path, code, tookMs, requestId);
            }
        }
    }
}

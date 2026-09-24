package my.project.apigateway.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

/**
 * Не трассируем служебные запросы к {@code /actuator}: Prometheus опрашивает
 * {@code /actuator/prometheus} каждые 15 секунд, и без фильтра каждый опрос был бы отдельным трейсом в Tempo.
 */
@Configuration(proxyBeanMethods = false)
public class ObservationConfig {

    @Bean
    public ObservationPredicate skipActuatorObservations() {
        return (name, context) -> !(context instanceof ServerRequestObservationContext serverContext
                && serverContext.getCarrier() != null
                && serverContext.getCarrier().getPath().value().startsWith("/actuator"));
    }
}

package my.project.common.logging.tracing;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

/**
 * Не трассируем служебные запросы к {@code /actuator}.
 * <p>
 * Prometheus опрашивает {@code /actuator/prometheus} каждые 15 секунд. Без этого фильтра каждый опрос
 * становился бы отдельным трейсом в Tempo, и настоящие запросы терялись бы среди тысяч пустых.
 * Подхватывается сервисами через {@code scanBasePackages} ("my.project.common" / "my.project.common.logging").
 */
@Configuration(proxyBeanMethods = false)
public class ActuatorObservationConfig {

    @Bean
    public ObservationPredicate skipActuatorObservations() {
        return (name, context) -> !(context instanceof ServerRequestObservationContext serverContext
                && serverContext.getCarrier() != null
                && serverContext.getCarrier().getRequestURI().startsWith("/actuator"));
    }
}

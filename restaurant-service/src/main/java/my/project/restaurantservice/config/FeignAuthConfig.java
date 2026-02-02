package my.project.restaurantservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor forwardAuthorization() {
        return template -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (!(attrs instanceof ServletRequestAttributes sra)) return;

            String auth = sra.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && !auth.isBlank()) {
                template.header(HttpHeaders.AUTHORIZATION, auth);
            }
        };
    }
}

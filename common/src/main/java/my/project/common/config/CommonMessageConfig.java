package my.project.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Slf4j
@Configuration
public class CommonMessageConfig {

    @Bean
    public MessageSource messageSource() {
        log.info("Инициализация MessageSource для common модуля");

        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasenames("common-i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setUseCodeAsDefaultMessage(true);

        return ms;
    }

    @Bean
    public LocaleResolver localeResolver() {
        log.info("Инициализация LocaleResolver с локалью по умолчанию ru");

        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(new Locale("ru"));
        return resolver;
    }
}
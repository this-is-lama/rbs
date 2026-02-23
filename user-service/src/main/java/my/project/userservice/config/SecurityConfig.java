package my.project.userservice.config;

import lombok.RequiredArgsConstructor;
import my.project.common.security.CommonAccessDeniedHandler;
import my.project.common.security.CommonAuthenticationEntryPoint;
import my.project.common.security.JwtAuthConverterFactory;
import my.project.common.security.JwtDecoderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CommonAuthenticationEntryPoint commonAuthenticationEntryPoint;
	private final CommonAccessDeniedHandler commonAccessDeniedHandler;


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.httpBasic(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(e -> e
						.authenticationEntryPoint(commonAuthenticationEntryPoint)
						.accessDeniedHandler(commonAccessDeniedHandler)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html"
						).permitAll()
						.requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth -> oauth
						.jwt(jwt -> jwt.jwtAuthenticationConverter(JwtAuthConverterFactory.rolesFromClaim()))
				)
				.build();
	}

	@Bean
	public JwtDecoder jwtDecoder(@Value("${jwt.access-secret}") String secret,
								 @Value("${jwt.issuer:user-service}") String issuer) {
		return JwtDecoderFactory.hs256(secret, issuer);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}
}

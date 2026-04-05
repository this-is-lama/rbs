package my.project.common.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtDecoderFactory {

    public static JwtDecoder hs256(String base64Secret, String issuer) {
        log.info("Создание JwtDecoder с алгоритмом HS256 для issuer={}", issuer);

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(JwtAuthSupport.accessTokenValidator(issuer));

        return decoder;
    }
}
package my.project.common.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtDecoderFactory {

    public static JwtDecoder hs256(String base64Secret, String issuer) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(JwtAuthSupport.accessTokenValidator(issuer));
        return decoder;
    }
}

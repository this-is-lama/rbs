package my.project.common.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtAuthSupport {

    public static OAuth2TokenValidator<Jwt> accessTokenValidator(String issuer) {
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> tokenTypeValidator = jwt -> {
            String tokenType = jwt.getClaimAsString(JwtClaims.TOKEN_TYPE_CLAIM);
            if (JwtClaims.ACCESS_TOKEN.equals(tokenType)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "token_type must be access_token", null)
            );
        };

        return new DelegatingOAuth2TokenValidator<>(withIssuer, tokenTypeValidator);
    }

}

package my.project.common.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtClaims {

    public static final String ROLES_CLAIM = "roles";
    public static final String EMAIL_CLAIM = "email";
    public static final String USERNAME_CLAIM = "name";
    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String JTI_CLAIM = "jti";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
}

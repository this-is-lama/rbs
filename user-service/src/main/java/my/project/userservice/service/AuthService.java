package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.userservice.dto.auth.AuthRequest;
import my.project.userservice.dto.auth.AuthTokens;
import my.project.userservice.dto.logout.LogoutRequest;
import my.project.userservice.dto.refresh.RefreshRequest;
import my.project.userservice.dto.register.RegistrationRequest;
import my.project.userservice.dto.register.RegistrationResponse;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.exception.InvalidCredentialsException;
import my.project.userservice.exception.InvalidTokenException;
import my.project.userservice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthTokens login(AuthRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("user.invalid-credentials");
        }

        var user = userService.findByEmail(req.email());

        return generateTokens(user);
    }

    public RegistrationResponse register(RegistrationRequest req) {
        if (userService.existsByEmail(req.email())) {
            throw new ConflictException("user.email-already-use");
        }
        UserEntity user = userService.save(req);
        return new RegistrationResponse(user.getId(), user.getEmail());
    }

    public AuthTokens refresh(RefreshRequest req) {
        String refreshToken = req.refreshToken();
        jwtService.validateRefreshToken(refreshToken);

        UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        String jti = jwtService.getJtiClaimFromRefreshToken(refreshToken);

        refreshTokenService.deactivateRefreshTokenForRefresh(jti, userId);

        UserEntity user = userService.findById(userId);
        if (!user.isEnabled()) {
            throw new ForbiddenException("user.not-enabled");
        }

        return generateTokens(user);
    }

    public void logout(LogoutRequest req) {
        String refreshToken = req.refreshToken();
        try {
            jwtService.validateRefreshToken(refreshToken);

            UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
            String jti = jwtService.getJtiClaimFromRefreshToken(refreshToken);

            refreshTokenService.deactivateRefreshTokenForLogout(jti, userId);
        } catch (InvalidTokenException ignored) {
            //ignored
        }
    }


    private AuthTokens generateTokens(UserEntity user) {
        String accessToken = jwtService.generateToken(user);
        String jti = refreshTokenService.createRefreshJti(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user, jti);
        return new AuthTokens(accessToken, refreshToken);
    }

}
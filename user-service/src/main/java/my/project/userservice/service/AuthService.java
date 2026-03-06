package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.userservice.dto.AuthRequest;
import my.project.userservice.dto.AuthTokens;
import my.project.userservice.dto.RefreshTokenDto;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.exception.InvalidCredentialsException;
import my.project.userservice.exception.InvalidTokenException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshJtiService refreshJtiService;
    private final AuthenticationManager authManager;

    public AuthTokens login(AuthRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (Exception e) {
            throw new InvalidCredentialsException("user.invalid-credentials");
        }

        var user = userService.findByEmail(req.email());

        return generateTokens(user);
    }

    public UUID register(RegistrationRequest req) {
        if (userService.existsByEmail(req.email())) {
            throw new ConflictException("user.email-already-use");
        }
        UserEntity user = userService.save(req);
        return user.getId();
    }

    public AuthTokens refresh(RefreshTokenDto req) {
        String refreshToken = req.refreshToken();
        jwtService.validateRefreshToken(refreshToken);

        UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        String jti = jwtService.getJtiClaimFromRefreshToken(refreshToken);

        refreshJtiService.deactivateForRefresh(jti, userId);

        UserEntity user = userService.findById(userId);
        if (!user.isEnabled()) {
            throw new ForbiddenException("user.not-enabled");
        }

        return generateTokens(user);
    }

    public void logout(RefreshTokenDto dto) {
        String refreshToken = dto.refreshToken();
        try {
            jwtService.validateRefreshToken(refreshToken);

            UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
            String jti = jwtService.getJtiClaimFromRefreshToken(refreshToken);

            refreshJtiService.deactivateForLogout(jti, userId);
        } catch (InvalidTokenException ignored) {
            //ignored
        }
    }


    private AuthTokens generateTokens(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user);
        String jti = refreshJtiService.save(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user, jti);
        return new AuthTokens(accessToken, refreshToken);
    }

}
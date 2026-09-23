package my.project.userservice.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.common.logging.Loggable;
import my.project.common.security.UserRole;
import my.project.userservice.dto.AuthRequest;
import my.project.userservice.dto.AuthTokens;
import my.project.userservice.dto.RefreshTokenDto;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.exception.InvalidCredentialsException;
import my.project.userservice.exception.InvalidTokenException;
import my.project.userservice.service.jwt.JwtService;
import my.project.userservice.service.jwt.RefreshJtiService;
import my.project.userservice.repository.UserRepositoryService;
import my.project.userservice.service.user.UserCommandService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Loggable
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCommandService userCommandService;
    private final UserRepositoryService userRepositoryService;
    private final JwtService jwtService;
    private final RefreshJtiService refreshJtiService;
    private final AuthenticationManager authManager;

    public AuthTokens login(AuthRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (Exception e) {
            log.warn("Ошибка аутентификации пользователя, email={}", req.email());
            throw new InvalidCredentialsException("user.invalid-credentials");
        }

        var user = userRepositoryService.getByEmail(req.email());

        return generateTokens(user);
    }

    public UUID register(RegistrationRequest req) {
        if (userRepositoryService.existsByEmail(req.email())) {
            log.warn("Регистрация отклонена: пользователь с email={} уже существует", req.email());
            throw new ConflictException("user.email-already-use");
        }

        if (req.role() == UserRole.ROLE_ADMIN) {
            log.warn("Регистрация отклонена: пользователь с email={} не может зарегистрироваться как админ", req.email());
            throw new ConflictException("user.invalid-role");
        }

        return userCommandService.save(req);
    }

    public AuthTokens refresh(RefreshTokenDto req) {
        String refreshToken = req.refreshToken();
        jwtService.validateRefreshToken(refreshToken);

        UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        String jti = jwtService.getJtiClaimFromRefreshToken(refreshToken);

        refreshJtiService.deactivateForRefresh(jti, userId);

        UserEntity user = userRepositoryService.getById(userId);
        if (!user.isEnabled()) {
            log.warn("Обновление токенов отклонено: пользователь отключён, userId={}", userId);
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
            log.warn("Выход из системы выполнен с некорректным или уже недействительным refresh token");
        }
    }

    private AuthTokens generateTokens(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user);
        String jti = refreshJtiService.save(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        return new AuthTokens(accessToken, refreshToken);
    }
}
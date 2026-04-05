package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshJtiService refreshJtiService;
    private final AuthenticationManager authManager;

    public AuthTokens login(AuthRequest req) {
        log.info("Попытка аутентификации пользователя, email={}", req.email());

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (Exception e) {
            log.warn("Ошибка аутентификации пользователя, email={}", req.email());
            throw new InvalidCredentialsException("user.invalid-credentials");
        }

        var user = userService.findByEmail(req.email());
        log.info("Пользователь успешно аутентифицирован, userId={}, email={}", user.getId(), user.getEmail());

        return generateTokens(user);
    }

    public UUID register(RegistrationRequest req) {
        log.info("Попытка регистрации пользователя, email={}", req.email());

        if (userService.existsByEmail(req.email())) {
            log.warn("Регистрация отклонена: пользователь с email={} уже существует", req.email());
            throw new ConflictException("user.email-already-use");
        }

        UserEntity user = userService.save(req);
        log.info("Пользователь успешно зарегистрирован, userId={}, email={}", user.getId(), user.getEmail());

        return user.getId();
    }

    public AuthTokens refresh(RefreshTokenDto req) {
        log.info("Попытка обновления токенов");

        String refreshToken = req.refreshToken();
        jwtService.validateRefreshToken(refreshToken);

        UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        String jti = jwtService.getJtiClaimFromRefreshToken(refreshToken);

        log.info("Refresh token прошёл первичную проверку, userId={}", userId);

        refreshJtiService.deactivateForRefresh(jti, userId);

        UserEntity user = userService.findById(userId);
        if (!user.isEnabled()) {
            log.warn("Обновление токенов отклонено: пользователь отключён, userId={}", userId);
            throw new ForbiddenException("user.not-enabled");
        }

        log.info("Токены успешно обновлены, userId={}", userId);
        return generateTokens(user);
    }

    public void logout(RefreshTokenDto dto) {
        log.info("Попытка выхода пользователя из системы");

        String refreshToken = dto.refreshToken();
        try {
            jwtService.validateRefreshToken(refreshToken);

            UUID userId = jwtService.getUserIdFromRefreshToken(refreshToken);
            String jti = jwtService.getJtiClaimFromRefreshToken(refreshToken);

            refreshJtiService.deactivateForLogout(jti, userId);
            log.info("Пользователь успешно вышел из системы, userId={}", userId);
        } catch (InvalidTokenException ignored) {
            log.warn("Выход из системы выполнен с некорректным или уже недействительным refresh token");
        }
    }

    private AuthTokens generateTokens(UserEntity user) {
        log.debug("Генерация JWT токенов для userId={}", user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String jti = refreshJtiService.save(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        log.info("JWT токены успешно сгенерированы для userId={}", user.getId());
        return new AuthTokens(accessToken, refreshToken);
    }
}
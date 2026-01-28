package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.common.exception.ConflictException;
import my.project.common.exception.ForbiddenException;
import my.project.userservice.dto.auth.AuthRequest;
import my.project.userservice.dto.auth.AuthResponse;
import my.project.userservice.dto.refresh.RefreshRequest;
import my.project.userservice.dto.refresh.RefreshResponse;
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


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(AuthRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("user.invalid-credentials");
        }
        var user = userService.findByEmail(req.email());
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(token, refreshToken);
    }

    public RegistrationResponse register(RegistrationRequest req) {
        if (userService.existsByEmail(req.email())) {
            throw new ConflictException("user.email-already-use");
        }
        UserEntity user = userService.save(req);
        return new RegistrationResponse(user.getId(), user.getEmail());
    }

    public RefreshResponse refresh(RefreshRequest req) {
        String refreshToken = req.refreshToken();
        if (refreshToken != null && jwtService.validateRefreshToken(refreshToken)) {
            UserEntity user = userService.findById(jwtService.getUserIdFromRefreshToken(refreshToken));
            if (!user.isEnabled()) {
                throw new ForbiddenException("user.not-enabled");
            }
            String token = jwtService.generateToken(user);
            String refreshedToken = jwtService.generateRefreshToken(user);
            return new RefreshResponse(token, refreshedToken);
        }
        throw new InvalidTokenException("user.invalid-token");
    }
}
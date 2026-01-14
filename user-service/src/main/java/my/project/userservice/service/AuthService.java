package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.*;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.exception.InvalidCredentialsException;
import my.project.userservice.exception.InvalidTokenException;
import my.project.userservice.exception.UserEmailAlreadyUseException;
import my.project.userservice.exception.UserNotEnabledException;
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
            throw new InvalidCredentialsException();
        }
        var user = userService.findByEmail(req.email());
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(token, refreshToken);
    }

    public RegistrationResponse register(RegistrationRequest req) {
        if (userService.existsByEmail(req.email())) {
            throw new UserEmailAlreadyUseException();
        }
        UserEntity user = userService.save(req);
        return new RegistrationResponse(user.getId(), user.getEmail());
    }

    public RefreshResponse refresh(RefreshRequest req) {
        String refreshToken = req.refreshToken();
        if (refreshToken != null && jwtService.validateRefreshToken(refreshToken)) {
            UserEntity user = userService.findById(jwtService.getUserIdFromRefreshToken(refreshToken));
            if (!user.isEnabled()) {
                throw new UserNotEnabledException(user.getId());
            }
            String token = jwtService.generateToken(user);
            String refreshedToken = jwtService.generateRefreshToken(user);
            return new RefreshResponse(token, refreshedToken);
        }
        throw new InvalidTokenException();
    }
}
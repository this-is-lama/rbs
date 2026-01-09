package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.AuthRequest;
import my.project.userservice.dto.AuthResponse;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.RegistrationResponse;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.util.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(AuthRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Неправильный логин или пароль");
        }
        var userDetails = userService.loadUserByUsername(req.email());
        String token = jwtUtils.generateToken(userDetails);
        return new AuthResponse(token);
    }

    public RegistrationResponse register(RegistrationRequest req) {
        if (userService.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        UserEntity user = userService.save(req);
        return new RegistrationResponse(user.getId(), user.getEmail());
    }
}
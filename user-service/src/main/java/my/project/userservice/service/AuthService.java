package my.project.userservice.service;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.AuthRequest;
import my.project.userservice.dto.AuthResponse;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.RegistrationResponse;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.exception.InvalidCredentialsException;
import my.project.userservice.exception.UserEmailAlreadyUse;
import my.project.userservice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class    AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(AuthRequest req) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
        var userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token);
    }

    public RegistrationResponse register(RegistrationRequest req) {
        if (userService.existsByEmail(req.email())) {
            throw new UserEmailAlreadyUse();
        }
        UserEntity user = userService.save(req);
        return new RegistrationResponse(user.getId(), user.getEmail());
    }
}
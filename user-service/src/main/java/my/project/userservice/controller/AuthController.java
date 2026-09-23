package my.project.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.AuthRequest;
import my.project.userservice.dto.AuthTokens;
import my.project.userservice.dto.RefreshTokenDto;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<AuthTokens> login(@RequestBody @Valid AuthRequest req) {
		AuthTokens tokens = authService.login(req);
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/register")
	public ResponseEntity<UUID> register(@RequestBody @Valid RegistrationRequest req) {
		UUID userId = authService.register(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(userId);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthTokens> refresh(@RequestBody @Valid RefreshTokenDto dto) {
		AuthTokens tokens = authService.refresh(dto);
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenDto dto) {
		authService.logout(dto);
		return ResponseEntity.ok().build();
	}
}

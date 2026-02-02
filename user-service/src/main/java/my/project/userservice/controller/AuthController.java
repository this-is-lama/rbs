package my.project.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.AuthRequest;
import my.project.userservice.dto.AuthTokens;
import my.project.userservice.dto.RefreshTokenDto;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.service.AuthService;
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
		return ResponseEntity.ok(authService.login(req));
	}

	@PostMapping("/register")
	public ResponseEntity<UUID> register(@RequestBody @Valid RegistrationRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthTokens> refresh(@RequestBody @Valid RefreshTokenDto dto) {
		return ResponseEntity.ok(authService.refresh(dto));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenDto dto) {
		authService.logout(dto);
		return ResponseEntity.ok().build();
	}

}

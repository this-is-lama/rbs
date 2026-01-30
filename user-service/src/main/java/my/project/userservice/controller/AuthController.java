package my.project.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.auth.AuthRequest;
import my.project.userservice.dto.auth.AuthTokens;
import my.project.userservice.dto.logout.LogoutRequest;
import my.project.userservice.dto.refresh.RefreshRequest;
import my.project.userservice.dto.register.RegistrationRequest;
import my.project.userservice.dto.register.RegistrationResponse;
import my.project.userservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<AuthTokens> login(@RequestBody @Valid AuthRequest req) {
		return ResponseEntity.ok(authService.login(req));
	}

	@PostMapping("/register")
	public ResponseEntity<RegistrationResponse> register(@RequestBody @Valid RegistrationRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthTokens> refresh(@RequestBody @Valid RefreshRequest req) {
		return ResponseEntity.ok(authService.refresh(req));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest req) {
		authService.logout(req);
		return ResponseEntity.ok().build();
	}

}

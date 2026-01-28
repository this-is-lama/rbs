package my.project.userservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.auth.AuthRequest;
import my.project.userservice.dto.auth.AuthResponse;
import my.project.userservice.dto.refresh.RefreshRequest;
import my.project.userservice.dto.refresh.RefreshResponse;
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
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
		return ResponseEntity.ok(authService.login(req));
	}

	@PostMapping("/register")
	public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest req) {
		return ResponseEntity.ok(authService.refresh(req));
	}
}

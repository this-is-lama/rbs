package my.project.userservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.AuthRequest;
import my.project.userservice.dto.RegistrationRequest;
import my.project.userservice.dto.RegistrationResponse;
import my.project.userservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
	public ResponseEntity<?> login(@RequestBody AuthRequest req) {
		try {
			return ResponseEntity.ok(authService.login(req));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegistrationRequest req) {
		try {
			RegistrationResponse created = authService.register(req);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}

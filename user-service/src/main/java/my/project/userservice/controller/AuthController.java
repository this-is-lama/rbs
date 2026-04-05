package my.project.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<AuthTokens> login(@RequestBody @Valid AuthRequest req) {
		log.info("Получен запрос на вход в систему для email={}", req.email());
		AuthTokens tokens = authService.login(req);
		log.info("Вход в систему выполнен успешно для email={}", req.email());
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/register")
	public ResponseEntity<UUID> register(@RequestBody @Valid RegistrationRequest req) {
		log.info("Получен запрос на регистрацию пользователя с email={}", req.email());
		UUID userId = authService.register(req);
		log.info("Пользователь успешно зарегистрирован, userId={}, email={}", userId, req.email());
		return ResponseEntity.status(HttpStatus.CREATED).body(userId);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthTokens> refresh(@RequestBody @Valid RefreshTokenDto dto) {
		log.info("Получен запрос на обновление токенов");
		AuthTokens tokens = authService.refresh(dto);
		log.info("Токены успешно обновлены");
		return ResponseEntity.ok(tokens);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenDto dto) {
		log.info("Получен запрос на выход из системы");
		authService.logout(dto);
		log.info("Выход из системы выполнен");
		return ResponseEntity.ok().build();
	}
}

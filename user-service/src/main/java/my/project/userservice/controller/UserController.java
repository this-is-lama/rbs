package my.project.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.security.AuthUtil;
import my.project.userservice.dto.ChangePasswordRequest;
import my.project.userservice.dto.ChangeRoleRequest;
import my.project.userservice.dto.UpdateUserRequest;
import my.project.userservice.dto.UserDto;
import my.project.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<UserDto> me(Authentication auth) {
		var userId = AuthUtil.id(auth);
		log.info("Получен запрос на получение профиля пользователя, userId={}", userId);
		return ResponseEntity.ok(userService.getById(userId));
	}

	@PatchMapping("/me")
	public ResponseEntity<UserDto> update(@RequestBody @Valid UpdateUserRequest req,
										  Authentication auth) {
		var userId = AuthUtil.id(auth);
		log.info("Получен запрос на обновление профиля пользователя, userId={}", userId);
		UserDto updatedUser = userService.update(userId, req);
		log.info("Профиль пользователя успешно обновлён, userId={}", userId);
		return ResponseEntity.ok(updatedUser);
	}

	@PatchMapping("/me/password")
	public ResponseEntity<Void> changeMyPassword(@RequestBody @Valid ChangePasswordRequest req,
												 Authentication auth) {
		var userId = AuthUtil.id(auth);
		log.info("Получен запрос на смену пароля, userId={}", userId);
		userService.changePassword(userId, req);
		log.info("Пароль пользователя успешно изменён, userId={}", userId);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PatchMapping("/change-role")
	public ResponseEntity<UUID> changeRole(@RequestBody ChangeRoleRequest req, Authentication auth) {
		log.info("Получен запрос на смену роли для email={}, новая роль={}", req.email(), req.role());
		var id = userService.changeRoleByEmail(req, auth);
		log.info("Роль пользователя успешно изменена, userId={}, email={}, новая роль={}", id, req.email(), req.role());
		return ResponseEntity.ok(id);
	}
}
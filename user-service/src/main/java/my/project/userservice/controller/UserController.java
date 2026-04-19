package my.project.userservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.common.security.AuthUtil;
import my.project.userservice.dto.ChangePasswordRequest;
import my.project.userservice.dto.ChangeRoleByIdRequest;
import my.project.userservice.dto.UpdateUserRequest;
import my.project.userservice.dto.UserBriefDto;
import my.project.userservice.dto.UserDto;
import my.project.userservice.dto.UserLookupDto;
import my.project.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<UserDto> getMe(Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		log.info("Получен запрос на профиль текущего пользователя, userId={}", userId);
		return ResponseEntity.ok(userService.getMe(auth));
	}

	@PutMapping("/me")
	public ResponseEntity<UserDto> updateMe(@RequestBody @Valid UpdateUserRequest req,
											Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		log.info("Получен запрос на обновление профиля, userId={}", userId);
		UserDto updatedUser = userService.update(userId, req);
		log.info("Профиль пользователя успешно обновлён, userId={}", userId);
		return ResponseEntity.ok(updatedUser);
	}

	@PatchMapping("/me/password")
	public ResponseEntity<Void> changeMyPassword(@RequestBody @Valid ChangePasswordRequest req,
												 Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		log.info("Получен запрос на смену пароля, userId={}", userId);
		userService.changePassword(userId, req);
		log.info("Пароль пользователя успешно изменён, userId={}", userId);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/change-role-by-id")
	public ResponseEntity<UUID> changeRoleById(@RequestBody @Valid ChangeRoleByIdRequest req,
											   Authentication auth) {
		log.info("Получен запрос на смену роли по userId={}, новая роль={}", req.userId(), req.role());
		UUID id = userService.changeRoleById(req, auth);
		log.info("Роль пользователя успешно изменена, userId={}, новая роль={}", id, req.role());
		return ResponseEntity.ok(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/lookup")
	public ResponseEntity<UserLookupDto> lookupByEmail(@RequestParam @NotBlank @Email String email) {
		log.info("Получен запрос на lookup пользователя по email={}", email);
		return ResponseEntity.ok(userService.lookupByEmail(email));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/summaries")
	public ResponseEntity<List<UserLookupDto>> summaries(@RequestBody Set<UUID> ids) {
		log.info("Получен запрос на summaries пользователей, count={}", ids == null ? 0 : ids.size());
		return ResponseEntity.ok(userService.getSummaries(ids));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/briefs")
	public ResponseEntity<List<UserBriefDto>> briefs(@RequestBody Set<UUID> ids) {
		log.info("Получен запрос на краткую информацию о пользователях, count={}", ids == null ? 0 : ids.size());
		return ResponseEntity.ok(userService.getBriefs(ids));
	}
}
package my.project.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<UserDto> me(Authentication auth) {
		var userId = AuthUtil.id(auth);
		return ResponseEntity.ok(userService.getById(userId));
	}

	@PatchMapping("/me")
	public ResponseEntity<UserDto> update(@RequestBody @Valid UpdateUserRequest req,
										  Authentication auth) {
		var userId = AuthUtil.id(auth);
		return ResponseEntity.ok(userService.update(userId, req));
	}

	@PatchMapping("/me/password")
	public ResponseEntity<Void> changeMyPassword(@RequestBody @Valid ChangePasswordRequest req,
												 Authentication auth) {
		var userId = AuthUtil.id(auth);
		userService.changePassword(userId, req);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PatchMapping("/change-role")
	public ResponseEntity<UUID> changeRole(@RequestBody ChangeRoleRequest req, Authentication auth) {
		var id = userService.changeRoleByEmail(req, auth);
		return ResponseEntity.ok(id);
	}

}

package my.project.userservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import my.project.common.security.AuthUtil;
import my.project.userservice.dto.ChangePasswordRequest;
import my.project.userservice.dto.ChangeRoleByIdRequest;
import my.project.userservice.dto.UpdateUserRequest;
import my.project.userservice.dto.UserDto;
import my.project.userservice.service.user.UserCommandService;
import my.project.userservice.service.user.UserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserCommandService commandService;
	private final UserQueryService queryService;

	@GetMapping("/me")
	public ResponseEntity<UserDto> getMe(Authentication auth) {
		return ResponseEntity.ok(queryService.getById(AuthUtil.id(auth)));
	}

	@PutMapping("/me")
	public ResponseEntity<UserDto> updateMe(@RequestBody @Valid UpdateUserRequest req,
											Authentication auth) {
		UserDto updatedUser = commandService.update(req, auth);
		return ResponseEntity.ok(updatedUser);
	}

	@PatchMapping("/me/password")
	public ResponseEntity<Void> changeMyPassword(@RequestBody @Valid ChangePasswordRequest req,
												 Authentication auth) {
		commandService.changePassword(req, auth);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping("/change-role-by-id")
	public ResponseEntity<UUID> changeRoleById(@RequestBody @Valid ChangeRoleByIdRequest req,
											   Authentication auth) {
		UUID id = commandService.changeRoleById(req, auth);
		return ResponseEntity.ok(id);
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/email")
	public ResponseEntity<UserDto> getUserByEmail(@RequestParam @NotBlank @Email String email) {
		return ResponseEntity.ok(queryService.getByEmail(email));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
		return ResponseEntity.ok(queryService.getById(id));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PostMapping()
	public ResponseEntity<List<UserDto>> getUsersByIds(@RequestBody Set<UUID> ids) {
		return ResponseEntity.ok(queryService.findAllByIds(ids));
	}

}
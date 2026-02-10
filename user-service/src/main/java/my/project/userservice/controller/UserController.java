package my.project.userservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.common.security.AuthUtil;
import my.project.userservice.dto.ChangeRoleRequest;
import my.project.userservice.dto.UserDto;
import my.project.userservice.mapper.UserMapper;
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
	private final UserMapper userMapper;

	@GetMapping("/me")
	public ResponseEntity<UserDto> me(Authentication auth) {
		var userId = AuthUtil.id(auth);
		var user = userService.findById(userId);
		return ResponseEntity.ok(userMapper.toDto(user));
	}

	@PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN')")
	@PatchMapping("/change-role")
	public ResponseEntity<UUID> changeRole(@RequestBody ChangeRoleRequest req, Authentication auth) {
		var id = userService.changeRoleByEmail(req, auth);
		return ResponseEntity.ok(id);
	}

}

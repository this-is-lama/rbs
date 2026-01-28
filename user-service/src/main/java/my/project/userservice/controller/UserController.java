package my.project.userservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.user.UserProfileResponse;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.service.UserService;
import my.project.userservice.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserMapper userMapper;

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
		UUID id = UUID.fromString(authentication.getName());
		UserEntity user = userService.findById(id);
		return ResponseEntity.ok(userMapper.toResponse(user));
	}
}

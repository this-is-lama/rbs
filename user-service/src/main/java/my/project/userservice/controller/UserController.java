package my.project.userservice.controller;

import lombok.RequiredArgsConstructor;
import my.project.userservice.dto.UserProfileResponse;
import my.project.userservice.entity.UserEntity;
import my.project.userservice.service.UserService;
import my.project.userservice.util.UserProfileMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserProfileMapper userProfileMapper;

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
		String email = authentication.getName();
		UserEntity user = userService.findByEmail(email);
		return ResponseEntity.ok(userProfileMapper.toResponse(user));
	}
}

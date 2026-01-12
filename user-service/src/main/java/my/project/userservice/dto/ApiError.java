package my.project.userservice.dto;

public record ApiError(
        int status,
        String code,
        String message,
		String path
) {}
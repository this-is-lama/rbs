package my.project.common.dto;

public record ApiError(

        int status,

        String code,

        String message,

		String path

) {}
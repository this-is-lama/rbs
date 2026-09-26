package my.project.apigateway.dto;

public record ApiError(

        int status,

        String code,

        String message,

		String path

) {}
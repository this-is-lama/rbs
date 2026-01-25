package my.project.restaurantservice.config.minio;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(

		@NotBlank(message = "endpoint must not be blank")
		@URL(message = "endpoint must be a valid URL")
        String endpoint,

		@NotBlank(message = "accessKey must not be blank")
		String accessKey,

		@NotBlank(message = "secretKey must not be blank")
		String secretKey,

		@NotBlank(message = "publicBaseUrl must not be blank")
		@URL(message = "publicBaseUrl must be a valid URL")
        String publicBaseUrl
) {}

package my.project.restaurantservice.dto.photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PhotoConfirmRequest(

        @NotNull
        UUID id,

        @NotBlank
        @Size(max = 512)
        String objectKey
) {}

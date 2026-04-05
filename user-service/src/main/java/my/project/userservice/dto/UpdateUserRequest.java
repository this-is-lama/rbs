package my.project.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(

        @NotBlank
        @Size(min = 2, max = 100)
        String name,

        @NotBlank
        @Size(min = 2, max = 100)
        String surname,

        LocalDate dateOfBirth,

        @Size(max = 12)
        String phone,

        @NotBlank
        @Email
        String email
) {}
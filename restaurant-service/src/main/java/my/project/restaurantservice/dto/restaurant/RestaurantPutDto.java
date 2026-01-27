package my.project.restaurantservice.dto.restaurant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import my.project.restaurantservice.dto.contact.ContactDto;
import my.project.restaurantservice.dto.workinghours.WorkingHoursDto;

import java.util.List;

public record RestaurantPutDto(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @NotBlank
        @Size(max = 100)
        String category,

        @Size(max = 30)
        String phone,

        @NotBlank
        @Size(max = 255)
        String address,

        @NotNull
        Boolean active,

        @NotNull
        List<@Valid WorkingHoursDto> workingHours,

        @NotNull
        List<@Valid ContactDto> contacts
) {}

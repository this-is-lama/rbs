package my.project.restaurantservice.dto.table;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TableLayoutUpdateRequest(

        @NotEmpty
        @Size(max = 500)
        List<@Valid TableLayoutItemRequest> tables
) {}
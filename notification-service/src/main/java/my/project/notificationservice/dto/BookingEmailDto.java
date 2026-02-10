package my.project.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class BookingEmailDto {
    private String userName;
    private String restaurantName;
    private LocalDate date;
    private LocalTime time;
}

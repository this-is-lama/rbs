package my.project.bookingservice.pricing.parameters.calendar;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
		name = "is-day-off-client",
		url = "${pricing.calendar.is-day-off-url:https://isdayoff.ru}"
)
public interface IsDayOffClient {
	@GetMapping("/api/getdata")
	String getData(@RequestParam("year") int year,
				   @RequestParam("month") int month,
				   @RequestParam("day") int day,
				   @RequestParam("cc") String countryCode,
				   @RequestParam("holiday") int holiday);
}


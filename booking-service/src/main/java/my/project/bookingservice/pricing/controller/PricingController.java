package my.project.bookingservice.pricing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.project.bookingservice.pricing.dto.request.PricingOfferRequest;
import my.project.bookingservice.pricing.dto.response.PricingOfferResponse;
import my.project.bookingservice.pricing.service.PricingService;
import my.project.common.security.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings/pricing")
@RequiredArgsConstructor
public class PricingController {
	private final PricingService pricingService;

	@PostMapping("/offers")
	public ResponseEntity<PricingOfferResponse> createOffer(@RequestBody @Valid PricingOfferRequest request,
															Authentication auth) {
		UUID userId = AuthUtil.id(auth);
		return ResponseEntity.ok(pricingService.createOffer(userId, request));
	}
}


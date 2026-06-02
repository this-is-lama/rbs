package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.calculator.PricingCalculationResult;
import my.project.bookingservice.pricing.calculator.PricingCalculator;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.enums.PricingOfferStatus;
import my.project.bookingservice.pricing.offer.PricingOfferExpirationService;
import my.project.bookingservice.pricing.offer.PricingOfferFactory;
import my.project.bookingservice.pricing.offer.PricingOfferService;
import my.project.bookingservice.pricing.offer.PricingOfferUsageService;
import my.project.bookingservice.pricing.persistence.entity.PricingOfferEntity;
import my.project.bookingservice.pricing.persistence.repository.PricingOfferRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.common.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static my.project.bookingservice.pricing.PricingTestSupport.contextWithPreorder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingOfferServiceTest {
    private final PricingOfferRepository repository = mock(PricingOfferRepository.class);
    private final PricingCalculator calculator = mock(PricingCalculator.class);
    private final PricingOfferUsageService usageService = mock(PricingOfferUsageService.class);
    private final PricingOfferService service = new PricingOfferService(
            repository,
            new PricingOfferExpirationService(),
            calculator,
            new PricingOfferFactory(new PricingProperties()),
            usageService
    );

    @Test
    void actualOfferIsReused() {
        PricingContext context = contextWithPreorder(BigDecimal.TEN);
        PricingOfferEntity offer = offer(context, PricingOfferStatus.ACTIVE, Instant.now().plusSeconds(600));
        when(repository.findFirstByUserIdAndRestaurantIdAndTableIdAndCartHashAndVisitStartAndVisitEndAndStatusOrderByCreatedAtDesc(
                context.userId(), context.restaurantId(), context.tableId(), context.cartHash(), context.visitStart(), context.visitEnd(), PricingOfferStatus.ACTIVE
        )).thenReturn(Optional.of(offer));

        PricingOfferEntity result = service.getOrCreateOffer(context);

        assertThat(result).isSameAs(offer);
    }

    @Test
    void expiredOfferIsMarkedExpiredAndNewOfferIsCreated() {
        PricingContext context = contextWithPreorder(BigDecimal.TEN);
        PricingOfferEntity expired = offer(context, PricingOfferStatus.ACTIVE, Instant.now().minusSeconds(1));
        when(repository.findFirstByUserIdAndRestaurantIdAndTableIdAndCartHashAndVisitStartAndVisitEndAndStatusOrderByCreatedAtDesc(
                context.userId(), context.restaurantId(), context.tableId(), context.cartHash(), context.visitStart(), context.visitEnd(), PricingOfferStatus.ACTIVE
        )).thenReturn(Optional.of(expired));
        when(calculator.calculate(context)).thenReturn(result(context));
        when(repository.save(any(PricingOfferEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PricingOfferEntity result = service.getOrCreateOffer(context);

        assertThat(expired.getStatus()).isEqualTo(PricingOfferStatus.EXPIRED);
        assertThat(result.getStatus()).isEqualTo(PricingOfferStatus.ACTIVE);
    }

    @Test
    void changedCartCreatesDifferentOffer() {
        PricingContext context = contextWithPreorder(BigDecimal.TEN);
        when(repository.findFirstByUserIdAndRestaurantIdAndTableIdAndCartHashAndVisitStartAndVisitEndAndStatusOrderByCreatedAtDesc(
                context.userId(), context.restaurantId(), context.tableId(), context.cartHash(), context.visitStart(), context.visitEnd(), PricingOfferStatus.ACTIVE
        )).thenReturn(Optional.empty());
        when(calculator.calculate(context)).thenReturn(result(context));
        when(repository.save(any(PricingOfferEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PricingOfferEntity result = service.getOrCreateOffer(context);

        assertThat(result.getId()).isNotNull();
        verify(repository).save(any(PricingOfferEntity.class));
    }

    @Test
    void useOfferMarksItUsed() {
        PricingContext context = contextWithPreorder(BigDecimal.TEN);
        PricingOfferEntity offer = offer(context, PricingOfferStatus.ACTIVE, Instant.now().plusSeconds(600));
        offer.setStatus(PricingOfferStatus.USED);
        when(usageService.validateAndUse(context, offer.getId())).thenReturn(offer);

        PricingOfferEntity result = service.useOffer(offer.getId(), context);

        assertThat(result.getStatus()).isEqualTo(PricingOfferStatus.USED);
        verify(usageService).validateAndUse(context, offer.getId());
    }

    @Test
    void useOfferRejectsMismatch() {
        PricingContext context = contextWithPreorder(BigDecimal.TEN);
        PricingOfferEntity offer = offer(context, PricingOfferStatus.ACTIVE, Instant.now().plusSeconds(600));
        when(usageService.validateAndUse(context, offer.getId()))
                .thenThrow(new ValidationException("pricing.offer.mismatch"));

        assertThatThrownBy(() -> service.useOffer(offer.getId(), context))
                .isInstanceOf(ValidationException.class);
    }

    private PricingOfferEntity offer(PricingContext context, PricingOfferStatus status, Instant expiresAt) {
        PricingOfferEntity offer = new PricingOfferEntity();
        offer.setId(UUID.randomUUID());
        offer.setUserId(context.userId());
        offer.setRestaurantId(context.restaurantId());
        offer.setTableId(context.tableId());
        offer.setCartHash(context.cartHash());
        offer.setVisitStart(context.visitStart());
        offer.setVisitEnd(context.visitEnd());
        offer.setStatus(status);
        offer.setExpiresAt(expiresAt);
        offer.setCreatedAt(Instant.now());
        offer.setPreorderAmount(context.preorderAmount());
        offer.setPricingCharge(BigDecimal.ONE);
        offer.setTotalAmount(context.preorderAmount().add(BigDecimal.ONE));
        return offer;
    }

    private PricingCalculationResult result(PricingContext context) {
        return new PricingCalculationResult(
                context.preorderAmount(),
                BigDecimal.ONE,
                context.preorderAmount().add(BigDecimal.ONE),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                Instant.now()
        );
    }
}

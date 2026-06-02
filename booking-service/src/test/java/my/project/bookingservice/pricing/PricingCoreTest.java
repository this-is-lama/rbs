package my.project.bookingservice.pricing;

import my.project.bookingservice.pricing.blocks.BlockCalculationResult;
import my.project.bookingservice.pricing.blocks.calendar.CalendarContextBlock;
import my.project.bookingservice.pricing.blocks.historical.HistoricalDemandBlock;
import my.project.bookingservice.pricing.blocks.load.LoadBlock;
import my.project.bookingservice.pricing.calculator.PricingCalculator;
import my.project.bookingservice.pricing.context.PricingContext;
import my.project.bookingservice.pricing.dto.request.PricingPreorderItemRequest;
import my.project.bookingservice.pricing.enums.CalendarDayType;
import my.project.bookingservice.pricing.enums.PricingBlockCode;
import my.project.bookingservice.pricing.enums.PricingWeightCode;
import my.project.bookingservice.pricing.history.HistoricalTransitionService;
import my.project.bookingservice.pricing.history.PricingHistoryService;
import my.project.bookingservice.pricing.offer.PricingOfferHashService;
import my.project.bookingservice.pricing.parameters.calendar.CalendarMembership;
import my.project.bookingservice.pricing.parameters.calendar.CalendarStatusParameter;
import my.project.bookingservice.pricing.parameters.calendar.DefaultCalendarClassifier;
import my.project.bookingservice.pricing.parameters.calendar.IsDayOffCalendarService;
import my.project.bookingservice.pricing.parameters.historical.TableDemandParameter;
import my.project.bookingservice.pricing.parameters.historical.TimeIntervalDemandParameter;
import my.project.bookingservice.pricing.parameters.historical.WeekdayDemandParameter;
import my.project.bookingservice.pricing.parameters.load.OccupancyParameter;
import my.project.bookingservice.pricing.parameters.load.UrgencyParameter;
import my.project.bookingservice.pricing.persistence.repository.PricingCalendarCoefficientRepository;
import my.project.bookingservice.pricing.settings.PricingProperties;
import my.project.bookingservice.pricing.settings.PricingCalendarCoefficientService;
import my.project.bookingservice.pricing.weights.PricingWeightProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static my.project.bookingservice.pricing.PricingTestSupport.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingOfferHashServiceTest {
    private final PricingOfferHashService service = new PricingOfferHashService();

    @Test
    void sameCartInDifferentOrderHasSameHash() {
        UUID userId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        UUID dishA = UUID.randomUUID();
        UUID dishB = UUID.randomUUID();
        Instant start = Instant.parse("2026-06-01T10:00:00Z");
        Instant end = Instant.parse("2026-06-01T12:00:00Z");

        String first = service.hash(userId, restaurantId, tableId, start, end, List.of(
                new PricingPreorderItemRequest(dishA, 2),
                new PricingPreorderItemRequest(dishB, 1)
        ));
        String second = service.hash(userId, restaurantId, tableId, start, end, List.of(
                new PricingPreorderItemRequest(dishB, 1),
                new PricingPreorderItemRequest(dishA, 2)
        ));

        assertThat(second).isEqualTo(first);
    }

    @Test
    void differentCartHasDifferentHash() {
        UUID userId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        Instant start = Instant.parse("2026-06-01T10:00:00Z");
        Instant end = Instant.parse("2026-06-01T12:00:00Z");

        String first = service.hash(userId, restaurantId, tableId, start, end, List.of(new PricingPreorderItemRequest(dishId, 1)));
        String second = service.hash(userId, restaurantId, tableId, start, end, List.of(new PricingPreorderItemRequest(dishId, 2)));

        assertThat(second).isNotEqualTo(first);
    }
}

class OccupancyParameterTest {
    private final OccupancyParameter parameter = new OccupancyParameter();

    @Test
    void calculatesOccupiedToTotalRatio() {
        assertThat(parameter.calculate(context(0, 10)).value()).isEqualByComparingTo("0");
        assertThat(parameter.calculate(context(5, 10)).value()).isEqualByComparingTo("0.5000000000");
        assertThat(parameter.calculate(context(10, 10)).value()).isEqualByComparingTo("1");
    }

    @Test
    void rejectsZeroTotalTables() {
        assertThatThrownBy(() -> parameter.calculate(context(0, 0)))
                .isInstanceOf(RuntimeException.class);
    }
}

class UrgencyParameterTest {
    @Test
    void closerBookingHasHigherUrgency() {
        UrgencyParameter parameter = new UrgencyParameter(new PricingProperties());
        Instant now = Instant.parse("2026-06-01T10:00:00Z");

        BigDecimal close = parameter.calculate(context(now, now.plusSeconds(3600))).value();
        BigDecimal far = parameter.calculate(context(now, now.plusSeconds(48 * 3600))).value();

        assertThat(close).isGreaterThan(far);
    }

    @Test
    void rejectsPastVisitStart() {
        UrgencyParameter parameter = new UrgencyParameter(new PricingProperties());
        Instant now = Instant.parse("2026-06-01T10:00:00Z");

        assertThatThrownBy(() -> parameter.calculate(context(now, now.minusSeconds(1))))
                .isInstanceOf(RuntimeException.class);
    }
}

class LoadBlockTest {
    @Test
    void usesOccupancyUrgencyAndInteraction() {
        PricingWeightProvider weights = (restaurantId, code) -> switch (code) {
            case OCCUPANCY_PARAMETER, URGENCY_PARAMETER, OCCUPANCY_URGENCY_INTERACTION -> BigDecimal.ONE;
            default -> BigDecimal.ZERO;
        };
        PricingProperties properties = new PricingProperties();
        LoadBlock block = new LoadBlock(new OccupancyParameter(), new UrgencyParameter(properties), weights, properties);

        BigDecimal value = block.calculate(context(5, 10)).value();

        assertThat(value).isBetween(BigDecimal.ZERO, BigDecimal.ONE);
        assertThat(value).isGreaterThan(BigDecimal.ZERO);
    }
}

class HistoricalDemandBlockTest {
    @Test
    void defaultsWhenHistoryIsInsufficient() {
        PricingHistoryService history = mock(PricingHistoryService.class);
        PricingProperties properties = new PricingProperties();
        HistoricalTransitionService transitionService = new HistoricalTransitionService();
        PricingWeightProvider weights = (restaurantId, code) -> BigDecimal.ONE;
        HistoricalDemandBlock block = new HistoricalDemandBlock(
                new WeekdayDemandParameter(history, properties, transitionService),
                new TimeIntervalDemandParameter(history, properties, transitionService),
                new TableDemandParameter(history, properties, transitionService),
                weights,
                properties
        );

        BigDecimal value = block.calculate(context(5, 10)).value();

        assertThat(value).isCloseTo(new BigDecimal("0.5"), within(new BigDecimal("0.000000001")));
    }

    @Test
    void ratiosAreUsedWhenHistoryIsSufficient() {
        PricingHistoryService history = mock(PricingHistoryService.class);
        UUID restaurantId = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        when(history.countSuccessfulBookings(restaurantId)).thenReturn(150L);
        when(history.countSuccessfulBookingsByWeekday(restaurantId, DayOfWeek.MONDAY)).thenReturn(5L);
        when(history.maxSuccessfulBookingsByWeekday(restaurantId)).thenReturn(10L);
        when(history.countSuccessfulBookingsByTimeInterval(restaurantId, "LUNCH")).thenReturn(8L);
        when(history.maxSuccessfulBookingsByTimeInterval(restaurantId)).thenReturn(10L);
        when(history.countSuccessfulBookingsByTable(restaurantId, tableId)).thenReturn(60L);
        when(history.maxSuccessfulBookingsByTable(restaurantId)).thenReturn(100L);
        PricingProperties properties = new PricingProperties();
        HistoricalTransitionService transitionService = new HistoricalTransitionService();
        PricingWeightProvider weights = (id, code) -> BigDecimal.ONE;
        HistoricalDemandBlock block = new HistoricalDemandBlock(
                new WeekdayDemandParameter(history, properties, transitionService),
                new TimeIntervalDemandParameter(history, properties, transitionService),
                new TableDemandParameter(history, properties, transitionService),
                weights,
                properties
        );

        BigDecimal value = block.calculate(context(restaurantId, tableId, Instant.parse("2026-06-01T09:00:00Z"))).value();

        assertThat(value).isCloseTo(new BigDecimal("0.6333333333"), within(new BigDecimal("0.000000001")));
    }
}

class CalendarStatusParameterTest {
    @Test
    void classifiesWorkdayAndWeekendWithDefaultCoefficients() {
        IsDayOffCalendarService isDayOff = mock(IsDayOffCalendarService.class);
        when(isDayOff.isDayOff(java.time.LocalDate.parse("2026-06-01"))).thenReturn(false);
        when(isDayOff.isDayOff(java.time.LocalDate.parse("2026-06-07"))).thenReturn(true);
        CalendarStatusParameter parameter = calendarParameter(new DefaultCalendarClassifier(new PricingProperties(), isDayOff));

        BigDecimal workday = parameter.calculate(context(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-06-01T09:00:00Z"))).value();
        BigDecimal weekend = parameter.calculate(context(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-06-07T09:00:00Z"))).value();

        assertThat(workday).isEqualByComparingTo("0.35");
        assertThat(weekend).isEqualByComparingTo("0.65");
    }

    @Test
    void neutralValueWhenMembershipIsEmpty() {
        CalendarStatusParameter parameter = calendarParameter(date -> new CalendarMembership(new EnumMap<>(CalendarDayType.class)));

        BigDecimal value = parameter.calculate(context(5, 10)).value();

        assertThat(value).isEqualByComparingTo("0.5");
    }

    @Test
    void holidayAndPeakHolidayAreSupported() {
        PricingProperties properties = new PricingProperties();
        properties.getCalendar().setHolidays(List.of(java.time.LocalDate.parse("2026-03-08")));
        properties.getCalendar().setPeakHolidays(List.of(java.time.LocalDate.parse("2026-03-08")));
        IsDayOffCalendarService isDayOff = mock(IsDayOffCalendarService.class);
        when(isDayOff.isDayOff(java.time.LocalDate.parse("2026-03-08"))).thenReturn(true);
        when(isDayOff.isHoliday(java.time.LocalDate.parse("2026-03-08"))).thenReturn(true);
        CalendarStatusParameter parameter = calendarParameter(new DefaultCalendarClassifier(properties, isDayOff), properties);

        BigDecimal value = parameter.calculate(context(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-03-08T09:00:00Z"))).value();

        assertThat(value).isGreaterThan(new BigDecimal("0.65"));
    }
}

class PricingCalculatorTest {
    @Test
    void noPreorderMeansZeroCharge() {
        PricingCalculator calculator = calculator(BigDecimal.ONE);

        var result = calculator.calculate(contextWithPreorder(BigDecimal.ZERO));

        assertThat(result.pricingCharge()).isEqualByComparingTo("0.00");
        assertThat(result.totalAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void preorderChargeIsBetweenBoundsAndIndependentFromPreorderAmount() {
        PricingCalculator calculator = calculator(new BigDecimal("0.5"));

        var cheap = calculator.calculate(contextWithPreorder(new BigDecimal("100.00")));
        var expensive = calculator.calculate(contextWithPreorder(new BigDecimal("900.00")));

        assertThat(cheap.pricingCharge()).isBetween(new BigDecimal("100.00"), new BigDecimal("1000.00"));
        assertThat(cheap.totalAmount()).isEqualByComparingTo(cheap.preorderAmount().add(cheap.pricingCharge()));
        assertThat(expensive.pricingCharge()).isEqualByComparingTo(cheap.pricingCharge());
    }

    @Test
    void preorderIsCalculatedByDemandModel() {
        PricingCalculator calculator = calculator(BigDecimal.ONE);

        var result = calculator.calculate(contextWithPreorder(new BigDecimal("500.00")));

        assertThat(result.pricingCharge()).isGreaterThan(new BigDecimal("100.00"));
        assertThat(result.totalAmount()).isEqualByComparingTo(result.preorderAmount().add(result.pricingCharge()));
    }

    @Test
    void zeroBlockWeightsFallbackToDefaults() {
        LoadBlock load = mock(LoadBlock.class);
        HistoricalDemandBlock historical = mock(HistoricalDemandBlock.class);
        CalendarContextBlock calendar = mock(CalendarContextBlock.class);
        when(load.calculate(org.mockito.ArgumentMatchers.any())).thenReturn(new BlockCalculationResult(PricingBlockCode.LOAD, BigDecimal.ONE, Map.of()));
        when(historical.calculate(org.mockito.ArgumentMatchers.any())).thenReturn(new BlockCalculationResult(PricingBlockCode.HISTORICAL_DEMAND, BigDecimal.ONE, Map.of()));
        when(calendar.calculate(org.mockito.ArgumentMatchers.any())).thenReturn(new BlockCalculationResult(PricingBlockCode.CALENDAR_CONTEXT, BigDecimal.ONE, Map.of()));
        PricingCalculator calculator = new PricingCalculator(load, historical, calendar, (restaurantId, code) -> BigDecimal.ZERO, new PricingProperties());

        var result = calculator.calculate(contextWithPreorder(BigDecimal.TEN));

        assertThat(result.demandIndex()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.pricingCharge()).isGreaterThan(new BigDecimal("100.00"));
    }

    private PricingCalculator calculator(BigDecimal blockValue) {
        LoadBlock load = mock(LoadBlock.class);
        HistoricalDemandBlock historical = mock(HistoricalDemandBlock.class);
        CalendarContextBlock calendar = mock(CalendarContextBlock.class);
        when(load.calculate(org.mockito.ArgumentMatchers.any())).thenReturn(new BlockCalculationResult(PricingBlockCode.LOAD, blockValue, Map.of()));
        when(historical.calculate(org.mockito.ArgumentMatchers.any())).thenReturn(new BlockCalculationResult(PricingBlockCode.HISTORICAL_DEMAND, blockValue, Map.of()));
        when(calendar.calculate(org.mockito.ArgumentMatchers.any())).thenReturn(new BlockCalculationResult(PricingBlockCode.CALENDAR_CONTEXT, blockValue, Map.of()));
        PricingWeightProvider weights = (restaurantId, code) -> switch (code) {
            case LOAD_BLOCK, HISTORICAL_DEMAND_BLOCK, CALENDAR_CONTEXT_BLOCK -> BigDecimal.ONE;
            default -> BigDecimal.ZERO;
        };
        return new PricingCalculator(load, historical, calendar, weights, new PricingProperties());
    }
}

final class PricingTestSupport {
    private PricingTestSupport() {
    }

    static PricingContext context(int occupied, int total) {
        return context(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-06-01T09:00:00Z"), occupied, total, BigDecimal.TEN);
    }

    static PricingContext context(Instant requestTime, Instant visitStart) {
        return context(UUID.randomUUID(), UUID.randomUUID(), visitStart, 1, 10, BigDecimal.TEN, requestTime);
    }

    static PricingContext context(UUID restaurantId, UUID tableId, Instant visitStart) {
        return context(restaurantId, tableId, visitStart, 1, 10, BigDecimal.TEN);
    }

    static PricingContext contextWithPreorder(BigDecimal preorderAmount) {
        return context(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-06-01T09:00:00Z"), 5, 10, preorderAmount);
    }

    static PricingContext context(UUID restaurantId, UUID tableId, Instant visitStart, int occupied, int total, BigDecimal preorderAmount) {
        return context(restaurantId, tableId, visitStart, occupied, total, preorderAmount, Instant.parse("2026-05-31T09:00:00Z"));
    }

    static PricingContext context(UUID restaurantId, UUID tableId, Instant visitStart, int occupied, int total,
                                  BigDecimal preorderAmount, Instant requestTime) {
        return new PricingContext(
                UUID.randomUUID(),
                restaurantId,
                tableId,
                visitStart,
                visitStart.plusSeconds(3600),
                requestTime,
                preorderAmount.compareTo(BigDecimal.ZERO) > 0
                        ? List.of(new PricingPreorderItemRequest(UUID.randomUUID(), 1))
                        : List.of(),
                preorderAmount,
                "hash",
                new BigDecimal("100.00"),
                new BigDecimal("1000.00"),
                total,
                occupied
        );
    }

    static CalendarStatusParameter calendarParameter(my.project.bookingservice.pricing.parameters.calendar.CalendarClassifier classifier) {
        return calendarParameter(classifier, new PricingProperties());
    }

    static CalendarStatusParameter calendarParameter(my.project.bookingservice.pricing.parameters.calendar.CalendarClassifier classifier,
                                                     PricingProperties properties) {
        PricingCalendarCoefficientRepository repository = mock(PricingCalendarCoefficientRepository.class);
        PricingCalendarCoefficientService settings = new PricingCalendarCoefficientService(repository, properties);
        return new CalendarStatusParameter(classifier, settings, properties, new HistoricalTransitionService());
    }

    static PricingContext context(UUID restaurantId, UUID tableId, Instant visitStart, int occupied, int total,
                                  BigDecimal preorderAmount, Instant requestTime, Instant visitEnd) {
        return new PricingContext(
                UUID.randomUUID(),
                restaurantId,
                tableId,
                visitStart,
                visitEnd,
                requestTime,
                preorderAmount.compareTo(BigDecimal.ZERO) > 0
                        ? List.of(new PricingPreorderItemRequest(UUID.randomUUID(), 1))
                        : List.of(),
                preorderAmount,
                "hash",
                new BigDecimal("100.00"),
                new BigDecimal("1000.00"),
                total,
                occupied
        );
    }
}

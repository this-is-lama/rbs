package my.project.bookingservice.pricing.history;

import my.project.bookingservice.pricing.util.HistoryBlendUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class HistoricalTransitionService {
	public BigDecimal blend(BigDecimal defaultValue,
							BigDecimal historicalValue,
							long successfulCount,
							int minThreshold,
							int fullThreshold) {
		return HistoryBlendUtils.blend(defaultValue, historicalValue, successfulCount, minThreshold, fullThreshold);
	}

	public BigDecimal lambda(long successfulCount, int minThreshold, int fullThreshold) {
		return HistoryBlendUtils.lambda(successfulCount, minThreshold, fullThreshold);
	}
}

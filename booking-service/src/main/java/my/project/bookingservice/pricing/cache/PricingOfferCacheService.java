package my.project.bookingservice.pricing.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingOfferCacheService {
	private static final String OFFERS_BY_ID_CACHE = "pricingOffersById";
	private static final String OFFERS_BY_HASH_CACHE = "pricingOffersByHash";

	private final CacheManager cacheManager;

	public Optional<PricingOfferCacheDto> findByOfferId(UUID offerId) {
		if (offerId == null) {
			return Optional.empty();
		}
		return find(OFFERS_BY_ID_CACHE, offerId, "идентификатору");
	}

	public Optional<PricingOfferCacheDto> findByCartHash(String cartHash) {
		if (cartHash == null || cartHash.isBlank()) {
			return Optional.empty();
		}
		return find(OFFERS_BY_HASH_CACHE, cartHash, "хэшу корзины");
	}

	private Optional<PricingOfferCacheDto> find(String cacheName, Object key, String label) {
		Cache cache = cacheManager.getCache(cacheName);
		if (cache == null) {
			log.warn("Кэш ценовых предложений по {} недоступен: cacheName={}", label, cacheName);
			return Optional.empty();
		}
		return Optional.ofNullable(cache.get(key, PricingOfferCacheDto.class));
	}

	@Caching(put = {
			@CachePut(cacheNames = OFFERS_BY_ID_CACHE, key = "#offer.offerId()"),
			@CachePut(cacheNames = OFFERS_BY_HASH_CACHE, key = "#offer.cartHash()")
	})
	public PricingOfferCacheDto save(PricingOfferCacheDto offer) {
		return offer;
	}

	@Caching(evict = {
			@CacheEvict(cacheNames = OFFERS_BY_ID_CACHE, key = "#offer.offerId()"),
			@CacheEvict(cacheNames = OFFERS_BY_HASH_CACHE, key = "#offer.cartHash()")
	})
	public void evict(PricingOfferCacheDto offer) {
	}
}

package my.project.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.bookingservice.client.UserServiceClient;
import my.project.bookingservice.dto.client.UserBriefDto;
import my.project.bookingservice.dto.response.BookingUserResponse;
import my.project.bookingservice.mapper.BookingMapper;
import my.project.common.exception.NotFoundException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBriefCacheService {
	private static final String USER_BRIEFS_CACHE = "userBriefs";

	private final UserServiceClient userServiceClient;
	private final BookingMapper mapper;
	private final CacheManager cacheManager;

	@Cacheable(cacheNames = USER_BRIEFS_CACHE, key = "#userId")
	public BookingUserResponse getUserBrief(UUID userId) {
		UserBriefDto dto = userServiceClient.getBriefs(Set.of(userId))
				.stream()
				.findFirst()
				.orElseThrow(() -> {
					log.warn("Пользователь бронирования не найден, userId={}", userId);
					return new NotFoundException("user.not-found-by-id", userId);
				});
		return mapper.toBookingUserResponse(dto);
	}

	public Map<UUID, BookingUserResponse> getUserBriefs(Set<UUID> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Map.of();
		}

		Cache cache = cacheManager.getCache(USER_BRIEFS_CACHE);
		Map<UUID, BookingUserResponse> result = new HashMap<>();
		Set<UUID> missingIds = new HashSet<>();

		for (UUID userId : userIds) {
			if (userId == null) {
				continue;
			}

			BookingUserResponse cached = cache == null ? null : cache.get(userId, BookingUserResponse.class);
			if (cached != null) {
				result.put(userId, cached);
			} else {
				missingIds.add(userId);
			}
		}

		if (!missingIds.isEmpty()) {
			log.info("Загрузка кратких данных пользователей из user-service, count={}", missingIds.size());
			List<UserBriefDto> loadedUsers = userServiceClient.getBriefs(missingIds);
			Set<UUID> loadedIds = new HashSet<>();

			for (UserBriefDto dto : loadedUsers) {
				BookingUserResponse response = mapper.toBookingUserResponse(dto);
				result.put(response.id(), response);
				loadedIds.add(response.id());

				if (cache != null) {
					cache.put(response.id(), response);
				}
			}

			Set<UUID> notFoundIds = missingIds.stream()
					.filter(id -> !loadedIds.contains(id))
					.collect(Collectors.toSet());
			if (!notFoundIds.isEmpty()) {
				UUID firstMissingId = notFoundIds.iterator().next();
				log.warn("Не найдены краткие данные пользователя при batch-загрузке, userId={}", firstMissingId);
				throw new NotFoundException("user.not-found-by-id", firstMissingId);
			}
		}

		return result;
	}
}

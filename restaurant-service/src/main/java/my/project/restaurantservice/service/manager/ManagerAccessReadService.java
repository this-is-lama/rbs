package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.project.restaurantservice.repository.ManagerRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerAccessReadService {

  private final ManagerRepository repository;

  @Cacheable(cacheNames = "managerAccess", key = "#restId + ':' + #managerId", sync = true)
  @Transactional(readOnly = true)
  public boolean managerHasAccess(UUID restId, UUID managerId) {
    boolean result = repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId);
    log.debug("Проверка доступа менеджера к ресторану, restId={}, managerId={}, result={}",
            restId, managerId, result);
    return result;
  }
}
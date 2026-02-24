package my.project.restaurantservice.service.manager;

import lombok.RequiredArgsConstructor;
import my.project.restaurantservice.repository.ManagerRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerAccessReadService {

  private final ManagerRepository repository;

  @Cacheable(
          cacheNames = "managerAccess",
          key = "#restId + ':' + #managerId",
          sync = true
  )
  @Transactional(readOnly = true)
  public boolean managerHasAccess(UUID restId, UUID managerId) {
    return repository.existsByIdRestaurantIdAndIdManagerId(restId, managerId);
  }
}
package my.project.restaurantservice.manager.repository;

import my.project.restaurantservice.manager.entity.ManagerEntity;
import my.project.restaurantservice.manager.entity.ManagerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ManagerRepository extends JpaRepository<ManagerEntity, ManagerId> {

    boolean existsByIdRestaurantIdAndIdManagerId(UUID restId, UUID managerId);

    List<ManagerEntity> findAllByIdRestaurantIdOrderByCreatedAtAsc(UUID restId);

    @Query("""
            select m.id.managerId
            from ManagerEntity m
            where m.id.restaurantId = :restId
            """)
    List<UUID> findManagerIdsByRestaurantId(@Param("restId") UUID restId);

    @Query("""
            select distinct m.id.managerId
            from ManagerEntity m
            where m.id.managerId in :managerIds
            """)
    Set<UUID> findManagerIdsWithRestaurants(@Param("managerIds") Collection<UUID> managerIds);

    void deleteByIdRestaurantIdAndIdManagerId(UUID restId, UUID managerId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from ManagerEntity m
            where m.id.restaurantId = :restId
            """)
    int deleteAllByRestaurantId(@Param("restId") UUID restId);
}

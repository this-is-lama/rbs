package my.project.restaurantservice.photo.repository;

import my.project.restaurantservice.photo.entity.PhotoEntity;
import my.project.restaurantservice.photo.entity.enums.PhotoCategory;
import my.project.restaurantservice.photo.entity.enums.PhotoStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.*;

public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID> {

	List<PhotoEntity> findAllByRestaurantIdAndStatus(UUID restId, PhotoStatus status);
	List<PhotoEntity> findAllByDishIdAndStatus(UUID dishId, PhotoStatus status);

	@EntityGraph(attributePaths = {"restaurant", "dish"})
	List<PhotoEntity> findTop500ByStatusAndUploadedAtBefore(PhotoStatus status, Instant threshold);

	@EntityGraph(attributePaths = {"restaurant", "dish"})
	List<PhotoEntity> findTop500ByStatus(PhotoStatus status);

	@EntityGraph(attributePaths = {"restaurant", "dish"})
	List<PhotoEntity> findAllByIdIn(Set<UUID> ids);

	@Query("""
        select p
        from PhotoEntity p
        where p.category = :category
          and p.sortOrder = (
              select min(p2.sortOrder)
              from PhotoEntity p2
              where p2.restaurant.id = p.restaurant.id
                and p2.category = :category
          )
          and p.restaurant.id in :restIds
    """)
	List<PhotoEntity> findFirstPhotosForRestaurants(@Param("restIds") Set<UUID> restIds,
													@Param("category") PhotoCategory category);

	List<PhotoEntity> findAllByDishIdInAndStatusOrderBySortOrderAsc(Collection<UUID> dishIds, PhotoStatus status);

	List<PhotoEntity> findAllByIdInAndStatus(Collection<UUID> ids, PhotoStatus status);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
        update PhotoEntity p
        set p.status = :status,
            p.restaurant = null,
            p.dish = null
        where p.restaurant.id = :restId
           or p.dish.id in (
               select d.id
               from DishEntity d
               where d.restaurant.id = :restId
           )
    """)
	int detachAllByRestaurantId(@Param("restId") UUID restId, @Param("status") PhotoStatus status);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
        update PhotoEntity p
        set p.status = :status,
            p.dish = null
        where p.dish.id in (
            select d.id
            from DishEntity d
            where d.id = :dishId
              and d.restaurant.id = :restId
        )
    """)
	int detachAllByDishIdAndRestaurantId(@Param("dishId") UUID dishId,
										 @Param("restId") UUID restId,
										 @Param("status") PhotoStatus status);

}

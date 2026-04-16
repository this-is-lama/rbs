package my.project.restaurantservice.repository;

import my.project.restaurantservice.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantEntity, UUID>,
		JpaSpecificationExecutor<RestaurantEntity> {

	Optional<RestaurantEntity> findByIdAndActiveTrue(UUID id);

	@Query("""
            select distinct r.category
            from RestaurantEntity r
            where r.category is not null
              and trim(r.category) <> ''
            order by r.category asc
            """)
	List<String> findDistinctCategories();
}

package my.project.restaurantservice.restaurant.entity;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import java.util.UUID;

public class RestaurantSpecifications {

    public static Specification<RestaurantEntity> hasCategory(String category) {
        return (root, query, cb) ->
                category == null || category.isBlank() ? null :
                        cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%");
    }

    public static Specification<RestaurantEntity> hasName(String name) {
        return (root, query, cb) ->
                name == null || name.isBlank() ? null :
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<RestaurantEntity> hasStatus(RestaurantStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    /** Поиск по адресу: подстрока ищется в строке «город улица дом». */
    public static Specification<RestaurantEntity> hasAddress(String address) {
        return (root, query, cb) -> {
            if (address == null || address.isBlank()) {
                return null;
            }
            var fullAddress = cb.concat(
                    cb.concat(cb.concat(root.<String>get("city"), " "), root.<String>get("street")),
                    cb.concat(" ", root.<String>get("house"))
            );
            return cb.like(cb.lower(fullAddress), "%" + address.toLowerCase() + "%");
        };
    }

    public static Specification<RestaurantEntity> ownedByManager(UUID managerId) {
        return (root, query, cb) -> {
			query.distinct(true);
			var join = root.join("managers", JoinType.INNER);
            return cb.equal(join.get("id").get("managerId"), managerId);
        };
    }

    public static Specification<RestaurantEntity> isActiveOrOwnedByManager(UUID managerId) {
        return (root, query, cb) -> {
			query.distinct(true);
			var join = root.join("managers", JoinType.LEFT);
            return cb.or(
                    cb.equal(root.get("status"), RestaurantStatus.ACTIVE),
                    cb.equal(join.get("id").get("managerId"), managerId)
            );
        };
    }

    public static Specification<RestaurantEntity> getSpecification(String category, String name,
                                                                   RestaurantStatus status, String address) {
        return Specification
                .where(RestaurantSpecifications.hasCategory(category))
                .and(RestaurantSpecifications.hasName(name))
                .and(RestaurantSpecifications.hasStatus(status))
                .and(RestaurantSpecifications.hasAddress(address));
    }
}
package my.project.restaurantservice.entity;

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

    public static Specification<RestaurantEntity> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null :
                        cb.equal(root.get("active"), active);
    }

    public static Specification<RestaurantEntity> hasAddress(String address) {
        return (root, query, cb) ->
                address == null || address.isBlank() ? null :
                        cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%");
    }

    public static Specification<RestaurantEntity> ownedByManager(UUID managerId) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            var join = root.join("managers", JoinType.INNER);
            return cb.equal(join.get("id").get("managerId"), managerId);
        };
    }

    public static Specification<RestaurantEntity> isActiveOrOwnedByManager(UUID managerId) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            var join = root.join("managers", JoinType.LEFT);
            return cb.or(
                    cb.isTrue(root.get("active")),
                    cb.equal(join.get("id").get("managerId"), managerId)
            );
        };
    }

    public static Specification<RestaurantEntity> getSpecification(String category, String name,
                                                                   Boolean active, String address) {
        return Specification
                .where(RestaurantSpecifications.hasCategory(category))
                .and(RestaurantSpecifications.hasName(name))
                .and(RestaurantSpecifications.isActive(active))
                .and(RestaurantSpecifications.hasAddress(address));
    }
}
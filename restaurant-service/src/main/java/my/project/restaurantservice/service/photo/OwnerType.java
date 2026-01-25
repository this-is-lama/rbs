package my.project.restaurantservice.service.photo;

public enum OwnerType {
    RESTAURANT("restaurants"),
    DISH("dishes");

    private final String path;

    OwnerType(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public static OwnerType fromPath(String container) {
        if (container == null) throw new IllegalArgumentException("container is null");
        return switch (container.toLowerCase()) {
            case "restaurants" -> RESTAURANT;
            case "dishes" -> DISH;
            default -> throw new IllegalArgumentException("Unsupported container: " + container);
        };
    }
}


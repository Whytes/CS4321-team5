package model;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A reservable campus space with a stable identity.
 */
public final class Space {

    private final String spaceId;
    private final String name;
    private final String building;
    private final int capacity;
    private final List<String> features;

    public Space(String spaceId, String name, String building, int capacity,
                 Collection<String> features) {
        this.spaceId = requireNonBlank(spaceId, "spaceId");
        this.name = requireNonBlank(name, "name");
        this.building = requireNonBlank(building, "building");

        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;

        if (features == null) {
            throw new IllegalArgumentException("features must not be null");
        }
        this.features = features.stream()
                .map(feature -> requireNonBlank(feature, "feature"))
                .toList();
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getName() {
        return name;
    }

    public String getBuilding() {
        return building;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<String> getFeatures() {
        return features;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Space space)) {
            return false;
        }
        return spaceId.equals(space.spaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spaceId);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}

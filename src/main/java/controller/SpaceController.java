package controller;

import java.util.List;
import java.util.Optional;

import model.Space;

/**
 * Coordinates space catalog queries for views.
 */
public final class SpaceController {

    private final List<Space> spaces;

    public SpaceController(List<Space> spaces) {
        if (spaces == null) {
            throw new IllegalArgumentException("spaces must not be null");
        }
        this.spaces = List.copyOf(spaces);
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public Optional<Space> findById(String spaceId) {
        return spaces.stream()
                .filter(space -> space.getSpaceId().equals(spaceId))
                .findFirst();
    }

    public List<Space> filterByMinCapacity(Integer minCapacity) {
        if (minCapacity == null) {
            return spaces;
        }
        return spaces.stream()
                .filter(space -> space.getCapacity() >= minCapacity)
                .toList();
    }

    public List<Space> filterByMinCapacity(String minCapacityStr) {
        if (minCapacityStr == null || minCapacityStr.trim().isEmpty()) {
            return spaces;
        }
        try {
            int capacity = Integer.parseInt(minCapacityStr.trim());
            return filterByMinCapacity(capacity);
        } catch (NumberFormatException e) {
            return List.of();
        }
    }
}
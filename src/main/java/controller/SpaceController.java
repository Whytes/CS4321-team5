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
}
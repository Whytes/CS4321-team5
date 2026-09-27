package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Space;

/**
 * Integration coverage for space discovery through the real controller.
 */
class SpaceControllerIntegrationTest {

    private SpaceController controller;

    @BeforeEach
    void setUp() {
        controller = new SpaceController(List.of(
                new Space("small", "Small Room", "North Hall", 8, List.of()),
                new Space("medium", "Medium Room", "North Hall", 20, List.of("whiteboard")),
                new Space("large", "Large Room", "South Hall", 40, List.of("projector"))));
    }

    // US-3 AT1: applying a minimum capacity keeps spaces at or above the boundary.
    @Test
    void applyingCapacityFilterReturnsMatchingSpaces() {
        assertEquals(List.of("medium", "large"),
                controller.filterByMinCapacity(20).stream()
                        .map(Space::getSpaceId)
                        .toList());
    }

    // US-3 AT2: clearing the capacity filter restores every space.
    @Test
    void clearingCapacityFilterRestoresAllSpaces() {
        controller.filterByMinCapacity(20);

        assertEquals(controller.getSpaces(), controller.filterByMinCapacity((Integer) null));
        assertEquals(controller.getSpaces(), controller.filterByMinCapacity(" "));
    }

    // US-3 AT3: a capacity with no matches produces an empty result.
    @Test
    void capacityFilterWithNoMatchesReturnsEmptyList() {
        assertTrue(controller.filterByMinCapacity(100).isEmpty());
    }
}

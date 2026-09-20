package model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpaceTest {

    @Test
    void createsSpaceWithAllRequiredValues() {
        Space space = new Space(
                "study-room-a",
                "Study Room A",
                "Magnolia Building",
                4,
                List.of("Table", "whiteboard"));

        assertEquals("study-room-a", space.getSpaceId());
        assertEquals("Study Room A", space.getName());
        assertEquals("Magnolia Building", space.getBuilding());
        assertEquals(4, space.getCapacity());
        assertEquals(List.of("Table", "whiteboard"), space.getFeatures());
    }

    @Test
    void allowsEmptyFeatures() {
        Space space = new Space("quiet-room", "Quiet Room", "Library", 1, List.of());

        assertTrue(space.getFeatures().isEmpty());
    }

    @Test
    void makesFeaturesDefensiveAndUnmodifiable() {
        List<String> features = new ArrayList<>(List.of("Table"));
        Space space = new Space("study-room-a", "Study Room A", "Magnolia Building", 4, features);

        features.add("Projector");

        assertEquals(List.of("Table"), space.getFeatures());
        assertThrows(UnsupportedOperationException.class,
                () -> space.getFeatures().add("Projector"));
    }

    @Test
    void rejectsMissingRequiredText() {
        assertThrows(IllegalArgumentException.class,
                () -> new Space(" ", "Name", "Building", 1, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Space("id", null, "Building", 1, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Space("id", "Name", "\t", 1, List.of()));
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new Space("id", "Name", "Building", 0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Space("id", "Name", "Building", -1, List.of()));
    }

    @Test
    void rejectsNullFeaturesAndBlankFeatureValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new Space("id", "Name", "Building", 1, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Space("id", "Name", "Building", 1, List.of(" ")));
    }

    @Test
    void usesStableIdentityForEquality() {
        Space first = new Space("study-room-a", "Study Room A", "Magnolia Building", 4, List.of());
        Space sameIdentity = new Space("study-room-a", "Renamed", "Other Building", 10, List.of("Projector"));

        assertEquals(first, sameIdentity);
        assertEquals(first.hashCode(), sameIdentity.hashCode());
    }
}

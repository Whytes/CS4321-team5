package persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import model.Space;

class InitialSpaceCatalogTest {

    @Test
    void testGetDefaultSpacesReturnsCatalog() {
        List<Space> spaces = InitialSpaceCatalog.getDefaultSpaces();

        // Verify the catalog contains the expected 5 spaces from our scope
        assertNotNull(spaces, "Default spaces list should not be null");
        assertEquals(5, spaces.size(), "Catalog should contain exactly 5 default spaces");

        // Verify specific rooms exist with correct IDs and buildings
        boolean foundStudyA = spaces.stream().anyMatch(s -> s.getSpaceId().equals("study-room-a") && s.getBuilding().equals("Magnolia Building"));
        boolean foundTheater = spaces.stream().anyMatch(s -> s.getSpaceId().equals("theater") && s.getBuilding().equals("University Center"));

        assertTrue(foundStudyA, "Catalog should include study-room-a in Magnolia Building");
        assertTrue(foundTheater, "Catalog should include theater in University Center");
    }
}